@echo off
echo ========================================
echo Starting Replica 2 on port 8082
echo Replica ID: replica_2
echo ========================================
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=replica2

pause
