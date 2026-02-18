1. Root cause: missing ObjectMapper bean (Jackson 2)
Spring Boot 4 uses Jackson 3 by default (tools.jackson), so it no longer creates a bean of type com.fasterxml.jackson.databind.ObjectMapper. Your code (e.g. ApplicationDataApiController, SessionLogFilter, DB migrations) still injects and uses Jackson 2 (com.fasterxml.jackson), which led to:
NoSuchBeanDefinitionException: No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper' available
Changes made
1. build.gradle
Re-enabled the Jackson 2 dependency:
implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
Added a short comment that this is for compatibility with existing Jackson 2 usage under Spring Boot 4.
2. New JacksonConfiguration.java
Added src/main/java/org/codeforamerica/shiba/configurations/JacksonConfiguration.java.
Defines a @Bean of type com.fasterxml.jackson.databind.ObjectMapper (Jackson 2).
Registers JavaTimeModule and disables WRITE_DATES_AS_TIMESTAMPS.
Marked with @Primary so any unqualified ObjectMapper injection (e.g. in ApplicationDataApiController) uses this bean.
With this in place, the failing test EmailContentCreatorTest.shouldCreateShortConfirmationEmail passes and the application context starts.
2. What you should do next
Run the full test suite locally
The single test we ran passes; run everything to catch any remaining issues:
   ./gradlew test
If other tests fail
They may be due to:
Other missing or changed beans in Boot 4
Deprecations or API changes (you already have some deprecation/unchecked notes in PageControllerTest and SpecialCareExpensesPreparerTest)
Test-only configuration (e.g. @MockBean / @MockitoBean, or needing spring-boot-starter-security-test for @WithMockUser)
Optional: long-term migration
When you’re ready, you can migrate from Jackson 2 to Jackson 3 (tools.jackson) and then remove this custom ObjectMapper bean and the explicit jackson-databind dependency.
If you paste the next failing test name and stack trace (or the relevant part of test output), we can fix those errors next.
