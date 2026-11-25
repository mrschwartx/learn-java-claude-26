#!/usr/bin/env bash
# Menjalankan latihan JDBC Modul 14 terhadap PostgreSQL ASLI sementara (port 54329) dengan driver JDBC MAINAN (alat/minipg).
#   SIMPAN=1 ./jalankan.sh   menulis ulang hasil tersimpan
# Butuh: JDK 21+, binary server PostgreSQL (initdb/pg_ctl) dan psql. Di mesin Anda dengan Docker/PostgreSQL biasa,
# lebih baik pakai driver resmi (org.postgresql:postgresql) -- kode latihan tidak perlu diubah selain baris Class.forName("MiniPg").
# JPA/Spring Data/Ebean TIDAK dijalankan (butuh Maven Central).
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null && command -v javac >/dev/null || { echo "butuh JDK 21+"; exit 1; }
command -v psql >/dev/null || { echo "psql tidak ditemukan"; exit 1; }
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
L=hasil-lokal; mkdir -p $L
TMP=$(mktemp -d)
bersih(){ sed '/^Picked up /d'; }
saring(){ grep -v 'BERVARIASI' || true; }
trap './alat/pg-down.sh >/dev/null 2>&1; rm -rf "$TMP"' EXIT
./alat/pg-up.sh >/dev/null
PSQL="psql -h 127.0.0.1 -p 54329 -U belajar -d toko -q"
javac -d "$TMP/driver" alat/minipg/MiniPg.java 2>&1 | bersih

jalan(){ # nama berkas.java hasil_tersimpan
  PGOPTIONS="-c client_min_messages=warning" $PSQL -f schema.sql
  java -cp "$TMP/driver" "$2" 2>&1 | bersih > "$L/$1.txt" || true
  if [ "$1" = latihan-2 ]; then { echo "--- bukti dari psql (sesi terpisah):"; $PSQL -c "select pemilik, saldo from rekening order by id" -c "select dari, ke, jumlah from riwayat_transfer"; } >> "$L/$1.txt"; fi
  if [ "${SIMPAN:-0}" = 1 ]; then cp "$L/$1.txt" "$3"; echo "SIMPAN $1"
  elif diff <(saring < "$L/$1.txt") <(saring < "$3") >/dev/null; then echo "OK    $1"; else echo "BEDA  $1 (diff $L/$1.txt $3)"; fi; }
jalan latihan-1 latihan-1/DaftarPelanggan.java latihan-1/hasil.txt
jalan latihan-2 latihan-2/TransferAman.java    latihan-2/hasil.txt
jalan latihan-3 latihan-3/BatchVsSatuan.java   latihan-3/hasil.txt
jalan latihan-4 latihan-4/LockingDemo.java     latihan-4/hasil.txt
jalan latihan-5 latihan-5/NPlusSatu.java       latihan-5/hasil.txt
jalan latihan-6 latihan-6/ColumnDanQuery.java  latihan-6/hasil.txt
echo "Selesai. JPA, Spring Data JPA dan Ebean: TIDAK dijalankan (lihat jawaban-latihan.md)."
