#!/usr/bin/env bash
# Menjalankan ulang seluruh latihan Modul 2 di MESIN ANDA (butuh JDK 21+: java dan javac di PATH).
# Hasil ditulis ke folder hasil-lokal/ sehingga Anda bisa membandingkannya dengan folder latihan-*/.
set -euo pipefail
cd "$(dirname "$0")"

for cmd in java javac; do
  command -v "$cmd" >/dev/null || { echo "Perintah '$cmd' tidak ditemukan. Pasang JDK (bukan hanya JRE) terlebih dahulu."; exit 1; }
done

OUT=hasil-lokal
rm -rf "$OUT" && mkdir -p "$OUT"

echo "== Latihan 1: 8 tipe primitif"
java latihan-1/Primitif.java 2>&1 | tee "$OUT/1-primitif.txt"

echo; echo "== Latihan 2: prediksi byte dan pembagian int"
java latihan-2/Prediksi.java 2>&1 | tee "$OUT/2-prediksi.txt"

echo; echo "== Latihan 3: persegi panjang (masukan 3 x 4, lalu 5.5 x 2.5)"
{ java latihan-3/Persegi.java < latihan-3/masukan-1.txt; echo
  java latihan-3/Persegi.java < latihan-3/masukan-2.txt; } 2>&1 | tee "$OUT/3-persegi.txt"

echo; echo "== Latihan 4: double vs BigDecimal"
java latihan-4/Uang.java 2>&1 | tee "$OUT/4-uang.txt"

echo; echo "== Latihan 5: scope dan shadowing"
java latihan-5/Scope.java 2>&1 | tee "$OUT/5-scope.txt"

echo; echo "== Latihan 5 (bonus): error kompilasi saat variabel dipakai di luar scope"
mkdir -p "$OUT/tmp-err"
cat > "$OUT/tmp-err/Err.java" <<'JAVA'
public class Err { public static void main(String[] a){ int x=10; if(x>5){int y=1;} System.out.println(y);} }
JAVA
javac -d "$OUT/tmp-err" "$OUT/tmp-err/Err.java" 2>&1 | tee "$OUT/5-error-kompilasi.txt" || true
rm -rf "$OUT/tmp-err"

echo; echo "Selesai. Hasil tersimpan di $OUT/"
