package org.codeforamerica.shiba.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shiba")
public class ShibaProperties {

  /**
   * Application build / release label. Baked in at build time from {@code GITHUB_REF_NAME} when
   * set (e.g. GitHub release tag); may be overridden at runtime with {@code SHIBA_BUILD_VERSION}.
   */
  private String buildVersion = "local";
}
