// Bakes META-INF/build-info.properties into the jar (build.time, build.version, etc.) — read by Spring's BuildProperties.
// Git short SHA is written to META-INF/git.properties by the gradle-git-properties plugin above — read by Spring's GitProperties.
springBoot {
    buildInfo()
}
