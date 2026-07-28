package com.stockflow.ledger;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.common.dto.PageResponse;
import com.stockflow.exception.ApiErrorCode;
import com.stockflow.exception.BusinessRuleException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.ledger.dto.LedgerAdjustmentCreateRequest;
import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import com.stockflow.ledger.dto.MonthlyLedgerSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private static final String DEFAULT_CURRENCY = "NOK";

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponse<LedgerTransactionResponse> findAll(int page, int size) {
        return PageResponse.from(
                ledgerTransactionRepository.findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                                        .and(Sort.by(Sort.Direction.DESC, "id")))),
                LedgerTransactionMapper::toResponse
        );
    }

    @Transactional(readOnly = true)
    public LedgerTransactionResponse findById(Long id) {
        return LedgerTransactionMapper.toResponse(ledgerTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger transaction not found: " + id)));
    }

    @Transactional(readOnly = true)
    public LedgerSummaryResponse summary() {
        return summarize(ledgerTransactionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<MonthlyLedgerSummaryResponse> monthlySummary(Integer year) {
        List<LedgerTransaction> transactions = year == null
                ? ledgerTransactionRepository.findAll()
                : findTransactionsForYear(year);

        Map<YearMonth, List<LedgerTransaction>> byMonth = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        transaction -> YearMonth.from(transaction.getCreatedAt()),
                        TreeMap::new,
                        java.util.stream.Collectors.toList()
                ));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(entry -> {
                    LedgerSummaryResponse summary = summarize(entry.getValue());
                    return new MonthlyLedgerSummaryResponse(
                            entry.getKey().toString(),
                            summary.totalRevenue(),
                            summary.totalExpenses(),
                            summary.totalRefunds(),
                            summary.totalAdjustments(),
                            summary.netProfit(),
                            summary.transactionCount()
                    );
                })
                .toList();
    }

    private List<LedgerTransaction> findTransactionsForYear(int year) {
        LocalDateTime from = LocalDateTime.of(year, 1, 1, 0, 0);
        return ledgerTransactionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                from,
                from.plusYears(1)
        );
    }

    @Transactional
    public LedgerTransactionResponse recordExpense(BigDecimal amount,
                                                   String description,
                                                   LedgerSourceType sourceType,
                                                   Long sourceId) {
        return recordTransaction(LedgerTransactionType.EXPENSE, amount, description, sourceType, sourceId);
    }

    @Transactional
    public LedgerTransactionResponse recordRevenue(BigDecimal amount,
                                                   String description,
                                                   LedgerSourceType sourceType,
                                                   Long sourceId) {
        return recordTransaction(LedgerTransactionType.REVENUE, amount, description, sourceType, sourceId);
    }

    @Transactional
    public LedgerTransactionResponse recordRefund(BigDecimal amount,
                                                  String description,
                                                  LedgerSourceType sourceType,
                                                  Long sourceId) {
        return recordTransaction(LedgerTransactionType.REFUND, amount, description, sourceType, sourceId);
    }

    @Transactional
    public LedgerTransactionResponse recordAdjustment(LedgerAdjustmentCreateRequest request) {
        if (request.amount().signum() == 0) {
            throw new BusinessRuleException(
                    ApiErrorCode.LEDGER_ADJUSTMENT_ZERO,
                    "Ledger adjustment amount cannot be zero");
        }

        return recordTransaction(
                LedgerTransactionType.ADJUSTMENT,
                request.amount(),
                request.description(),
                LedgerSourceType.MANUAL,
                0L
        );
    }

    private LedgerTransactionResponse recordTransaction(LedgerTransactionType type,
                                                        BigDecimal amount,
                                                        String description,
                                                        LedgerSourceType sourceType,
                                                        Long sourceId) {
        LedgerTransaction transaction = LedgerTransaction.builder()
                .type(type)
                .amount(amount)
                .currency(DEFAULT_CURRENCY)
                .description(description)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .build();

        LedgerTransaction savedTransaction = ledgerTransactionRepository.save(transaction);
        auditLogService.record(
                AuditAction.LEDGER_TRANSACTION_CREATED,
                "LedgerTransaction",
                savedTransaction.getId(),
                "Ledger transaction " + savedTransaction.getType() + " recorded for " + sourceType + " " + sourceId
        );

        return LedgerTransactionMapper.toResponse(savedTransaction);
    }

    private LedgerSummaryResponse summarize(List<LedgerTransaction> transactions) {
        BigDecimal totalRevenue = sumByType(transactions, LedgerTransactionType.REVENUE);
        BigDecimal totalExpenses = sumByType(transactions, LedgerTransactionType.EXPENSE);
        BigDecimal totalRefunds = sumByType(transactions, LedgerTransactionType.REFUND);
        BigDecimal totalAdjustments = sumByType(transactions, LedgerTransactionType.ADJUSTMENT);
        return new LedgerSummaryResponse(
                totalRevenue,
                totalExpenses,
                totalRefunds,
                totalAdjustments,
                totalRevenue
                        .subtract(totalExpenses)
                        .subtract(totalRefunds)
                        .add(totalAdjustments),
                transactions.size()
        );
    }

    private BigDecimal sumByType(List<LedgerTransaction> transactions, LedgerTransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(LedgerTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
