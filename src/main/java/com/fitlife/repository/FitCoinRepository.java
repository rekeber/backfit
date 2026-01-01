package com.fitlife.repository;

import com.fitlife.entity.FitCoin;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FitCoinRepository extends JpaRepository<FitCoin, Long> {
    
    List<FitCoin> findByUserOrderByCreatedAtDesc(User user);
    
    Page<FitCoin> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<FitCoin> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, FitCoin.TransactionType transactionType);
    
    List<FitCoin> findByUserAndSourceOrderByCreatedAtDesc(User user, FitCoin.CoinSource source);
    
    @Query("SELECT SUM(f.amount) FROM FitCoin f WHERE f.user = :user AND f.transactionType = 'EARNED'")
    BigDecimal getTotalEarnedByUser(@Param("user") User user);
    
    @Query("SELECT SUM(f.amount) FROM FitCoin f WHERE f.user = :user AND f.transactionType = 'SPENT'")
    BigDecimal getTotalSpentByUser(@Param("user") User user);
    
    @Query("SELECT f.balanceAfter FROM FitCoin f WHERE f.user = :user ORDER BY f.createdAt DESC LIMIT 1")
    BigDecimal getCurrentBalance(@Param("user") User user);
    
    @Query("SELECT f FROM FitCoin f WHERE f.user = :user AND f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<FitCoin> findByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(f.amount) FROM FitCoin f WHERE f.user = :user AND f.transactionType = 'EARNED' AND f.createdAt >= :since")
    BigDecimal getEarnedSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(f) FROM FitCoin f WHERE f.user = :user AND f.source = :source AND f.createdAt >= :since")
    Long countBySourceSince(@Param("user") User user, @Param("source") FitCoin.CoinSource source, @Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM FitCoin f WHERE f.expiresAt IS NOT NULL AND f.expiresAt <= :now AND f.transactionType != 'EXPIRED'")
    List<FitCoin> findExpiredCoins(@Param("now") LocalDateTime now);
    
    @Query("SELECT f.source, SUM(f.amount) FROM FitCoin f WHERE f.user = :user AND f.transactionType = 'EARNED' GROUP BY f.source")
    List<Object[]> getEarningsBySource(@Param("user") User user);
    
    @Query("SELECT DATE(f.createdAt), SUM(f.amount) FROM FitCoin f WHERE f.user = :user AND f.transactionType = 'EARNED' AND f.createdAt >= :since GROUP BY DATE(f.createdAt) ORDER BY DATE(f.createdAt)")
    List<Object[]> getDailyEarnings(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM FitCoin f WHERE f.user = :user AND f.isBonus = true ORDER BY f.createdAt DESC")
    List<FitCoin> findBonusTransactions(@Param("user") User user);
    
    // New methods for dashboard
    @Query("SELECT COALESCE(SUM(CASE WHEN f.transactionType = 'EARNED' OR f.transactionType = 'BONUS' OR f.transactionType = 'REFUND' THEN f.amount ELSE -f.amount END), 0) FROM FitCoin f WHERE f.user.id = :userId")
    BigDecimal getCurrentBalance(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT f.user.id) FROM FitCoin f WHERE (SELECT COALESCE(SUM(CASE WHEN fc.transactionType = 'EARNED' OR fc.transactionType = 'BONUS' OR fc.transactionType = 'REFUND' THEN fc.amount ELSE -fc.amount END), 0) FROM FitCoin fc WHERE fc.user.id = f.user.id) > :balance")
    Long countUsersWithMoreCoins(@Param("balance") BigDecimal balance);
}