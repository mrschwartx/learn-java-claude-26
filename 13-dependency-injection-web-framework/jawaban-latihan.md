# Jawaban Latihan Modul 13

Lingkungan: Java 21, tanpa Maven Central. **[DIJALANKAN]** = keluaran asli ada di berkas hasil. **[TIDAK DIJALANKAN]** = ditulis dari materi/dokumentasi, belum dieksekusi.
Hal yang saya ketahui tentang perilaku Spring (bukan hasil percobaan di sini) saya tandai **(pengetahuan Spring, tidak diuji)**.

## Latihan 1 — `Pembayaran` dengan dua implementasi **[DIJALANKAN]**
**Prediksi:** `MiniContainer` memakai `findFirst()`, jadi bila `PembayaranKartu` dan `PembayaranTransfer` terdaftar bersamaan, **yang terdaftar lebih dulu menang, tanpa peringatan**. Terbukti: urutan (Kartu, Transfer) menghasilkan "kartu kredit"; urutan dibalik menghasilkan "transfer bank" untuk kode yang sama persis. Ini berbahaya karena perilaku bergantung urutan pendaftaran.

`latihan-1/DemoPembayaran.java` menambahkan container yang:
- melempar `Ambigu: 2 bean untuk Pembayaran [PembayaranKartu, PembayaranTransfer]; beri @Utama pada satu, atau pakai @Nama`;
- menyelesaikan dengan **`@Utama`** pada satu implementasi (meniru `@Primary`: dompet digital terpilih), atau **`@Nama("transfer")`** pada parameter constructor (meniru `@Qualifier`);
- memberi pesan jelas bila nama tidak ada.

**Di Spring** *(pengetahuan Spring, tidak diuji)*: dua bean bertipe sama membuat injeksi gagal saat start dengan `NoUniqueBeanDefinitionException`. `@Primary` pada salah satu bean menjadikannya pilihan default; `@Qualifier("nama")` pada titik injeksi memilih bean tertentu, dan menang atas `@Primary`.

## Latihan 2 — Dependensi melingkar **[DIJALANKAN]**
- Container lama, A butuh B dan B butuh A: **`StackOverflowError`** tanpa petunjuk bean mana.
- Container baru mencatat bean yang sedang dibuat (`LinkedHashSet`) dan melaporkan jalurnya: `Dependensi melingkar: A -> B -> A` dan `C -> D -> E -> C`.
- Dua uji penting: mengambil bean yang sama dua kali **tidak** dianggap siklus, dan container tetap bisa dipakai setelah gagal (jalur dibersihkan di `finally`).

**Mengapa constructor injection membuatnya langsung ketahuan:** objek tidak dapat dibuat sebelum semua argumen constructor ada, sehingga siklus membuat pembuatan A menunggu B yang menunggu A — gagal seketika saat container dibangun (fail-fast), bukan pada request ke-1000. Dengan setter/field injection objek dapat dibuat dulu lalu dihubungkan, sehingga siklus bisa "lolos" diam-diam *(pengetahuan Spring, tidak diuji: Spring dapat mengatasi siklus setter singleton dengan referensi awal, dan Spring Boot sejak 2.6 melarang referensi melingkar secara default)*.

## Latihan 3 — Constructor vs field injection **[DIJALANKAN]**
Tiga alasan, masing-masing dibuktikan di `DemoInjeksi`:
1. **Dependency tersembunyi.** `new ServiceField()` lolos kompilasi tetapi `NullPointerException` baru muncul saat dipakai; `ServiceCtor` menolak dependency `null` langsung saat dibuat, dan tidak bisa dibuat tanpa argumen sama sekali (tanda tangan constructor mendokumentasikan kebutuhannya).
2. **Immutabilitas.** Field yang diinjeksi tidak bisa `final` (hasil: `false`); field constructor `final` (hasil: `true`). Objek tidak bisa berubah dependency-nya setelah dibuat dan aman dibagi antar-thread.
3. **Uji tanpa container.** Field private hanya bisa diisi lewat reflection (`setAccessible(true)`), sedangkan constructor cukup `new ServiceCtor(repoFake, notifFake)`.

Uji manual `PesananService` tanpa container atau Mockito, memakai fake buatan tangan: **6 uji lulus** (item tersimpan dan berurutan, notifikasi terkirim, null ditolak, kegagalan repo diteruskan, notifikasi tidak terkirim bila simpan gagal).

## Latihan 4 — Spring Boot: PUT, DELETE, validasi baru
**[TIDAK DIJALANKAN]** `latihan-4/spring-toko`: lengkap (pom, aplikasi, `ProdukService.ganti/hapus`, controller `PUT` → 200/404, `DELETE` → 204/404, `@Size(max = 50)` sebagai aturan baru pada `ProdukRequest`, `@RestControllerAdvice` `ProblemDetail`). Hanya sintaks Java yang saya periksa dengan `javac` (tidak ada galat sintaks); nama paket/starter mengikuti materi (Boot 4.1.0) dan belum dicoba — rujuk dokumentasi versi Anda. Saya tidak menulis test Spring karena nama paket test Boot 4 belum bisa saya verifikasi.

**[DIJALANKAN]** `TokoStandIn.java` (**bukan Spring**) menguji kontrak yang sama, **16 uji lulus**: GET daftar/detail, POST valid → 201 + `Location`, 404, 400 dengan dua galat, nama 51 karakter → 400 (aturan baru) dan 50 karakter → 201 (batas), PUT valid → 200 dan nilai berubah, PUT id tidak ada → 404, PUT tidak valid → 400, DELETE → 204 lalu 404, id bukan angka → 400.
Keputusan desain yang terbukti di stand-in dan mengikuti perilaku `@Valid` Spring: validasi dijalankan **sebelum** pencarian, sehingga `PUT` tidak valid ke id yang tidak ada menghasilkan 400 (bukan 404). `PUT` memakai `computeIfPresent` agar atomik. *(Catatan: pada Spring, `long harga` primitif yang hilang dari JSON menjadi 0 sehingga `@Positive` gagal; pesannya sama dengan stand-in.)*

## Latihan 5 — Javalin vs Spring Boot
**[TIDAK DIJALANKAN]** `latihan-5/javalin-demo` (`GET /halo`, `GET /produk/{id}` + 4 test `javalin-testtools`) dan `latihan-5/spring-halo` (endpoint yang sama).

Angka yang **benar-benar saya ukur**:
| | Javalin | Spring Boot | Stand-in JDK (diukur) |
|---|---|---|---|
| Baris kode Java (tanpa kosong/komentar) | 21 | 22 | 41 |
| Baris `pom.xml` | 17 | 11 | — |
| Waktu startup | **tidak diukur** | **tidak diukur** | server hidup dalam sekitar 28 ms di JVM yang sudah jalan; proses penuh sekitar 0,9–1,0 detik (`[BERVARIASI]`) |
| Ukuran artefak | **tidak diukur** | **tidak diukur** | jar 4.021 byte (tanpa dependency) |

Peringatan: stand-in 41 baris karena routing, parsing path dan respons ditulis tangan; itu tidak mewakili Javalin atau Spring. Jumlah baris Javalin dan Spring hampir sama untuk dua endpoint sederhana; perbedaan nyata muncul pada skala, bukan pada contoh sekecil ini. Waktu startup dan ukuran artefak untuk Spring/Javalin **tidak saya klaim** karena tidak bisa dibangun di sini; ukurlah sendiri dengan `mvn package` dan `time java -jar`.
Kemudahan menambah fitur *(dari materi, bukan pengukuran)*: Spring Boot menyediakan validasi (`spring-boot-starter-validation`), keamanan (Spring Security), dan akses database (Data JPA) sebagai starter siap pakai dengan auto-configuration; Javalin lebih sedikit batasan dan "ajaib", tetapi validasi, DI, dan akses data Anda rakit sendiri dari library lain.

## Latihan 6 — `POST /produk` di Javalin
**[TIDAK DIJALANKAN]** `latihan-6/javalin-post`: `ProdukApp` menerima `{"nama": "..."}`, mengembalikan **201** + JSON, **400** (`BadRequestResponse`) bila `nama` kosong, plus dua test `JavalinTest`. Sintaks `ctx.bodyAsClass`/`ctx.status(...).json(...)` mengikuti dokumentasi Javalin 6 dan belum dicoba.

**[DIJALANKAN]** `PostStandIn.java` (**bukan Javalin**): **10 uji lulus** — 201 dan isi respons, 400 untuk nama kosong/spasi/field hilang/bukan JSON, dan service tidak dipanggil untuk permintaan tidak valid. Bagian DI-nya nyata: service dapat diganti **fake** lewat constructor (fake dipanggil tepat 1 kali dan id 777 dari fake muncul di respons).

**Menyuntikkan `ProdukService` di Javalin:** Javalin tidak punya DI container; `main()` membuat `new ProdukApp(new ProdukService())` — constructor injection **manual**, dan `main()` adalah "container"-nya. Di Spring Boot cukup mendeklarasikan constructor `ProdukController(ProdukService)` dan `@Service` pada service; container yang membuat dan menyambungnya, mengelola scope (singleton default), siklus hidup, dan profil. Konsekuensi: Javalin lebih eksplisit dan transparan untuk aplikasi kecil, tetapi perangkaian manual membengkak di aplikasi besar (itulah alasan container ada, lihat `MiniContainer` di Latihan 1–2).

## Tidak diuji
Quarkus dan Play (hanya contoh materi, tidak ada latihan yang memintanya). Semua kode proyek Spring/Javalin yang disebut TIDAK DIJALANKAN di atas.
