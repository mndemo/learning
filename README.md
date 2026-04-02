
// Baked-in label: GitHub Actions sets GITHUB_REF_NAME to the release tag (e.g. v1.2.0) on tag builds.
def resolvedShibaBuildVersion = System.getenv('GITHUB_REF_NAME') ?: project.version

processResources {
    filesMatching('**/application.yaml') {
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: [
                'shiba.build.version': resolvedShibaBuildVersion.toString()
        ])
    }
}
