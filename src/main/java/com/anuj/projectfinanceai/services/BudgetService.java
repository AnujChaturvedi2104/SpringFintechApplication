package com.anuj.projectfinanceai.services;



import com.anuj.projectfinanceai.entity.Budget;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.repository.BudgetRepository;
import com.anuj.projectfinanceai.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

/**
 * BudgetService handles business logic for budget management
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new budget for a user
     */
    public Budget createBudget(Budget budget) {
        if (budget.getUser() == null) {
            throw new IllegalArgumentException("Budget must be associated with a user");
        }

        // Check if budget already exists for this user, category, and month
        if (budgetRepository.existsByUserAndCategoryAndBudgetMonth(
                budget.getUser(), budget.getCategory(), budget.getBudgetMonth())) {
            throw new IllegalArgumentException("Budget already exists for " +
                    budget.getCategory().getDisplayName() + " in " + budget.getBudgetMonth());
        }

        return budgetRepository.save(budget);
    }

    /**
     * Find budget by ID
     */
    @Transactional(readOnly = true)
    public Optional<Budget> findById(Long id) {
        return budgetRepository.findById(id);
    }

    /**
     * Find all budgets for a user in a specific month
     */
    @Transactional(readOnly = true)
    public List<Budget> findBudgetsByUserAndMonth(User user, YearMonth month) {
        return budgetRepository.findByUserAndBudgetMonthOrderByCategoryAsc(user, month);
    }

    /**
     * Find all budgets for a user (all months)
     */
    @Transactional(readOnly = true)
    public List<Budget> findBudgetsByUser(User user) {
        return budgetRepository.findByUserOrderByBudgetMonthDescCategoryAsc(user);
    }

    /**
     * Get budget summary with spending data for a specific month
     */
    @Transactional(readOnly = true)
    public List<BudgetSummary> getBudgetSummaryForMonth(User user, YearMonth month) {
        List<Budget> budgets = findBudgetsByUserAndMonth(user, month);
        List<BudgetSummary> summaries = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spentAmount = transactionRepository.calculateSpendingByCategoryAndMonth(
                    user.getId(), budget.getCategory(), month.getYear(), month.getMonthValue());

            BudgetSummary summary = new BudgetSummary(budget, spentAmount);
            summaries.add(summary);
        }

        return summaries;
    }

    /**
     * Update an existing budget
     */
    public Budget updateBudget(Budget budget) {
        if (budget.getId() == null) {
            throw new IllegalArgumentException("Cannot update budget without ID");
        }

        Optional<Budget> existingOpt = budgetRepository.findById(budget.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Budget not found with ID: " + budget.getId());
        }

        Budget existing = existingOpt.get();
        existing.setBudgetAmount(budget.getBudgetAmount());
        // Note: We don't allow changing category or month for existing budgets

        return budgetRepository.save(existing);
    }

    /**
     * Delete a budget
     */
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new IllegalArgumentException("Budget not found with ID: " + id);
        }
        budgetRepository.deleteById(id);
    }

    /**
     * Get expense categories that don't have budgets for a specific month
     */
    @Transactional(readOnly = true)
    public List<Transaction.Category> getAvailableCategoriesForMonth(User user, YearMonth month) {
        List<Budget> existingBudgets = findBudgetsByUserAndMonth(user, month);
        Set<Transaction.Category> usedCategories = new HashSet<>();

        for (Budget budget : existingBudgets) {
            usedCategories.add(budget.getCategory());
        }

        List<Transaction.Category> availableCategories = new ArrayList<>();
        for (Transaction.Category category : Transaction.Category.getExpenseCategories()) {
            if (!usedCategories.contains(category)) {
                availableCategories.add(category);
            }
        }

        return availableCategories;
    }

    /**
     * Inner class to represent budget summary with spending data
     */
    public static class BudgetSummary {
        private final Budget budget;
        private final BigDecimal spentAmount;
        private final BigDecimal remainingAmount;
        private final double usagePercentage;
        private final boolean isExceeded;

        public BudgetSummary(Budget budget, BigDecimal spentAmount) {
            this.budget = budget;
            this.spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;
            this.remainingAmount = budget.getBudgetAmount().subtract(this.spentAmount);
            this.usagePercentage = budget.calculateUsagePercentage(this.spentAmount);
            this.isExceeded = budget.isExceeded(this.spentAmount);
        }

        // Getters
        public Budget getBudget() { return budget; }
        public BigDecimal getSpentAmount() { return spentAmount; }
        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public double getUsagePercentage() { return usagePercentage; }
        public boolean isExceeded() { return isExceeded; }

        public String getFormattedSpentAmount() {
            return String.format("$%.2f", spentAmount);
        }

        public String getFormattedRemainingAmount() {
            return String.format("$%.2f", remainingAmount);
        }

        public String getUsagePercentageString() {
            return String.format("%.1f%%", usagePercentage);
        }

        public String getProgressBarClass() {
            if (isExceeded) return "bg-danger";
            if (usagePercentage >= 80) return "bg-warning";
            if (usagePercentage >= 60) return "bg-info";
            return "bg-success";
        }
    }
}
