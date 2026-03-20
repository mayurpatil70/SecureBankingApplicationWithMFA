package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.TransactionRequest;
import com.securebank.dto.TransactionSearchRequest;
import com.securebank.model.Transaction;
import com.securebank.security.JwtUtil;
import com.securebank.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/transactions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            Transaction transaction = transactionService.createTransaction(request, userId);
            return ResponseEntity.ok(ApiResponse.success("Transaction completed successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAccountTransactions(
            @PathVariable String accountNumber,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber, userId);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<Transaction>>> searchTransactions(
            @RequestBody TransactionSearchRequest searchRequest,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            List<Transaction> transactions = transactionService.searchTransactions(userId, searchRequest);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return Long.parseLong(jwtUtil.extractClaim(token, claims -> claims.get("userId").toString()));
    }
}