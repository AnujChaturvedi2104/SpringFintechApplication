package com.anuj.projectfinanceai.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * ChatGptService provides AI-powered financial insights
 *
 * This service demonstrates:
 * - External API integration with authentication
 * - JSON request/response handling
 * - Business context for AI prompts
 * - Error handling for external services
 */
@Service
public class ChatGptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatGptService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate financial advice based on user's spending patterns
     */
    public String generateFinancialAdvice(Map<String, Object> financialData) {
        try {
            String prompt = buildFinancialAdvicePrompt(financialData);
            return callChatGpt(prompt);
        } catch (Exception e) {
            System.err.println("Error calling ChatGPT: " + e.getMessage());
            return getFallbackAdvice();
        }
    }

    /**
     * Analyze spending patterns and provide insights
     */
    public String analyzeSpendingPatterns(Map<String, BigDecimal> categorySpending, BigDecimal totalBudget) {
        try {
            String prompt = buildSpendingAnalysisPrompt(categorySpending, totalBudget);
            return callChatGpt(prompt);
        } catch (Exception e) {
            System.err.println("Error analyzing spending: " + e.getMessage());
            return "Unable to analyze spending patterns at this time. Please check your budget categories and try again.";
        }
    }

    /**
     * Generate budget recommendations based on income and expenses
     */
    public String generateBudgetRecommendations(BigDecimal monthlyIncome, Map<String, BigDecimal> currentSpending) {
        try {
            String prompt = buildBudgetRecommendationPrompt(monthlyIncome, currentSpending);
            return callChatGpt(prompt);
        } catch (Exception e) {
            System.err.println("Error generating budget recommendations: " + e.getMessage());
            return "Consider following the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings and debt repayment.";
        }
    }

    /**
     * Build a comprehensive financial advice prompt
     */
    private String buildFinancialAdvicePrompt(Map<String, Object> financialData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As a professional financial advisor, please analyze this financial situation and provide personalized advice:\n\n");

        // Add financial data to prompt
        financialData.forEach((key, value) -> {
            prompt.append(key).append(": ").append(value).append("\n");
        });

        prompt.append("\nPlease provide:\n");
        prompt.append("1. Assessment of current financial health\n");
        prompt.append("2. Specific actionable recommendations\n");
        prompt.append("3. Areas for improvement\n");
        prompt.append("4. Positive reinforcement for good habits\n");
        prompt.append("\nKeep the advice practical, encouraging, and under 300 words.");

        return prompt.toString();
    }

    /**
     * Build spending analysis prompt
     */
    private String buildSpendingAnalysisPrompt(Map<String, BigDecimal> categorySpending, BigDecimal totalBudget) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this monthly spending breakdown and provide insights:\n\n");
        prompt.append("Total Budget: $").append(totalBudget).append("\n\n");
        prompt.append("Spending by Category:\n");

        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : categorySpending.entrySet()) {
            prompt.append("- ").append(entry.getKey()).append(": $").append(entry.getValue()).append("\n");
            totalSpent = totalSpent.add(entry.getValue());
        }

        prompt.append("\nTotal Spent: $").append(totalSpent).append("\n");
        prompt.append("Remaining Budget: $").append(totalBudget.subtract(totalSpent)).append("\n\n");
        prompt.append("Please identify spending patterns, highlight any concerning areas, and suggest optimizations. Keep response under 200 words.");

        return prompt.toString();
    }

    /**
     * Build budget recommendation prompt
     */
    private String buildBudgetRecommendationPrompt(BigDecimal monthlyIncome, Map<String, BigDecimal> currentSpending) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a budget recommendation for someone with:\n\n");
        prompt.append("Monthly Income: $").append(monthlyIncome).append("\n\n");
        prompt.append("Current Spending:\n");

        BigDecimal totalSpending = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : currentSpending.entrySet()) {
            prompt.append("- ").append(entry.getKey()).append(": $").append(entry.getValue()).append("\n");
            totalSpending = totalSpending.add(entry.getValue());
        }

        prompt.append("\nTotal Current Spending: $").append(totalSpending).append("\n\n");
        prompt.append("Please suggest an optimized budget allocation with specific dollar amounts for each category. ");
        prompt.append("Include emergency fund and savings recommendations. Keep response under 250 words.");

        return prompt.toString();
    }

    /**
     * Make the actual API call to ChatGPT
     */
    private String callChatGpt(String prompt) throws Exception {
        // Build request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("max_tokens", 400);
        requestBody.put("temperature", 0.7);

        // Build messages array
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", List.of(message));

        // Make the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        // Parse response
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.get("choices").get(0).get("message").get("content").asText().trim();
    }

    /**
     * Fallback advice when ChatGPT is unavailable
     */
    private String getFallbackAdvice() {
        return "Here are some general financial tips while our AI advisor is unavailable:\n\n" +
                "• Track your spending regularly to understand where your money goes\n" +
                "• Build an emergency fund covering 3-6 months of expenses\n" +
                "• Pay off high-interest debt first\n" +
                "• Automate your savings to make it consistent\n" +
                "• Review and adjust your budget monthly\n" +
                "• Consider increasing your income through side hustles or skill development";
    }
}