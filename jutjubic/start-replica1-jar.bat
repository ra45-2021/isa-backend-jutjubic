@echo off
echo ========================================
echo Starting Replica 1 on port 8081
echo Replica ID: replica_1
echo ========================================
echo.
echo Using JAR file: target/jutjubic-0.0.1-SNAPSHOT.jar
echo.

java -jar target/jutjubic-0.0.1-SNAPSHOT.jar --spring.profiles.active=replica1

pause
