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
 * Exposes the current session's application data as JSON so you can inspect the
 * data structure as the user moves through the app.
 *
 * <p>Usage: while the app is running, call {@code GET /api/application-data} in the
 * same browser session (or with the same session cookie). The response is the
 * application data: id, pagesData (page name → inputs), subworkflows, uploadedDocs, etc.
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
          : null);
      view.put("flow", applicationData.getFlow() != null ? applicationData.getFlow().name() : null);
      view.put("isSubmitted", applicationData.isSubmitted());
      view.put("pagesData", pagesDataToMap(applicationData.getPagesData()));
      view.put("subworkflows", subworkflowsToMap(applicationData.getSubworkflows()));
      view.put("incompleteIterations", applicationData.getIncompleteIterations().entrySet().stream()
          .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), pagesDataToMap(e.getValue())), Map::putAll));
      view.put("uploadedDocs", applicationData.getUploadedDocs().stream()
          .map(ApplicationDataApiController::uploadedDocToMap).toList());
      view.put("originalCounty", applicationData.getOriginalCounty());

      String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(view);
      return ResponseEntity.ok(json);
    } catch (JsonProcessingException e) {
      return ResponseEntity.internalServerError()
          .body("{\"error\": \"Failed to serialize application data: " + e.getMessage() + "\"}");
    }
  }

  /** Convert PagesData to Map&lt;String, Map&lt;String, List&lt;String&gt;&gt;&gt; (page -> input -> values). */
  private static Map<String, Object> pagesDataToMap(PagesData pagesData) {
    if (pagesData == null) return Map.of();
    Map<String, Object> out = new LinkedHashMap<>();
    pagesData.forEach((pageName, pageData) -> out.put(pageName, pageDataToMap(pageData)));
    return out;
  }

  /** Convert PageData to Map&lt;String, List&lt;String&gt;&gt; (input name -> value list). */
  private static Map<String, List<String>> pageDataToMap(PageData pageData) {
    if (pageData == null) return Map.of();
    Map<String, List<String>> out = new LinkedHashMap<>();
    pageData.forEach((inputName, inputData) -> out.put(inputName, new ArrayList<>(inputData.getValue())));
    return out;
  }

  /** Convert Subworkflows to Map&lt;String, List&lt;Map&gt;&gt; (group -> iterations with id + pagesData). */
  private static Map<String, Object> subworkflowsToMap(Subworkflows subworkflows) {
    if (subworkflows == null) return Map.of();
    Map<String, Object> out = new LinkedHashMap<>();
    subworkflows.forEach((groupName, subworkflow) -> out.put(groupName, subworkflowToList(subworkflow)));
    return out;
  }

  private static List<Map<String, Object>> subworkflowToList(Subworkflow subworkflow) {
    if (subworkflow == null) return List.of();
    List<Map<String, Object>> list = new ArrayList<>();
    for (Iteration iter : subworkflow) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", iter.getId() != null ? iter.getId().toString() : null);
      m.put("pagesData", pagesDataToMap(iter.getPagesData()));
      list.add(m);
    }
    return list;
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
}
