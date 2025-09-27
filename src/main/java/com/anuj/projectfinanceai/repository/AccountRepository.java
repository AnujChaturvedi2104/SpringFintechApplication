package com.anuj.projectfinanceai.repository;

import com.anuj.projectfinanceai.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import com.anuj.projectfinanceai.entity.User;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts for a specific user
     */
    List<Account> findByUserOrderByAccountNameAsc(User user);

    /**
     * Find accounts by type for a user
     */
    List<Account> findByUserAndAccountTypeOrderByAccountNameAsc(User user, Account.AccountType accountType);

    /**
     * Calculate total balance across all accounts for a user
     */
    @Query("SELECT COALESCE(SUM(a.currentBalance), 0) FROM Account a WHERE a.user = :user")
    BigDecimal calculateTotalBalanceForUser(User user);

    /**
     * Find accounts with balance above a threshold
     */
    List<Account> findByUserAndCurrentBalanceGreaterThanOrderByCurrentBalanceDesc(User user, BigDecimal threshold);
}