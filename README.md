Here’s a short way to verify both under Spring Boot 4.
1. Security
Your security is in SecurityConfiguration.java (lambda style, compatible with Spring Boot 4 / Security 7).
Run the existing security test:
  ./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest"
It already checks:
/download/{applicationId} without auth → unauthenticated
With OAuth2 but non-allowlisted email → 4xx
With OAuth2 and allowlisted admin email → 2xx and correct download header
If this test passes, your security setup is verified for Spring Boot 4.
2. Download endpoints
Controller behavior (no security):
  ./gradlew test --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest"
Security + download (admin by applicationId):
Same as above: SecurityConfigurationTest already hits GET /download/{applicationId} with OAuth2 and checks status and Content-Disposition.
Full stack (session download + security):
Tests like PdfMockMvcTest and JourneyTest use MockMvc with the security filter and hit /download in a session; run them if you want full integration coverage.
3. One command for both
./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest" --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest"
If both test classes pass, security and download endpoints are verified under Spring Boot 4.
A short guide with these steps (and a bit more detail) is in docs/verification-spring-boot-4-security-and-downloads.md. You don’t need to change production code; the existing tests are enough for this verification.
