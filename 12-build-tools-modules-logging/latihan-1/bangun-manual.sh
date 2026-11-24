#!/usr/bin/env bash
# EMULASI manual fase compile -> package dengan javac + jar (tanpa Maven, karena Maven butuh plugin dari Maven Central).
# Ini BUKAN keluaran Maven; hanya memperlihatkan apa yang dikerjakan fase `compile` dan `package`.
set -euo pipefail
cd "$(dirname "$0")/demo-quickstart"
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
rm -rf target && mkdir -p target/classes
javac --release 21 -d target/classes src/main/java/com/contoh/app/App.java        # fase compile
jar --create --file target/demo-quickstart-1.0.0.jar --main-class com.contoh.app.App -C target/classes .   # fase package
echo "isi target/:"; (cd target && find . -type f | sort)
echo "--- jar dijalankan:"; java -jar target/demo-quickstart-1.0.0.jar
rm -rf target
