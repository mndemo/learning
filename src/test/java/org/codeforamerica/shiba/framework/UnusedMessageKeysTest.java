package org.codeforamerica.shiba.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test to identify unused message keys in messages.properties.
 * 
 * This test:
 * 1. Reads all message keys from messages.properties
 * 2. Searches for usage of each key in Java files, Thymeleaf templates, and YAML config files
 * 3. Reports unused keys
 * 4. Fails the test if unused keys are found
 */
@Tag("framework")
public class UnusedMessageKeysTest {

	private static final String MESSAGES_PROPERTIES_PATH = "messages.properties";
	private static final Path SOURCE_ROOT = Paths.get("src");
	private static final Path MAIN_RESOURCES = SOURCE_ROOT.resolve("main/resources");
	private static final Path TEST_RESOURCES = SOURCE_ROOT.resolve("test/resources");
	
	// Patterns to search for message key usage
	private static final Pattern THYMELEAF_PATTERN = Pattern.compile("#\\{([^}]+)\\}"); // #{key.name}
	private static final Pattern JAVA_STRING_PATTERN = Pattern.compile("[\"']([^\"']+)[\"']"); // "key.name" or 'key.name'
	private static final Pattern YAML_KEY_PATTERN = Pattern.compile(
		"(pageTitle|headerKey|messageKey|errorMessageKey|helpMessageKey|promptMessageKey|headerHelpMessageKey|buttonText|linkText|primaryButtonTextKey|subtleLinkTextKey|cardFooterTextKey|contextFragment|fragmentName|postButtonContentFragment)\\s*:\\s*([^\\s#\\n]+)"
	);
	
	// Keys that are dynamically generated or conditionally used - exclude from unused check
	private static final Set<String> EXCLUDED_KEYS = Set.of(
		"general.validation.blank", // Empty validation message, used conditionally
		"general.validation.invalid.select.option.value" // Used for invalid option values
	);

	@Test
	void shouldNotHaveUnusedMessageKeys() throws IOException {
		// Read all message keys from messages.properties
		Set<String> messageKeys = readMessageKeys();
		
		if (messageKeys.isEmpty()) {
			fail("No message keys found in messages.properties");
		}
		
		// Find all used keys in the codebase
		Set<String> usedKeys = findUsedKeys();
		
		// Find unused keys
		Set<String> unusedKeys = new HashSet<>(messageKeys);
		unusedKeys.removeAll(usedKeys);
		unusedKeys.removeAll(EXCLUDED_KEYS);
		
		// Sort unused keys for better reporting
		List<String> sortedUnusedKeys = new ArrayList<>(unusedKeys);
		Collections.sort(sortedUnusedKeys);
		
		if (!sortedUnusedKeys.isEmpty()) {
			StringBuilder report = new StringBuilder();
			report.append("\n═══════════════════════════════════════════════════════════════\n");
			report.append("UNUSED MESSAGE KEYS FOUND IN messages.properties\n");
			report.append("═══════════════════════════════════════════════════════════════\n\n");
			report.append("Statistics:\n");
			report.append("  - Total message keys: ").append(messageKeys.size()).append("\n");
			report.append("  - Keys found in codebase: ").append(usedKeys.size()).append("\n");
			report.append("  - Excluded keys: ").append(EXCLUDED_KEYS.size()).append("\n");
			report.append("  - Unused keys: ").append(sortedUnusedKeys.size()).append("\n\n");
			report.append("Unused keys (first 50):\n");
			report.append("───────────────────────────────────────────────────────────────\n");
			
			int maxKeysToShow = Math.min(50, sortedUnusedKeys.size());
			for (int i = 0; i < maxKeysToShow; i++) {
				report.append("  - ").append(sortedUnusedKeys.get(i)).append("\n");
			}
			
			if (sortedUnusedKeys.size() > 50) {
				report.append("  ... and ").append(sortedUnusedKeys.size() - 50).append(" more\n");
				report.append("\n  Writing all unused keys to: build/unused-message-keys.txt\n");
				
				// Write all unused keys to a file
				try {
					Path outputFile = Paths.get("build/unused-message-keys.txt");
					Files.createDirectories(outputFile.getParent());
					Files.write(outputFile, sortedUnusedKeys);
					report.append("  Full list of ").append(sortedUnusedKeys.size()).append(" unused keys written to file.\n");
				} catch (IOException e) {
					report.append("  Failed to write unused keys to file: ").append(e.getMessage()).append("\n");
				}
			}
			
			report.append("\n───────────────────────────────────────────────────────────────\n");
			report.append("To fix this issue:\n");
			report.append("  1. Review each unused key to confirm it's not needed\n");
			report.append("  2. Remove unused keys from messages.properties\n");
			report.append("  3. If a key is used conditionally or dynamically, add it to EXCLUDED_KEYS\n");
			report.append("  4. If the test is missing usage patterns, improve the search logic\n");
			report.append("\nNote: Some keys may appear unused if they are:\n");
			report.append("  - Referenced through variable expressions in Thymeleaf\n");
			report.append("  - Used in conditional YAML configurations\n");
			report.append("  - Referenced through Java constants\n");
			report.append("═══════════════════════════════════════════════════════════════\n");
			
			System.err.println(report.toString());
			fail("Found " + sortedUnusedKeys.size() + " unused message key(s). See test output for details.");
		}
		
		// Test passes if no unused keys found
		assertThat(sortedUnusedKeys).isEmpty();
	}

	/**
	 * Reads all message keys from messages.properties
	 */
	private Set<String> readMessageKeys() throws IOException {
		Set<String> keys = new HashSet<>();
		
		try (InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(MESSAGES_PROPERTIES_PATH)) {
			if (inputStream == null) {
				throw new IOException("Could not find messages.properties in classpath");
			}
			
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					// Skip comments and empty lines
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					
					// Extract key (everything before the first '=')
					int equalsIndex = line.indexOf('=');
					if (equalsIndex > 0) {
						String key = line.substring(0, equalsIndex).trim();
						// Handle multi-line values (keys ending with backslash)
						if (!key.endsWith("\\")) {
							keys.add(key);
						}
					}
				}
			}
		}
		
		return keys;
	}

	/**
	 * Searches for used message keys in Java files, Thymeleaf templates, and YAML config files
	 */
	private Set<String> findUsedKeys() throws IOException {
		Set<String> usedKeys = new HashSet<>();
		
		// Search Java source files
		Path javaSourceDir = SOURCE_ROOT.resolve("main/java");
		if (Files.exists(javaSourceDir)) {
			usedKeys.addAll(searchJavaFiles(javaSourceDir));
		}
		
		Path javaTestDir = SOURCE_ROOT.resolve("test/java");
		if (Files.exists(javaTestDir)) {
			usedKeys.addAll(searchJavaFiles(javaTestDir));
		}
		
		// Search Thymeleaf templates
		Path templatesDir = MAIN_RESOURCES.resolve("templates");
		if (Files.exists(templatesDir)) {
			usedKeys.addAll(searchThymeleafTemplates(templatesDir));
		}
		
		// Search YAML config files
		Path pagesConfig = MAIN_RESOURCES.resolve("pages-config.yaml");
		if (Files.exists(pagesConfig)) {
			usedKeys.addAll(searchYamlFile(pagesConfig));
		}
		
		Path testPagesConfig = TEST_RESOURCES.resolve("pages-config");
		if (Files.exists(testPagesConfig)) {
			try (Stream<Path> paths = Files.walk(testPagesConfig)) {
				paths.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
					.forEach(p -> {
						try {
							usedKeys.addAll(searchYamlFile(p));
						} catch (IOException e) {
							// Ignore errors for individual files
						}
					});
			}
		}
		
		// Search properties files that might reference message keys
		usedKeys.addAll(searchPropertiesFiles());
		
		return usedKeys;
	}

	/**
	 * Searches Java files for message key usage
	 */
	private Set<String> findUsedKeysInJavaFiles(Path directory) throws IOException {
		Set<String> usedKeys = new HashSet<>();
		
		try (Stream<Path> paths = Files.walk(directory)) {
			paths.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".java"))
				.forEach(p -> {
					try {
						String content = Files.readString(p);
						
						// Search for string constants assigned to message keys
						// Pattern: private static final String CONSTANT = "key.name";
						Pattern constantPattern = Pattern.compile(
							"(private|public|protected)?\\s*(static)?\\s*(final)?\\s*String\\s+\\w+\\s*=\\s*[\"']([^\"']+)[\"']"
						);
						Matcher constantMatcher = constantPattern.matcher(content);
						while (constantMatcher.find()) {
							String key = constantMatcher.group(4);
							if (isValidMessageKey(key)) {
								usedKeys.add(key);
							}
						}
						
						// Search for string literals that might be message keys
						// Look for patterns like getMessage("key.name") or "key.name"
						Pattern messageKeyPattern = Pattern.compile(
							"(getMessage|addMessage|messageSource\\.getMessage)\\s*\\([^,)]*[\"']([^\"']+)[\"']"
						);
						Matcher messageKeyMatcher = messageKeyPattern.matcher(content);
						while (messageKeyMatcher.find()) {
							String key = messageKeyMatcher.group(2);
							if (isValidMessageKey(key)) {
								usedKeys.add(key);
							}
						}
						
						// Search for LocaleSpecificMessageSource usage
						// Pattern: lms.getMessage(KEY, ...) or lms.getMessage("key.name", ...)
						Pattern lmsPattern = Pattern.compile(
							"lms\\.getMessage\\s*\\([^,)]*[\"']([^\"']+)[\"']"
						);
						Matcher lmsMatcher = lmsPattern.matcher(content);
						while (lmsMatcher.find()) {
							String key = lmsMatcher.group(1);
							if (isValidMessageKey(key)) {
								usedKeys.add(key);
							}
						}
						
						// Search for static message source usage in tests
						Pattern staticMessagePattern = Pattern.compile(
							"staticMessageSource\\.addMessage\\s*\\([^,)]*[\"']([^\"']+)[\"']"
						);
						Matcher staticMessageMatcher = staticMessagePattern.matcher(content);
						while (staticMessageMatcher.find()) {
							String key = staticMessageMatcher.group(1);
							if (isValidMessageKey(key)) {
								usedKeys.add(key);
							}
						}
						
						// Search for getMessage() calls with variable names that might be constants
						// This is less precise but catches cases like getMessage(CLIENT_BODY)
						// We'll rely on the constant pattern above to find the actual key values
						
					} catch (IOException e) {
						// Ignore errors for individual files
					}
				});
		}
		
		return usedKeys;
	}

	/**
	 * Searches Thymeleaf templates for message key usage
	 */
	private Set<String> searchThymeleafTemplates(Path directory) throws IOException {
		Set<String> usedKeys = new HashSet<>();
		
		try (Stream<Path> paths = Files.walk(directory)) {
			paths.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".html"))
				.forEach(p -> {
					try {
						String content = Files.readString(p);
						Matcher matcher = THYMELEAF_PATTERN.matcher(content);
						while (matcher.find()) {
							String key = matcher.group(1);
							
							// Skip Thymeleaf utility expressions
							if (key.contains("#locale") || key.contains("#dates") || key.contains("#strings") 
								|| key.contains("#numbers") || key.contains("#lists") || key.contains("#sets")) {
								continue;
							}
							
							// Handle direct message key references like #{key.name}
							if (!key.contains("$") && !key.contains("(") && !key.contains(")")) {
								// Remove parameter placeholders like {0}, {1}
								key = key.replaceAll("\\{\\d+\\}", "").trim();
								if (isValidMessageKey(key)) {
									usedKeys.add(key);
								}
							}
							// Handle variable references like #{${input.promptMessage.promptMessageKey}}
							// These are resolved at runtime, so we can't detect them statically
							// But we can extract the key if it's in a simple format
							else if (key.contains("$") && key.matches(".*\\$\\{[^}]*\\}.*")) {
								// Try to extract message keys from variable expressions
								// Pattern: ${input.promptMessage.promptMessageKey}
								Pattern varPattern = Pattern.compile("\\$\\{([^}]+)\\}");
								Matcher varMatcher = varPattern.matcher(key);
								while (varMatcher.find()) {
									String varExpr = varMatcher.group(1);
									// If the variable ends with Key, it might contain a message key
									if (varExpr.contains("Key") || varExpr.contains("key")) {
										// We can't determine the actual key at compile time
										// This is handled by YAML config where these keys are defined
									}
								}
							}
						}
					} catch (IOException e) {
						// Ignore errors for individual files
					}
				});
		}
		
		return usedKeys;
	}

	/**
	 * Searches YAML config files for message key usage
	 */
	private Set<String> searchYamlFile(Path yamlFile) throws IOException {
		Set<String> usedKeys = new HashSet<>();
		String content = Files.readString(yamlFile);
		
		// Search for YAML key patterns (pageTitle, headerKey, messageKey, etc.)
		Matcher yamlMatcher = YAML_KEY_PATTERN.matcher(content);
		while (yamlMatcher.find()) {
			String key = yamlMatcher.group(2).trim();
			// Remove quotes if present
			key = key.replaceAll("^[\"']|[\"']$", "");
			// Skip YAML anchors and references (starting with * or &)
			if (key.startsWith("*") || key.startsWith("&")) {
				continue;
			}
			if (isValidMessageKey(key)) {
				usedKeys.add(key);
			}
		}
		
		// Search for conditional values in YAML (headerKey conditionalValues, etc.)
		// Pattern: conditionalValues:\n    - value: key.name
		// This is a multiline pattern that matches conditionalValues sections
		Pattern conditionalValuePattern = Pattern.compile(
			"conditionalValues:\\s*\\n(?:[^\\n]*\\n)*?\\s+-\\s+value:\\s*([a-zA-Z0-9._-]+)",
			Pattern.MULTILINE
		);
		Matcher conditionalMatcher = conditionalValuePattern.matcher(content);
		while (conditionalMatcher.find()) {
			String key = conditionalMatcher.group(1).trim();
			// Remove quotes if present
			key = key.replaceAll("^[\"']|[\"']$", "");
			// Skip YAML anchors
			if (key.startsWith("*") || key.startsWith("&")) {
				continue;
			}
			if (isValidMessageKey(key)) {
				usedKeys.add(key);
			}
		}
		
		// Also search for defaultValue in conditional configurations
		Pattern defaultValuePattern = Pattern.compile(
			"defaultValue:\\s*([a-zA-Z0-9._-]+)",
			Pattern.MULTILINE
		);
		Matcher defaultValueMatcher = defaultValuePattern.matcher(content);
		while (defaultValueMatcher.find()) {
			String key = defaultValueMatcher.group(1).trim();
			key = key.replaceAll("^[\"']|[\"']$", "");
			if (key.startsWith("*") || key.startsWith("&")) {
				continue;
			}
			if (isValidMessageKey(key)) {
				usedKeys.add(key);
			}
		}
		
		// Search for message keys in alertBox messages
		Pattern alertBoxPattern = Pattern.compile(
			"alertBox:\\s*\\n\\s*type:\\s*[^\\n]+\\n\\s*message:\\s*([a-zA-Z0-9._-]+)"
		);
		Matcher alertBoxMatcher = alertBoxPattern.matcher(content);
		while (alertBoxMatcher.find()) {
			String key = alertBoxMatcher.group(1).trim();
			if (isValidMessageKey(key)) {
				usedKeys.add(key);
			}
		}
		
		return usedKeys;
	}

	/**
	 * Searches Java files for message key usage (alternative approach)
	 */
	private Set<String> searchJavaFiles(Path directory) throws IOException {
		return findUsedKeysInJavaFiles(directory);
	}

	/**
	 * Searches properties files for message key references
	 */
	private Set<String> searchPropertiesFiles() throws IOException {
		Set<String> usedKeys = new HashSet<>();
		
		// Search application.properties and application-*.yaml files
		try (Stream<Path> paths = Files.walk(MAIN_RESOURCES)) {
			paths.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().startsWith("application") 
					&& (p.toString().endsWith(".properties") || p.toString().endsWith(".yaml") || p.toString().endsWith(".yml")))
				.forEach(p -> {
					try {
						String content = Files.readString(p);
						// Properties files might reference message keys in comments or values
						// This is less common, but we'll check anyway
						Matcher stringMatcher = JAVA_STRING_PATTERN.matcher(content);
						while (stringMatcher.find()) {
							String potentialKey = stringMatcher.group(1);
							if (isValidMessageKey(potentialKey) && potentialKey.contains(".")) {
								usedKeys.add(potentialKey);
							}
						}
					} catch (IOException e) {
						// Ignore errors
					}
				});
		}
		
		return usedKeys;
	}

	/**
	 * Checks if a string looks like a valid message key
	 * Message keys typically contain dots and lowercase letters/numbers/hyphens
	 */
	private boolean isValidMessageKey(String key) {
		if (key == null || key.isEmpty()) {
			return false;
		}
		
		// Remove common prefixes/suffixes that aren't part of the key
		key = key.trim();
		
		// Skip if it's clearly not a message key
		if (key.startsWith("$") || key.startsWith("#") || key.contains("(") || key.contains(")")) {
			return false;
		}
		
		// Message keys typically have at least one dot and contain alphanumeric characters, dots, and hyphens
		if (!key.contains(".")) {
			return false;
		}
		
		// Check if it matches the pattern of message keys (e.g., "general.validation.make-sure")
		return key.matches("^[a-zA-Z0-9._-]+$") && key.length() > 3;
	}
}

