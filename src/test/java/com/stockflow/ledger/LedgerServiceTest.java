package com.stockflow.ledger;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerTransactionRepository ledgerTransactionRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void canRecordExpenseTransaction() {
        when(ledgerTransactionRepository.save(any(LedgerTransaction.class))).thenAnswer(invocation -> {
            LedgerTransaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            transaction.setCreatedAt(LocalDateTime.of(2026, 6, 11, 12, 0));
            return transaction;
        });

        LedgerTransactionResponse response = ledgerService.recordExpense(
                new BigDecimal("3495.00"),
                "Purchase order received: 10",
                LedgerSourceType.PURCHASE_ORDER,
                10L
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(LedgerTransactionType.EXPENSE);
        assertThat(response.amount()).isEqualByComparingTo("3495.00");
        assertThat(response.currency()).isEqualTo("NOK");
        assertThat(response.sourceType()).isEqualTo(LedgerSourceType.PURCHASE_ORDER);
        assertThat(response.sourceId()).isEqualTo(10L);
        verify(auditLogService).record(
                AuditAction.LEDGER_TRANSACTION_CREATED,
                "LedgerTransaction",
                1L,
                "Ledger transaction EXPENSE recorded for PURCHASE_ORDER 10"
        );
    }

    @Test
    void canRecordRevenueTransaction() {
        when(ledgerTransactionRepository.save(any(LedgerTransaction.class))).thenAnswer(invocation -> {
            LedgerTransaction transaction = invocation.getArgument(0);
            transaction.setId(2L);
            transaction.setCreatedAt(LocalDateTime.of(2026, 6, 11, 12, 0));
            return transaction;
        });

        LedgerTransactionResponse response = ledgerService.recordRevenue(
                new BigDecimal("1798.00"),
                "Sales order shipped: 20",
                LedgerSourceType.SALES_ORDER,
                20L
        );

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.type()).isEqualTo(LedgerTransactionType.REVENUE);
        assertThat(response.amount()).isEqualByComparingTo("1798.00");
        assertThat(response.currency()).isEqualTo("NOK");
        assertThat(response.sourceType()).isEqualTo(LedgerSourceType.SALES_ORDER);
        assertThat(response.sourceId()).isEqualTo(20L);
        verify(auditLogService).record(
                AuditAction.LEDGER_TRANSACTION_CREATED,
                "LedgerTransaction",
                2L,
                "Ledger transaction REVENUE recorded for SALES_ORDER 20"
        );
    }

    @Test
    void calculatesRevenueExpensesAndNetProfit() {
        when(ledgerTransactionRepository.findAll()).thenReturn(List.of(
                transaction(LedgerTransactionType.REVENUE, "1200.00"),
                transaction(LedgerTransactionType.REVENUE, "300.00"),
                transaction(LedgerTransactionType.EXPENSE, "500.00")
        ));

        LedgerSummaryResponse response = ledgerService.summary();

        assertThat(response.totalRevenue()).isEqualByComparingTo("1500.00");
        assertThat(response.totalExpenses()).isEqualByComparingTo("500.00");
        assertThat(response.netProfit()).isEqualByComparingTo("1000.00");
        assertThat(response.transactionCount()).isEqualTo(3);
    }

    @Test
    void monthlySummaryGroupsTransactionsByCreatedMonth() {
        LedgerTransaction juneExpense = transaction(LedgerTransactionType.EXPENSE, "250.00");
        juneExpense.setCreatedAt(LocalDateTime.of(2026, 6, 11, 12, 0));
        LedgerTransaction mayRevenue = transaction(LedgerTransactionType.REVENUE, "900.00");
        mayRevenue.setCreatedAt(LocalDateTime.of(2026, 5, 5, 12, 0));

        when(ledgerTransactionRepository.findAll()).thenReturn(List.of(juneExpense, mayRevenue));

        var response = ledgerService.monthlySummary(null);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).month()).isEqualTo("2026-06");
        assertThat(response.get(0).totalExpenses()).isEqualByComparingTo("250.00");
        assertThat(response.get(0).netProfit()).isEqualByComparingTo("-250.00");
        assertThat(response.get(1).month()).isEqualTo("2026-05");
        assertThat(response.get(1).totalRevenue()).isEqualByComparingTo("900.00");
    }

    @Test
    void monthlySummaryCanBeFilteredByYear() {
        LocalDateTime startOf2026 = LocalDateTime.of(2026, 1, 1, 0, 0);
        LedgerTransaction juneRevenue = transaction(LedgerTransactionType.REVENUE, "1500.00");

        when(ledgerTransactionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                startOf2026,
                startOf2026.plusYears(1)
        )).thenReturn(List.of(juneRevenue));

        var response = ledgerService.monthlySummary(2026);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().month()).isEqualTo("2026-06");
        assertThat(response.getFirst().totalRevenue()).isEqualByComparingTo("1500.00");
    }

    private LedgerTransaction transaction(LedgerTransactionType type, String amount) {
        return LedgerTransaction.builder()
                .id(1L)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency("NOK")
                .description("Test transaction")
                .sourceType(LedgerSourceType.MANUAL)
                .sourceId(1L)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                .build();
    }
}
