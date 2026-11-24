#!/usr/bin/env bash
# Menjalankan semua jawaban Modul 11 dan membandingkan dengan hasil yang tersimpan.
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null  || { echo "java tidak ditemukan (butuh Java 21+)"; exit 1; }
command -v javac >/dev/null || { echo "javac tidak ditemukan (butuh JDK, bukan JRE)"; exit 1; }
export JAVA_TOOL_OPTIONS= _JAVA_OPTIONS=
mkdir -p hasil-lokal
bersih(){ sed '/^Picked up /d'; }
saring(){ grep -v -e 'BERVARIASI' || true; }
jalan(){ java "$2" 2>&1 | bersih > "hasil-lokal/$1.txt" || true; }
banding(){ if diff <(saring < "hasil-lokal/$1.txt") <(saring < "$2") >/dev/null; then echo "OK    $1"; else echo "BEDA  $1 (diff hasil-lokal/$1.txt $2)"; fi; }

jalan latihan-1 latihan-1/Umur.java;               banding latihan-1 latihan-1/hasil.txt
jalan latihan-1-yyyy latihan-1/YyyyStrict.java;    banding latihan-1-yyyy latihan-1/hasil-yyyy.txt
jalan latihan-2 latihan-2/Rapat.java;              banding latihan-2 latihan-2/hasil.txt
jalan latihan-3 latihan-3/AccessLog.java;          banding latihan-3 latihan-3/hasil.txt
jalan latihan-4 latihan-4/PasswordHasher.java;     banding latihan-4 latihan-4/hasil-passwordhasher.txt
jalan latihan-4-random latihan-4/RandomLemah.java; banding latihan-4-random latihan-4/hasil-randomlemah.txt
jalan latihan-4-sha latihan-4/SHA256Polos.java;    banding latihan-4-sha latihan-4/hasil-sha256polos.txt
jalan latihan-5 latihan-5/Jaringan.java;           banding latihan-5 latihan-5/hasil.txt

echo "--- contoh error kompilasi (sengaja gagal) ---"
for f in latihan-1/contoh-error/KonstruktorPrivat.java latihan-4/contoh-error/TanpaThrows.java; do
  javac -d /tmp/m11x "$f" 2>&1 | bersih | head -2 || true
done
