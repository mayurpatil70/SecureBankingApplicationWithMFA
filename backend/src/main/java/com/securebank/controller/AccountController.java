package com.securebank.controller;

import com.securebank.dto.AccountStatementRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.securebank.dto.AccountRequest;
import com.securebank.dto.ApiResponse;
import com.securebank.model.Account;
import com.securebank.security.JwtUtil;
import com.securebank.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/accounts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(
            @Valid @RequestBody AccountRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            Account account = accountService.createAccount(request, userId);
            return ResponseEntity.ok(ApiResponse.success("Account created successfully", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> getUserAccounts(HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            List<Account> accounts = accountService.getUserAccounts(userId);
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<Account>> getAccount(
            @PathVariable String accountNumber,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            Account account = accountService.getAccountByNumber(accountNumber, userId);
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{accountNumber}/limits")
    public ResponseEntity<ApiResponse<Account>> updateLimits(
            @PathVariable String accountNumber,
            @RequestParam(required = false) BigDecimal dailyLimit,
            @RequestParam(required = false) BigDecimal monthlyLimit,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            Account account = accountService.updateAccountLimits(accountNumber, userId, dailyLimit, monthlyLimit);
            return ResponseEntity.ok(ApiResponse.success("Limits updated successfully", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @PathVariable String accountNumber,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            accountService.deactivateAccount(accountNumber, userId);
            return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return Long.parseLong(jwtUtil.extractClaim(token, claims -> claims.get("userId").toString()));
    }

    @PostMapping("/statement")
public ResponseEntity<byte[]> generateStatement(
        @Valid @RequestBody AccountStatementRequest request,
        HttpServletRequest httpRequest) {
    try {
        Long userId = extractUserId(httpRequest);
        byte[] pdfBytes = accountService.generateAccountStatement(userId, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "statement-" + request.getMonth() + "-" + request.getYear() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}