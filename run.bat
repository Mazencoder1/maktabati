@echo off
REM يشغّل مكتبتي (Windows). ينشئ قاعدة بيانات SQLite تلقائياً مع بيانات تجريبية عند أول تشغيل.
java -cp "out;lib\sqlite-jdbc.jar" sa.mazen.maktabati.app.Main
