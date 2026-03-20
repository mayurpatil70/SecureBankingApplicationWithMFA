package com.securebank.service;

import com.securebank.dto.AccountRequest;
import com.securebank.dto.AccountStatementRequest;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.model.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PdfService pdfService;

    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public Account createAccount(AccountRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setUser(user);
        account.setActive(true);
        account.setDailyTransferLimit(new BigDecimal("10000.00"));
        account.setMonthlyTransferLimit(new BigDecimal("50000.00"));

        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccountByNumber(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this account");
        }

        return account;
    }

    @Transactional
    public Account updateDailyLimit(String accountNumber, BigDecimal newLimit, Long userId) {
        Account account = getAccountByNumber(accountNumber, userId);
        account.setDailyTransferLimit(newLimit);
        return accountRepository.save(account);
    }

    @Transactional
    public void deactivateAccount(String accountNumber, Long userId) {
        Account account = getAccountByNumber(accountNumber, userId);
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot deactivate account with balance. Please transfer funds first.");
        }

        account.setActive(false);
        accountRepository.save(account);
    }

    public byte[] generateAccountStatement(Long userId, AccountStatementRequest request) {
        // Get account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify ownership
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this account");
        }

        // Calculate date range for the requested month
        LocalDateTime startDate = LocalDateTime.of(request.getYear(), request.getMonth(), 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        // Get transactions for the month
        List<Transaction> transactions = transactionRepository.searchTransactions(
                account.getId(),
                startDate,
                endDate,
                null,  // all types
                null,  // all statuses
                null   // no description filter
        );

        // Generate PDF
        return pdfService.generateAccountStatement(
                account,
                account.getUser(),
                transactions,
                request.getMonth(),
                request.getYear()
        );
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%012d", random.nextLong(1000000000000L));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }

    @Transactional
public Account updateAccountLimits(String accountNumber, Long userId, 
                                   BigDecimal dailyLimit, BigDecimal monthlyLimit) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));

    // Verify ownership
    if (!account.getUser().getId().equals(userId)) {
        throw new RuntimeException("Unauthorized: You don't own this account");
    }

    // Validate limits
    if (dailyLimit != null && dailyLimit.compareTo(BigDecimal.ZERO) < 0) {
        throw new RuntimeException("Daily limit cannot be negative");
    }

    if (monthlyLimit != null && monthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
        throw new RuntimeException("Monthly limit cannot be negative");
    }

    if (dailyLimit != null && monthlyLimit != null && 
        dailyLimit.compareTo(monthlyLimit) > 0) {
        throw new RuntimeException("Daily limit cannot exceed monthly limit");
    }

    // Update limits
    if (dailyLimit != null) {
        account.setDailyTransferLimit(dailyLimit);
    }

    if (monthlyLimit != null) {
        account.setMonthlyTransferLimit(monthlyLimit);
    }

    return accountRepository.save(account);
}
}