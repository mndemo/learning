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
 * Exposes the current session's application data as JSON so you can inspect the
 * data structure as the user moves through the app.
 *
 * <p>Usage: while the app is running, call {@code GET /api/application-data} in the
 * same browser session (or with the same session cookie). The response is the
 * full {@link ApplicationData} object: id, pagesData (page name → inputs), subworkflows,
 * uploadedDocs, etc.
 *
 * <p>Security: this endpoint returns PII (e.g. names, addresses). Restrict it in
 * production (e.g. enable only when {@code spring.profiles.active=dev} or behind
 * an admin check).
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
      return ResponseEntity.internalServerError().body("{\"error\": \"Failed to serialize application data\"}");
    }
  }
}



curl -b cookies.txt http://localhost:8080/api/application-data
