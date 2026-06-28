package com.stockflow.ledger;

import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import com.stockflow.ledger.dto.MonthlyLedgerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Validated
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
    @Operation(summary = "Get monthly ledger summary, optionally filtered by year")
    public List<MonthlyLedgerSummaryResponse> monthlySummary(
            @RequestParam(required = false) @Min(1900) @Max(2100) Integer year
    ) {
        return ledgerService.monthlySummary(year);
    }
}
