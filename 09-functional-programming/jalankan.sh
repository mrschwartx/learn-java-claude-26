#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 9 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
# Baris berawalan "[BERVARIASI]" di Latihan 5 (demo bahaya parallel stream) memang berbeda di setiap
# mesin dan setiap eksekusi; itu yang dimaksudkan.
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT/tmp"

echo "== Latihan 1: empat bentuk method reference"
java latihan-1/EmpatJenis.java 2>&1 | tee "$OUT/1-empat-jenis.txt"
echo "-- error kompilasi yang disengaja (method reference ambigu):"
javac -d "$OUT/tmp" latihan-1/contoh-error/Ambigu.java 2>&1 | sed 's#latihan-1/contoh-error/##' | tee "$OUT/1-error-kompilasi.txt" || true

echo; echo "== Latihan 2: higher-order function"
java latihan-2/DemoHof.java 2>&1 | tee "$OUT/2-hof.txt"

echo; echo "== Latihan 3: pipeline normalisasi dan validator"
java latihan-3/DemoPipeline.java 2>&1 | tee "$OUT/3-pipeline.txt"

echo; echo "== Latihan 4: analisis Pegawai dengan stream"
java latihan-4/DemoPegawai.java 2>&1 | tee "$OUT/4-pegawai.txt"

echo; echo "== Latihan 5: frekuensi kata (stream), lazy, sekali pakai, parallel"
java latihan-5/FrekuensiStream.java 2>&1 | tee "$OUT/5-frekuensi.txt"

rm -rf "$OUT/tmp"
echo; echo "Selesai. Hasil tersimpan di $OUT/"
