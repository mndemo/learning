processResources {
    filesMatching('**/application.yaml') {
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: [
                'shiba.build.version': resolvedShibaBuildVersion.toString()
        ])
    }
}
