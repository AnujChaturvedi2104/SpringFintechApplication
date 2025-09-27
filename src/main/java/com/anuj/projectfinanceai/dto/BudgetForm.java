package com.anuj.projectfinanceai.dto;

import com.anuj.projectfinanceai.entity.Budget;
import com.anuj.projectfinanceai.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * BudgetForm DTO for handling budget creation and editing forms
 */
public class BudgetForm {

    @NotNull(message = "Please select a category")
    private Transaction.Category category;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    private BigDecimal budgetAmount;

    @NotNull(message = "Budget month is required")
    private YearMonth budgetMonth;

    // String field for form binding (HTML input type="month" gives YYYY-MM format)
    private String budgetMonthString;

    // Default constructor
    public BudgetForm() {
        // Default to current month
        this.budgetMonth = YearMonth.now();
        this.budgetMonthString = this.budgetMonth.toString();
    }

    // Constructor for editing existing budgets
    public BudgetForm(Budget budget) {
        this.category = budget.getCategory();
        this.budgetAmount = budget.getBudgetAmount();
        this.budgetMonth = budget.getBudgetMonth();
        this.budgetMonthString = this.budgetMonth.toString();
    }

    // Getters and Setters
    public Transaction.Category getCategory() { return category; }
    public void setCategory(Transaction.Category category) { this.category = category; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public YearMonth getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(YearMonth budgetMonth) {
        this.budgetMonth = budgetMonth;
        if (budgetMonth != null) {
            this.budgetMonthString = budgetMonth.toString();
        }
    }

    public String getBudgetMonthString() { return budgetMonthString; }
    public void setBudgetMonthString(String budgetMonthString) {
        this.budgetMonthString = budgetMonthString;
        if (budgetMonthString != null && !budgetMonthString.isEmpty()) {
            try {
                this.budgetMonth = YearMonth.parse(budgetMonthString);
            } catch (Exception e) {
                this.budgetMonth = YearMonth.now();
            }
        }
    }

    /**
     * Convert form data to Budget entity
     */
    public Budget toBudget() {
        Budget budget = new Budget();
        budget.setCategory(this.category);
        budget.setBudgetAmount(this.budgetAmount);
        budget.setBudgetMonth(this.budgetMonth);
        return budget;
    }
}