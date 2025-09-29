package com.anuj.projectfinanceai.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
/**
 * Budget entity represents spending limits by category for a specific month
 *
 * This demonstrates:
 * - Composite business logic
 * - YearMonth for monthly budgets
 * - Calculated fields and business rules
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category", "budget_month"})
})
// A user can only have one budget per category per month. @UniqueContstraint
// kind of like composite unique key.
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    @Column(nullable = false)
    private Transaction.Category category;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    @Column(name = "budget_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal budgetAmount;

    @NotNull(message = "Budget month is required")
    @Column(name = "budget_month", nullable = false)
    private YearMonth budgetMonth;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Many budgets belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Default constructor for JPA
     */
    public Budget() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating budgets
     */
    public Budget(Transaction.Category category, BigDecimal budgetAmount, YearMonth budgetMonth, User user) {
        this();
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.budgetMonth = budgetMonth;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transaction.Category getCategory() { return category; }
    public void setCategory(Transaction.Category category) { this.category = category; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public YearMonth getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(YearMonth budgetMonth) { this.budgetMonth = budgetMonth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /**
     * Get formatted budget amount for display
     */
    public String getFormattedBudgetAmount() {
        return String.format("$%.2f", budgetAmount);
    }

    /**
     * Calculate percentage of budget used (requires spending amount)
     */
    public double calculateUsagePercentage(BigDecimal spentAmount) {
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Check if budget is exceeded
     */
    public boolean isExceeded(BigDecimal spentAmount) {
        return spentAmount.compareTo(budgetAmount) > 0;
    }

    /**
     * Get remaining budget amount
     */
    public BigDecimal getRemainingAmount(BigDecimal spentAmount) {
        return budgetAmount.subtract(spentAmount);
    }

    @Override
    public String toString() {
        return String.format("Budget{id=%d, category=%s, amount=%s, month=%s}",
                id, category, getFormattedBudgetAmount(), budgetMonth);
    }
}