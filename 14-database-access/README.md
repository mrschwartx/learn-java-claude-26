# Modul 14 — Database Access: JDBC, Hibernate & Spring Data JPA: Jawaban Latihan

**Baca dulu — ada dua keterbatasan sandbox:**
1. Tidak ada akses ke Maven Central, jadi **tidak ada driver JDBC resmi, Hibernate, Spring Data JPA, maupun Ebean**. Bagian itu ditulis tetapi **TIDAK DIJALANKAN**.
2. Yang ada adalah **server PostgreSQL 16 asli** (terpasang di sandbox, dinyalakan sementara oleh `alat/pg-up.sh` di port 54329). Agar kode JDBC bisa benar-benar dijalankan, saya menulis **driver JDBC mainan** (`alat/minipg/MiniPg.java`, bicara protokol PostgreSQL langsung). Ini **bukan driver produksi**; di mesin Anda pakai `org.postgresql:postgresql`. Kode latihan memakai API `java.sql` biasa dan URL `jdbc:postgresql://...`; satu-satunya baris khusus adalah `Class.forName("MiniPg")`.

| Latihan | Status | Berkas |
|---|---|---|
| 1. JOIN + GROUP BY, `PreparedStatement` vs SQL injection | **Dijalankan** (PostgreSQL asli + driver mainan) | `latihan-1/` |
| 2. Transfer + `riwayat_transfer` dalam satu transaksi, simulasi gagal | **Dijalankan** (+ bukti `psql`) | `latihan-2/` |
| 3. 1.000 INSERT: batch vs satuan | **Dijalankan** | `latihan-3/` |
| 4. `@Version`/`OptimisticLockException`, optimistic vs pessimistic | Proyek JPA **TIDAK dijalankan**; mekanisme SQL-nya **dijalankan** (`version` manual, `FOR UPDATE NOWAIT`) | `latihan-4/` |
| 5. N+1 vs `join fetch`/`@EntityGraph`/batch fetching | JPA **TIDAK dijalankan**; pola N+1 dan tiga perbaikannya dihitung di JDBC | `latihan-5/` |
| 6. Ebean: kolom `email`, query, perbandingan SQL | Ebean **TIDAK dijalankan**; `ALTER TABLE` dan SQL setaranya **dijalankan** | `latihan-6/` |

Penjelasan: `jawaban-latihan.md`.

## Yang perlu Anda lakukan
```bash
./jalankan.sh     # menyalakan PostgreSQL sementara, menjalankan latihan 1-6, membandingkan hasil, lalu mematikannya
```
Butuh JDK 21+, binary server PostgreSQL (`initdb`, `pg_ctl`) dan `psql`. Jika Anda memakai Docker Compose dari materi, jalankan `schema.sql` di database `toko`, ganti `Class.forName("MiniPg")` dengan driver resmi, dan arahkan URL ke `localhost:5432`.
