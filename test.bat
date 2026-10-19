@echo off
REM يبني مشروع مكتبتي ثم يشغّل اختبارات JUnit 5 (Windows).
setlocal
if not exist out mkdir out
if not exist out-test mkdir out-test

call build.bat
if %errorlevel% neq 0 exit /b 1

javac -encoding UTF-8 -d out-test -cp "out;lib\sqlite-jdbc.jar;lib\junit-platform-console-standalone.jar" ^
  src\test\java\sa\mazen\maktabati\model\*.java ^
  src\test\java\sa\mazen\maktabati\service\*.java

if %errorlevel% neq 0 (
  echo فشل بناء الاختبارات.
  exit /b 1
)

java -cp "out;out-test;lib\sqlite-jdbc.jar;lib\junit-platform-console-standalone.jar" ^
  org.junit.platform.console.ConsoleLauncher execute --select-package sa.mazen.maktabati --details=tree
