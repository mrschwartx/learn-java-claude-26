#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 4 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT"

echo "== Latihan 1: class Mahasiswa"
java latihan-1/Mahasiswa.java 2>&1 | tee "$OUT/1-mahasiswa.txt"

echo; echo "== Latihan 2: field dan method static"
java latihan-2/Mahasiswa.java 2>&1 | tee "$OUT/2-static.txt"
echo "-- error kompilasi yang disengaja (method static memakai field instance):"
mkdir -p "$OUT/tmp-ce"
javac -d "$OUT/tmp-ce" latihan-2/ContohError.java 2>&1 | sed 's#latihan-2/##' | tee "$OUT/2-error-kompilasi.txt" || true
rm -rf "$OUT/tmp-ce"

echo; echo "== Latihan 3: urutan static block, instance block, constructor"
java latihan-3/UrutanBlok.java 2>&1 | tee "$OUT/3-urutan.txt"

echo; echo "== Latihan 4: pass-by-value"
java latihan-4/PassByValue.java 2>&1 | tee "$OUT/4-passbyvalue.txt"

echo; echo "== Latihan 5: project dua package + access specifier"
latihan-5/jalankan-latihan5.sh "$OUT" >/dev/null
cat "$OUT/5a-berhasil.txt"; echo; cat "$OUT/5b-error-package-private.txt"

echo; echo "Selesai. Hasil tersimpan di $OUT/"
