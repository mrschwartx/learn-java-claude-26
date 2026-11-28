#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 5 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT/tmp"

echo "== Latihan 1: hierarki Karyawan"
java latihan-1/DemoKaryawan.java 2>&1 | tee "$OUT/1-karyawan.txt"

echo; echo "== Latihan 2: method hiding"
java latihan-2/MethodHiding.java 2>&1 | tee "$OUT/2-hiding.txt"
echo "-- error kompilasi yang disengaja (@Override pada method static):"
javac -d "$OUT/tmp" latihan-2/contoh-error/Salah.java 2>&1 | sed 's#latihan-2/contoh-error/##' | tee "$OUT/2-error-kompilasi.txt" || true

echo; echo "== Latihan 3: konflik default method"
java latihan-3/DemoBebek.java 2>&1 | tee "$OUT/3-bebek.txt"
echo "-- error kompilasi yang disengaja (konflik tidak diselesaikan):"
javac -d "$OUT/tmp" latihan-3/contoh-error/BebekTanpaOverride.java 2>&1 | sed 's#latihan-3/contoh-error/##' | tee "$OUT/3-error-kompilasi.txt" || true

echo; echo "== Latihan 4: equals, hashCode, toString"
java latihan-4/DemoUang.java 2>&1 | tee "$OUT/4-uang.txt"

echo; echo "== Latihan 5: constructor 6 parameter vs Builder"
java latihan-5/DemoMobil.java 2>&1 | tee "$OUT/5-mobil.txt"

rm -rf "$OUT/tmp"
echo; echo "Selesai. Hasil tersimpan di $OUT/"
