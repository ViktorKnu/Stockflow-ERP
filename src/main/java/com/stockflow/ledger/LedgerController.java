package com.stockflow.ledger;

import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import com.stockflow.ledger.dto.MonthlyLedgerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions")
    @Operation(summary = "List ledger transactions")
    public List<LedgerTransactionResponse> findAllTransactions() {
        return ledgerService.findAll();
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get ledger transaction by id")
    public LedgerTransactionResponse findTransactionById(@PathVariable Long id) {
        return ledgerService.findById(id);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get ledger summary")
    public LedgerSummaryResponse summary() {
        return ledgerService.summary();
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = "Get monthly ledger summary")
    public List<MonthlyLedgerSummaryResponse> monthlySummary() {
        return ledgerService.monthlySummary();
    }
}
