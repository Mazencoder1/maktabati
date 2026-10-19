#!/usr/bin/env bash
# يشغّل مكتبتي (Linux/macOS/Git Bash). ينشئ قاعدة بيانات SQLite تلقائياً مع بيانات تجريبية عند أول تشغيل.
set -e
if [[ "$OS" == "Windows_NT" || "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
  SEP=";"
else
  SEP=":"
fi

java -cp "out${SEP}lib/sqlite-jdbc.jar" sa.mazen.maktabati.app.Main
