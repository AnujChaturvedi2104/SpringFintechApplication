package com.anuj.projectfinanceai.controller;

import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.AccountService;
import com.anuj.projectfinanceai.services.BudgetService;
import com.anuj.projectfinanceai.services.TransactionService;
import com.anuj.projectfinanceai.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardController provides overview of financial status
 */
@Controller
public class DashboardController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final UserService userService;

    @Autowired
    public DashboardController(AccountService accountService,
                               TransactionService transactionService,
                               BudgetService budgetService,
                               UserService userService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getTestUser();

        // Get current month data
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        // Account Summary
        List<Account> accounts = accountService.findAccountsByUser(user);
        BigDecimal netWorth = accountService.calculateNetWorth(user);

        // Transaction Summary
        List<Transaction> recentTransactions = transactionService.findRecentTransactionsByUser(user);
        BigDecimal monthlyIncome = transactionService.calculateTotalIncomeForPeriod(user, monthStart, monthEnd);
        BigDecimal monthlyExpenses = transactionService.calculateTotalExpensesForPeriod(user, monthStart, monthEnd);

        // Budget Summary
        List<BudgetService.BudgetSummary> budgetSummaries = budgetService.getBudgetSummaryForMonth(user, currentMonth);

        // Quick Stats
        DashboardStats stats = new DashboardStats();
        stats.setTotalAccounts(accounts.size());
        stats.setNetWorth(netWorth);
        stats.setMonthlyIncome(monthlyIncome);
        stats.setMonthlyExpenses(monthlyExpenses);
        stats.setMonthlyBalance(monthlyIncome.subtract(monthlyExpenses));
        stats.setTotalBudgets(budgetSummaries.size());

        // Budget Progress
        int budgetsOnTrack = 0;
        int budgetsOverLimit = 0;
        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (BudgetService.BudgetSummary summary : budgetSummaries) {
            totalBudgeted = totalBudgeted.add(summary.getBudget().getBudgetAmount());
            totalSpent = totalSpent.add(summary.getSpentAmount());

            if (summary.isExceeded()) {
                budgetsOverLimit++;
            } else if (summary.getUsagePercentage() <= 80) {
                budgetsOnTrack++;
            }
        }

        stats.setBudgetsOnTrack(budgetsOnTrack);
        stats.setBudgetsOverLimit(budgetsOverLimit);
        stats.setTotalBudgeted(totalBudgeted);
        stats.setTotalSpentOnBudgets(totalSpent);

        // Category Spending (top 5)
        Map<String, BigDecimal> categorySpending = getCategorySpendingForMonth(user, currentMonth);

        // Add all data to model
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", recentTransactions.subList(0, Math.min(10, recentTransactions.size())));
        model.addAttribute("budgetSummaries", budgetSummaries);
        model.addAttribute("stats", stats);
        model.addAttribute("categorySpending", categorySpending);
        model.addAttribute("currentMonth", currentMonth);

        return "dashboard/overview";
    }

    private Map<String, BigDecimal> getCategorySpendingForMonth(User user, YearMonth month) {
        Map<String, BigDecimal> categorySpending = new HashMap<>();

        for (Transaction.Category category : Transaction.Category.getExpenseCategories()) {
            BigDecimal spent = transactionService.calculateSpendingByCategoryAndMonth(
                    user, category, month.getYear(), month.getMonthValue());

            if (spent.compareTo(BigDecimal.ZERO) > 0) {
                categorySpending.put(category.getDisplayName(), spent);
            }
        }

        return categorySpending;
    }

    private User getTestUser() {
        return userService.findByEmail("test@example.com")
                .orElseThrow(() -> new RuntimeException("Test user not found"));
    }

    /**
     * Inner class to hold dashboard statistics
     */
    public static class DashboardStats {
        private int totalAccounts;
        private BigDecimal netWorth;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpenses;
        private BigDecimal monthlyBalance;
        private int totalBudgets;
        private int budgetsOnTrack;
        private int budgetsOverLimit;
        private BigDecimal totalBudgeted;
        private BigDecimal totalSpentOnBudgets;

        // Getters and Setters
        public int getTotalAccounts() { return totalAccounts; }
        public void setTotalAccounts(int totalAccounts) { this.totalAccounts = totalAccounts; }

        public BigDecimal getNetWorth() { return netWorth; }
        public void setNetWorth(BigDecimal netWorth) { this.netWorth = netWorth; }

        public BigDecimal getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

        public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
        public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }

        public BigDecimal getMonthlyBalance() { return monthlyBalance; }
        public void setMonthlyBalance(BigDecimal monthlyBalance) { this.monthlyBalance = monthlyBalance; }

        public int getTotalBudgets() { return totalBudgets; }
        public void setTotalBudgets(int totalBudgets) { this.totalBudgets = totalBudgets; }

        public int getBudgetsOnTrack() { return budgetsOnTrack; }
        public void setBudgetsOnTrack(int budgetsOnTrack) { this.budgetsOnTrack = budgetsOnTrack; }

        public int getBudgetsOverLimit() { return budgetsOverLimit; }
        public void setBudgetsOverLimit(int budgetsOverLimit) { this.budgetsOverLimit = budgetsOverLimit; }

        public BigDecimal getTotalBudgeted() { return totalBudgeted; }
        public void setTotalBudgeted(BigDecimal totalBudgeted) { this.totalBudgeted = totalBudgeted; }

        public BigDecimal getTotalSpentOnBudgets() { return totalSpentOnBudgets; }
        public void setTotalSpentOnBudgets(BigDecimal totalSpentOnBudgets) { this.totalSpentOnBudgets = totalSpentOnBudgets; }

        // Formatted getters for display
        public String getFormattedNetWorth() {
            return String.format("$%.2f", netWorth);
        }

        public String getFormattedMonthlyIncome() {
            return String.format("$%.2f", monthlyIncome);
        }

        public String getFormattedMonthlyExpenses() {
            return String.format("$%.2f", monthlyExpenses);
        }

        public String getFormattedMonthlyBalance() {
            return String.format("$%.2f", monthlyBalance);
        }

        public String getFormattedTotalBudgeted() {
            return String.format("$%.2f", totalBudgeted);
        }

        public String getFormattedTotalSpentOnBudgets() {
            return String.format("$%.2f", totalSpentOnBudgets);
        }

        public boolean isMonthlyBalancePositive() {
            return monthlyBalance.compareTo(BigDecimal.ZERO) >= 0;
        }

        public boolean isNetWorthPositive() {
            return netWorth.compareTo(BigDecimal.ZERO) >= 0;
        }
    }
}