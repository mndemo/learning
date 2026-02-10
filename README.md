package org.codeforamerica.shiba.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current session's application data as JSON in the same structure
 * as stored in the database ({@code application_data} column, jsonb).
 *
 * <p>Uses the same serialization as {@link org.codeforamerica.shiba.application.ApplicationDataEncryptor}
 * (without encrypting SSN). Response shape matches what is recorded in the DB.
 *
 * <p>Usage: {@code GET /api/application-data} in the same browser session.
 *
 * <p>Security: returns PII. Restrict in production (e.g. dev profile only).
 * import com.fasterxml.jackson.annotation.JsonIgnore;
 *   @JsonIgnore

 */
@RestController
@RequestMapping("/api")
public class ApplicationDataApiController {

  private final ApplicationData applicationData;
  private final ObjectMapper objectMapper;

  public ApplicationDataApiController(
      ApplicationData applicationData,
      ObjectMapper objectMapper) {
    this.applicationData = applicationData;
    this.objectMapper = objectMapper;
  }

  @GetMapping(value = "/application-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getApplicationData() {
    try {
      String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(applicationData);
      return ResponseEntity.ok(json);
    } catch (JsonProcessingException e) {
      return ResponseEntity.internalServerError()
          .body("{\"error\": \"Failed to serialize application data: " + e.getMessage() + "\"}");
    }
  }
}
