package com.anuj.projectfinanceai.dto;

import com.anuj.projectfinanceai.entity.Account;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * AccountForm DTO for handling account creation forms
 *
 * DTOs separate form data from entity data and provide:
 * - Form-specific validation
 * - Clean separation of concerns
 * - Better error handling in forms
 */
public class AccountForm {

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotNull(message = "Please select an account type")
    private Account.AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    // Default constructor
    public AccountForm() {}

    // Constructor for editing existing accounts
    public AccountForm(Account account) {
        this.accountName = account.getAccountName();
        this.accountType = account.getAccountType();
        this.initialBalance = account.getInitialBalance();
    }

    // Getters and Setters
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    /**
     * Convert form data to Account entity
     */
    public Account toAccount() {
        Account account = new Account();
        account.setAccountName(this.accountName);
        account.setAccountType(this.accountType);
        account.setInitialBalance(this.initialBalance);
        account.setCurrentBalance(this.initialBalance);
        return account;
    }
}
