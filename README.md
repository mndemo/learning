# Verifying security and download endpoints (Spring Boot 4)

## 1. Security components

Your security is configured in `SecurityConfiguration.java` (Spring Security 7 style):

- **Session-based app flow**: `/pages/*`, `/groups/*`, `/document-upload`, `/submit`, etc. → `permitAll()`
- **Client downloads**: `/download`, `/download-xml` → `permitAll()` (controller uses session to scope data)
- **Admin-only**: `/download/*`, `/resend-confirmation-email/*` → OAuth2 + allowlisted email via `EmailAuthorizationManager`

### Run security tests

```bash
# Full security config test (authn/authz for /download/{applicationId})
./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest"

# All tests that use the security filter (includes download flows)
./gradlew test --tests "*SecurityConfigurationTest" --tests "*ResendFailedEmailControllerTest"
```

**What `SecurityConfigurationTest` verifies:**

- `GET /download/{applicationId}` without auth → **unauthenticated**
- `GET /download/{applicationId}` with OAuth2 but non-allowlisted email → **4xx**
- `GET /download/{applicationId}` with OAuth2 and allowlisted admin email → **2xx** and correct `Content-Disposition` header

So you verify Spring Boot 4 security by running these tests; no code changes needed if they pass.

---

## 2. Download endpoints

| Endpoint | Access | Purpose |
|----------|--------|---------|
| `GET /download` | permitAll (session) | Zip of client PDFs for current session application |
| `GET /download-xml` | permitAll (session) | XML for current session application |
| `GET /download/{applicationId}` | OAuth2 + admin email | Admin download by application ID |

### Run download-related tests

```bash
# Unit tests for FileDownloadController (no security filter)
./gradlew test --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest"

# Security + download: admin download by applicationId
./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest"

# Integration-style tests that hit /download with full context (security + session)
./gradlew test --tests "org.codeforamerica.shiba.output.pdf.PdfMockMvcTest"
./gradlew test --tests "org.codeforamerica.shiba.journeys.MinimumSnapFlowJourneyTest"
```

**What is verified:**

- **FileDownloadControllerTest**: Controller behavior (status, headers, zip content, XML, “not found” message).
- **SecurityConfigurationTest**: `/download/{applicationId}` authn/authz and response headers.
- **PdfMockMvcTest / JourneyTest**: Full stack including security and session when calling `/download`.

---

## 5. Why is JourneyTest failing?

`JourneyTest` is an **abstract** class; the runnable tests are in subclasses such as `MinimumSnapFlowJourneyTest`, `FullFlowJourneyTest`, `DocumentUploadJourneyTest`, etc. Use a concrete class when running:

```bash
./gradlew test --tests "org.codeforamerica.shiba.journeys.MinimumSnapFlowJourneyTest"
```

Common reasons journey tests fail:

1. **Application context doesn’t start**  
   The tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` and need a full context. If the context fails (e.g. missing `ObjectMapper` bean with Spring Boot 4), all journey tests fail at startup.  
   **Fix:** Ensure the Jackson 2 setup is in place: `JacksonConfiguration` + `implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")` in `build.gradle`.

2. **Chrome / Chromedriver**  
   Journey tests use Selenium with a **real Chrome** (headless) via `SeleniumFactory` and `WebDriverManager.chromedriver().setup()`. If Chrome isn’t installed, or chromedriver can’t be downloaded (e.g. no network or permissions), `start()` fails.  
   **Fix:** Install Chrome and ensure the test run can download chromedriver (network access), or set `webdriver.chrome.driver` if you use a fixed path.

3. **Seeing the real failure**  
   To get the actual error instead of a generic Gradle message, run with stacktrace and optional info:
   ```bash
   ./gradlew test --tests "org.codeforamerica.shiba.journeys.MinimumSnapFlowJourneyTest" --stacktrace
   ```
   Then open the HTML report (e.g. `build/reports/tests/test/classes/org.codeforamerica.shiba.journeys.MinimumSnapFlowJourneyTest.html`) to see the failing test and stack trace.

4. **Journey tests are excluded from default unit test**  
   In `build.gradle`, `unitTest` excludes tags like `fullFlowJourney`, `minimumFlowJourney`, `documentUploadJourney`, etc. So `./gradlew unitTest` does not run journey tests; use `./gradlew test` with a specific test class as above.

---

## 3. One-shot verification

To confirm both security and download endpoints under Spring Boot 4 in one go:

```bash
./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest" \
              --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest"
```

If both pass, security configuration and download endpoint behavior are verified for Spring Boot 4.

---

## 4. Optional: manual checks (local run)

1. Start the app (e.g. `./gradlew bootRun` with required config/env).
2. **Session download (no login):** In a browser, complete a flow that sets session application data, then open `/download` and `/download-xml` — should return zip and XML.
3. **Admin download:** Log in with an OAuth2 user whose email is in `EmailAuthorizationManager.ADMIN_EMAILS`, then open `/download/{applicationId}` — should return zip; without auth or with non-allowlisted email, access should be denied.

---

## 6. SOAP-based web components (Spring Boot 4)

The app uses **Spring Web Services** as a **SOAP client** only (no SOAP server):

- **FileNet SOAP:** `FilenetWebServiceClient` sends documents to the MN IT FileNet service (`mnit-filenet.upload-url`) via `WebServiceTemplate`. JAXB classes are generated from `src/main/resources/FileNetService.wsdl` (task `genJaxbFilenet`).
- **Config:** `EsbWebServiceTemplateConfiguration` builds the `filenetWebServiceTemplate` bean (JAXB marshaller, HTTP client, Basic auth, timeouts). Uses `spring-boot-starter-web-services` and `WebServiceTemplateBuilder`.

### How to verify SOAP under Spring Boot 4

**1. Run the SOAP client unit tests (recommended)**

These tests use Spring WS’s `MockWebServiceServer` to stub the SOAP endpoint and assert request/response and retry behavior. They load the full Spring context (including `filenetWebServiceTemplate`) and prove the client works with Boot 4.

```bash
# FileNet SOAP client: marshalling, retries, SOAP headers
./gradlew test --tests "org.codeforamerica.shiba.mnit.FilenetWebServiceClientTest"

# Other tests that use the FileNet client (mocked)
./gradlew test --tests "org.codeforamerica.shiba.output.MnitDocumentConsumerTest"
./gradlew test --tests "org.codeforamerica.shiba.pages.RoutingDestinationServiceTest"
```

**What `FilenetWebServiceClientTest` verifies:**

- Successful send: request built and sent, SOAP response unmarshalled, FileNet object ID returned.
- SOAP fault / transport errors: retries and final failure handling.
- SOAP headers: e.g. routing/authentication headers present when required.

**2. Run any test that mocks the SOAP endpoint**

`PageControllerTest` uses `MockWebServiceServer` for the ClamMIT (virus scan) SOAP call. Running it confirms Spring WS test support works under Boot 4:

```bash
./gradlew test --tests "org.codeforamerica.shiba.pages.PageControllerTest"
```

**3. One-shot SOAP verification**

```bash
./gradlew test --tests "org.codeforamerica.shiba.mnit.FilenetWebServiceClientTest" \
              --tests "org.codeforamerica.shiba.output.MnitDocumentConsumerTest" \
              --tests "org.codeforamerica.shiba.pages.RoutingDestinationServiceTest"
```

If these pass, the SOAP client stack (WebServiceTemplate, JAXB, marshalling, and your client logic) is verified under Spring Boot 4.

**4. Optional: integration test against a real or stub endpoint**

- **Real FileNet (e.g. non-prod):** Run the app with `mnit-filenet.upload-url` (and credentials) pointing at the real service and trigger a flow that uploads a document (e.g. submit an application that routes to a county using FileNet). Check logs or FileNet for the created document.
- **Stub server:** Use a SOAP stub (e.g. SoapUI, WireMock with SOAP, or a small Spring WS server) that returns a valid `createDocumentResponse` and point `mnit-filenet.upload-url` at it; same flow to confirm end-to-end.

**Spring Boot 4 notes**

- `spring-boot-starter-web-services:4.0.2` brings in Spring WS and the `WebServiceTemplateBuilder`; no API changes are required for the existing client.
- JAXB is generated at build time (`genJaxbFilenet`); ensure the build runs so `org.codeforamerica.shiba.filenetwsdl` classes exist before running tests.
- If a test fails with “bean not found” or marshalling errors, confirm `mnit-filenet.*` properties are set in the test profile (e.g. `application-test.yaml` or test properties) so `EsbWebServiceTemplateConfiguration` can create the bean.

