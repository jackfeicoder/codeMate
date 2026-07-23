@echo off
setlocal

set "CODEMATE_HOME=%~dp0.."
for %%I in ("%CODEMATE_HOME%") do set "CODEMATE_HOME=%%~fI"
set "CODEMATE_JAR=%CODEMATE_HOME%\target\codemate-0.1.0-SNAPSHOT.jar"

if not exist "%CODEMATE_JAR%" (
  echo codeMate executable not found: "%CODEMATE_JAR%"
  echo Run "mvn package" from "%CODEMATE_HOME%" first.
  exit /b 1
)

java -Dcodemate.home="%CODEMATE_HOME%" -jar "%CODEMATE_JAR%" %*
