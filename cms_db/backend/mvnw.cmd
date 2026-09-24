@echo off
rem Maven Wrapper (simple bootstrap) - 自動下載並呼叫 Apache Maven
setlocal
set "MAVEN_VERSION=3.9.6"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%"
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto :run
echo Downloading Apache Maven %MAVEN_VERSION% ...
powershell -NoProfile -Command ^
  "$v='%MAVEN_VERSION%'; $h='%MAVEN_HOME%';" ^
  "New-Item -ItemType Directory -Force -Path $h | Out-Null;" ^
  "$z=Join-Path $h 'maven.zip';" ^
  "Invoke-WebRequest -Uri ('https://archive.apache.org/dist/maven/maven-3/'+$v+'/binaries/apache-maven-'+$v+'-bin.zip') -OutFile $z -UseBasicParsing;" ^
  "$tmp=Join-Path $env:TEMP ('mvd'+$PID);" ^
  "New-Item -ItemType Directory -Force -Path $tmp | Out-Null;" ^
  "Expand-Archive -Path $z -DestinationPath $tmp -Force;" ^
  "Copy-Item -Recurse -Force (Join-Path $tmp ('apache-maven-'+$v+'\*')) $h;" ^
  "Remove-Item -Recurse -Force $tmp; Remove-Item $z"
if errorlevel 1 (
  echo Failed to download Maven. Please install Maven and add to PATH.
  exit /b 1
)
:run
call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal