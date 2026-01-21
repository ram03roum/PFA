@echo off
setlocal enabledelayedexpansion

REM ==============================================
REM Bacoge Backend - Development Startup Script
REM Usage: start-backend.bat [profile] [port]
REM Examples:
REM   start-backend.bat           -> dev on 8080
REM   start-backend.bat dev 8081  -> dev on 8080
REM   start-backend.bat prod 8080 -> prod on 8080
REM ==============================================

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=dev

set PORT=%2
if "%PORT%"=="" set PORT=8080

echo Starting Bacoge backend with profile %PROFILE% on port %PORT% ...
call .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%PROFILE% -Dspring-boot.run.jvmArguments="--add-opens=java.base/java.lang=ALL-UNNAMED" -Dspring-boot.run.arguments=--server.port=%PORT%
