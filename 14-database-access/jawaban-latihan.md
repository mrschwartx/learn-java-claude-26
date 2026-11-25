# Jawaban Latihan Modul 14

Lingkungan: Java 21, **PostgreSQL 16.15 asli** (cluster sementara, `fsync=on`), driver JDBC mainan buatan saya, tanpa Maven Central.
**[DIJALANKAN]** = keluaran asli ada di berkas hasil. **[TIDAK DIJALANKAN]** = ditulis dari materi/dokumentasi, belum dieksekusi.
Hal yang saya ketahui tentang perilaku Hibernate/Spring (bukan hasil percobaan di sini) saya tandai *(pengetahuan, tidak diuji)*.

## Latihan 1 — JOIN + GROUP BY dan `PreparedStatement` **[DIJALANKAN]**
`DaftarPelanggan.java` mencetak tiap pelanggan dengan jumlah pesanan dan total belanja memakai `LEFT JOIN ... GROUP BY`: Ani 2 pesanan Rp125.000, Dewi 1 pesanan Rp120.000, Candra 1 pesanan Rp20.000. Saya menambahkan pelanggan **Eka tanpa pesanan** untuk membuktikan `LEFT JOIN` tetap menampilkannya (0 pesanan, Rp0); `INNER JOIN` akan menghilangkannya.

**Mengapa `PreparedStatement`, bukan konkatenasi** (dibuktikan di PostgreSQL asli):
- Input `x' OR '1'='1` pada konkatenasi menghasilkan SQL `... WHERE nama = 'x' OR '1'='1'` dan mengembalikan **3 baris (semua pelanggan bocor)**; sebagai parameter hanya dicari nama literal itu → **0 baris**.
- Injeksi yang merusak: `DELETE FROM korban WHERE id = 1; DROP TABLE korban; --` lewat konkatenasi **benar-benar menghapus tabel** (`tabel korban masih ada? false`, diuji pada tabel cadangan, bukan tabel toko). Nilai yang sama sebagai parameter ditolak server (bukan bilangan bulat) dan tidak ada yang dieksekusi.
- Alasannya: parameter dikirim terpisah dari teks SQL (protokol extended: `Parse` lalu `Bind`), sehingga data tidak pernah ditafsirkan sebagai SQL. Catatan: perilaku ini juga berlaku pada driver mainan karena ia memakai protokol yang sama dengan driver resmi.

## Latihan 2 — Transfer dengan riwayat dalam satu transaksi **[DIJALANKAN]**
Tabel `riwayat_transfer` ditambahkan (`schema.sql`); `INSERT` riwayat berada di transaksi yang sama dengan kedua `UPDATE`. Total uang (saldo) dicetak tiap tahap sebagai pemeriksaan invarian (600.000):
- A. Sukses Andi→Budi 150.000: Andi 350.000, Budi 250.000, 1 baris riwayat.
- B. Saldo tidak cukup: dibatalkan, tidak ada perubahan.
- C. Exception manual **setelah UPDATE pertama**, dengan transaksi: `rollback` → saldo tetap 350.000/250.000, riwayat tetap 1.
- D. Kegagalan yang sama dengan **autocommit**: Andi turun 50.000 tetapi Budi tidak naik, total menjadi 550.000 — **uang hilang**. Ini memperlihatkan mengapa transaksi diperlukan.
Bukti dari sesi `psql` terpisah (di akhir `hasil.txt`) cocok dengan keluaran program.

## Latihan 3 — Batch vs satuan **[DIJALANKAN]**
1.000 insert pelanggan, tiga cara (hasil terakhir; semuanya `[BERVARIASI]`, tergantung disk dan beban mesin):
| Cara | Waktu (dua kali jalan) |
|---|---|
| (a) `executeUpdate` satu-satu, **autocommit** | sekitar 270–390 ms |
| (b) satu-satu dalam **satu transaksi** | sekitar 40–60 ms |
| (c) `addBatch`/`executeBatch` (satu transaksi) | sekitar 27 ms |
Ketiganya menyimpan tepat 1.000 baris.
Penjelasan (dan mengapa saya menyertakan (b)): perbedaan (a)→(b) berasal dari **commit per statement** (tiap commit memaksa flush WAL ke disk, `fsync`); perbedaan (b)→(c) berasal dari **round trip jaringan**: batch mengirim banyak perintah tanpa menunggu balasan satu per satu dan menyinkronkan sekali. Catatan jujur: batch pada driver mainan adalah pipelining sederhana; driver resmi bisa lebih optimal (mis. `reWriteBatchedInserts`). Angka ini dari satu sandbox kecil, bukan benchmark.

## Latihan 4 — Optimistic locking
**[TIDAK DIJALANKAN]** `latihan-4/jpa-toko`: `@Version` pada `Pelanggan`, migrasi Flyway `V1__init.sql` dengan kolom `version`, dan `OptimisticLockTest` yang mengharapkan `ObjectOptimisticLockingFailureException` saat `salinan2` disimpan dengan versi kedaluwarsa. Hanya pembacaan manual; tidak dikompilasi (butuh dependency).

**[DIJALANKAN]** mekanisme SQL yang dipakai `@Version` (`LockingDemo.java`, tabel `pelanggan_v` dengan kolom `version`):
- A. T1 dan T2 sama-sama membaca versi 0. T1 `UPDATE ... WHERE id=? AND version=0` sukses (versi jadi 1). T2 menjalankan UPDATE yang sama dengan `version=0` → **0 baris terubah** → dianggap konflik (analog `OptimisticLockException`). Nama "Ani Lestari" dari T1 tidak tertimpa.
- B. Tanpa kolom versi (update buta), update T2 **menimpa diam-diam**: perubahan T1 hilang tanpa error.
- C. **Pessimistic**: T1 `SELECT ... FOR UPDATE` mengunci baris; T2 dengan `FOR UPDATE NOWAIT` langsung ditolak dengan SQLState **55P03**; setelah T1 commit, T2 bisa mengunci.

**Kapan optimistic lebih tepat** (alasan umum, bukan hasil pengujian): konflik jarang (mis. pengguna jarang mengedit data yang sama), transaksi panjang atau melibatkan "pikir dulu" pengguna (form edit di web) karena tidak menahan kunci database, dan skala besar (tidak ada kunci yang ditahan). **Pessimistic** lebih tepat saat konflik sering (rebutan stok/saldo yang sama) atau ulangan transaksi mahal/berisiko (penarikan uang); harganya antrean/blokir dan risiko deadlock.

## Latihan 5 — N+1 **[DIJALANKAN di JDBC; JPA TIDAK DIJALANKAN]**
`NPlusSatu.java`: 20 pelanggan Surabaya dengan 3 pesanan masing-masing. Pola yang dihasilkan lazy loading dihitung sebagai jumlah statement di server:
- **N+1:** 1 query pelanggan + 1 query pesanan per pelanggan = **21 query**.
- **JOIN (setara `join fetch`/`@EntityGraph`): 1 query.**
- **Batch fetching** (`WHERE pelanggan_id = ANY(...)`, 20 id per query): **2 query**.
Ketiganya menghasilkan total sama (Rp1.200.000). Dengan 1.000 pelanggan: 1.001 vs 1 vs 51 query.
Peringatan: ini **emulasi pola SQL di JDBC**, bukan Hibernate yang sebenarnya; jumlah query Hibernate sebenarnya harus Anda lihat di log `org.hibernate.SQL`.

**[TIDAK DIJALANKAN]** `jpa-toko` memuat ketiga perbaikan: `@Query("... left join fetch ...")` (`cariDenganPesanan`), `@EntityGraph(attributePaths = "pesanan")` (`findWithPesananByKota`), dan `spring.jpa.properties.hibernate.default_batch_fetch_size: 20`.

**Mengapa `ddl-auto: update` tidak boleh di production** *(dari materi)*: Hibernate mengubah skema otomatis tanpa kontrol/peninjauan/rollback (bisa menambah kolom salah, tidak menghapus kolom lama, tidak menangani migrasi data), dan skema tidak tercatat di version control. Gunakan `validate`/`none` dan migrasi berversi dengan Flyway atau Liquibase (`V1__init.sql` sudah ada di proyek).

## Latihan 6 — Ebean
**[TIDAK DIJALANKAN]** `latihan-6/demo-ebean`: `pom.xml` (versi Ebean dibiarkan kosong seperti materi), entity `Pelanggan` dengan field `email`, `Pesanan`, dan `DemoEbean6` dengan query bean `new QPesanan().total.gt(100_000)`, agregasi native, serta dua loop lazy vs `fetch("pesanan")`. Nama field query bean dihasilkan annotation processor dan belum diverifikasi.

**[DIJALANKAN]** di PostgreSQL asli (`ColumnDanQuery.java`):
- (a) `ALTER TABLE pelanggan ADD COLUMN email VARCHAR(150)` lalu `UPDATE` untuk Ani; baris lain `null`.
- (b) Total pesanan **> 100.000**: Ani Rp125.000 dan Dewi Rp120.000 (agregasi `GROUP BY ... HAVING`). Berdasarkan **pesanan tunggal** > 100.000: hanya Dewi (Rp120.000). Dua pertanyaan ini berbeda; `QPesanan.total.gt(...)` menjawab yang kedua, sedangkan yang pertama butuh agregasi (SQL native).
- (c) Perbandingan SQL dengan/tanpa `fetch("pesanan")`: tidak dapat diukur tanpa Ebean; pola SQL-nya sama dengan Latihan 5 (satu query join vs query tambahan saat relasi diakses). Materi menyebut Ebean memuat relasi lazy dalam batch kecil sehingga lebih ringan daripada N+1 murni *(dari materi, tidak diuji)*.

**Dua perbedaan utama Ebean vs JPA yang paling memengaruhi cara menulis kode** *(dari materi)*:
1. **Tanpa sesi/persistence context:** tidak ada `EntityManager`, state *managed/detached*; object di-`save()` kapan saja, tanpa `@Transactional` wajib untuk dirty checking atau pusing `LazyInitializationException`.
2. **Query bean type-safe + langkah build tambahan:** salah nama field tertangkap compiler, tetapi build butuh annotation processor dan enhancement bytecode (di IDE perlu plugin enhancement).

## Tidak diuji
Hibernate/JPA/Spring Data (kompilasi dan eksekusi), Ebean, HikariCP (driver mainan tidak menyediakan pool; `ds.getConnection()` membuka koneksi baru), serta isolation level 14.7 (teori tidak diminta latihan).
