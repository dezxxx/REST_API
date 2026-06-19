@echo off
echo Building project...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo BUILD FAILED
    pause
    exit /b 1
)
echo Deploying to Tomcat...
copy /Y target\REST_API-1.0-SNAPSHOT.war "D:\Program Files\apache-tomcat-9.0.118\webapps\REST_API.war"
echo Done! Tomcat will redeploy automatically.
