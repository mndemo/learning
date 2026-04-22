// Ensure both metadata files are on the classpath for every run mode (bootRun, IDE, tests, JAR, Docker),
// otherwise the footer version is empty because BuildProperties/GitProperties beans aren't created.
tasks.named('processResources') {
    dependsOn 'bootBuildInfo', 'generateGitProperties'
}
