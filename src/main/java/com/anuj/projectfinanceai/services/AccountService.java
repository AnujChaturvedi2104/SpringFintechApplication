package com.anuj.projectfinanceai.services;
import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.anuj.projectfinanceai.repository.AccountRepository;

/**
 * AccountService handles business logic for Account operations
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Create a new account for a user
     */
    public Account createAccount(Account account) {
        if (account.getUser() == null) {
            throw new IllegalArgumentException("Account must be associated with a user");
        }

        // Set current balance to initial balance
        account.setCurrentBalance(account.getInitialBalance());

        return accountRepository.save(account);
    }

    /**
     * Find account by ID
     */
    @Transactional(readOnly = true)
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    /**
     * Find all accounts for a user
     */
    @Transactional(readOnly = true)
    public List<Account> findAccountsByUser(User user) {
        return accountRepository.findByUserOrderByAccountNameAsc(user);
    }

    /**
     * Calculate total net worth for a user
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateNetWorth(User user) {
        List<Account> accounts = accountRepository.findByUserOrderByAccountNameAsc(user);
        BigDecimal netWorth = BigDecimal.ZERO;

        for (Account account : accounts) {
            if (account.getAccountType() == Account.AccountType.CREDIT_CARD) {
                // Credit card balances are liabilities (subtract from net worth)
                netWorth = netWorth.subtract(account.getCurrentBalance());
            } else {
                // Checking and savings are assets (add to net worth)
                netWorth = netWorth.add(account.getCurrentBalance());
            }
        }

        return netWorth;
    }

    /**
     * Update account balance (called when transactions are added)
     */
    public Account updateBalance(Long accountId, BigDecimal newBalance) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found with ID: " + accountId);
        }

        Account account = accountOpt.get();
        account.setCurrentBalance(newBalance);
        return accountRepository.save(account);
    }

    /**
     * Delete account (only if no transactions exist)
     */
    public void deleteAccount(Long id) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found with ID: " + id);
        }

        Account account = accountOpt.get();
        if (!account.getTransactions().isEmpty()) {
            throw new IllegalStateException("Cannot delete account with existing transactions");
        }

        accountRepository.deleteById(id);
    }
}