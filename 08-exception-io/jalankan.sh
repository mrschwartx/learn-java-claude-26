#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 8 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
# Catatan: Latihan 2 sengaja diakhiri exception yang tidak ditangani (stack trace di akhir bagian itu).
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT/tmp"

echo "== Latihan 1: ambilUmur (masukan bermasalah lalu valid, dan masukan yang habis)"
java latihan-1/Umur.java < latihan-1/masukan.txt 2>&1 | tee "$OUT/1-umur.txt"
echo "--"
java latihan-1/Umur.java < latihan-1/masukan-habis.txt 2>&1 | tee "$OUT/1-umur-habis.txt"

echo; echo "== Latihan 2: checked vs unchecked (berakhir dengan exception tak tertangani, memang disengaja)"
java latihan-2/DemoGudang.java 2>&1 | tee "$OUT/2-gudang.txt" || true
echo "-- error kompilasi yang disengaja (checked exception tidak ditangani):"
javac -d "$OUT/tmp" latihan-2/contoh-error/TidakDitangani.java 2>&1 | sed 's#latihan-2/contoh-error/##' | tee "$OUT/2-error-kompilasi.txt" || true

echo; echo "== Latihan 3: try-with-resources, urutan tutup, suppressed"
java latihan-3/DemoKoneksi.java 2>&1 | tee "$OUT/3-koneksi.txt"

echo; echo "== Latihan 4: ringkasan file (contoh kecil, lalu 300.000 baris)"
java latihan-4/Ringkas.java latihan-4/contoh.txt "$OUT/tmp/ringkasan.txt" 2>&1 | tee "$OUT/4-contoh.txt"
echo "--"
java latihan-4/Ringkas.java --besar 2>&1 | tee "$OUT/4-besar.txt"

echo; echo "== Latihan 5: exception chaining dan catch kosong"
java latihan-5/DemoLaporan.java 2>&1 | tee "$OUT/5-laporan.txt"

rm -rf "$OUT/tmp"
echo; echo "Selesai. Hasil tersimpan di $OUT/"
