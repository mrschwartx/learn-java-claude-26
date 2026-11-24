# Jawaban Latihan Modul 11

Lingkungan uji: Java 21, tanpa jaringan eksternal. Tanggal acuan dibuat tetap (**24 Nov 2026**) agar hasil deterministik; di aplikasi nyata injeksikan `Clock`.

## Latihan 1 — Umur, hari lahir, ulang tahun berikutnya
- Format `dd/MM/uuuu` + `ResolverStyle.STRICT`. `31/02/2026`, `31/04/2020`, `30/02/1999` ditolak dengan `DateTimeParseException` (`getParsedString()` memberi teks asli). Format salah (`2026-11-01`) juga ditolak.
- Pembanding: resolver default SMART mengubah `31/02/2026` menjadi `2026-02-28` dan `31/04/2020` menjadi `2020-04-30` tanpa peringatan.
- **Jebakan:** STRICT dengan `yyyy` menolak tanggal yang *valid* (`05/03/2026`), karena `yyyy` adalah tahun-era dan era tidak ditentukan. Harus `uuuu` (lihat `YyyyStrict.java`).
- Umur memakai `Period.between`; hari lahir `EEEE` dengan `Locale.of("id","ID")` (mis. 17/05/1998 = Minggu); sisa hari memakai `ChronoUnit.DAYS.between`.
- Kasus tepi: bila acuan tepat hari ulang tahun, hasilnya **0 hari lagi** (bukan 365). Lahir 29 Februari: di tahun non-kabisat dirayakan 28 Februari (perilaku `withYear`; ini pilihan konvensi, ada yang memakai 1 Maret).

## Latihan 2 — Rapat 10.00 Asia/Jakarta
Pada 24 Nov 2026: Makassar 11.00, Jayapura 12.00, New York **22.00 hari sebelumnya** (23 Nov, UTC-05:00).
Pada 24 Jul 2026 New York menjadi 23.00 (UTC-04:00, DST) — selisih ke New York **berubah sepanjang tahun**, ke zona Indonesia tidak (Indonesia tidak memakai DST).

Mengapa `Instant` lebih aman daripada `LocalDateTime`:
- `LocalDateTime` `2026-11-24T10:00` tidak mengatakan 10.00 *di mana*; ditafsirkan di Jakarta dan New York hasilnya `Instant` berbeda 12 jam (03:00Z vs 15:00Z).
- `Instant` adalah satu titik di garis waktu (UTC) dan baru diubah ke zona pengguna saat ditampilkan.
- Peralihan DST menimbulkan jam yang tidak ada (8 Mar 2026 02:30 New York otomatis digeser ke 03:30) dan jam yang muncul dua kali (1 Nov 2026 01:30; Java memilih offset pertama). Jadwal masa depan "10.00 waktu setempat" tetap perlu `ZonedDateTime` + zona; `Instant` untuk kejadian yang sudah terjadi.

## Latihan 3 — Regex access log dan ReDoS
Pola: `^(?<method>GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) (?<path>/\S*) (?<status>[1-5]\d{2}) (?<ms>\d+)ms$`, di-compile sekali (`static final`).
Dari 9 baris, 7 cocok dan **3 berstatus 5xx** (500, 503, 502). Dua baris tak cocok (tanpa format; path tanpa `/`).

**ReDoS** = input yang memicu *catastrophic backtracking* sehingga waktu regex meledak (eksponensial), dan dapat melumpuhkan server.
Demo ini menghitung **jumlah pembacaan karakter** oleh engine (deterministik, bukan waktu):
- `^((a+)+)+$` pada `aaa…a!`: langkah naik **4x tiap n bertambah 2** (1.395 → 23.068.645 pada n=8 → 22).
- `^a+$` (makna sama untuk input ini): hanya n+3 langkah.
- **Catatan jujur:** pola buku teks `^(a+)+$` ternyata *tidak* meledak di JDK 21 (tumbuh linear: 60 → 319 langkah), karena engine modern punya optimasi untuk kasus sederhana. Pola yang saya coba dengan backreference (`^(a+)+\1$`) atau bersarang dua tingkat masih eksponensial. Jangan mengandalkan mitigasi engine: batasi panjang input, hindari pengulangan bersarang, atau gunakan timeout/pustaka berbasis automata.

## Latihan 4 — `PasswordHasher`
- Format `iterasi:salt:hash` (Base64): 310.000 iterasi, salt 16 byte dari `SecureRandom`, hash 32 byte.
- Password sama dihash dua kali menghasilkan string berbeda (salt berbeda), tetapi keduanya lolos `verifikasi`.
- Iterasi disimpan ikut hash, jadi hash lama dengan iterasi lebih rendah tetap bisa diverifikasi saat iterasi dinaikkan di masa depan.
- `verifikasi` memakai `MessageDigest.isEqual` (waktu konstan), menolak format/Base64 rusak tanpa melempar exception, dan menolak iterasi absurd (>5.000.000) agar data tersimpan yang dimanipulasi tidak jadi vektor DoS. Password di-`clearPassword()`/ditimpa setelah dipakai.
- Biaya `[BERVARIASI]`: PBKDF2 sekitar 75 ms per tebakan vs SHA-256 polos sekitar 0,0001 ms — penyerang yang mencoba tebakan jauh lebih lambat (rasio ratusan ribu kali di mesin ini).

**Mengapa SHA-256 polos tidak layak:** terlalu cepat (miliaran tebakan/detik di GPU), dan tanpa salt password sama menghasilkan hash sama. `SHA256Polos.java` memperlihatkan dua pengguna dengan `password123` punya hash identik dan keduanya langsung terbongkar oleh kamus kecil.
**Mengapa `Random` tidak layak:** deterministik. `Random(42)` dua kali menghasilkan urutan yang persis sama (`RandomLemah.java`); keadaan internalnya hanya 48 bit dan dapat dipulihkan dari keluarannya (fakta umum, tidak saya demokan). Pakai `SecureRandom`.
Untuk produksi tetap lebih baik Argon2/bcrypt lewat pustaka teruji (mis. Spring Security).

## Latihan 5 — Jaringan
Ditambahkan `/waktu` (ISO-8601). Server memakai `Clock.fixed` agar keluaran deterministik; juga `/rusak` (500) dan `/lambat` (tidur 3 detik).
- Dua panggilan paralel (`sendAsync`) digabung dengan `CompletableFuture.allOf`; masing-masing future menangani error-nya sendiri (`exceptionally`), sehingga satu kegagalan tidak membatalkan yang lain dan `allOf(...).join()` tidak melempar.
- Status non-2xx (500, 404) ditandai GAGAL dengan kode statusnya — `HttpClient` **tidak** melempar exception untuk 4xx/5xx.
- Timeout per request (`HttpRequest.timeout`, 400 ms) menghasilkan `HttpTimeoutException`; gabungan selesai sekitar 0,4 detik walau server butuh 3 detik (`[BERVARIASI]`).
- Server memakai virtual thread executor agar request lambat tidak menahan yang lain.

## Tidak diuji
Tidak ada pemanggilan ke internet. Algoritma pasca-kuantum (ML-KEM/ML-DSA) dan KDF API Java 25 dari teori tidak dijalankan (butuh Java 24/25).
