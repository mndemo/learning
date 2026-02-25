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

## 5a. SOAP vs REST: what’s the difference? (examples in this app)

This app uses **both** SOAP and REST. They solve different problems and look different on the wire. Below is a concise comparison with concrete examples from the codebase.

### What is SOAP?

- **SOAP** = **S**imple **O**bject **A**ccess **P**rotocol. It’s a **protocol** for exchanging structured messages (usually XML) over HTTP (or other transports).
- **Contract-first:** The service is described by a **WSDL** (Web Services Description Language) file. From that, you generate Java types (e.g. JAXB) and send/receive **strongly typed XML** payloads.
- **One endpoint, many operations:** A single URL (e.g. the FileNet service URL) is used for many logical “operations”; the operation is identified **inside** the XML body (e.g. `<CreateDocument>`), not by the HTTP path.
- **Message format:** XML envelope with optional headers (security, routing) and a body. Example (simplified):
  ```xml
  <soap:Envelope>
    <soap:Header>...</soap:Header>
    <soap:Body>
      <CreateDocument>
        <repositoryId>...</repositoryId>
        <properties>...</properties>
        <contentStream>...</contentStream>
      </CreateDocument>
    </soap:Body>
  </soap:Envelope>
  ```
- **Typical use:** Enterprise integration where the **other side** (e.g. MN IT FileNet) already exposes a SOAP service and provides a WSDL.

### What is REST?

- **REST** = **Re**presentational **S**tate **T**ransfer. It’s an **architectural style** that uses HTTP as intended: **URLs identify resources**, and **HTTP methods** (GET, POST, PUT, DELETE) express what you do.
- **No WSDL:** APIs are described by documentation or OpenAPI; payloads are often **JSON** (or XML). No generated stub from a single WSDL.
- **Many endpoints, one “verb” per resource:** Each logical resource has its own path (e.g. `/api/application-data`, `comm-hub-text.url`). The **HTTP method** (GET vs POST) and the **path** define the operation.
- **Message format:** Plain HTTP request/response. Body is usually JSON. Example:
  ```http
  POST /api/comm-hub/text HTTP/1.1
  Content-Type: application/json

  {"applicationId":"abc123","phone":"555-1234",...}
  ```
- **Typical use:** Modern APIs (internal or external), mobile/SPA backends, and any service that prefers simple HTTP + JSON.

### Side-by-side in this app

| Aspect | SOAP (in this app) | REST (in this app) |
|--------|--------------------|--------------------|
| **Where** | **FileNet document upload** (`FilenetWebServiceClient`) only | **Comm-hub** text/email (`CommunicationClient`), **ClamMIT virus scan** (PageController, plain HTTP POST), **ApplicationData API** (`ApplicationDataApiController`), **downloads**, **Mailgun**, **SmartyStreets** |
| **Contract** | WSDL: `FileNetService.wsdl` → JAXB classes (`CreateDocument`, `CmisContentStreamType`, etc.) | No WSDL; JSON payloads and documented paths (e.g. `GET /api/application-data`, `POST` to comm-hub URL) |
| **Client stack** | `WebServiceTemplate` (Spring WS), JAXB marshalling, SOAP envelope + headers | `RestTemplate` or `WebClient`, Jackson/JSON or raw body |
| **Request shape** | XML envelope with operation and typed elements inside the body | HTTP method + URL + headers + JSON (or form) body |
| **Example: outbound call** | **FileNet:** Build `CreateDocument` + CMIS types → marshal to SOAP → send to one FileNet URL. Operation = “create document” is in the XML. | **Comm-hub:** Build `JsonObject` → `RestTemplate.postForEntity(commHubUrl, entity, String.class)`. Operation = “send” is implied by POST to that URL. |
| **Example: inbound API** | (App does not expose SOAP; it only **calls** SOAP services.) | **ApplicationDataApiController:** `GET /api/application-data` returns JSON of session application data. Resource = “current application data”; verb = GET. |

### Why this app has both

- **SOAP** is used where the **external system** (MN IT FileNet) **is** SOAP and provides a WSDL. The app has no choice but to speak SOAP to that endpoint.
- **REST** is used for:
  - **Our own API** (e.g. `/api/application-data`) — simple JSON over HTTP.
  - **External APIs** that are REST (comm-hub, Mailgun, SmartyStreets) — HTTP + JSON (or form-encoded) with `RestTemplate` / `WebClient`.

So: **SOAP = XML + WSDL + one URL, many operations in the body. REST = HTTP resources + methods + usually JSON.** In this app, SOAP is used for FileNet only; REST (or plain HTTP) is used for comm-hub, ClamMIT scan, application data API, downloads, and other HTTP/JSON services.

---

## 5b. Why FileNet, ClamMIT, and comm-hub? (with examples)

The app integrates with three external systems that serve distinct purposes in the MNbenefits flow. Below is why each exists and how it’s used, with concrete examples from the codebase.

### FileNet (MN IT document storage)

**Why we need it**  
The State of Minnesota stores official application documents (CAF, CCAP, XML, uploaded proofs) in **FileNet**. Caseworkers and county systems pull applications from FileNet. MNbenefits does not replace that system; it must **deliver** every submitted application and its attachments into FileNet so the state can process them.

**What it does**  
- Accepts generated PDFs (e.g. CAF, CCAP) and XML, plus uploaded documents (e.g. proof of income).
- Stores them under the correct repository and metadata so they’re visible to the right county/tribal nation.
- Returns document IDs used for status tracking (e.g. `filenet_id` on application status).

**Example in the app**  
1. Applicant completes a SNAP application and clicks **Submit**.  
2. `ApplicationSubmittedListener` fires and calls `MnitDocumentConsumer.processCafAndCcap(application)`.  
3. For each routing destination (e.g. Dakota County), the app generates the CAF PDF and XML.  
4. `FilenetWebServiceClient.send(...)` is called with each file; it builds a SOAP `CreateDocument` request (CMIS properties + content stream), sends it to `mnit-filenet.upload-url` (e.g. `.../FileNet/ObjectService/SOAP`), and records the returned object ID.  
5. Caseworkers see the application and attachments in the state’s FileNet-based system.

**Config**  
- `mnit-filenet.enabled`, `mnit-filenet.upload-url`, `mnit-filenet.username` / `password`, timeouts, retries. When `enabled` is false, submission still saves the application in our DB but does not send to FileNet.

---

### ClamMIT (virus scanning for uploads)

**Why we need it**  
Users upload documents (IDs, pay stubs, etc.) that we store and later send to FileNet. To avoid storing or forwarding malware, every uploaded file is scanned **before** we accept it. **ClamMIT** is the MN IT–hosted virus scan service (ClamAV-based); we must use it so uploads meet the state’s security requirements.

**What it does**  
- Exposes an HTTP endpoint that accepts the raw file bytes (e.g. `POST` with body = file).
- Scans the file and returns a status: clean or virus detected.
- The app never stores or forwards the file if the scan indicates a virus.

**Example in the app**  
1. Applicant chooses a file and submits the **Upload documents** form (`POST /document-upload`).  
2. `PageController` receives the multipart file. After basic checks (non-empty, allowed type, no Office/XFA PDF), if `mnit-clammit.enabled` is true it calls the ClamMIT scan endpoint (`mnit-clammit.url`, e.g. `.../clammit/scan`) with the file bytes.  
3. If the response indicates **virus detected**, the controller returns `422` and the message `upload-documents.virus-detected` (“Your file cannot be uploaded because a virus was detected. Try uploading a different copy.”).  
4. If the scan passes or ClamMIT is disabled, the file is stored (e.g. in Azure Blob) and can later be sent to FileNet with the rest of the application.

**Config**  
- `mnit-clammit.url`, `mnit-clammit.enabled`. Errors (e.g. connection failure) surface as `upload-documents.clammit-server-error` so the user can retry later.

---

### comm-hub (SMS and email delivery)

**Why we need it**  
MNbenefits must send **SMS** (e.g. confirmation that an application was received) and **email** (e.g. confirmation with PDFs, or reminders to upload documents). The state uses a central **comm-hub** service to send these messages so that delivery is consistent, auditable, and compliant. The app does not send email/SMS directly; it calls comm-hub with the payload, and comm-hub performs the actual delivery.

**What it does**  
- **Text (SMS):** Accepts a JSON payload (application id, phone, opt-in status, county, etc.) and sends the appropriate text to the applicant (e.g. “Your application was received.”).  
- **Email:** When `comm-hub-email.delivery` is set to `commhub`, the app sends email payloads (e.g. confirmation text, document-upload reminders) to comm-hub’s email endpoint; comm-hub sends the actual emails.

**Example in the app — SMS**  
1. Applicant submits an application and has opted into SMS.  
2. `ApplicationSubmittedListener` builds a `JsonObject` with `applicationId`, `email`, `opt-status-sms`, `county`, `applicationPDF` (base64), etc.  
3. It calls `communicationClient.send(appJsonObject)`, which does `RestTemplate.postForEntity(commHubUrl, entity, String.class)` to `comm-hub-text.url` (e.g. `.../mnb-confirmation`).  
4. Comm-hub sends the SMS to the applicant’s phone.

**Example in the app — email**  
1. Applicant submits **uploaded documents**; the app sends a confirmation.  
2. `UploadedDocumentsSubmittedListener.sendConfirmationEmail(...)` builds email data and calls `communicationClient.sendEmailDataToCommhub(emailData)` when comm-hub email is enabled.  
3. That POSTs to `comm-hub-email.url` (e.g. `.../mnb-email-controller`). Comm-hub then sends the confirmation email.  
4. Similarly, `DocumentUploadEmailService` can send reminder emails via `commHubEmailSendingClient.sendEmailDataToCommhub(emailJson)` when `comm-hub-email.delivery` is `commhub` (otherwise it may use Mailgun via `emailClient.sendEmail(...)`).

**Config**  
- **Text:** `comm-hub-text.url`, `comm-hub-text.enabled`, plus retry/backoff (`max-attempts`, `delay`, `multiplier`, `max-delay`).  
- **Email:** `comm-hub-email.url`, `comm-hub-email.enabled`, `comm-hub-email.delivery` (`commhub` vs e.g. `mnbenefits` for Mailgun). When delivery is `commhub`, confirmation and reminder emails go through comm-hub.

---

### Summary

| System     | Purpose | When it’s used | Example |
|-----------|---------|----------------|--------|
| **FileNet** | Deliver application PDFs/XML and uploads to the state so caseworkers can process them. | After application submit; after uploaded-docs submit (for uploads). | User submits SNAP application → app generates CAF + XML → `FilenetWebServiceClient` sends them via SOAP to FileNet → caseworker sees application in state system. |
| **ClamMIT** | Virus-scan user uploads before we store or forward them. | On every `POST /document-upload` (when enabled). | User uploads a PDF → app POSTs bytes to ClamMIT → if virus, return “virus detected” and reject; if clean, save and allow later delivery to FileNet. |
| **comm-hub** | Send SMS and (when configured) email through the state’s central communication service. | After application submit (SMS/email confirmation); after uploaded-docs submit (email); scheduled reminders. | User submits and opts into SMS → app POSTs JSON to comm-hub text URL → comm-hub sends “Application received” SMS; same for email via comm-hub email URL. |

---

## 6. SOAP-based web components (Spring Boot 4)

The app uses **Spring Web Services** as a **SOAP client** only (no SOAP server). For how SOAP differs from REST and where each is used in this app, see **§5a**.

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

---

## 7. REST-based web services (Spring Boot 4)

The app exposes **REST API endpoints** and uses **REST clients** to call external services (see **§5a** for SOAP vs REST and where each is used). Under Spring Boot 4 these use `spring-boot-restclient` for `RestTemplate`-based clients and standard Spring MVC/WebFlux for controllers.

### REST API endpoints (in-app)

| Component | Endpoint(s) | Purpose |
|-----------|-------------|---------|
| **ApplicationDataApiController** | `GET /api/application-data` | Returns current session application data as JSON (PII; restrict in production). |
| **ResendFailedEmailController** | `GET /resend-confirmation-email/{applicationId}` | Admin-only: resend confirmation email (OAuth2 + allowlisted email). |
| **FileDownloadController** | `GET /download`, `GET /download/{applicationId}`, `GET /download-xml` | Session and admin downloads (see §2). |
| **PageController** | `GET /`, `/pages/*`, `POST /submit`, `/document-upload`, etc. | Main app flow (HTML/form submission). |

### REST clients (outbound)

| Component | Technology | Purpose |
|-----------|------------|--------|
| **CommunicationClient** | `RestTemplate` (from `RestTemplateBuilder`) | Calls comm-hub text/email APIs (`comm-hub-text.url`, `comm-hub-email.url`). Uses `spring-boot-restclient` via `CommHubRestServiceTemplateConfiguration`. |
| **RestTemplateConfiguration** | `RestTemplate` (custom SSL/truststore) | Generic REST client with custom SSL; used where truststore is required (e.g. some MN IT endpoints). |
| **MailGunEmailClient** | `WebClient` (WebFlux) | Sends email via Mailgun HTTP API. |
| **SmartyStreetClient** | `WebClient` (WebFlux) | Address validation via Smarty Streets API. |

### How to verify REST under Spring Boot 4

**1. Run REST client tests (comm-hub)**

`CommunicationClient` is the main RestTemplate-based REST client. Its tests use `MockRestServiceServer` and load the full context (including `RestTemplateBuilder` from `spring-boot-restclient`). This confirms the Boot 4 REST client stack works.

```bash
# Comm-hub REST client: RestTemplate, retries, timeouts (uses spring-boot-restclient)
./gradlew test --tests "org.codeforamerica.shiba.mnit.CommHubServiceTest"
```

**What `CommHubServiceTest` verifies:**

- Successful POST to comm-hub → single request, no retry.
- Server error (e.g. 5xx) or connection failure → retries up to `comm-hub-text.max-attempts`, then failure.
- Correct URL, method, and request handling via `MockRestServiceServer.bindTo(communicationClient.getCommHubRestServiceTemplate())`.

**2. Run tests that use REST clients (mocked)**

These tests wire or mock the REST clients and confirm the app starts and flows work with Boot 4:

```bash
# Document upload / email flows (CommunicationClient mocked or used)
./gradlew test --tests "org.codeforamerica.shiba.output.DocumentUploadEmailServiceTest"
./gradlew test --tests "org.codeforamerica.shiba.output.DocumentUploadEmailServiceUnitTest"
./gradlew test --tests "org.codeforamerica.shiba.pages.events.ApplicationSubmittedListenerTest"
./gradlew test --tests "org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedListenerTest"
```

**3. Run REST controller / security tests**

- **ResendFailedEmailController** (admin REST endpoint): covered by `ResendFailedEmailControllerTest` and security tests.
- **FileDownloadController**: covered by `FileDownloadControllerTest` and `SecurityConfigurationTest` (see §1–2).

```bash
./gradlew test --tests "org.codeforamerica.shiba.output.ResendFailedEmailControllerTest"
./gradlew test --tests "org.codeforamerica.shiba.SecurityConfigurationTest"
./gradlew test --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest"
```

**4. One-shot REST verification**

```bash
./gradlew test --tests "org.codeforamerica.shiba.mnit.CommHubServiceTest" \
              --tests "org.codeforamerica.shiba.output.ResendFailedEmailControllerTest" \
              --tests "org.codeforamerica.shiba.output.FileDownloadControllerTest" \
              --tests "org.codeforamerica.shiba.output.DocumentUploadEmailServiceUnitTest"
```

If these pass, RestTemplate (spring-boot-restclient), REST controllers, and integration with security are verified under Spring Boot 4.

**5. Optional: manual check of REST API**

- **GET /api/application-data**  
  There is no dedicated unit test. To verify: start the app, complete (or simulate) a session so `ApplicationData` has data, then in the same browser session open `GET /api/application-data`. Expect JSON with `id`, `pagesData`, `subworkflows`, etc. Ensure this endpoint is restricted in production (e.g. dev-only or behind auth).

### Spring Boot 4 REST notes

- **spring-boot-restclient** (`org.springframework.boot:spring-boot-restclient:4.0.1`) provides `RestTemplateBuilder` and `RestTemplateBuilderConfigurer` in package `org.springframework.boot.restclient` (replacing the old `org.springframework.boot.web.client` usage). `CommHubRestServiceTemplateConfiguration` and `CommunicationClient` use these; no further API changes are required.
- **RestTemplateConfiguration** creates a plain `RestTemplate` with custom SSL (HttpClient, truststore). It does not use `RestTemplateBuilder`; it works unchanged under Boot 4.
- **WebClient** (MailGun, SmartyStreets) comes from `spring-boot-starter-webflux`; unchanged in Boot 4.
- If **CommHubServiceTest** fails with "bean not found" for `RestTemplateBuilder` or `commHubRestServiceTemplate`, ensure the test profile has `comm-hub-text.*` and `comm-hub-email.*` properties (e.g. in `application-test.yaml`) so the beans can be created.
