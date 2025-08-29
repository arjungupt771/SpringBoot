package com.wizardking.bfh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StartupService {
    private static final Logger log = LoggerFactory.getLogger(StartupService.class);

    private final WebClient webClient;

    @Value("${api.generateWebhook}")
    private String generateWebhookUrl;

    @Value("${api.submitFallback}")
    private String submitFallbackUrl;

    @Value("${app.name}")
    private String name;
    @Value("${app.regNo}")
    private String regNo;
    @Value("${app.email}")
    private String email;

    @Value("${app.finalQueryOdd}")
    private String finalQueryOdd;
    @Value("${app.finalQueryEven}")
    private String finalQueryEven;

    public StartupService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void executeFlow() {
        log.info("Starting BFH automation flow");
        log.debug("Using regNo: {}", regNo);

        // 1) Generate webhook + token
        GenerateWebhookRequest request = new GenerateWebhookRequest(name, regNo, email);
        GenerateWebhookResponse response = webClient.post()
                .uri(generateWebhookUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .onErrorResume(err -> {
                    log.error("Failed to call generateWebhook endpoint: {}", err.getMessage());
                    return Mono.empty();
                })
                .block();

        if (response == null) {
            log.error("No response from generateWebhook; aborting.");
            return;
        }

        String webhookUrl = StringUtils.hasText(response.getWebhook()) ? response.getWebhook() : submitFallbackUrl;
        String accessToken = response.getAccessToken();

        log.info("Received webhook: {}", webhookUrl);
        log.info("Received accessToken: {}{}", (accessToken != null && accessToken.length() > 10) ? accessToken.substring(0, 10) : accessToken, "...");

        // 2) Decide which SQL to submit based on last two digits of regNo
        int lastTwo = extractLastTwoDigits(regNo);
        boolean isOdd = (lastTwo % 2) == 1;
        String selectedSql = isOdd ? finalQueryOdd : finalQueryEven;
        log.info("Last two digits: {} -> {}", lastTwo, isOdd ? "ODD (Question 1)" : "EVEN (Question 2)");

        if (!StringUtils.hasText(selectedSql)) {
            log.warn("Selected SQL is empty. Check application.properties (app.finalQueryOdd/app.finalQueryEven).");
            selectedSql = "SELECT 1;";
        }

        // 3) Store the result locally (optional requirement)
        try {
            Path out = Path.of("target", "solution.txt");
            Files.createDirectories(out.getParent());
            Files.writeString(out, selectedSql);
            log.info("Stored selected SQL to {}", out.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to write solution.txt: {}", e.getMessage());
        }

        // 4) Submit the SQL to the webhook with JWT
        if (!StringUtils.hasText(accessToken)) {
            log.error("Missing accessToken; cannot submit. Aborting.");
            return;
        }

        SubmissionRequest submission = new SubmissionRequest(selectedSql);

        String result = webClient.post()
                .uri(webhookUrl)
                .headers(h -> h.set("Authorization", accessToken))
                .bodyValue(submission)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(err -> {
                    log.error("Submission failed: {}", err.getMessage());
                    return Mono.just("Submission failed: " + err.getMessage());
                })
                .block();

        log.info("Submission response: {}", result);
    }

    private int extractLastTwoDigits(String s) {
        if (!StringUtils.hasText(s)) return 0;
        Matcher m = Pattern.compile("(+)").matcher(s);
        String digits = "";
        while (m.find()) digits += m.group(1);
        if (digits.length() < 2) {
            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        String lastTwo = digits.substring(digits.length() - 2);
        try {
            return Integer.parseInt(lastTwo);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
