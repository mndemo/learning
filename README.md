It wasn’t copied from a single “JacksonConfiguration” tutorial. It came from a few standard places:
Spring’s own message
After moving to Spring Boot 4, the app failed to start with something like:
“No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper'”
and often:
“Consider defining a bean of type 'com.fasterxml.jackson.databind.ObjectMapper'.”
So the idea was literally: define that bean.
Usual Spring way to fix “missing bean”
When something needs an ObjectMapper and the context doesn’t have one, the normal fix is to add a @Configuration class with a @Bean method that creates and returns an ObjectMapper. No special name is required; “JacksonConfiguration” is just a clear class name.
@Primary
Boot 4 might still register another ObjectMapper (e.g. from Jackson 3). So we declare our bean as @Primary so that this one is chosen when a single ObjectMapper is injected.
JavaTimeModule and WRITE_DATES_AS_TIMESTAMPS
Those settings are standard Jackson setup for Java 8 date/time types (e.g. Instant) so they serialize as ISO-8601 strings. That’s in normal Jackson and Spring docs (e.g. “Customize the ObjectMapper”, “Jackson support”).
So the “JacksonConfiguration” solution is: follow Spring’s “define a bean” hint + standard @Configuration/@Bean + common ObjectMapper tweaks. You’ll see the same pattern in Spring docs about customizing/custom ObjectMapper and in many posts about “No qualifying bean ObjectMapper” or “Spring Boot 4 Jackson 2”, but the exact file name and contents were derived from the error and standard patterns, not from one specific “JacksonConfiguration” page.
