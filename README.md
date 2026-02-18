package org.codeforamerica.shiba.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Provides a Jackson 2 ObjectMapper bean for components that use {@code com.fasterxml.jackson}.
 * Spring Boot 4 defaults to Jackson 3 ({@code tools.jackson}); this bean supports existing code
 * (ApplicationDataApiController, SessionLogFilter, DB migrations, etc.) until migrated.
 */
@Configuration
public class JacksonConfiguration {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
