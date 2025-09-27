package com.anuj.projectfinanceai.dto;

import com.anuj.projectfinanceai.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * TransactionForm DTO for handling transaction entry forms
 */
public class TransactionForm {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Please select transaction type")
    private Transaction.TransactionType transactionType;

    @NotNull(message = "Please select a category")
    private Transaction.Category category;

    @NotNull(message = "Transaction date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @NotNull(message = "Please select an account")
    private Long accountId;

    // Default constructor
    public TransactionForm() {
        // Default to today's date
        this.transactionDate = LocalDate.now();
    }

    // Constructor for editing existing transactions
    public TransactionForm(Transaction transaction) {
        this.description = transaction.getDescription();
        this.amount = transaction.getAmount();
        this.transactionType = transaction.getTransactionType();
        this.category = transaction.getCategory();
        this.transactionDate = transaction.getTransactionDate();
        this.accountId = transaction.getAccount().getId();
    }

    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Transaction.TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(Transaction.TransactionType transactionType) { this.transactionType = transactionType; }

    public Transaction.Category getCategory() { return category; }
    public void setCategory(Transaction.Category category) { this.category = category; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    /**
     * Convert form data to Transaction entity (account must be set separately)
     */
    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDescription(this.description);
        transaction.setAmount(this.amount);
        transaction.setTransactionType(this.transactionType);
        transaction.setCategory(this.category);
        transaction.setTransactionDate(this.transactionDate);
        return transaction;
    }
}