package com.anuj.projectfinanceai.controller;

import com.anuj.projectfinanceai.dto.BudgetForm;
import com.anuj.projectfinanceai.entity.Budget;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.BudgetService;
import com.anuj.projectfinanceai.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * BudgetController handles budget management operations
 */
@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    @Autowired
    public BudgetController(BudgetService budgetService, UserService userService) {
        this.budgetService = budgetService;
        this.userService = userService;
    }

    /**
     * Display budgets for current month with option to switch months
     */
    @GetMapping
    public String listBudgets(@RequestParam(required = false) String month, Model model) {
        User user = getCurrentUser();

        // Parse month parameter or default to current month
        YearMonth selectedMonth;
        try {
            selectedMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        } catch (Exception e) {
            selectedMonth = YearMonth.now();
        }

        List<BudgetService.BudgetSummary> budgetSummaries =
                budgetService.getBudgetSummaryForMonth(user, selectedMonth);

        model.addAttribute("budgetSummaries", budgetSummaries);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedMonthFormatted",
                selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("user", user);

        return "budgets/list";
    }

    /**
     * Show form for creating new budget
     */
    @GetMapping("/new")
    public String showNewBudgetForm(@RequestParam(required = false) String month, Model model) {
        User user = getCurrentUser();

        // Parse month parameter or default to current month
        YearMonth selectedMonth;
        try {
            selectedMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        } catch (Exception e) {
            selectedMonth = YearMonth.now();
        }

        // Get available categories for this month
        List<Transaction.Category> availableCategories =
                budgetService.getAvailableCategoriesForMonth(user, selectedMonth);

        if (availableCategories.isEmpty()) {
            return "redirect:/budgets?month=" + selectedMonth +
                    "&message=All expense categories already have budgets for " +
                    selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        }

        BudgetForm budgetForm = new BudgetForm();
        budgetForm.setBudgetMonth(selectedMonth);

        model.addAttribute("budgetForm", budgetForm);
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("editing", false);

        return "budgets/form";
    }

    /**
     * Process new budget creation
     */
    @PostMapping("/new")
    public String createBudget(@Valid @ModelAttribute BudgetForm budgetForm,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Handle validation errors
        if (result.hasErrors()) {
            User user = getCurrentUser();
            YearMonth month = budgetForm.getBudgetMonth() != null ? budgetForm.getBudgetMonth() : YearMonth.now();
            List<Transaction.Category> availableCategories =
                    budgetService.getAvailableCategoriesForMonth(user, month);

            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("editing", false);
            return "budgets/form";
        }

        try {
            Budget budget = budgetForm.toBudget();
            budget.setUser(getCurrentUser());

            Budget savedBudget = budgetService.createBudget(budget);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Budget for " + savedBudget.getCategory().getDisplayName() + " created successfully!");

            return "redirect:/budgets?month=" + savedBudget.getBudgetMonth().toString();

        } catch (Exception e) {
            User user = getCurrentUser();
            YearMonth month = budgetForm.getBudgetMonth() != null ? budgetForm.getBudgetMonth() : YearMonth.now();
            List<Transaction.Category> availableCategories =
                    budgetService.getAvailableCategoriesForMonth(user, month);

            model.addAttribute("errorMessage", "Error creating budget: " + e.getMessage());
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("editing", false);
            return "budgets/form";
        }
    }

    /**
     * Show form for editing existing budget
     */
    @GetMapping("/{id}/edit")
    public String showEditBudgetForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Budget> budgetOpt = budgetService.findById(id);

        if (budgetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Budget not found");
            return "redirect:/budgets";
        }

        Budget budget = budgetOpt.get();

        model.addAttribute("budgetForm", new BudgetForm(budget));
        model.addAttribute("budget", budget);
        model.addAttribute("budgetId", id);
        model.addAttribute("editing", true);

        return "budgets/form";
    }

    /**
     * Process budget update
     */
    @PostMapping("/{id}/edit")
    public String updateBudget(@PathVariable Long id,
                               @Valid @ModelAttribute BudgetForm budgetForm,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        System.out.println("=== EDIT BUDGET DEBUG ===");
        System.out.println("Budget ID: " + id);
        System.out.println("Form Budget Amount: " + budgetForm.getBudgetAmount());
        System.out.println("Has errors: " + result.hasErrors());

        if (result.hasErrors()) {
            System.out.println("Errors: " + result.getAllErrors());

            Optional<Budget> budgetOpt = budgetService.findById(id);
            if (budgetOpt.isPresent()) {
                model.addAttribute("budget", budgetOpt.get());
            }
            model.addAttribute("budgetId", id);
            model.addAttribute("editing", true);
            System.out.println("No validation errors, proceeding to update...");

            return "budgets/form";
        }

        try {
            // Get the existing budget to preserve user, category, and month
            Optional<Budget> existingBudgetOpt = budgetService.findById(id);
            if (existingBudgetOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Budget not found");
                return "redirect:/budgets";
            }

            Budget existingBudget = existingBudgetOpt.get();

            // Only update the budget amount
            existingBudget.setBudgetAmount(budgetForm.getBudgetAmount());

            Budget updatedBudget = budgetService.updateBudget(existingBudget);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Budget for " + updatedBudget.getCategory().getDisplayName() + " updated successfully!");

            return "redirect:/budgets?month=" + updatedBudget.getBudgetMonth().toString();

        } catch (Exception e) {
            Optional<Budget> budgetOpt = budgetService.findById(id);
            if (budgetOpt.isPresent()) {
                model.addAttribute("budget", budgetOpt.get());
            }
            model.addAttribute("errorMessage", "Error updating budget: " + e.getMessage());
            model.addAttribute("budgetId", id);
            model.addAttribute("editing", true);
            return "budgets/form";
        }
    }

    /**
     * Delete budget
     */
    @PostMapping("/{id}/delete")
    public String deleteBudget(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Budget> budgetOpt = budgetService.findById(id);
            YearMonth budgetMonth = YearMonth.now();

            if (budgetOpt.isPresent()) {
                budgetMonth = budgetOpt.get().getBudgetMonth();
            }

            budgetService.deleteBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Budget deleted successfully!");

            return "redirect:/budgets?month=" + budgetMonth;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting budget: " + e.getMessage());
            return "redirect:/budgets";
        }
    }

    /**
     * Helper method to get test user (replace with actual authentication)
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

}