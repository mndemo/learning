2. How this works in production — no commands, nothing to set
Nothing runs in production. The identifier is baked into the deployed artifact when the CI/build system produces it. At runtime the app just reads two files that are already inside the JAR.
Walk the lifecycle for a production deploy:
A developer pushes code and cuts a release via workflow_dispatch → Deploy to MNbenefits ATST (or the equivalent promotion step).
GitHub Actions logs into OpenShift and runs the OpenShift BuildConfig (oc start-build mn-benefits-release).
The BuildConfig pod:
git clones your repo at the release tag — so .git is present inside the build pod.
Runs the Dockerfile:
./gradlew assemble triggers two automatic Gradle tasks:
bootBuildInfo → writes META-INF/build-info.properties with build.time=2026-04-20T14:03:11Z, build.version=0.0.1-SNAPSHOT, etc.
generateGitProperties → writes git.properties with git.commit.id.abbrev=a1b2c3d, git.commit.id=a1b2c3d…, etc.
Both files land in build/resources/main/ and get sealed into the fat JAR.
OpenShift tags the image and promotes it: in prod, import-image copies it from non-prod to prod, then patch deployment + rollout restarts pods with the new image.
When a pod starts, Spring Boot sees both files on the classpath and auto-creates the BuildProperties and GitProperties beans — with the values that were frozen at build time.
Every HTTP response rendered by that pod runs ShibaGlobalModelAttributes#shibaBuildVersion(), which reads those beans and returns the string. Footer shows Version 2026-04-20 · a1b2c3d.
Key properties of this setup:
No env var, no oc set env, no secret, no deployment-time parameter. That’s why the SHIBA_BUILD_VERSION lines were deleted from both workflow files.
Same JAR = same label in every environment. If the image gets promoted from ATST to prod, both will display the same build id (because the label comes from the JAR, not the pod).
Two pods of the same image always agree — no risk of one replica showing 0.0.1-SNAPSHOT because someone forgot to set an env var.
3. What a developer needs to do after pulling this change
Short answer: nothing special. ./gradlew assemble, ./gradlew bootRun, and IDE runs all work. But two small things to be aware of:
a) First run after pulling
That’s it. The tasks.named('processResources') { dependsOn 'bootBuildInfo', 'generateGitProperties' } block I added guarantees the metadata files are generated every time, so bootRun, IDE, and tests all populate the footer correctly without any extra command.
If a dev was already running the app from a previous clean, they should stop and restart — the running JVM is still using the old classpath.
b) IntelliJ / IDE run configurations
If a developer runs the main class directly in IntelliJ (not via Gradle), IntelliJ compiles sources but doesn’t always run processResources automatically. Two ways to handle it:
Build once with ./gradlew processResources after pulling — the files stay put.
Or, in IntelliJ: Settings → Build, Execution, Deployment → Build Tools → Gradle → Build and run using: Gradle. That makes IntelliJ delegate to Gradle, which runs processResources and hence bootBuildInfo/generateGitProperties.
Most teams already use the Gradle-delegated setting. If a dev still sees an empty footer in the IDE after these changes, it’s almost always this setting.
c) What they'll see locally
Version 2026-04-20 · a1b2c3d — date is the time they built, SHA is their current commit. So two devs building at different times from different commits will each see their own label, which is fine locally.
Empty footer line → they ran the app without a build. Fix by ./gradlew processResources once.
4. A 60-second mental model for the team
You can paste this into a PR description or team doc:
> The footer build version is now baked into the JAR, not set at deploy time.
>
> - At build (./gradlew assemble): Spring Boot’s bootBuildInfo and the gradle-git-properties plugin write two tiny files — META-INF/build-info.properties (build date) and git.properties (short SHA) — into the JAR.
> - At startup: Spring reads them into the BuildProperties and GitProperties beans.
> - At request time: ShibaGlobalModelAttributes combines them into "<yyyy-MM-dd> · <shortSha>" and exposes it as ${shibaBuildVersion} for the footer templates.
>
> Because the value is part of the artifact, every replica in every environment that runs the same image shows the same label — and there’s nothing to set or configure at deploy time. The env-var fallback (SHIBA_BUILD_VERSION) and the OpenShift oc set env commands have been removed.
>
> Local dev: ./gradlew bootRun (or IntelliJ with Gradle-delegated builds) just works. If the footer is ever empty locally, run ./gradlew processResources and restart the app.
That’s the whole mental model. Nothing else is needed.
