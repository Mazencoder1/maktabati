@echo off
REM يبني مشروع مكتبتي (Windows). يحتاج JDK 21 على PATH.
setlocal
if not exist out mkdir out

javac -encoding UTF-8 -d out -cp "lib\sqlite-jdbc.jar" ^
  src\main\java\sa\mazen\maktabati\model\*.java ^
  src\main\java\sa\mazen\maktabati\dao\*.java ^
  src\main\java\sa\mazen\maktabati\service\*.java ^
  src\main\java\sa\mazen\maktabati\ui\*.java ^
  src\main\java\sa\mazen\maktabati\tools\*.java ^
  src\main\java\sa\mazen\maktabati\app\*.java

if %errorlevel% neq 0 (
  echo فشل البناء.
  exit /b 1
)
echo تم البناء بنجاح. شغّل run.bat لتشغيل البرنامج.
