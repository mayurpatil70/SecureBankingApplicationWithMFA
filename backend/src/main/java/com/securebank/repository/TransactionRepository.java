package com.securebank.repository;

import com.securebank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
        Long sourceId, Long destId
    );
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sourceAccount.id = :accountId " +
           "AND t.createdAt >= :startDate AND t.status = :status")
    BigDecimal sumAmountByAccountAndDateAndStatus(
        Long accountId, LocalDateTime startDate, String status
    );
    
    List<Transaction> findByFlaggedForReviewTrue();
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    // New methods for search and filter
   @Query("SELECT t FROM Transaction t WHERE " +
       "(t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
       "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
       "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
       "AND (:type IS NULL OR :type = '' OR t.type = :type) " +
       "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
       "AND (:description IS NULL OR :description = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
       "ORDER BY t.createdAt DESC")
List<Transaction> searchTransactions(
    @Param("accountId") Long accountId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate,
    @Param("type") String type,
    @Param("status") String status,
    @Param("description") String description
);
}