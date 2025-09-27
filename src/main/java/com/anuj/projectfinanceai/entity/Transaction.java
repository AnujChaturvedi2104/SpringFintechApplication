package com.anuj.projectfinanceai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Transaction entity represents a financial transaction
 *
 * This demonstrates:
 * - Complex business logic in entities
 * - Multiple enum types
 * - Date handling
 * - Validation rules
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    @Column(nullable = false)
    private Category category;

    @NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Many transactions belong to one account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Transaction types for income vs expenses
     */
    public enum TransactionType {
        INCOME("Income"),
        EXPENSE("Expense");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Transaction categories for spending analysis
     */
    public enum Category {
        // Income categories
        SALARY("Salary"),
        FREELANCE("Freelance"),
        INVESTMENT("Investment"),
        OTHER_INCOME("Other Income"),

        // Expense categories
        GROCERIES("Groceries"),
        DINING_OUT("Dining Out"),
        TRANSPORTATION("Transportation"),
        ENTERTAINMENT("Entertainment"),
        UTILITIES("Utilities"),
        RENT_MORTGAGE("Rent/Mortgage"),
        HEALTHCARE("Healthcare"),
        SHOPPING("Shopping"),
        EDUCATION("Education"),
        TRAVEL("Travel"),
        OTHER_EXPENSE("Other Expense");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Get categories by transaction type for form dropdowns
         */
        public static Category[] getIncomeCategories() {
            return new Category[]{SALARY, FREELANCE, INVESTMENT, OTHER_INCOME};
        }

        public static Category[] getExpenseCategories() {
            return new Category[]{GROCERIES, DINING_OUT, TRANSPORTATION, ENTERTAINMENT,
                    UTILITIES, RENT_MORTGAGE, HEALTHCARE, SHOPPING,
                    EDUCATION, TRAVEL, OTHER_EXPENSE};
        }
    }

    /**
     * Default constructor for JPA
     */
    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating transactions
     */
    public Transaction(String description, BigDecimal amount, TransactionType transactionType,
                       Category category, LocalDate transactionDate, Account account) {
        this();
        this.description = description;
        this.amount = amount;
        this.transactionType = transactionType;
        this.category = category;
        this.transactionDate = transactionDate;
        this.account = account;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    /**
     * Get formatted amount for display
     */
    public String getFormattedAmount() {
        return String.format("$%.2f", amount);
    }

    /**
     * Get display text with +/- for income/expense
     */
    public String getSignedFormattedAmount() {
        String prefix = transactionType == TransactionType.INCOME ? "+" : "-";
        return prefix + getFormattedAmount();
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, desc='%s', amount=%s, type=%s, category=%s}",
                id, description, getFormattedAmount(), transactionType, category);
    }
}