package org.codeforamerica.shiba.config;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides the {@code shibaBuildVersion} string rendered by the site footer
 * ({@code templates/fragments/footer.html} and {@code footerHealthcareRenewal.html}).
 *
 * <p><b>Why this class exists:</b> we want every deployed environment (dev, ATST, prod) to show the
 * <i>same</i> build identifier that was produced when the JAR was built — with zero manual steps at
 * deploy time. So the id is baked into the artifact during {@code ./gradlew assemble} and read back
 * here at request time.
 *
 * <p><b>Where the numbers come from:</b>
 * <ul>
 *   <li><b>Build date</b> → {@code META-INF/build-info.properties}, written by Spring Boot's
 *       {@code bootBuildInfo} Gradle task (configured in {@code build.gradle} via
 *       {@code springBoot { buildInfo() }}). Spring Boot auto-publishes a {@link BuildProperties}
 *       bean from this file at startup.</li>
 *   <li><b>Short git SHA</b> → {@code git.properties}, written by the
 *       {@code com.gorylenko.gradle-git-properties} plugin during the build. Spring Boot
 *       auto-publishes a {@link GitProperties} bean from this file at startup.</li>
 * </ul>
 *
 * <p>Rendered footer example: {@code "Version 2026-04-20 · a1b2c3d"}. Falls back gracefully if a
 * metadata file happens to be missing — e.g. a local {@code bootRun} launched without building
 * resources first.
 */
// @ControllerAdvice: makes the @ModelAttribute below visible to every Thymeleaf view in the app,
// so any template can use ${shibaBuildVersion} without each controller having to set it manually.
@ControllerAdvice
public class ShibaGlobalModelAttributes {

  // Formats the build instant as an ISO date string in UTC. Using UTC keeps the value consistent
  // regardless of the timezone of the machine that built or runs the app.
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

  // Provided by Spring Boot autoconfiguration when build-info.properties is on the classpath.
  // Exposes build.time, build.version, etc.
  private final BuildProperties buildProperties;

  // Provided by Spring Boot autoconfiguration when git.properties is on the classpath.
  // Exposes git.commit.id, git.commit.id.abbrev (short SHA), git.branch, etc.
  private final GitProperties gitProperties;

  // Both dependencies are optional (required = false) because either metadata file can be absent —
  // e.g. running tests before resources are processed, or building outside a git checkout. In that
  // case Spring just passes null and shibaBuildVersion() falls back to whatever info is available.
  public ShibaGlobalModelAttributes(
      @Autowired(required = false) BuildProperties buildProperties,
      @Autowired(required = false) GitProperties gitProperties) {
    this.buildProperties = buildProperties;
    this.gitProperties = gitProperties;
  }

  // The attribute name ("shibaBuildVersion") is the exact key Thymeleaf uses in the footer:
  //   th:text="#{generic.footer.build-version(${shibaBuildVersion})}"
  // The returned string replaces {0} in messages.properties -> "Version {0}".
  @ModelAttribute("shibaBuildVersion")
  public String shibaBuildVersion() {
    String date = buildDate();
    String sha = shortSha();

    // Preferred form: both pieces available -> "2026-04-20 · a1b2c3d".
    if (StringUtils.hasText(date) && StringUtils.hasText(sha)) {
      return date + " · " + sha;
    }
    // No SHA (e.g. built without .git) -> just the date.
    if (StringUtils.hasText(date)) {
      return date;
    }
    // No build-info (e.g. ran app without bootBuildInfo) -> just the SHA.
    if (StringUtils.hasText(sha)) {
      return sha;
    }
    // Neither file reached the classpath. Returning empty hides the footer line via th:if,
    // which is preferable to displaying something misleading like "null".
    return "";
  }

  private String buildDate() {
    // buildProperties is null when META-INF/build-info.properties isn't on the classpath.
    // buildProperties.getTime() can be null if the file exists but has no build.time entry.
    if (buildProperties == null || buildProperties.getTime() == null) {
      return "";
    }
    // getTime() returns an Instant; DATE_FORMAT is UTC-zoned so output is deterministic.
    return DATE_FORMAT.format(buildProperties.getTime());
  }

  private String shortSha() {
    // gitProperties is null when git.properties isn't on the classpath (no git during build,
    // or gorylenko plugin task didn't run before app launch).
    if (gitProperties == null) {
      return "";
    }
    // Short SHA (7 chars by default), written by the gorylenko plugin from git.commit.id.abbrev.
    String sha = gitProperties.getShortCommitId();
    return sha != null ? sha.trim() : "";
  }
}
