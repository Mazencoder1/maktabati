#!/usr/bin/env bash
# يبني مشروع مكتبتي (Linux/macOS/Git Bash). يحتاج JDK 21 على PATH.
set -e
mkdir -p out

javac -encoding UTF-8 -d out -cp "lib/sqlite-jdbc.jar" \
  src/main/java/sa/mazen/maktabati/model/*.java \
  src/main/java/sa/mazen/maktabati/dao/*.java \
  src/main/java/sa/mazen/maktabati/service/*.java \
  src/main/java/sa/mazen/maktabati/ui/*.java \
  src/main/java/sa/mazen/maktabati/tools/*.java \
  src/main/java/sa/mazen/maktabati/app/*.java

echo "تم البناء بنجاح. شغّل ./run.sh لتشغيل البرنامج."
