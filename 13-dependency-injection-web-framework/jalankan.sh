#!/usr/bin/env bash
# Menjalankan semua bagian Modul 13 yang BISA dijalankan tanpa Maven Central, lalu membandingkan dengan hasil tersimpan.
#   SIMPAN=1 ./jalankan.sh   menulis ulang hasil tersimpan
# Proyek Spring Boot, Javalin (pom) dan test framework TIDAK dijalankan (butuh dependency dari internet).
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "java tidak ditemukan (butuh JDK 21+)"; exit 1; }
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
L=hasil-lokal; mkdir -p $L
bersih(){ sed '/^Picked up /d'; }
saring(){ grep -v 'BERVARIASI' || true; }
jalan(){ java "$2" 2>&1 | bersih > "$L/$1.txt" || true
  if [ "${SIMPAN:-0}" = 1 ]; then cp "$L/$1.txt" "$3"; echo "SIMPAN $1"
  elif diff <(saring < "$L/$1.txt") <(saring < "$3") >/dev/null; then echo "OK    $1"; else echo "BEDA  $1 (diff $L/$1.txt $3)"; fi; }
jalan latihan-1 latihan-1/DemoPembayaran.java latihan-1/hasil.txt
jalan latihan-2 latihan-2/DemoMelingkar.java  latihan-2/hasil.txt
jalan latihan-3 latihan-3/DemoInjeksi.java    latihan-3/hasil.txt
jalan latihan-4 latihan-4/TokoStandIn.java    latihan-4/hasil-standin.txt
jalan latihan-5 latihan-5/HaloStandIn.java    latihan-5/hasil-standin.txt
jalan latihan-6 latihan-6/PostStandIn.java    latihan-6/hasil-standin.txt
bash latihan-5/ukuran.sh 2>&1 | bersih > $L/latihan-5-ukuran.txt
if [ "${SIMPAN:-0}" = 1 ]; then cp $L/latihan-5-ukuran.txt latihan-5/hasil-ukuran.txt; fi
grep '^baris' $L/latihan-5-ukuran.txt | diff - <(grep '^baris' latihan-5/hasil-ukuran.txt) >/dev/null && echo "OK    latihan-5-ukuran (jumlah baris)" || echo "BEDA  latihan-5-ukuran"
echo "Selesai. Spring Boot/Javalin/Quarkus/Play: TIDAK dijalankan (lihat jawaban-latihan.md)."
