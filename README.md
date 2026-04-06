  ./gradlew processResources

  notepad build\resources\main\application.yaml
or
Select-String -Path build\resources\main\application.yaml -Pattern "shiba" -Context 0,2

  ./gradlew bootRun
