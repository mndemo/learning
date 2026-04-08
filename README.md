package org.codeforamerica.shiba.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Values shared with every Thymeleaf page. The footer uses {@code shibaBuildVersion}; that string
 * comes from {@code shiba.build-version} in {@code application.yaml}, which resolves
 * {@code SHIBA_BUILD_VERSION} at runtime (see {@code application.yaml}).
 */
@ControllerAdvice
public class ShibaGlobalModelAttributes {

  @Value("${shiba.build-version:0.0.1-SNAPSHOT}")
  private String buildVersion;

  @ModelAttribute("shibaBuildVersion")
  public String shibaBuildVersion() {
    return buildVersion;
  }
}
