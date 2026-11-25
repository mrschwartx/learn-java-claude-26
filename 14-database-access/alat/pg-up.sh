#!/usr/bin/env bash
# Menyalakan PostgreSQL SEMENTARA (cluster baru di direktori sementara, port 54329, trust auth, hanya localhost).
# Tidak menyentuh cluster sistem. Butuh binary PostgreSQL server (initdb, pg_ctl). Cetak PGDATA di baris terakhir.
set -euo pipefail
PGBIN=$(ls -d /usr/lib/postgresql/*/bin 2>/dev/null | sort -V | tail -1 || true)
[ -n "$PGBIN" ] || { echo "binary PostgreSQL server tidak ditemukan (initdb)"; exit 2; }
D=${PGDATA_DIR:-/tmp/pg-modul14}
RUN() { if [ "$(id -u)" = 0 ]; then runuser -u postgres -- "$@"; else "$@"; fi; }
if [ -f "$D/postmaster.pid" ] && RUN "$PGBIN/pg_ctl" -D "$D/data" status >/dev/null 2>&1; then echo "sudah berjalan"; echo "$D"; exit 0; fi
rm -rf "$D"; mkdir -p "$D"; [ "$(id -u)" = 0 ] && chown postgres "$D"
RUN "$PGBIN/initdb" -D "$D/data" -A trust -U belajar --no-sync >/dev/null
RUN "$PGBIN/pg_ctl" -D "$D/data" -l "$D/log.txt" -o "-p 54329 -c listen_addresses=127.0.0.1 -c unix_socket_directories=$D -c fsync=on" -w start >/dev/null
for i in $(seq 1 20); do psql -h 127.0.0.1 -p 54329 -U belajar -d postgres -Atc 'select 1' >/dev/null 2>&1 && break; sleep 0.5; done
psql -h 127.0.0.1 -p 54329 -U belajar -d postgres -Atc 'create database toko' >/dev/null
echo "PostgreSQL siap di 127.0.0.1:54329 (user belajar, database toko)"; echo "$D"
