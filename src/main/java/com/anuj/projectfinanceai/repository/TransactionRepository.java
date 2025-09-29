package com.anuj.projectfinanceai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.Account;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transactions for a specific account, ordered by date (most recent first)
     */
    List<Transaction> findByAccountOrderByTransactionDateDescCreatedAtDesc(Account account);

    /**
     * Find transactions by category and date range
     */
    List<Transaction> findByCategoryAndTransactionDateBetweenOrderByTransactionDateDesc(
            Transaction.Category category, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total spending by category for a user in a specific month
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.account a " +
            "WHERE a.user.id = :userId AND t.category = :category " +
            "AND t.transactionType = 'EXPENSE' " +
            "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    BigDecimal calculateSpendingByCategoryAndMonth(Long userId, Transaction.Category category, int year, int month);

    /**
     * Find recent transactions across all user accounts
     */
    @Query("SELECT t FROM Transaction t JOIN t.account a WHERE a.user.id = :userId " +
            "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactionsByUser(Long userId);

    /**
     * Calculate total income for a user in a date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.account a " +
            "WHERE a.user.id = :userId AND t.transactionType = 'INCOME' " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalIncomeForUserInPeriod(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total expenses for a user in a date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.account a " +
            "WHERE a.user.id = :userId AND t.transactionType = 'EXPENSE' " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalExpensesForUserInPeriod(Long userId, LocalDate startDate, LocalDate endDate);
}