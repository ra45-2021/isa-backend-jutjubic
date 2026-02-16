@echo off
echo =============================================
echo   JUTJUBIC KLASTER - POKRETANJE
echo =============================================
echo.

REM Provera da li Docker Desktop radi
docker info >nul 2>&1
if errorlevel 1 (
    echo [GRESKA] Docker Desktop nije pokrenut!
    echo Pokreni Docker Desktop i probaj ponovo.
    pause
    exit /b 1
)

echo [1/4] Zaustavljam postojece kontejnere...
docker-compose down

echo.
echo [2/4] Gradim Docker image aplikacije...
echo (Ovo moze potrajati prvi put...)
docker-compose build

echo.
echo [3/4] Pokrecem klaster...
docker-compose up -d

echo.
echo [4/4] Cekam da se servisi podignu...
timeout /t 30 /nobreak >nul

echo.
echo =============================================
echo   KLASTER JE POKRENUT!
echo =============================================
echo.
echo Pristup aplikaciji:
echo   - API Gateway (NGINX):  http://localhost/api/posts
echo   - Health Check:         http://localhost/api/health
echo   - Actuator:             http://localhost/actuator/health
echo.
echo Direktan pristup replikama (za testiranje):
echo   - Replika 1: http://localhost:8081/api/health (potrebno dodati port mapping)
echo   - Replika 2: http://localhost:8082/api/health (potrebno dodati port mapping)
echo.
echo Korisne komande:
echo   docker-compose logs -f          - Prati logove u realnom vremenu
echo   docker-compose ps               - Prikazi status kontejnera
echo   docker stop jutjubic-replica-1  - Simuliraj pad replike 1
echo   docker start jutjubic-replica-1 - Ponovo podigni repliku 1
echo   docker-compose down             - Zaustavi sve
echo.
pause
