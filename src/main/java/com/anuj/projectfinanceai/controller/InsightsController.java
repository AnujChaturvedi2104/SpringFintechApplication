package com.anuj.projectfinanceai.controller;

import com.anuj.projectfinanceai.entity.User;
import com.anuj.projectfinanceai.services.FinancialInsightsService;
import com.anuj.projectfinanceai.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * InsightsController handles AI-powered financial insights
 */
@Controller
@RequestMapping("/insights")
public class InsightsController {

    private final FinancialInsightsService financialInsightsService;
    private final UserService userService;

    @Autowired
    public InsightsController(FinancialInsightsService financialInsightsService, UserService userService) {
        this.financialInsightsService = financialInsightsService;
        this.userService = userService;
    }

    /**
     * Display AI-powered financial insights
     */
    @GetMapping
    public String showInsights(Model model) {
        User user = getTestUser();

        try {
            // Generate insights (these will call ChatGPT API)
            String generalInsights = financialInsightsService.generateFinancialInsights(user);
            String spendingAnalysis = financialInsightsService.analyzeMonthlySpending(user);
            String budgetRecommendations = financialInsightsService.generateBudgetRecommendations(user);
            int healthScore = financialInsightsService.calculateFinancialHealthScore(user);

            model.addAttribute("user", user);
            model.addAttribute("generalInsights", generalInsights);
            model.addAttribute("spendingAnalysis", spendingAnalysis);
            model.addAttribute("budgetRecommendations", budgetRecommendations);
            model.addAttribute("healthScore", healthScore);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to generate insights at this time. Please check your API configuration.");
            System.err.println("Error generating insights: " + e.getMessage());
        }

        return "insights/dashboard";
    }

    private User getTestUser() {
        return userService.findByEmail("test@example.com")
                .orElseThrow(() -> new RuntimeException("Test user not found"));
    }
}