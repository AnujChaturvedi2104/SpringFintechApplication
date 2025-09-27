package com.anuj.projectfinanceai.services;

import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.Transaction;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.repository.AccountRepository;
import com.anuj.projectfinanceai.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TransactionService handles business logic for transactions
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Create a new transaction and update account balance
     */
    public Transaction createTransaction(Transaction transaction, Long accountId) {
        // Find the account
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found with ID: " + accountId);
        }

        Account account = accountOpt.get();
        transaction.setAccount(account);

        // Save the transaction first
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Update account balance
        BigDecimal currentBalance = account.getCurrentBalance();
        BigDecimal newBalance;

        if (transaction.getTransactionType() == Transaction.TransactionType.INCOME) {
            newBalance = currentBalance.add(transaction.getAmount());
        } else { // EXPENSE
            newBalance = currentBalance.subtract(transaction.getAmount());
        }

        account.setCurrentBalance(newBalance);
        accountRepository.save(account);

        return savedTransaction;
    }

    /**
     * Find transaction by ID
     */
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Find all transactions for an account
     */
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByAccount(Account account) {
        return transactionRepository.findByAccountOrderByTransactionDateDescCreatedAtDesc(account);
    }

    /**
     * Find recent transactions for a user across all accounts
     */
    @Transactional(readOnly = true)
    public List<Transaction> findRecentTransactionsByUser(User user) {
        return transactionRepository.findRecentTransactionsByUser(user.getId());
    }

    /**
     * Calculate total spending by category for a user in a specific month
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateSpendingByCategoryAndMonth(User user, Transaction.Category category,
                                                          int year, int month) {
        return transactionRepository.calculateSpendingByCategoryAndMonth(user.getId(), category, year, month);
    }

    /**
     * Calculate total income for a user in a date range
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIncomeForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateTotalIncomeForUserInPeriod(user.getId(), startDate, endDate);
    }

    /**
     * Calculate total expenses for a user in a date range
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalExpensesForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateTotalExpensesForUserInPeriod(user.getId(), startDate, endDate);
    }

    /**
     * Update an existing transaction
     */
    public Transaction updateTransaction(Transaction transaction) {
        if (transaction.getId() == null) {
            throw new IllegalArgumentException("Cannot update transaction without ID");
        }

        Optional<Transaction> existingOpt = transactionRepository.findById(transaction.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found with ID: " + transaction.getId());
        }

        Transaction existing = existingOpt.get();
        Account account = existing.getAccount();

        // Reverse the effect of the old transaction
        BigDecimal currentBalance = account.getCurrentBalance();
        if (existing.getTransactionType() == Transaction.TransactionType.INCOME) {
            currentBalance = currentBalance.subtract(existing.getAmount());
        } else {
            currentBalance = currentBalance.add(existing.getAmount());
        }

        // Apply the new transaction
        if (transaction.getTransactionType() == Transaction.TransactionType.INCOME) {
            currentBalance = currentBalance.add(transaction.getAmount());
        } else {
            currentBalance = currentBalance.subtract(transaction.getAmount());
        }

        // Update account balance
        account.setCurrentBalance(currentBalance);
        accountRepository.save(account);

        // Update transaction fields
        existing.setDescription(transaction.getDescription());
        existing.setAmount(transaction.getAmount());
        existing.setTransactionType(transaction.getTransactionType());
        existing.setCategory(transaction.getCategory());
        existing.setTransactionDate(transaction.getTransactionDate());

        return transactionRepository.save(existing);
    }

    /**
     * Delete a transaction and update account balance
     */
    public void deleteTransaction(Long id) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found with ID: " + id);
        }

        Transaction transaction = transactionOpt.get();
        Account account = transaction.getAccount();

        // Reverse the transaction's effect on account balance
        BigDecimal currentBalance = account.getCurrentBalance();
        if (transaction.getTransactionType() == Transaction.TransactionType.INCOME) {
            currentBalance = currentBalance.subtract(transaction.getAmount());
        } else {
            currentBalance = currentBalance.add(transaction.getAmount());
        }

        account.setCurrentBalance(currentBalance);
        accountRepository.save(account);

        // Delete the transaction
        transactionRepository.deleteById(id);
    }
}