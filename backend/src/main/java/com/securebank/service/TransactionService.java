package com.securebank.service;

import com.securebank.dto.TransactionSearchRequest;

import com.securebank.dto.TransactionRequest;
import com.securebank.model.*;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HttpServletRequest request;

    @Transactional
    public Transaction createTransaction(TransactionRequest transactionRequest, Long userId) {
        // Validate source account
        Account sourceAccount = accountRepository.findByAccountNumber(transactionRequest.getSourceAccountNumber())
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        // Verify account ownership
        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this account");
        }

        // Validate destination account
        Account destinationAccount = accountRepository.findByAccountNumber(transactionRequest.getDestinationAccountNumber())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        // Validate amount
        BigDecimal amount = transactionRequest.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        // Check sufficient balance
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Check daily transfer limit
        BigDecimal dailyTotal = getDailyTransferTotal(sourceAccount.getId());
        if (dailyTotal.add(amount).compareTo(sourceAccount.getDailyTransferLimit()) > 0) {
            throw new RuntimeException("Daily transfer limit exceeded");
        }

        // Check monthly transfer limit
        BigDecimal monthlyTotal = getMonthlyTransferTotal(sourceAccount.getId());
        if (monthlyTotal.add(amount).compareTo(sourceAccount.getMonthlyTransferLimit()) > 0) {
            throw new RuntimeException("Monthly transfer limit exceeded");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(amount);
        transaction.setCurrency(sourceAccount.getCurrency());
        transaction.setType("TRANSFER");
        transaction.setDescription(transactionRequest.getDescription());
        transaction.setIpAddress(getClientIpAddress());
        transaction.setUserAgent(request.getHeader("User-Agent"));
        transaction.setStatus("PROCESSING");

        // Flag large transactions for review
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            transaction.setFlaggedForReview(true);
        }

        // Calculate fee (0.5% for amounts over $1000)
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            BigDecimal fee = amount.multiply(new BigDecimal("0.005"));
            transaction.setFee(fee);
        }

        try {
            // Update balances
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount).subtract(transaction.getFee()));
            destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

            // Save changes
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            transaction.setStatus("COMPLETED");
            transaction.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            transaction.setStatus("FAILED");
            transaction.setFailureReason(e.getMessage());
            throw new RuntimeException("Transaction failed: " + e.getMessage());
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this account");
        }

       return transactionRepository.findBySourceAccount_IdOrDestinationAccount_IdOrderByCreatedAtDesc(
        account.getId(), account.getId()
        );
    }

    private BigDecimal getDailyTransferTotal(Long accountId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        BigDecimal total = transactionRepository.sumAmountByAccountAndDateAndStatus(
                accountId, startOfDay, "COMPLETED"
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    private BigDecimal getMonthlyTransferTotal(Long accountId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal total = transactionRepository.sumAmountByAccountAndDateAndStatus(
                accountId, startOfMonth, "COMPLETED"
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    private String getClientIpAddress() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public List<Transaction> searchTransactions(Long userId, TransactionSearchRequest searchRequest) {
    // Get account
    Account account = accountRepository.findByAccountNumber(searchRequest.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Account not found"));

    // Verify ownership
    if (!account.getUser().getId().equals(userId)) {
        throw new RuntimeException("Unauthorized: You don't own this account");
    }

    return transactionRepository.searchTransactions(
            account.getId(),
            searchRequest.getStartDate(),
            searchRequest.getEndDate(),
            searchRequest.getType(),
            searchRequest.getStatus(),
            searchRequest.getDescription()
    );
}
}
