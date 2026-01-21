param(
  [string]$Profile = "dev",
  [int]$Port = 8080
)

Write-Host "Starting Bacoge backend with profile $Profile on port $Port ..."

# Use Maven Wrapper to run Spring Boot with desired profile and port
& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=$Profile" "-Dspring-boot.run.jvmArguments=--add-opens=java.base/java.lang=ALL-UNNAMED" "-Dspring-boot.run.arguments=--server.port=$Port"
