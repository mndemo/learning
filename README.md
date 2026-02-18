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
./gradlew test --tests "org.codeforamerica.shiba.journeys.JourneyTest"
```

**What is verified:**

- **FileDownloadControllerTest**: Controller behavior (status, headers, zip content, XML, “not found” message).
- **SecurityConfigurationTest**: `/download/{applicationId}` authn/authz and response headers.
- **PdfMockMvcTest / JourneyTest**: Full stack including security and session when calling `/download`.

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
