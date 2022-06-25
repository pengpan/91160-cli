@echo off
rem 91160-CLI
rem https://github.com/pengpan/91160-cli
if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java jdk8 or later! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal

set BASE_DIR=%~dp0

set "JAVA_OPT=%JAVA_OPT% -Xms512m -Xmx512m -Xmn256m"
set "JAVA_OPT=%JAVA_OPT% -Dfile.encoding=utf-8"
set "JAVA_OPT=%JAVA_OPT% -jar %BASE_DIR%\91160-cli.jar"
set "JAVA_OPT=%JAVA_OPT% init"

chcp 65001
call "%JAVA%" %JAVA_OPT% %*
