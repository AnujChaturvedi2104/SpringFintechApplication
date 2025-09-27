package com.anuj.projectfinanceai.controller;


import com.anuj.projectfinanceai.dto.TransactionForm;
import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.AccountService;
import com.anuj.projectfinanceai.services.TransactionService;
import com.anuj.projectfinanceai.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * TransactionController handles transaction management operations
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 AccountService accountService,
                                 UserService userService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.userService = userService;
    }

    /**
     * Display all recent transactions for the user
     */
    @GetMapping
    public String listTransactions(Model model) {
        User user = getTestUser();
        List<Transaction> transactions = transactionService.findRecentTransactionsByUser(user);

        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);

        return "transactions/list";
    }

    /**
     * Show form for creating new transaction
     */
    @GetMapping("/new")
    public String showNewTransactionForm(@RequestParam(required = false) Long accountId, Model model) {
        User user = getTestUser();
        List<Account> accounts = accountService.findAccountsByUser(user);

        if (accounts.isEmpty()) {
            return "redirect:/accounts/new?message=Please create an account first";
        }

        TransactionForm transactionForm = new TransactionForm();
        if (accountId != null) {
            transactionForm.setAccountId(accountId);
        }

        model.addAttribute("transactionForm", transactionForm);
        model.addAttribute("accounts", accounts);
        model.addAttribute("transactionTypes", Transaction.TransactionType.values());
        model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
        model.addAttribute("editing", false);

        return "transactions/form";
    }

    /**
     * Process new transaction creation
     */
    @PostMapping("/new")
    public String createTransaction(@Valid @ModelAttribute TransactionForm transactionForm,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            User user = getTestUser();
            List<Account> accounts = accountService.findAccountsByUser(user);

            model.addAttribute("accounts", accounts);
            model.addAttribute("transactionTypes", Transaction.TransactionType.values());
            model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
            model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
            model.addAttribute("editing", false);

            return "transactions/form";
        }

        try {
            Transaction transaction = transactionForm.toTransaction();
            Transaction savedTransaction = transactionService.createTransaction(transaction, transactionForm.getAccountId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Transaction '" + savedTransaction.getDescription() + "' created successfully!");

            return "redirect:/transactions";

        } catch (Exception e) {
            User user = getTestUser();
            List<Account> accounts = accountService.findAccountsByUser(user);

            model.addAttribute("errorMessage", "Error creating transaction: " + e.getMessage());
            model.addAttribute("accounts", accounts);
            model.addAttribute("transactionTypes", Transaction.TransactionType.values());
            model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
            model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
            model.addAttribute("editing", false);

            return "transactions/form";
        }
    }

    /**
     * Show form for editing existing transaction
     */
    @GetMapping("/{id}/edit")
    public String showEditTransactionForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> transactionOpt = transactionService.findById(id);

        if (transactionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found");
            return "redirect:/transactions";
        }

        User user = getTestUser();
        List<Account> accounts = accountService.findAccountsByUser(user);
        Transaction transaction = transactionOpt.get();

        model.addAttribute("transactionForm", new TransactionForm(transaction));
        model.addAttribute("accounts", accounts);
        model.addAttribute("transactionTypes", Transaction.TransactionType.values());
        model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
        model.addAttribute("transactionId", id);
        model.addAttribute("editing", true);

        return "transactions/form";
    }

    /**
     * Process transaction update
     */
    @PostMapping("/{id}/edit")
    public String updateTransaction(@PathVariable Long id,
                                    @Valid @ModelAttribute TransactionForm transactionForm,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            User user = getTestUser();
            List<Account> accounts = accountService.findAccountsByUser(user);

            model.addAttribute("accounts", accounts);
            model.addAttribute("transactionTypes", Transaction.TransactionType.values());
            model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
            model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
            model.addAttribute("transactionId", id);
            model.addAttribute("editing", true);

            return "transactions/form";
        }

        try {
            Transaction transaction = transactionForm.toTransaction();
            transaction.setId(id);

            Transaction updatedTransaction = transactionService.updateTransaction(transaction);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Transaction '" + updatedTransaction.getDescription() + "' updated successfully!");

            return "redirect:/transactions";

        } catch (Exception e) {
            User user = getTestUser();
            List<Account> accounts = accountService.findAccountsByUser(user);

            model.addAttribute("errorMessage", "Error updating transaction: " + e.getMessage());
            model.addAttribute("accounts", accounts);
            model.addAttribute("transactionTypes", Transaction.TransactionType.values());
            model.addAttribute("incomeCategories", Transaction.Category.getIncomeCategories());
            model.addAttribute("expenseCategories", Transaction.Category.getExpenseCategories());
            model.addAttribute("transactionId", id);
            model.addAttribute("editing", true);

            return "transactions/form";
        }
    }

    /**
     * Delete transaction
     */
    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting transaction: " + e.getMessage());
        }

        return "redirect:/transactions";
    }

    /**
     * Helper method to get test user (replace with actual authentication)
     */
    private User getTestUser() {
        return userService.findByEmail("test@example.com")
                .orElseThrow(() -> new RuntimeException("Test user not found"));
    }
}