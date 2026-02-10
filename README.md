package org.codeforamerica.shiba.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current session's application data as JSON in the same structure
 * as stored in the database ({@code application_data} column, jsonb).
 *
 * <p>Response shape matches what is recorded in the DB: pagesData (page -&gt; input -&gt; {"value": [...]}),
 * subworkflows (group -&gt; [{"id", "pagesData"}]), etc.
 *
 * <p>Usage: {@code GET /api/application-data} in the same browser session.
 *
 * <p>Security: returns PII. Restrict in production (e.g. dev profile only).
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
      Map<String, Object> view = new LinkedHashMap<>();
      view.put("id", applicationData.getId());
      view.put("clientIP", applicationData.getClientIP());
      view.put("startTime", applicationData.getStartTime() != null ? applicationData.getStartTime().toString() : null);
      view.put("utmSource", applicationData.getUtmSource());
      view.put("lastPageViewed", applicationData.getLastPageViewed());
      view.put("deviceType", applicationData.getDeviceType());
      view.put("devicePlatform", applicationData.getDevicePlatform());
      view.put("expeditedEligibility", applicationData.getExpeditedEligibility() != null
          ? applicationData.getExpeditedEligibility().stream().map(Enum::name).toList()
          : List.<String>of());
      view.put("flow", applicationData.getFlow() != null ? applicationData.getFlow().name() : null);
      view.put("isSubmitted", applicationData.isSubmitted());
      view.put("pagesData", pagesDataToDbShape(applicationData.getPagesData()));
      view.put("subworkflows", subworkflowsToDbShape(applicationData.getSubworkflows()));
      Map<String, PagesData> incomplete = applicationData.getIncompleteIterations();
      view.put("incompleteIterations", incomplete != null
          ? incomplete.entrySet().stream()
              .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), pagesDataToDbShape(e.getValue())), Map::putAll)
          : Map.of());
      view.put("uploadedDocs", applicationData.getUploadedDocs() != null
          ? applicationData.getUploadedDocs().stream().map(ApplicationDataApiController::uploadedDocToMap).toList()
          : List.of());
      view.put("originalCounty", applicationData.getOriginalCounty());

      String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(view);
      return ResponseEntity.ok(json);
    } catch (JsonProcessingException e) {
      String msg = e.getMessage();
      if (e.getCause() != null) {
        msg = msg + " | cause: " + e.getCause().getMessage();
      }
      return ResponseEntity.internalServerError()
          .body("{\"error\": \"Failed to serialize application data: " + escapeJson(msg) + "\"}");
    } catch (Exception e) {
      String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
      if (e.getCause() != null && e.getCause().getMessage() != null) {
        msg = msg + " | cause: " + e.getCause().getMessage();
      }
      return ResponseEntity.internalServerError()
          .body("{\"error\": \"Failed to serialize application data: " + escapeJson(msg) + "\"}");
    }
  }

  private static String escapeJson(String s) {
    return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
  }

  private static Map<String, Object> uploadedDocToMap(UploadedDocument doc) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("filename", doc.getFilename());
    m.put("s3Filepath", doc.getS3Filepath());
    m.put("thumbnailFilepath", doc.getThumbnailFilepath());
    m.put("type", doc.getType());
    m.put("size", doc.getSize());
    m.put("sysFileName", doc.getSysFileName());
    return m;
  }

  /** DB shape: page name -&gt; input name -&gt; {"value": ["..."]}. */
  private static Map<String, Object> pagesDataToDbShape(PagesData pagesData) {
    if (pagesData == null) return Map.of();
    Map<String, Object> out = new LinkedHashMap<>();
    pagesData.forEach((pageName, pageData) -> out.put(pageName, pageDataToDbShape(pageData)));
    return out;
  }

  /** DB shape: input name -&gt; {"value": ["..."]} (same as InputData in DB). */
  private static Map<String, Object> pageDataToDbShape(PageData pageData) {
    if (pageData == null) return Map.of();
    Map<String, Object> out = new LinkedHashMap<>();
    pageData.forEach((inputName, inputData) -> {
      List<String> value = inputData.getValue() != null ? new ArrayList<>(inputData.getValue()) : new ArrayList<>();
      out.put(inputName, Map.<String, Object>of("value", value));
    });
    return out;
  }

  /** DB shape: group name -&gt; [{"id": "...", "pagesData": {...}}]. */
  private static Map<String, Object> subworkflowsToDbShape(Subworkflows subworkflows) {
    if (subworkflows == null) return Map.of();
    Map<String, Object> out = new LinkedHashMap<>();
    subworkflows.forEach((groupName, subworkflow) -> out.put(groupName, subworkflowToDbShape(subworkflow)));
    return out;
  }

  private static List<Map<String, Object>> subworkflowToDbShape(Subworkflow subworkflow) {
    if (subworkflow == null) return List.of();
    List<Map<String, Object>> list = new ArrayList<>();
    for (Iteration iter : subworkflow) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", iter.getId() != null ? iter.getId().toString() : null);
      m.put("pagesData", pagesDataToDbShape(iter.getPagesData()));
      list.add(m);
    }
    return list;
  }
}
