#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 3 di MESIN ANDA (butuh JDK 21+: java di PATH).
# Hasil ditulis ke folder hasil-lokal/ untuk dibandingkan dengan folder latihan-*/.
# Catatan: satu baris di Latihan 4 berisi waktu eksekusi dan PASTI berbeda di mesin Anda.
set -euo pipefail
cd "$(dirname "$0")"

command -v java >/dev/null || { echo "Perintah 'java' tidak ditemukan. Pasang JDK 21 atau lebih baru."; exit 1; }

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT"

echo "== Latihan 1: ganjil/genap, tanda, kelipatan 5 (masukan 15, -8, 0, -7)"
for v in 15 -8 0 -7; do
  java latihan-1/Angka.java < "latihan-1/masukan-$v.txt" 2>&1 | tee -a "$OUT/1-angka.txt"
done

echo; echo "== Latihan 2: FizzBuzz 1..100, versi switch dan versi if-else"
java latihan-2/FizzBuzzSwitch.java > "$OUT/2-fizzbuzz-switch.txt"
java latihan-2/FizzBuzzIf.java     > "$OUT/2-fizzbuzz-if.txt"
if cmp -s "$OUT/2-fizzbuzz-switch.txt" "$OUT/2-fizzbuzz-if.txt"; then
  echo "Kedua versi menghasilkan keluaran identik ($(wc -l < "$OUT/2-fizzbuzz-if.txt") baris)."
else
  echo "PERINGATAN: keluaran kedua versi berbeda!"
fi

echo; echo "== Latihan 3: nilai terbesar kedua"
java latihan-3/TerbesarKedua.java 2>&1 | tee "$OUT/3-terbesar-kedua.txt"

echo; echo "== Latihan 4: hitungVokal, balik, biaya penyambungan string"
java latihan-4/Teks.java 2>&1 | tee "$OUT/4-teks.txt"

echo; echo "== Latihan 5: string pool dan struk kasir"
{ java latihan-5/StringPool.java; echo; java latihan-5/Kasir.java; } 2>&1 | tee "$OUT/5-pool-dan-kasir.txt"

echo; echo "Selesai. Hasil tersimpan di $OUT/"
