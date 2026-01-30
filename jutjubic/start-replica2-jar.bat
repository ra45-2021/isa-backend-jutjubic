@echo off
echo ========================================
echo Starting Replica 2 on port 8082
echo Replica ID: replica_2
echo ========================================
echo.
echo Using JAR file: target/jutjubic-0.0.1-SNAPSHOT.jar
echo.

java -jar target/jutjubic-0.0.1-SNAPSHOT.jar --spring.profiles.active=replica2

pause
