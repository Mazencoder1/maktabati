#!/usr/bin/env bash
# يبني مشروع مكتبتي ثم يشغّل اختبارات JUnit 5 (Linux/macOS/Git Bash).
set -e
mkdir -p out out-test

if [[ "$OS" == "Windows_NT" || "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
  SEP=";"
else
  SEP=":"
fi

./build.sh

javac -encoding UTF-8 -d out-test -cp "out${SEP}lib/sqlite-jdbc.jar${SEP}lib/junit-platform-console-standalone.jar" \
  src/test/java/sa/mazen/maktabati/model/*.java \
  src/test/java/sa/mazen/maktabati/service/*.java

java -cp "out${SEP}out-test${SEP}lib/sqlite-jdbc.jar${SEP}lib/junit-platform-console-standalone.jar" \
  org.junit.platform.console.ConsoleLauncher execute --select-package sa.mazen.maktabati --details=tree
