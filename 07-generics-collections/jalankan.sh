#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 7 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
# Java 21 diperlukan (Locale.of, dan sequenced collections di materi modul).
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT/tmp"

echo "== Latihan 1: daftar unik (urutan masuk dan terurut)"
java latihan-1/Unik.java < latihan-1/masukan.txt 2>&1 | tee "$OUT/1-unik.txt"

echo; echo "== Latihan 2: Pasangan<A,B> dan min()"
java latihan-2/DemoPasangan.java 2>&1 | tee "$OUT/2-pasangan.txt"
echo "-- error kompilasi yang disengaja (bounded type dilanggar):"
javac -d "$OUT/tmp" latihan-2/contoh-error/MinSalah.java 2>&1 | sed 's#latihan-2/contoh-error/##' | tee "$OUT/2-error-kompilasi.txt" || true

echo; echo "== Latihan 3: kurung seimbang dan perbandingan Stack vs ArrayDeque"
java latihan-3/Kurung.java 2>&1 | tee "$OUT/3-kurung.txt"

echo; echo "== Latihan 4: kelompokkan produk per kategori"
java latihan-4/GrupProduk.java 2>&1 | tee "$OUT/4-grup.txt"

echo; echo "== Latihan 5: cache LRU vs FIFO, dan Optional sebagai field"
java latihan-5/DemoLru.java 2>&1 | tee "$OUT/5-lru.txt"
echo
java latihan-5/OptionalField.java 2>&1 | tee "$OUT/5-optional.txt"

rm -rf "$OUT/tmp"
echo; echo "Selesai. Hasil tersimpan di $OUT/"
