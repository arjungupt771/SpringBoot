package com.wizardking.bfh;

public class GenerateWebhookRequest {
    private String name;
    private String regNo;
    private String email;

    public GenerateWebhookRequest() {}
    public GenerateWebhookRequest(String name, String regNo, String email) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}

class SubmissionRequest {
    private String finalQuery;

    public SubmissionRequest() {}
    public SubmissionRequest(String finalQuery) { this.finalQuery = finalQuery; }

    public String getFinalQuery() { return finalQuery; }
    public void setFinalQuery(String finalQuery) { this.finalQuery = finalQuery; }
}
