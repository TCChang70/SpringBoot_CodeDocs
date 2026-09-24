@echo off
title CMS Dev Launcher
setlocal

echo ==========================================
echo  CMS Dev Server Launcher  (backend 8080 + frontend 5173)
echo ==========================================

echo.
echo [1/2] Checking backend port 8080 ...
netstat -ano | findstr ":8080 " >nul 2>&1
if %errorlevel%==0 (
  echo   Backend already running on 8080.
) else (
  echo   Starting backend jar (keep this window, wait for Started ...)
  start "CMS Backend" cmd /k "cd /d C:\temp\cms_db\backend && run-jar.bat"
)

echo.
echo [2/2] Checking frontend port 5173 ...
netstat -ano | findstr ":5173 " >nul 2>&1
if %errorlevel%==0 (
  echo   Frontend already running on 5173.
) else (
  echo   Starting frontend (vite) ...
  start "CMS Frontend" cmd /k "cd /d C:\temp\cms_db\frontend && npm run dev -- --strictPort"
)

echo.
echo Done. Open http://localhost:5173 in your browser.
echo If you clicked login already, hold Ctrl+F5 to force-refresh (clear old cache).
timeout /t 4 >nul
exit /b 0