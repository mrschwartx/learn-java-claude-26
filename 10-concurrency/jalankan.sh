#!/usr/bin/env bash
# Menjalankan semua jawaban Modul 10 dan membandingkan dengan hasil yang tersimpan.
# Pakai SKIP_LAMBAT=1 untuk melewati demo 50 detik (pool 200 thread) di latihan 5.
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null  || { echo "java tidak ditemukan (butuh Java 21+)"; exit 1; }
command -v javac >/dev/null || { echo "javac tidak ditemukan (butuh JDK, bukan JRE)"; exit 1; }
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
mkdir -p hasil-lokal
bersih(){ sed '/^Picked up /d'; }
# Baris yang memang berubah tiap eksekusi dibuang sebelum membandingkan
saring(){ grep -v -e 'BERVARIASI' -e '\[live\]' -e '^flag biasa' || true; }

jalan(){ # nama berkas.java
  java "$2" 2>&1 | bersih > "hasil-lokal/$1.txt" || true
}
jalan latihan-1 latihan-1/DuaThread.java
jalan latihan-2 latihan-2/Counter.java
jalan latihan-3 latihan-3/Prima.java
jalan latihan-4 latihan-4/DemoHarga.java
jalan latihan-5-volatile latihan-5/Volatile.java
if [ "${SKIP_LAMBAT:-0}" != "1" ]; then jalan latihan-5-virtual latihan-5/VirtualThread.java; fi

cmp_(){ # lokal tersimpan
  if diff <(saring < "hasil-lokal/$1.txt") <(saring < "$2") >/dev/null; then echo "OK    $1"; else echo "BEDA  $1 (lihat diff hasil-lokal/$1.txt $2)"; fi
}
cmp_ latihan-1 latihan-1/hasil.txt
cmp_ latihan-2 latihan-2/hasil.txt
cmp_ latihan-3 latihan-3/hasil.txt
cmp_ latihan-4 latihan-4/hasil.txt
cmp_ latihan-5-volatile latihan-5/hasil-volatile.txt
[ "${SKIP_LAMBAT:-0}" = "1" ] || cmp_ latihan-5-virtual latihan-5/hasil-virtual.txt

echo "--- contoh error kompilasi (sengaja gagal) ---"
for f in latihan-1/contoh-error/TanpaTry.java latihan-4/contoh-error/LambdaBerubah.java; do
  javac -d /tmp/m10x "$f" 2>&1 | bersih | head -3 || true
done
