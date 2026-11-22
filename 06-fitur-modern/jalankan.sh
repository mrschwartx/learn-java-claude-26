#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 6 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
# Semua kode memakai fitur final Java 21 (tidak ada fitur preview).
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT/tmp"

echo "== Latihan 1: enum Level dan EnumMap"
java latihan-1/DemoLevel.java 2>&1 | tee "$OUT/1-level.txt"

echo; echo "== Latihan 2: Mahasiswa sebagai record"
java latihan-2/DemoMahasiswa.java 2>&1 | tee "$OUT/2-mahasiswa.txt"

echo; echo "== Latihan 3: sealed Hasil"
java latihan-3/DemoHasil.java 2>&1 | tee "$OUT/3-hasil.txt"
echo "-- setelah Menunggu ditambahkan dan switch diperbarui:"
java latihan-3/DemoHasilMenunggu.java 2>&1 | tee "$OUT/3-hasil-menunggu.txt"
echo "-- error kompilasi yang disengaja (Menunggu ditambahkan, switch tidak diubah):"
javac -d "$OUT/tmp" latihan-3/contoh-error/HasilBertambah.java 2>&1 | sed 's#latihan-3/contoh-error/##' | tee "$OUT/3-error-kompilasi.txt" || true

echo; echo "== Latihan 4: switch pattern"
java latihan-4/DemoFormat.java 2>&1 | tee "$OUT/4-format.txt"
echo "-- error kompilasi yang disengaja (urutan case salah):"
for f in UrutanSalah DefaultDiAtas; do
  javac -d "$OUT/tmp" "latihan-4/contoh-error/$f.java" 2>&1 | sed 's#latihan-4/contoh-error/##' || true
done | tee "$OUT/4-error-kompilasi.txt"

echo; echo "== Latihan 5: annotation @Panjang dan @Retention"
java latihan-5/DemoPanjang.java 2>&1 | tee "$OUT/5-panjang.txt"

rm -rf "$OUT/tmp"
echo; echo "Selesai. Hasil tersimpan di $OUT/"
