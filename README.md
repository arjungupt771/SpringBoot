# BFH Java Webhook (Spring Boot)

Automates the **Bajaj Finserv Health | Qualifier 1 | JAVA** task.

## What it does
1. On startup, POSTs to **/hiring/generateWebhook/JAVA** with your `name`, `regNo`, and `email`.
2. Receives a JSON with `webhook` and `accessToken` (JWT).
3. Determines which SQL to submit based on **last two digits** of `regNo`:
   - **Odd → Question 1**
   - **Even → Question 2**
4. Reads the corresponding SQL from `application.properties` (`app.finalQueryOdd` or `app.finalQueryEven`).
5. Stores the selected SQL to `target/solution.txt`.
6. Submits the SQL to the **returned `webhook` URL** (if present) with `Authorization: <accessToken>` header. Falls back to `/hiring/testWebhook/JAVA` if `webhook` is missing.

## Quick start
```bash
# Java 17+ and Maven required
mvn -v
java -version

# Build
mvn clean package

# Run
java -jar target/bfh-java-webhook-1.0.0.jar
```

## Configure your details
Edit `src/main/resources/application.properties`:
```properties
app.name=Your Name
app.regNo=REG12345
app.email=you@example.com

# Paste your final SQL answers here:
app.finalQueryOdd=-- your SQL for Question 1
SELECT 1;

app.finalQueryEven=-- your SQL for Question 2
SELECT 1;
```

> **Note:** The app uses the `webhook` and `accessToken` returned by the first API. It automatically sets the `Authorization` header for the submission call.

## Repo Submission Checklist
- Public GitHub repository containing:
  - Full source code
  - Final JAR (`target/bfh-java-webhook-1.0.0.jar`)
  - Direct raw downloadable link to the JAR (GitHub raw URL)
- Submit the form: https://forms.office.com/r/5Kzb1h7fre

## Tech
- Spring Boot 3 (WebFlux `WebClient`)
- Java 17
