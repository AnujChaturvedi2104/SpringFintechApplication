package com.anuj.projectfinanceai.services;
import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * FinancialInsightsService aggregates financial data for AI analysis
 *
 * This service demonstrates:
 * - Business intelligence and analytics
 * - Data aggregation for AI consumption
 * - Financial calculations and metrics
 * - Preparing context for AI recommendations
 */
@Service
public class FinancialInsightsService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ChatGptService chatGptService;

    @Autowired
    public FinancialInsightsService(TransactionRepository transactionRepository,
                                    AccountService accountService,
                                    ChatGptService chatGptService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.chatGptService = chatGptService;
    }

    /**
     * Generate comprehensive financial insights for a user
     */
    public String generateFinancialInsights(User user) {
        Map<String, Object> financialData = gatherFinancialData(user);
        return chatGptService.generateFinancialAdvice(financialData);
    }

    /**
     * Analyze spending patterns for current month
     */
    public String analyzeMonthlySpending(User user) {
        YearMonth currentMonth = YearMonth.now();
        Map<String, BigDecimal> categorySpending = getCategorySpendingForMonth(user, currentMonth);

        // Calculate total budget (simplified - could be enhanced with actual budget data)
        BigDecimal totalBudget = calculateEstimatedBudget(user);

        return chatGptService.analyzeSpendingPatterns(categorySpending, totalBudget);
    }

    /**
     * Generate budget recommendations based on income and spending
     */
    public String generateBudgetRecommendations(User user) {
        BigDecimal monthlyIncome = calculateMonthlyIncome(user);
        Map<String, BigDecimal> currentSpending = getCategorySpendingForMonth(user, YearMonth.now());

        return chatGptService.generateBudgetRecommendations(monthlyIncome, currentSpending);
    }

    /**
     * Gather comprehensive financial data for a user
     */
    private Map<String, Object> gatherFinancialData(User user) {
        Map<String, Object> data = new HashMap<>();

        // Account information
        List<Account> accounts = accountService.findAccountsByUser(user);
        BigDecimal netWorth = accountService.calculateNetWorth(user);

        data.put("Total Accounts", accounts.size());
        data.put("Net Worth", "$" + netWorth);

        // Current month financial data
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal monthlyIncome = transactionRepository.calculateTotalIncomeForUserInPeriod(
                user.getId(), startDate, endDate);
        BigDecimal monthlyExpenses = transactionRepository.calculateTotalExpensesForUserInPeriod(
                user.getId(), startDate, endDate);

        data.put("Monthly Income", "$" + monthlyIncome);
        data.put("Monthly Expenses", "$" + monthlyExpenses);
        data.put("Monthly Savings", "$" + monthlyIncome.subtract(monthlyExpenses));

        // Category breakdown
        Map<String, BigDecimal> categorySpending = getCategorySpendingForMonth(user, currentMonth);
        categorySpending.forEach((category, amount) -> {
            data.put("Spending on " + category, "$" + amount);
        });

        // Account balances
        for (Account account : accounts) {
            data.put(account.getAccountType().getDisplayName() + " (" + account.getAccountName() + ")",
                    account.getFormattedCurrentBalance());
        }

        return data;
    }

    /**
     * Get spending by category for a specific month
     */
    private Map<String, BigDecimal> getCategorySpendingForMonth(User user, YearMonth month) {
        Map<String, BigDecimal> categorySpending = new HashMap<>();

        for (Transaction.Category category : Transaction.Category.getExpenseCategories()) {
            BigDecimal amount = transactionRepository.calculateSpendingByCategoryAndMonth(
                    user.getId(), category, month.getYear(), month.getMonthValue());

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                categorySpending.put(category.getDisplayName(), amount);
            }
        }

        return categorySpending;
    }

    /**
     * Calculate monthly income for a user (average of last 3 months)
     */
    private BigDecimal calculateMonthlyIncome(User user) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        LocalDate now = LocalDate.now();

        BigDecimal totalIncome = transactionRepository.calculateTotalIncomeForUserInPeriod(
                user.getId(), threeMonthsAgo, now);

        // Average over 3 months
        return totalIncome.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Estimate total budget based on income (simplified calculation)
     */
    private BigDecimal calculateEstimatedBudget(User user) {
        BigDecimal monthlyIncome = calculateMonthlyIncome(user);

        // Use 80% of income as spending budget (20% for savings)
        return monthlyIncome.multiply(BigDecimal.valueOf(0.8));
    }

    /**
     * Get financial health score (0-100)
     */
    public int calculateFinancialHealthScore(User user) {
        int score = 50; // Base score

        try {
            // Factor 1: Savings rate (up to 25 points)
            BigDecimal monthlyIncome = calculateMonthlyIncome(user);
            LocalDate startOfMonth = YearMonth.now().atDay(1);
            LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

            BigDecimal monthlyExpenses = transactionRepository.calculateTotalExpensesForUserInPeriod(
                    user.getId(), startOfMonth, endOfMonth);

            if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal savingsRate = monthlyIncome.subtract(monthlyExpenses)
                        .divide(monthlyIncome, 4, BigDecimal.ROUND_HALF_UP);

                score += Math.min(25, (int)(savingsRate.doubleValue() * 100));
            }

            // Factor 2: Account diversity (up to 15 points)
            List<Account> accounts = accountService.findAccountsByUser(user);
            boolean hasChecking = accounts.stream().anyMatch(a -> a.getAccountType() == Account.AccountType.CHECKING);
            boolean hasSavings = accounts.stream().anyMatch(a -> a.getAccountType() == Account.AccountType.SAVINGS);

            if (hasChecking) score += 5;
            if (hasSavings) score += 10;

            // Factor 3: Positive net worth (up to 10 points)
            BigDecimal netWorth = accountService.calculateNetWorth(user);
            if (netWorth.compareTo(BigDecimal.ZERO) > 0) {
                score += 10;
            }

        } catch (Exception e) {
            System.err.println("Error calculating financial health score: " + e.getMessage());
        }

        return Math.max(0, Math.min(100, score));
    }
}
