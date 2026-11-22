#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 1 di MESIN ANDA (butuh JDK: java, javac, javap, jshell di PATH).
# Hasil ditulis ke folder hasil-lokal/ sehingga Anda bisa mengisi data vendor/versi milik Anda sendiri.
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac javap jshell; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" build && mkdir -p "$OUT" build

echo "== Latihan 1: versi JDK"
{ echo '$ java -version';  java -version 2>&1; echo
  echo '$ javac -version'; javac -version 2>&1; } | tee "$OUT/1-versi-java.txt"

echo; echo "== Latihan 2: HelloWorld dua cara"
cp latihan-2/HelloWorld.java build/ && cd build
{ echo '--- Cara A: javac + java'
  javac HelloWorld.java
  ls -l HelloWorld.*
  java HelloWorld
  rm HelloWorld.class
  echo '--- Cara B: java HelloWorld.java'
  java HelloWorld.java
  echo '$ ls   # tidak ada .class'; ls; } 2>&1 | tee "../$OUT/2-helloworld.txt"

echo; echo "== Latihan 3: javap -c"
javac HelloWorld.java
javap -c -p HelloWorld 2>&1 | tee "../$OUT/3-javap.txt"
cd ..

echo; echo "== Latihan 4: JIT vs interpreter"
{ echo '$ java WarmUp.java';       java latihan-4/WarmUp.java
  echo; echo '$ java -Xint WarmUp.java'; java -Xint latihan-4/WarmUp.java; } 2>&1 | tee "$OUT/4-jit.txt"

echo; echo "== Latihan 5: jshell"
jshell -s < latihan-5/masukan.jsh 2>&1 | tee "$OUT/5-jshell.txt"

rm -rf build
echo; echo "Selesai. Hasil tersimpan di $OUT/"
