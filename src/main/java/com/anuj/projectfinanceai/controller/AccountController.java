package com.anuj.projectfinanceai.controller;

import com.anuj.projectfinanceai.dto.AccountForm;
import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.AccountService;
import com.anuj.projectfinanceai.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
/**
 * AccountController handles account management operations
 *
 * This demonstrates:
 * - Form handling with validation
 * - Error message display
 * - Redirect with flash attributes
 * - CRUD operations through forms
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    /**
     * Display all accounts for a user
     */
    @GetMapping
    public String listAccounts(Model model) {
        // For simplicity, we'll use the test user. In a real app, you'd get this from authentication
        User user = getCurrentUser();

        List<Account> accounts = accountService.findAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);

        return "accounts/list";
    }

    /**
     * Show form for creating new account
     */
    @GetMapping("/new")
    public String showNewAccountForm(Model model) {
        model.addAttribute("accountForm", new AccountForm());
        model.addAttribute("accountTypes", Account.AccountType.values());
        model.addAttribute("editing", false);  // Add this line
        return "accounts/form";
    }

    /**
     * Process new account creation
     */
    @PostMapping("/new")
    public String createAccount(@Valid @ModelAttribute AccountForm accountForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", Account.AccountType.values());
            return "accounts/form";
        }

        try {
            // Convert form to entity and save
            Account account = accountForm.toAccount();
            account.setUser(getCurrentUser());

            Account savedAccount = accountService.createAccount(account);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Account '" + savedAccount.getAccountName() + "' created successfully!");

            return "redirect:/accounts";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating account: " + e.getMessage());
            model.addAttribute("accountTypes", Account.AccountType.values());
            return "accounts/form";
        }
    }

    /**
     * Show form for editing existing account
     */
    @GetMapping("/{id}/edit")
    public String showEditAccountForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountService.findById(id);

        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
            return "redirect:/accounts";
        }

        Account account = accountOpt.get();
        model.addAttribute("accountForm", new AccountForm(account));
        model.addAttribute("accountTypes", Account.AccountType.values());
        model.addAttribute("accountId", id);
        model.addAttribute("editing", true);

        return "accounts/form";
    }

    /**
     * Process account update
     */
    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @Valid @ModelAttribute AccountForm accountForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        System.out.println("=== EDIT ACCOUNT DEBUG ===");
        System.out.println("Account ID: " + id);
        System.out.println("Form Account Amount: " + accountForm.getInitialBalance());
        System.out.println("Has errors: " + result.hasErrors());

        if (result.hasErrors()) {
            System.out.println("Errors: " + result.getAllErrors());

            model.addAttribute("accountTypes", Account.AccountType.values());
            model.addAttribute("accountId", id);
            model.addAttribute("editing", true);
            return "accounts/form";
        }

        try {
            Optional<Account> existingAccountOpt = accountService.findById(id);
            if (existingAccountOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
                return "redirect:/accounts";
            }

            Account existingAccount = existingAccountOpt.get();
            existingAccount.setAccountName(accountForm.getAccountName());
            existingAccount.setAccountType(accountForm.getAccountType());
            // Note: We don't update initial balance for existing accounts

            accountService.createAccount(existingAccount); // This will update since ID exists

            redirectAttributes.addFlashAttribute("successMessage",
                    "Account '" + existingAccount.getAccountName() + "' updated successfully!");

            return "redirect:/accounts";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating account: " + e.getMessage());
            model.addAttribute("accountTypes", Account.AccountType.values());
            model.addAttribute("accountId", id);
            model.addAttribute("editing", true);
            return "accounts/form";
        }
    }

    /**
     * Helper method to get currently logged-in user
     */
    private User getCurrentUser() {
        // Get the email of the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}