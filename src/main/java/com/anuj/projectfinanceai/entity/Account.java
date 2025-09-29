package com.anuj.projectfinanceai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Account entity represents a bank account (checking, savings, credit card)
 *
 * This demonstrates:
 * - Enums for account types
 * - BigDecimal for monetary values
 * - Many-to-One relationships
 * - Calculated fields
 * No blank account names.
 *
 * Account type always set.
 * Initial balance strictly positive.
 * Every account belongs to a user (user_id is required).
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account name is required")
    @Column(name = "account_name", nullable = false)
    private String accountName;
    // usually enums have ordinal, this annotation below ensures we store string version otherwise we will have numeric entries.
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Account type is required")
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be positive")
    @Column(name = "initial_balance", precision = 10, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "current_balance", precision = 10, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Many accounts belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One account can have many transactions
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Enum for account types used in banking
     */
    public enum AccountType {
        CHECKING("Checking Account"),
        SAVINGS("Savings Account"),
        CREDIT_CARD("Credit Card");

        private final String displayName;

        AccountType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Default constructor for JPA
     */
    public Account() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating accounts
     */
    public Account(String accountName, AccountType accountType, BigDecimal initialBalance, User user) {
        this();
        this.accountName = accountName;
        this.accountType = accountType;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    /**
     * Helper method to add a transaction and update balance
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);

        // Update current balance based on transaction type
        if (transaction.getTransactionType() == Transaction.TransactionType.INCOME) {
            this.currentBalance = this.currentBalance.add(transaction.getAmount());
        } else {
            this.currentBalance = this.currentBalance.subtract(transaction.getAmount());
        }
    }

    /**
     * Get formatted balance for display
     */
    public String getFormattedCurrentBalance() {
        return String.format("$%.2f", currentBalance);
    }

    /**
     * Check if account is a credit card (affects balance calculations)
     */
    public boolean isCreditCard() {
        return accountType == AccountType.CREDIT_CARD;
    }

    @Override
    public String toString() {
        return String.format("Account{id=%d, name='%s', type=%s, balance=%s}",
                id, accountName, accountType, getFormattedCurrentBalance());
    }
}