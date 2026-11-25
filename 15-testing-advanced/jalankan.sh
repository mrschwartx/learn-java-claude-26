#!/usr/bin/env bash
# Menjalankan semua latihan Modul 15 dan membandingkan dengan hasil tersimpan. SIMPAN=1 menulis ulang baseline.
# Hanya butuh JDK 21+. Latihan 5 memakai PostgreSQL sementara dari Modul 14 (../14-database-access/alat/pg-up.sh); bila tidak ada, bagian itu DILEWATI.
set -euo pipefail
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
cd "$(dirname "$0")"
OUT=$(mktemp -d); trap 'rm -rf "$OUT"' EXIT
filter() { sed '/^Picked up /d'; }
banding() { # $1=berkas tersimpan $2=berkas baru
  if [ "${SIMPAN:-0}" = 1 ]; then cp "$2" "$1"; echo "  disimpan: $1"; return; fi
  if diff <(grep -v BERVARIASI "$1") <(grep -v BERVARIASI "$2") >/dev/null; then echo "  OK sama dengan $1"; else echo "  BEDA dengan $1:"; diff "$1" "$2" || true; GAGAL=1; fi
}
GAGAL=0
mkdir -p "$OUT/alat"; javac -d "$OUT/alat" alat/MiniUji.java alat/MiniMock.java alat/Cov.java 2>&1 | filter
CP="$OUT/alat"

echo "== Latihan 1: TDD Password (merah lalu hijau) =="
for v in merah hijau; do
  mkdir -p "$OUT/l1-$v"; javac -cp "$CP" -d "$OUT/l1-$v" latihan-1/PasswordTest.java latihan-1/$v/Password.java 2>&1 | filter
  java -cp "$CP:$OUT/l1-$v" MiniUji PasswordTest 2>&1 | filter > "$OUT/l1-$v.txt" || true
  tail -1 "$OUT/l1-$v.txt"; banding latihan-1/hasil-$v.txt "$OUT/l1-$v.txt"
done

echo "== Latihan 2: Clock.fixed dan hari Jumat =="
mkdir -p "$OUT/l2"; javac -cp "$CP" -d "$OUT/l2" latihan-2/MiniUjiModul.java 2>&1 | filter
java -cp "$CP:$OUT/l2" MiniUjiModul 2>&1 | filter > "$OUT/l2.txt" || true
tail -3 "$OUT/l2.txt"; banding latihan-2/hasil.txt "$OUT/l2.txt"

echo "== Latihan 3: RekeningBank, cakupan, mutation testing =="
java -cp "$CP" latihan-3/AnalisisKualitas.java 2>&1 | filter > "$OUT/l3.txt"
grep -E "Ringkasan|Skor mutasi|baris pernyataan" "$OUT/l3.txt"; banding latihan-3/hasil.txt "$OUT/l3.txt"

echo "== Latihan 4: mock vs fake =="
mkdir -p "$OUT/l4"; javac -cp "$CP" -d "$OUT/l4" latihan-4/Domain.java latihan-4/PesananServiceTest.java latihan-4/RefaktorTest.java 2>&1 | filter
{ java -cp "$CP:$OUT/l4" MiniUji PesananServiceTest 2>&1 | filter; echo; java -cp "$CP:$OUT/l4" RefaktorTest 2>&1 | filter; } > "$OUT/l4.txt" || true
grep -E "Ringkasan|GAGAL" "$OUT/l4.txt"; banding latihan-4/hasil.txt "$OUT/l4.txt"

echo "== Latihan 5: integration test + load test (PostgreSQL asli) =="
if ! command -v initdb >/dev/null 2>&1 && ! ls /usr/lib/postgresql/*/bin/initdb >/dev/null 2>&1; then
  echo "  DILEWATI: PostgreSQL server tidak ada."
else
  bash ../14-database-access/alat/pg-up.sh >/dev/null
  mkdir -p "$OUT/pg" "$OUT/l5"; javac -d "$OUT/pg" ../14-database-access/alat/minipg/MiniPg.java 2>&1 | filter
  javac -cp "$CP:$OUT/pg" -d "$OUT/l5" latihan-5/*.java 2>&1 | filter
  java -cp "$CP:$OUT/pg:$OUT/l5" ProdukApiTest 2>&1 | filter > "$OUT/l5.txt" || true
  tail -1 "$OUT/l5.txt"; banding latihan-5/hasil-integrasi.txt "$OUT/l5.txt"
  echo "  Load test (angka BERVARIASI, tidak dibandingkan):"
  java -cp "$CP:$OUT/pg:$OUT/l5" BebanTest 1,10 50 2>&1 | filter | sed 's/^/  /'
  bash ../14-database-access/alat/pg-down.sh >/dev/null 2>&1 || true
fi
[ "$GAGAL" = 0 ] && echo "SELESAI: semua hasil sesuai." || { echo "ADA PERBEDAAN"; exit 1; }
