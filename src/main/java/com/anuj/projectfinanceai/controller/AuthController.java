package com.anuj.projectfinanceai.controller;

import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/login")
    public String login() {
        return "auth/login";  // This will look for templates/auth/login.html
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user,
                           BindingResult result,
                           Model model) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Check if email already exists
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "An account with this email already exists");
            return "auth/register";
        }

        try {
            // Encrypt password and save user
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.createUser(user);

            model.addAttribute("successMessage",
                    "Registration successful! You can now log in with your credentials.");
            return "auth/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
}