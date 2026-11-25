# Jawaban Latihan — Modul 15

> Catatan jujur: JUnit/Mockito/JaCoCo/Spring/Testcontainers/JMeter tidak tersedia di sandbox. Semua yang "dijalankan" memakai alat mini buatan sendiri (`alat/`); versi memakai alat asli ditulis tetapi **tidak dijalankan**. Lihat `README.md`.

## Latihan 1 — TDD `Password.kuat`
Aturan: minimal 8 karakter, ada huruf besar, huruf kecil, dan angka.

1. **Merah**: `latihan-1/PasswordTest.java` (10 kasus, termasuk batas 7 vs 8 karakter, tanpa besar/kecil/angka, string kosong, `null`) dijalankan terhadap `merah/Password.java` yang selalu `false` → **7 lulus, 3 gagal** (`hasil-merah.txt`).
2. **Hijau**: `hijau/Password.java` → **10 lulus, 0 gagal** (`hasil-hijau.txt`).
3. Versi JUnit 6 (`@ParameterizedTest` + `@CsvSource`) ada di `latihan-1/junit/` — tidak dijalankan.

Pelajaran: stub "selalu false" tetap melewati 7 kasus karena kasus-kasus negatif memang bernilai `false`. Test merah yang berguna harus mencakup kasus positif; 3 kasus `true` itulah yang memaksa implementasi nyata.

## Latihan 2 — `Clock.fixed` dan hari Jumat
`latihan-2/MiniUjiModul.java` menambahkan test: pada Jumat harga penuh, hari lain diskon berlaku, dengan `Clock.fixed(...)` (7 lulus; 1 gagal disengaja untuk memperlihatkan laporan merah). `DiskonServiceBuruk` memakai `LocalDate.now()` sebagai pembanding.

**Mengapa menginjeksi `Clock` lebih baik daripada `LocalDate.now()`?**
- *Repeatable/deterministik*: test dengan `Clock.fixed` memberi hasil sama pada hari apa pun; versi `LocalDate.now()` lulus/gagal bergantung pada hari eksekusi (flaky, mis. hanya gagal tiap Jumat atau di CI akhir pekan).
- *Bisa menguji semua cabang waktu* (Jumat, akhir bulan, pergantian tahun, zona waktu) tanpa menunggu atau mengubah jam sistem.
- *Dependency eksplisit*: waktu menjadi masukan yang tampak di konstruktor, sejalan dengan DI (Modul 13), dan produksi cukup memakai `Clock.systemDefaultZone()`.

## Latihan 3 — `RekeningBank`: test, coverage, mutation
Kode dari Modul 4 disalin ke `latihan-3/asli/` (logika tidak diubah; hanya ditambah `getSaldo()` agar bisa diuji). `RekeningBankTest` mencakup setor, tarik, tarik tepat sebesar saldo, saldo tidak cukup (saldo tetap + pesan tercetak), jumlah nol/negatif (melempar `IllegalArgumentException`), `toString`, konstruktor, dan penghitung.

**Temuan bug nyata pada kode materi** (`hasil.txt` bagian 1): `tarik(-50)` dan `tarik(0)` tidak ditolak, dan `tarik(-50)` justru **menambah** saldo (`saldo -= -50`). Test gagal: 10 lulus, 2 gagal. Perbaikan di `latihan-3/perbaikan/` (`tarik` menolak `jumlah <= 0`) → 12 lulus, 0 gagal. Versi JUnit: `latihan-3/junit/` (tidak dijalankan).

**Coverage** (instrumentasi baris buatan sendiri, bukan JaCoCo): suite **lemah** dan suite **kuat** sama-sama **100%** (11 dari 11 baris pernyataan). Untuk JaCoCo asli: tambahkan `jacoco-maven-plugin` (goal `prepare-agent` dan `report`), jalankan `mvn test`, buka `target/site/jacoco/index.html` — tidak dijalankan di sini.

**Mengapa coverage 100% belum menjamin tidak ada bug?** Coverage hanya mengukur kode yang *dieksekusi*, bukan yang *diverifikasi*. Bukti di `hasil.txt` bagian 4 (mutation testing mini, 7 mutan: batas `<=`→`<`, tanda `+=`→`-=`, hapus `return`, hapus pesan, dst.):

| Suite | Coverage baris | Mutan mati |
|---|---|---|
| Lemah (menjalankan semua jalur, tanpa asersi) | 100% | **0/7** |
| Kuat | 100% | **7/7** |

Alasan lain: cakupan baris tidak melihat cabang (`if (...) throw` terhitung "kena" walau `throw` tak pernah jalan), tidak menjamin kasus batas/masukan tak terduga, dan tidak mendeteksi perilaku yang tidak ditulis (requirement yang terlewat — persis bug `tarik(-50)` yang lolos di kode asli). Pelengkapnya: mutation testing (PIT), uji batas, property-based test, code review.

## Latihan 4 — Mock vs Fake untuk `PesananService`
Domain di `latihan-4/Domain.java` (`Repositori`, `Notifikasi`, `KursClient`, `PesananService` + `hargaRupiah`; fake `RepositoriFake`, `NotifikasiFake`).

- **Test dengan mock + verify** (MiniMock; padanan Mockito `verify(repo).simpan("kopi")`, `doThrow`, `verifyNoInteractions`, stub kurs): 3 test lulus.
- **Test dengan fake buatan sendiri** untuk `Repositori` (periksa `repo.data`, bukan interaksi): 3 test lulus.
- Versi Mockito asli: `latihan-4/mockito/` (tidak dijalankan).

**Perbandingan** (`hasil.txt`, bagian refaktor): saya merefaktor service menjadi V2 yang memanggil `repo.ada(item)` dulu agar item sama tidak tersimpan dua kali (perilaku luar tak berubah). Test mock yang menuntut daftar panggilan persis `[simpan[kopi]]` **gagal** (`[ada[kopi], simpan[kopi]]`), sedangkan test fake tetap lulus, dan perilaku baru ("kopi dua kali → tersimpan sekali") mudah diuji dengan fake.

| | Mock | Fake |
|---|---|---|
| Memeriksa | interaksi (siapa memanggil apa) | hasil/state |
| Terhadap refaktor | rapuh bila terlalu spesifik | tahan |
| Biaya | murah, tanpa kelas tambahan | perlu menulis & merawat kelas |
| Cocok untuk | efek samping tanpa state (kirim email/notifikasi, "tidak boleh dipanggil"), men-stub kegagalan/dependency eksternal | dependency dengan perilaku (repositori, cache, antrean) yang dipakai banyak test |

**Kapan memilih mock?** Saat yang penting adalah *bahwa* panggilan terjadi (notifikasi terkirim, tidak terkirim saat gagal), atau saat dependency sulit ditiru (jaringan, kegagalan khusus). **Pilih fake** bila dependency punya state/aturan dan Anda ingin menguji hasil tanpa terikat urutan pemanggilan.

**Mengapa jangan me-mock `record` atau `List`?** Keduanya *value object*/tipe data murni tanpa efek samping dan tanpa dependency: membuat instans asli murah, cepat, deterministik. Me-mock membuat test mengunci detail implementasi (berapa kali `get/add` dipanggil), menguji mock bukan kode, dan bisa menyembunyikan bug (mock `List` tidak berperilaku seperti `List` sungguhan: urutan, duplikat, `equals`). `record` juga `final` sehingga mock butuh konfigurasi tambahan. Pakai objek asli (`List.of(...)`, `new Produk(...)`); mock hanya batas sistem (repositori, klien HTTP, email).

## Latihan 5 — Integration test + load test
**Yang dijalankan (pengganti)**: `ProdukApp` (JDK `HttpServer`) menggantikan aplikasi Spring dan menjaga kontrak `POST /api/produk` Modul 13 (201 + `Location` + JSON; 400 bila nama kosong/harga ≤ 0), menyimpan ke **PostgreSQL 16 sungguhan** lewat JDBC (driver mainan Modul 14). `ProdukApiTest` memakai HTTP sungguhan: 7 test lulus (`hasil-integrasi.txt`) — 201 + Location + baris ada di database, 4 kasus 400 (tanpa baris baru), 405 untuk GET, dan CHECK constraint database menolak harga negatif.

**Versi sesungguhnya (tidak dijalankan)**: `spring/ProdukApiIT.java` memakai `@SpringBootTest(RANDOM_PORT)` + REST Assured, dengan `@Testcontainers` + `@Container @ServiceConnection PostgreSQLContainer` agar DB diganti container (butuh Docker). `spring/pom-potongan.xml` memuat dependensi + Failsafe (`mvn verify`). `spring/produk.jmx` rencana JMeter minimal (tidak divalidasi); jalankan `jmeter -n -t produk.jmx -Jthreads=20 -Jloop=100 -l hasil.jtl -e -o laporan/`.

**Hasil load test mini** (`hasil-beban.txt`; 2 CPU, JVM, server, klien, dan PostgreSQL di mesin yang sama; tiap request membuka koneksi baru tanpa pool; setiap INSERT autocommit dengan `fsync=on`) **[BERVARIASI]**:

| Thread | Request | Throughput (req/s) | p50 (ms) | p95 (ms) | p99 (ms) | Error % |
|---:|---:|---:|---:|---:|---:|---:|
| 1 | 100 | 20 | 48.1 | 55.7 | 57.7 | 0.0 |
| 5 | 500 | 103 | 48.0 | 55.4 | 59.3 | 0.0 |
| 10 | 1000 | 200 | 48.3 | 59.6 | 67.5 | 0.0 |
| 20 | 2000 | 326 | 56.1 | 92.1 | 132.1 | 0.0 |

Pembacaan: latensi dasar ~48 ms hampir konstan sampai 10 thread dan throughput naik hampir linear — ciri waktu tunggu I/O (kemungkinan besar fsync commit WAL + pembukaan koneksi), **bukan** CPU; ini dugaan, tidak diukur terpisah. Pada 20 thread dengan hanya 2 CPU, p95 melonjak ke 92 ms dan p99 ke 132 ms sementara p50 hanya sedikit naik: ekor latensi mulai terdampak antrean/kontensi, itulah alasan membaca persentil dan bukan rata-rata. Karena ini bukan JMeter, bukan Spring, dan semua di satu mesin, angkanya hanya ilustrasi metode, bukan tolok ukur aplikasi sesungguhnya.
