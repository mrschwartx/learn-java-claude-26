#!/usr/bin/env bash
# Latihan 5: kompilasi project dua package, lalu ubah method public -> package-private dan amati errornya.
# Dipanggil dari jalankan.sh; bisa juga dijalankan sendiri. Argumen opsional: folder keluaran.
set -euo pipefail
OUT="$(mkdir -p "${1:-$(dirname "$0")}" && cd "${1:-$(dirname "$0")}" && pwd)"
cd "$(dirname "$0")"; SRC="$(pwd)"
rm -rf "$OUT/work" && mkdir -p "$OUT/work" && cp -r "$SRC/toko" "$OUT/work/"
cd "$OUT/work/toko"

{
echo '$ javac -d out src/com/contoh/toko/model/Produk.java src/com/contoh/toko/app/App.java'
javac -d out src/com/contoh/toko/model/Produk.java src/com/contoh/toko/app/App.java 2>&1 | grep -v '^Picked up' || true
echo '$ java -cp out com.contoh.toko.app.App'
java -cp out com.contoh.toko.app.App 2>&1 | grep -v '^Picked up'
echo
echo '$ find out -name "*.class"'
find out -name '*.class' | sort
} > "$OUT/5a-berhasil.txt"

# Ubah getNama() dari public menjadi package-private (hapus kata 'public')
sed -i 's/public String getNama()/String getNama()/' src/com/contoh/toko/model/Produk.java
{
echo '$ sed -i "s/public String getNama()/String getNama()/" Produk.java     # public -> package-private'
echo '$ javac -d out2 src/com/contoh/toko/model/Produk.java src/com/contoh/toko/app/App.java'
javac -d out2 src/com/contoh/toko/model/Produk.java src/com/contoh/toko/app/App.java 2>&1 | grep -v '^Picked up' || true
} > "$OUT/5b-error-package-private.txt"

cd "$SRC" && rm -rf "$OUT/work"
echo "Latihan 5 selesai: $OUT/5a-berhasil.txt dan $OUT/5b-error-package-private.txt"
