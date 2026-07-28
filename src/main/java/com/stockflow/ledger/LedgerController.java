package com.stockflow.ledger;

import com.stockflow.common.dto.PageResponse;
import com.stockflow.ledger.dto.LedgerSummaryResponse;
import com.stockflow.ledger.dto.LedgerAdjustmentCreateRequest;
import com.stockflow.ledger.dto.LedgerTransactionResponse;
import com.stockflow.ledger.dto.MonthlyLedgerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Validated
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions")
    @Operation(summary = "List ledger transactions")
    public PageResponse<LedgerTransactionResponse> findAllTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ledgerService.findAll(page, size);
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get ledger transaction by id")
    public LedgerTransactionResponse findTransactionById(@PathVariable Long id) {
        return ledgerService.findById(id);
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a signed manual ledger adjustment")
    public LedgerTransactionResponse recordAdjustment(
            @Valid @RequestBody LedgerAdjustmentCreateRequest request) {
        return ledgerService.recordAdjustment(request);
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
