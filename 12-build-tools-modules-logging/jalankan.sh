#!/usr/bin/env bash
# Menjalankan bagian Modul 12 yang BISA dijalankan tanpa Maven Central, lalu membandingkan dengan hasil tersimpan.
#   SIMPAN=1       menulis ulang berkas hasil yang tersimpan (pembuatan awal)
#   SKIP_GRADLE=1  lewati build Gradle (sekitar 35-70 detik)
# Bagian yang butuh Maven Central (pom Maven, Logback, Log4j2, TinyLog, Bazel) TIDAK dijalankan di sini.
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null  || { echo "java tidak ditemukan (butuh JDK 21+)"; exit 1; }
command -v javac >/dev/null || { echo "javac tidak ditemukan (butuh JDK, bukan JRE)"; exit 1; }
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
LOKAL=hasil-lokal; mkdir -p "$LOKAL"
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
bersih(){ sed -e '/^Picked up /d' -e 's/\x1b\[[0-9;]*m//g'; }
saring(){ grep -v -e 'BERVARIASI' -e 'file:///' -e 'Actual binding is of type' -e '^SLF4J: Found binding in' || true; }
banding(){ # nama berkas_tersimpan
  if [ "${SIMPAN:-0}" = "1" ]; then cp "$LOKAL/$1.txt" "$2"; echo "SIMPAN $1"; return; fi
  if diff <(saring < "$LOKAL/$1.txt") <(saring < "$2") >/dev/null; then echo "OK    $1"; else echo "BEDA  $1 (diff $LOKAL/$1.txt $2)"; fi
}

# ---- Latihan 1: emulasi compile+package
bash latihan-1/bangun-manual.sh 2>&1 | bersih > $LOKAL/latihan-1.txt || true;      banding latihan-1 latihan-1/hasil-manual.txt

# ---- Latihan 3: Gradle offline (tanpa dependency)
if [ "${SKIP_GRADLE:-0}" != "1" ] && command -v gradle >/dev/null; then
  ( cd latihan-3/gradle-offline && export GRADLE_USER_HOME="$TMP/gh"
    g(){ gradle --offline --no-daemon --console=plain "$@" 2>&1 | sed -e '/^Picked/d' -e '/^Daemon will/d' -e '/^To honour/d' -e '/^For more details/d' -e '/^$/d' -e '/^Welcome/d' -e '/^Starting a Gradle/d'; }
    { echo "### gradle build run (bersih)"; g clean build run | grep -v -E 'BUILD SUCCESSFUL in|^Deprecated|^You can use'; echo; echo "### ulangi tanpa perubahan"; g build | grep -v 'BUILD SUCCESSFUL in'; } > "../../$LOKAL/latihan-3.txt"
    rm -rf build .gradle )
  banding latihan-3 latihan-3/hasil-gradle.txt
else echo "LEWAT latihan-3 (gradle tidak ada / SKIP_GRADLE=1)"; fi

# ---- Latihan 4: JPMS
( cd latihan-4; rm -rf out runtime-mini
  javac -d out --module-source-path mods $(find mods -name '*.java') 2>&1 | bersih
  java --module-path out -m com.contoh.app/com.contoh.app.App 2>&1 | bersih > "../$LOKAL/latihan-4-run.txt"
  jdeps --module-path out -s out/com.contoh.app 2>&1 | bersih > "../$LOKAL/latihan-4-jdeps.txt"
  rm -rf "$TMP/err"; ( cd contoh-error && javac -d "$TMP/err" --module-source-path mods $(find mods -name '*.java') 2>&1 | bersih ) > "../$LOKAL/latihan-4-error.txt" || true
  JH=$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")
  if [ -d "$JH/jmods" ]; then
    jlink --module-path "$JH/jmods:out" --add-modules com.contoh.app --output runtime-mini --strip-debug --no-header-files --no-man-pages 2>&1 | bersih
    runtime-mini/bin/java --list-modules 2>&1 | bersih > "../$LOKAL/latihan-4-jlink.txt"
    { echo "[BERVARIASI] runtime-mini: $(du -sk runtime-mini | cut -f1) KB (lib/modules $(du -sk runtime-mini/lib/modules | cut -f1) KB)"
      echo "[BERVARIASI] JDK penuh   : $(du -sk "$JH" | cut -f1) KB (lib/modules $(du -sk "$JH/lib/modules" | cut -f1) KB)"; } > "../$LOKAL/latihan-4-ukuran.txt"
  else echo "jmods tidak ada: lewati jlink" > "../$LOKAL/latihan-4-jlink.txt"; fi
  rm -rf out runtime-mini )
banding latihan-4-run latihan-4/hasil-run.txt
banding latihan-4-jdeps latihan-4/hasil-jdeps.txt
banding latihan-4-error latihan-4/error-kompilasi-latihan4.txt
banding latihan-4-jlink latihan-4/hasil-jlink-modules.txt
cp "$LOKAL/latihan-4-ukuran.txt" latihan-4/hasil-ukuran.txt 2>/dev/null || true

# ---- Latihan 5/2: SLF4J facade, memakai slf4j-api + backend yang tersedia di instalasi Maven (jika ada)
MVN=$(command -v mvn || true)
if [ -n "$MVN" ] && [ -f "$(dirname "$(dirname "$(readlink -f "$MVN")")")/lib/slf4j-api-1.7.36.jar" ]; then
  MV=$(dirname "$(dirname "$(readlink -f "$MVN")")")/lib
  API=$MV/slf4j-api-1.7.36.jar
  PROV=$MV/maven-slf4j-provider-3.9.11.jar:$(ls $MV/maven-shared-utils*.jar | head -1):$(ls $MV/jansi-[0-9]*.jar | head -1)
  javac -cp "$API" -d "$TMP/backend" $(find latihan-5/backend-mini/src -name '*.java') 2>&1 | bersih
  javac -cp "$API" -d "$TMP/app" latihan-5/app/com/contoh/app/App.java 2>&1 | bersih
  javac -cp "$API" -d "$TMP/buruk" latihan-5/buruk/demo/lain/LogBuruk.java 2>&1 | bersih
  { echo "### A. tanpa backend";                java -cp "$TMP/app:$API" com.contoh.app.App 2>&1 | bersih
    echo "### B. backend milik Maven (SimpleLogger)"; java -cp "$TMP/app:$API:$PROV" com.contoh.app.App 2>&1 | bersih
    echo "### C. backend-mini buatan sendiri (JSON)"; java -cp "$TMP/app:$API:$TMP/backend" com.contoh.app.App 2>&1 | bersih
    echo "### D. dua backend sekaligus";              java -cp "$TMP/app:$API:$TMP/backend:$PROV" com.contoh.app.App 2>&1 | bersih | head -9
    echo "### App.class sama untuk semua skenario (sha256 depan): $(sha256sum "$TMP/app/com/contoh/app/App.class" | cut -c1-16)"; } > $LOKAL/latihan-5-facade.txt
  java -cp "$TMP/buruk:$API:$TMP/backend" demo.lain.LogBuruk 2>&1 | bersih > $LOKAL/latihan-5-buruk.txt
  banding latihan-5-facade latihan-5/hasil-facade.txt
  banding latihan-5-buruk latihan-5/hasil-buruk.txt
else echo "LEWAT latihan-5 (slf4j-api 1.7.36 dari instalasi Maven tidak ditemukan)"; fi

# ---- Latihan 7: JUL
java latihan-7/DemoLogging.java 2>&1 | bersih > $LOKAL/latihan-7-jul.txt;           banding latihan-7-jul latihan-7/hasil-jul.txt
echo "Selesai. Bagian yang tidak bisa dijalankan tanpa Maven Central: lihat jawaban-latihan.md (ditandai TIDAK DIJALANKAN)."
