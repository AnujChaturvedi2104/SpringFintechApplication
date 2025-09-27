package com.anuj.projectfinanceai.repository;

import com.anuj.projectfinanceai.entity.Budget;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all budgets for a user in a specific month
     */
    List<Budget> findByUserAndBudgetMonthOrderByCategoryAsc(User user, YearMonth budgetMonth);

    /**
     * Find budget for a specific user, category, and month
     */
    Optional<Budget> findByUserAndCategoryAndBudgetMonth(User user, Transaction.Category category, YearMonth budgetMonth);

    /**
     * Find all budgets for a user (all months)
     */
    List<Budget> findByUserOrderByBudgetMonthDescCategoryAsc(User user);

    /**
     * Check if budget exists for user, category, and month
     */
    boolean existsByUserAndCategoryAndBudgetMonth(User user, Transaction.Category category, YearMonth budgetMonth);
}