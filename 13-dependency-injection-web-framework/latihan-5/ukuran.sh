#!/usr/bin/env bash
# Mengukur HANYA stand-in JDK (bukan Spring/Javalin) dan menghitung baris kode file-file yang saya tulis.
set -euo pipefail
cd "$(dirname "$0")"
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
T=$(mktemp -d); trap 'rm -rf "$T"' EXIT
javac -d "$T/c" HaloStandIn.java
jar --create --file "$T/halo.jar" --main-class HaloStandIn -C "$T/c" .
echo "[BERVARIASI] ukuran jar stand-in: $(stat -c %s "$T/halo.jar") byte"
s=$(date +%s%N); java -jar "$T/halo.jar" >/dev/null 2>&1; e=$(date +%s%N)
echo "[BERVARIASI] waktu proses penuh stand-in (JVM start sampai exit, termasuk 4 request): $(( (e-s)/1000000 )) ms"
for f in javalin-demo/src/main/java/ProdukApp.java spring-halo/src/main/java/com/contoh/halo/HaloApplication.java HaloStandIn.java javalin-demo/pom.xml spring-halo/pom.xml; do
  printf "baris kode (tanpa baris kosong/komentar) %-62s %s\n" "$f" "$(grep -v -E '^\s*$|^\s*(//|<!--)' "$f" | wc -l)"
done
