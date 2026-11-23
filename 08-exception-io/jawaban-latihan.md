# Jawaban Latihan — Modul 8: Exception Handling & I/O

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran deterministik (file besar di Latihan 4 dibuat dari rumus tetap, bukan acak). Untuk Latihan 3, coba tebak dulu exception mana yang "menang" di tiap skenario sebelum membaca hasilnya.

---

## Latihan 1 — `ambilUmur` dan loop sampai valid

Kode: `latihan-1/Umur.java`. Masukan: `masukan.txt`, `masukan-habis.txt`. Keluaran: `hasil.txt`, `hasil-habis.txt`.

```java
static int ambilUmur(String input) {
    if (input == null || input.isBlank())
        throw new IllegalArgumentException("umur tidak boleh kosong");
    String teks = input.strip();
    int umur;
    try {
        umur = Integer.parseInt(teks);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("umur harus berupa bilangan bulat, bukan \"" + teks + "\"", e);
    }
    if (umur < 0 || umur > 150)
        throw new IllegalArgumentException("umur harus di antara 0 dan 150, bukan " + umur);
    return umur;
}
```

Masukan uji (satu per baris) dan hasilnya:

| Masukan | Hasil |
|---|---|
| `abc` | Ditolak: umur harus berupa bilangan bulat, bukan "abc" |
| *(kosong)* | Ditolak: umur tidak boleh kosong |
| `-5` | Ditolak: umur harus di antara 0 dan 150, bukan -5 |
| `200` | Ditolak: ... bukan 200 |
| `3.5` | Ditolak: harus bilangan bulat, bukan "3.5" |
| `99999999999` | Ditolak (melebihi rentang `int`, `parseInt` melempar `NumberFormatException`) |
| `   30   ` | **Umur valid: 30** (spasi di tepi dibuang oleh `strip()`) |

Poin desain:

- **Pesan yang berguna menyertakan nilai yang salah** (prinsip *fail fast* dan pesan informatif dari modul): `bukan -5`, bukan sekadar "umur tidak valid".
- **`NumberFormatException` diterjemahkan** menjadi `IllegalArgumentException` dengan pesan bahasa kita, dan penyebab aslinya diteruskan sebagai `cause` (`new IllegalArgumentException(pesan, e)`), supaya info teknisnya tidak hilang. (`NumberFormatException` sendiri turunan `IllegalArgumentException`, jadi `catch (IllegalArgumentException e)` di `main` menangkap keduanya.)
- **Loop keluar dengan `break`, bukan dengan exception.** Aturan modul: jangan memakai exception untuk alur normal. Exception di sini hanya menandai masukan buruk; keberhasilan mengakhiri loop secara biasa.
- **Masukan yang habis** (`masukan-habis.txt`: dua masukan salah lalu selesai) ditangani: `Scanner.nextLine()` melempar `NoSuchElementException` saat tidak ada baris lagi. Tanpa `catch` itu, program berhenti dengan stack trace; dengan `catch` ia pamit dengan pesan jelas. Kasus ini sering terlewat, padahal terjadi pada Ctrl+D atau input yang dialirkan dari file.
- `Integer.parseInt` menerima tanda `+` di depan (`+30` lolos). Bila itu tidak diinginkan, tambahkan pemeriksaan sendiri.

---

## Latihan 2 — `StokHabisException`: checked vs unchecked

Kode: `latihan-2/DemoGudang.java`. Keluaran: `latihan-2/hasil.txt`. Error kompilasi: `latihan-2/error-kompilasi.txt`.

### Versi checked

```java
class StokHabisException extends Exception { ... int getKekurangan() }
void ambil(String barang, int jumlah) throws StokHabisException { ... }
```

Pemanggil **wajib** menangani. Tanpa itu, kompilasi gagal (`contoh-error/TidakDitangani.java`):

```
error: unreported exception StokHabisException; must be caught or declared to be thrown
```

Hasil dengan `try/catch`: `Ditangani: stok beras hanya 2, diminta 4 (kurang 2)`. Pada waktu yang sama, **barang yang tidak dikenal** dilempar sebagai `IllegalArgumentException` (unchecked), karena itu kesalahan pemanggil, bukan kondisi bisnis yang wajar.

### Versi unchecked

Mengubah menjadi `extends RuntimeException` dan membuang `throws`. Sekarang `gr.ambil("gula", 2)` terkompilasi tanpa `try`, penanganan menjadi **opsional**, dan bila tidak ada yang menangkap, program berhenti dengan stack trace:

```
Exception in thread "main" StokHabisRuntimeException: stok gula hanya 1, diminta 99
	at GudangRuntime.ambil(DemoGudang.java:79)
	at DemoGudang.main(DemoGudang.java:35)
```

### Kapan masing-masing lebih tepat

| | **Checked** | **Unchecked** |
|---|---|---|
| Compiler | **Memaksa** pemanggil sadar akan kegagalan | Tidak memaksa |
| Cocok untuk | Kondisi yang **wajar terjadi dan bisa dipulihkan** oleh pemanggil langsung (file tidak ada, stok habis lalu tawarkan pesan ulang, jaringan putus lalu coba lagi) | **Bug pemrograman** atau kondisi yang tidak realistis dipulihkan pemanggil (argumen salah, state rusak, konfigurasi hilang) |
| Biaya | `throws` merambat ke seluruh rantai pemanggil; **tidak bisa dipakai di lambda/stream** (Modul 9) tanpa dibungkus; mendorong `catch` kosong yang malas | Mudah terlupakan; pemanggil tidak diberi tahu oleh compiler bahwa ada kegagalan |
| Contoh di JDK | `IOException`, `SQLException` | `IllegalArgumentException`, `NullPointerException` |

Untuk `StokHabisException` sendiri, saya cenderung ke **checked** bila stok habis memang bagian alur bisnis yang **harus** dipikirkan setiap pemanggil (misal kasir harus menawarkan alternatif). Bila `Gudang` dipanggil dari banyak lapisan dan tidak ada yang bisa berbuat banyak selain melaporkan kesalahan ke pengguna di lapisan paling luar, **unchecked** lebih praktis, dan itulah kecenderungan framework modern (Spring, misalnya, memilih unchecked untuk hampir seluruh kegagalannya). Tidak ada jawaban tunggal; yang penting keputusan itu sadar, dan **didokumentasikan** (Javadoc `@throws`), terutama untuk unchecked yang tidak terlihat di tanda tangan method.

---

## Latihan 3 — try-with-resources, urutan penutupan, dan *suppressed*

Kode: `latihan-3/DemoKoneksi.java`. Keluaran: `latihan-3/hasil.txt`.

| # | Skenario | Urutan | Exception yang ditangkap | Suppressed |
|---|---|---|---|---|
| 1 | Normal | buka A, buka B, badan, **tutup B, tutup A** | – | – |
| 2 | Badan gagal, `close()` B gagal | tutup B, tutup A | **kegagalan utama** | gagal menutup B |
| 3 | Badan sukses, `close()` B gagal | tutup B, tutup A | **gagal menutup B** (jadi exception utama) | – |
| 4 | Badan gagal, `close()` B **dan** A gagal | tutup B, tutup A | **kegagalan utama** | gagal menutup B, lalu gagal menutup A |
| 5 | Constructor B gagal | buka A, B gagal, **tutup A** | gagal membuka B | – |
| 6 | *(pembanding)* `finally` manual, `close()` gagal | – | **gagal menutup X** | (kosong) |

### Yang dibuktikan

- **Urutan penutupan terbalik dari pembukaan** (B dulu, baru A), seperti menumpuk dan membongkar. Alasannya: resource yang dibuka belakangan sering bergantung pada yang dibuka lebih dulu.
- **Exception utama tidak tertimpa.** Pada skenario 2 dan 4, kegagalan dari `close()` tidak hilang dan tidak mengganti exception utama; ia disimpan sebagai *suppressed* dan bisa dibaca lewat `e.getSuppressed()`. Semua resource tetap dicoba ditutup walau yang satu gagal (skenario 4: A tetap ditutup setelah B gagal).
- **Bila badan sukses**, kegagalan `close()` menjadi exception utama (skenario 3), karena tidak ada exception lain yang perlu dilindungi. Aturan lengkapnya ada di spesifikasi bahasa (JLS §14.20.3): exception dari badan lebih diutamakan; exception dari `close()` ditambahkan sebagai suppressed.
- **Kegagalan saat membuka** (skenario 5): resource yang sudah berhasil dibuka (A) tetap ditutup, sedangkan B yang gagal dibuat tentu tidak perlu ditutup. Ini yang paling sulit dilakukan benar dengan `finally` manual.
- **Skenario 6 menunjukkan mengapa `finally` + `close()` manual berbahaya:** exception dari `close()` di dalam `finally` **menggantikan** exception sebelumnya, sehingga penyebab sebenarnya ("kegagalan utama") hilang tanpa jejak, dan `getSuppressed()` kosong. Itu alasan nyata di balik aturan modul "hindari `finally` untuk `close()`".

---

## Latihan 4 — Membaca file besar baris-per-baris

Kode: `latihan-4/Ringkas.java`. Contoh kecil: `contoh.txt` → `hasil-contoh.txt`. File besar: `hasil-besar.txt`.

```java
try (BufferedReader in = Files.newBufferedReader(masuk, StandardCharsets.UTF_8)) {
    String baris;
    while ((baris = in.readLine()) != null) {
        jumlahBaris++;
        for (String kata : baris.split("[^\\p{L}\\p{N}]+")) { ... }
    }
}
// ringkasan ditulis dengan BufferedWriter ke file lain
```

| Berkas | Baris | Kata | Kata terpanjang |
|---|---|---|---|
| `contoh.txt` (6 baris, termasuk satu baris kosong) | 6 | 34 | `internasionalisasi` (18 huruf, baris 4) |
| File sementara 300.000 baris (20.100.019 byte, dibuat program lalu dihapus) | 300.000 | 2.400.001 | `internasionalisasi` (18 huruf, baris 250.000) |

Definisi yang saya pakai, dan harus dinyatakan karena soal tidak merincinya: **"kata" = rangkaian huruf atau angka berurutan** (`\p{L}` mencakup huruf bukan-ASCII). Maka `Kata-kata` dihitung dua kata, tanda baca dibuang, dan angka seperti `12345` dihitung sebagai kata. Bila seri panjang, kata yang **pertama** ditemukan menang (`>`, bukan `>=`). Charset ditulis eksplisit (`UTF_8`) agar portabel, dan akhir baris ditulis `"\n"` eksplisit agar berkas sama di semua OS. Keduanya `try-with-resources`, sehingga tertutup walau ada kegagalan di tengah.

### Mengapa `Files.readAllLines` bukan pilihan untuk file 5 GB

- **Seluruh file dimuat sekaligus ke heap** sebagai `List<String>`. Tiap baris menjadi object `String` (plus array internal) yang menambah beban di atas ukuran byte-nya, sehingga kebutuhan memori **lebih besar daripada 5 GB**. Heap bawaan JVM biasanya hanya sebagian dari RAM fisik, jadi hasilnya `OutOfMemoryError`.
- **Ada batas teknis lain:** metode yang memuat isi ke satu array (`readAllBytes`, `readString`) terbentur batas ukuran array Java (sekitar 2 GB) dan gagal untuk file sebesar itu.
- **Tidak ada hasil sampai selesai**, dan semua memori tertahan sampai Anda selesai memakainya.
- **Pembacaan per baris** hanya menahan satu baris (ditambah buffer kecil) di memori pada satu waktu, sehingga **kebutuhan memorinya tidak bergantung pada ukuran file**. Itu sebabnya program yang sama bisa memproses 20 MB maupun 5 GB dengan memori yang kurang lebih sama. Perlu dicatat bahwa saya hanya menjalankan versi 20 MB di sini; klaim untuk 5 GB bersandar pada cara kerja API, bukan pada percobaan.

Aturan praktis dari modul: `readString`/`readAllLines` untuk file **kecil**; `BufferedReader` atau `Files.lines()` (yang **wajib ditutup**) untuk file besar.

---

## Latihan 5 — Chaining `IOException` → `LaporanException`

Kode: `latihan-5/DemoLaporan.java`. Keluaran: `latihan-5/hasil.txt`.

```java
class LaporanException extends Exception {
    LaporanException(String pesan, Throwable penyebab) { super(pesan, penyebab); }
}

static List<String> buat(Path sumber) throws LaporanException {
    try {
        return Files.readAllLines(sumber);
    } catch (IOException e) {
        throw new LaporanException("Gagal membuat laporan penjualan dari " + sumber, e);   // 'e' = cause
    }
}
```

### Membaca hasilnya (bagian *Caused by*)

```
LaporanException: Gagal membuat laporan penjualan dari data-penjualan-tidak-ada.csv
  at Laporan.buat(DemoLaporan.java:81)
  at DemoLaporan.main(DemoLaporan.java:15)
Caused by: java.nio.file.NoSuchFileException: data-penjualan-tidak-ada.csv
  at Laporan.buat(DemoLaporan.java:79)
```

Cara membacanya (sesuai saran modul): baris pertama adalah **exception domain** dengan pesan yang bermakna bagi pembaca log; barisan `at` menunjukkan di mana ia dilempar (baris 81 milik kita); lalu **`Caused by:`** menunjuk **akar masalah sebenarnya**, yaitu `NoSuchFileException`, dilempar di baris 79 (`readAllLines`). Tanpa argumen `e` pada constructor, bagian `Caused by:` tidak akan ada, dan kita hanya tahu "gagal" tanpa tahu mengapa (file tidak ada, izin ditolak, disk rusak?). Catatan: demi hasil yang stabil, program ini membuang frame internal JDK dari tampilan; stack trace asli lebih panjang.

Mengapa membungkus? Lapisan atas (pemanggil `Laporan`) tidak perlu tahu bahwa laporan dibaca dari file; besok bisa dari database, dan `LaporanException` tetap sama. Itulah **abstraksi lapisan**, sementara `cause` menjaga informasi diagnostik.

### Mengapa `catch` kosong buruk

Bagian B program memperagakannya: `LaporanMenelan.buat` menelan exception dan mengembalikan list kosong. Hasilnya `[]`, **tampak sukses**, dan pemanggil mengira "memang tidak ada penjualan". File yang tidak terbaca, izin yang salah, atau data yang rusak **tidak meninggalkan jejak apa pun**: tidak ada log, tidak ada pesan, tidak ada stack trace. Bug seperti itu berminggu-minggu bisa tak terdeteksi, dan baru ketahuan lewat angka laporan yang janggal. Minimal, exception dicatat (log) atau dilempar ulang (dibungkus).

### Kapan boleh menangkap `Exception` secara umum

Pada **batas terluar** suatu unit kerja yang harus tetap hidup walau salah satu bagiannya gagal. Bagian C program:

```java
for (String nama : tugas) {
    try { proses(nama); ... }
    catch (Exception e) { /* catat/laporkan: GAGAL nama -> jenis: pesan */ }
}
```
Hasil: `laporan-1` OK, `laporan-rusak` GAGAL (dengan jenis dan pesan tercetak), `laporan-3` **tetap diproses**. Satu tugas buruk tidak menghentikan seluruh batch. Tempat serupa: `main`, *handler* request di server web, *worker thread*, dan adapter yang menerjemahkan exception library ke exception domain (`catch (Exception e) { throw new X(..., e); }`).

Syaratnya: tangkapan itu harus **melakukan sesuatu yang berarti** (mencatat dengan jenis exception dan penyebabnya, melaporkan ke pengguna, menerjemahkan), bukan menelan; dan jangan menangkap `Throwable`/`Error` (OutOfMemoryError tidak bisa dipulihkan dengan `catch`). Di lapisan tengah, di mana Anda tidak tahu apa yang harus dilakukan terhadap kegagalan, **biarkan exception naik** (atau bungkus) alih-alih menangkap.
