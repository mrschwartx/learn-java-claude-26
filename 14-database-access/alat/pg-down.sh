#!/usr/bin/env bash
set -uo pipefail
PGBIN=$(ls -d /usr/lib/postgresql/*/bin 2>/dev/null | sort -V | tail -1 || true)
D=${PGDATA_DIR:-/tmp/pg-modul14}
RUN() { if [ "$(id -u)" = 0 ]; then runuser -u postgres -- "$@"; else "$@"; fi; }
[ -n "$PGBIN" ] && RUN "$PGBIN/pg_ctl" -D "$D/data" -m fast stop >/dev/null 2>&1
rm -rf "$D"; echo "PostgreSQL sementara dimatikan dan dihapus"
