# Build label: Gradle replaces @shiba.build.version@ (prefers GITHUB_REF_NAME, e.g. release tag).
# Override at runtime with SHIBA_BUILD_VERSION to match a GitHub release identifier when needed.
shiba:
  build-version: ${SHIBA_BUILD_VERSION:@shiba.build.version@}
