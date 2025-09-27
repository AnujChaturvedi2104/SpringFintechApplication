package com.anuj.projectfinanceai.controller;
import com.anuj.projectfinanceai.entity.Account;
import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.AccountService;
import com.anuj.projectfinanceai.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.math.BigDecimal;
@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/")
    public String home(Model model) {
        // Create a test user and account to verify everything works
        createTestData();

        model.addAttribute("message", "Finance app with JPA is working!");
        return "index";
    }

    private void createTestData() {
        // Check if test user already exists
        if (userService.findByEmail("test@example.com").isEmpty()) {
            // Create test user
            User testUser = new User("John", "Doe", "test@example.com");
            testUser = userService.createUser(testUser);

            // Create test accounts
            Account checking = new Account("Main Checking", Account.AccountType.CHECKING,
                    new BigDecimal("2500.00"), testUser);
            accountService.createAccount(checking);

            Account savings = new Account("Emergency Fund", Account.AccountType.SAVINGS,
                    new BigDecimal("10000.00"), testUser);
            accountService.createAccount(savings);

            System.out.println("Created test user: " + testUser);
            System.out.println("Created test accounts successfully!");
        }
    }
}
