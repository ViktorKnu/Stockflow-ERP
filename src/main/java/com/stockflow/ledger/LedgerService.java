package com.stockflow.ledger;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import com.stockflow.ledger.dto.MonthlyLedgerSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public List<LedgerTransactionResponse> findAll() {
        return ledgerTransactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(LedgerTransactionMapper::toResponse)
                .toList();
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
    public List<MonthlyLedgerSummaryResponse> monthlySummary() {
        Map<YearMonth, List<LedgerTransaction>> byMonth = ledgerTransactionRepository.findAll().stream()
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
                            summary.netProfit(),
                            summary.transactionCount()
                    );
                })
                .toList();
    }

    @Transactional
    public LedgerTransactionResponse recordExpense(BigDecimal amount,
                                                   String description,
                                                   LedgerSourceType sourceType,
                                                   Long sourceId) {
        LedgerTransaction transaction = LedgerTransaction.builder()
                .type(LedgerTransactionType.EXPENSE)
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
        return new LedgerSummaryResponse(
                totalRevenue,
                totalExpenses,
                totalRevenue.subtract(totalExpenses),
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
