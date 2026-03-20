package com.securebank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, updatable = false)
    private String accountNumber;
    
    @NotNull
    @Column(nullable = false)
    private String accountType;
    
    @NotNull
    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private String currency = "USD";
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> outgoingTransactions;
    
    @OneToMany(mappedBy = "destinationAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> incomingTransactions;
    
    @DecimalMin(value = "0.00")
    @Column(precision = 19, scale = 2)
    private BigDecimal dailyTransferLimit = new BigDecimal("10000.00");
    
    @DecimalMin(value = "0.00")
    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyTransferLimit = new BigDecimal("50000.00");
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.accountNumber == null) {
            this.accountNumber = generateAccountNumber();
        }
    }
    
    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        String baseNumber = String.valueOf(timestamp).substring(2);
        if (baseNumber.length() < 12) {
            baseNumber = String.format("%012d", Long.parseLong(baseNumber));
        } else if (baseNumber.length() > 12) {
            baseNumber = baseNumber.substring(0, 12);
        }
        return baseNumber;
    }
    
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}