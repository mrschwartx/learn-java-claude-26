# Jawaban Latihan — Modul 1: Pengenalan Java & Lifecycle Program

> **Cara membaca dokumen ini.** Semua perintah di bawah benar-benar dijalankan; keluaran lengkapnya ada di folder `latihan-N/`. Namun saya menjalankannya di *lingkungan saya* (sandbox Linux), bukan di komputer Anda. Dua hal karenanya perlu Anda isi/ulang sendiri:
> - **Latihan 1 dan 2** meminta data pribadi (vendor/versi JDK di mesin Anda, nama dan tahun lahir Anda). Jalankan `./jalankan.sh` untuk mendapatkan data mesin Anda, dan ubah dua konstanta di `HelloWorld.java`.
> - **Latihan 5 (bagian akhir)** bertanya tentang "perusahaan Anda". Saya tidak tahu konteksnya, jadi saya beri kerangka keputusan yang bisa Anda terapkan.

---

## Latihan 1 — Memasang JDK dan memeriksa versi

**Keluaran di lingkungan saya** (`latihan-1/versi-java.txt`):

```
$ java -version
openjdk version "21.0.13.0.3" 2026-10-24
OpenJDK Runtime Environment (build 21.0.13.0.3+1-1-24.04.1-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.13.0.3+1-1-24.04.1-Ubuntu, mixed mode, sharing)

$ javac -version
javac 21.0.13.0.3
```

| Item | Nilai di lingkungan saya | Nilai di mesin Anda (isi) |
|---|---|---|
| Vendor | Ubuntu (paket OpenJDK dari Ubuntu 24.04) | ........ |
| Nomor versi | 21.0.13 | ........ |
| Tanggal rilis build | 2026-10-24 | ........ |
| LTS? | **Ya** | ........ |

**LTS atau bukan?** Java 21 adalah rilis **LTS** (Long-Term Support). Daftar LTS saat ini: Java 8, 11, 17, 21, dan 25. Cara mengecek sendiri tanpa menghafal: keluaran `java -version` pada build Temurin/Oracle biasanya menambahkan kata `LTS` di baris pertama (misalnya `... 2025-10-21 LTS`); build distro seperti Ubuntu tidak selalu menampilkannya, jadi cocokkan nomor *feature release* (angka 21) dengan daftar LTS di atas.

**Catatan jujur tentang lingkungan saya:** sandbox saya hanya memiliki runtime (`java`) beserta modul kompiler (`jdk.compiler`, `jdk.jdeps`, `jdk.jshell`), tanpa executable `javac`, `javap`, dan `jshell` terpisah. Saya memanggil ketiganya lewat `java -m <modul>/<kelas>`. Hasilnya identik dengan memanggil perintah aslinya, dan `jalankan.sh` memakai perintah aslinya di mesin Anda.

---

## Latihan 2 — `HelloWorld.java`, dua cara menjalankan

Program (`latihan-2/HelloWorld.java`) mencetak dua baris. Isi `NAMA` dan `TAHUN_LAHIR` dengan data Anda; nilai bawaannya hanya penanda.

```java
public class HelloWorld {
    private static final String NAMA = "Nama Anda";   // TODO: isi nama Anda
    private static final int TAHUN_LAHIR = 2000;      // TODO: isi tahun lahir Anda

    public static void main(String[] args) {
        System.out.println("Nama        : " + NAMA);
        System.out.println("Tahun lahir : " + TAHUN_LAHIR);
    }
}
```

**Cara A — `javac` + `java`** (`latihan-2/hasil-cara-A.txt`):

```
$ javac HelloWorld.java
$ ls -l
-rw-r--r-- 1 root root 587  HelloWorld.class      <- file baru
-rw-r--r-- 1 root root 513  HelloWorld.java
$ java HelloWorld
Nama        : Nama Anda
Tahun lahir : 2000
```

**Cara B — `java HelloWorld.java`** (`latihan-2/hasil-cara-B.txt`): program langsung tercetak, dan `ls` setelahnya **tidak menampilkan `HelloWorld.class`**.

**Perbedaan file yang dihasilkan:**

| | Cara A | Cara B |
|---|---|---|
| File di disk | `HelloWorld.class` (587 byte) tercipta | **Tidak ada** file baru |
| Tahap kompilasi | Terpisah, dilakukan oleh `javac` | Terjadi **di memori**, dalam satu perintah `java` (*source-file launcher*, Java 11+) |
| Argumen ke `java` | Nama **class** (`HelloWorld`) | Nama **file** (`HelloWorld.java`) |
| Dipakai ulang | `.class` bisa dijalankan berkali-kali tanpa kompilasi ulang, atau dikemas ke JAR | Kompilasi diulang setiap menjalankan |
| Cocok untuk | Aplikasi sungguhan, build, deployment | Skrip kecil, belajar, eksperimen cepat |

Satu fakta menarik: lima byte pertama `.class` selalu `CA FE BA BE` (magic number), diikuti nomor versi class file. Di sini `0x41` = 65 = Java 21 (`xxd HelloWorld.class | head -1`).

---

## Latihan 3 — Membaca bytecode dengan `javap -c`

Keluaran lengkap ada di `latihan-3/javap-output.txt`. Bagian pentingnya:

```
public HelloWorld();
  Code:
     0: aload_0
     1: invokespecial #1   // Method java/lang/Object."<init>":()V
     4: return

public static void main(java.lang.String[]);
  Code:
     0: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
     3: ldc           #15  // String Nama        : Nama Anda
     5: invokevirtual #17  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
     8: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
    11: ldc           #23  // String Tahun lahir : 2000
    13: invokevirtual #17  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
    16: return
```

Setiap `System.out.println(...)` menjadi **tiga instruksi**. Karena ada dua `println`, polanya muncul dua kali (offset 0–5 dan 8–13):

| Instruksi | Fungsi |
|---|---|
| `getstatic #7` | Mengambil field statis `System.out` (objek `PrintStream`) dan **menaruhnya di operand stack** |
| `ldc #15` / `ldc #23` | *Load constant*: memuat konstanta (di sini sebuah `String`) dari **constant pool** ke operand stack |
| `invokevirtual #17` | Memanggil method instance `PrintStream.println(String)`. Instruksi ini **mengambil dua nilai dari stack** (objek `out` dan argumen string) lalu mengeksekusi method |
| `return` | Mengakhiri method `void` dan kembali ke pemanggil |

Angka `#7`, `#15`, dan seterusnya adalah **indeks constant pool** (tabel konstanta di dalam file `.class`); nomornya bisa berbeda di mesin Anda. `javap -v` menampilkannya (`latihan-3/javap-verbose-cuplikan.txt`).

**Dua pengamatan tambahan:**

1. **Constructor tersembunyi.** Kita tidak menulis constructor, tetapi `javap` menampilkan `public HelloWorld()` yang dibuat compiler. Isinya `aload_0` (muat `this`), `invokespecial Object.<init>` (panggil constructor `Object`), lalu `return`.
2. **Constant folding.** Di source kita menulis `"Nama        : " + NAMA`, namun bytecode hanya memuat **satu** string utuh `"Nama        : Nama Anda"`. Karena `NAMA` adalah `static final` bertipe konstanta, compiler menggabungkan string itu *saat kompilasi*, sehingga tidak ada operasi penyambungan saat program berjalan.

---

## Latihan 4 — JDK, JRE, JVM, dan JIT compiler

### Perbedaan JDK, JRE, dan JVM

| Istilah | Singkatan | Peran |
|---|---|---|
| **JVM** | Java Virtual Machine | Mesin virtual yang **menjalankan bytecode**: memuat class, mengelola memori (heap dan garbage collection), serta mengeksekusi instruksi. Inilah yang membuat "tulis sekali, jalan di mana saja" mungkin, karena tiap OS punya JVM-nya sendiri sementara bytecode-nya sama |
| **JRE** | Java Runtime Environment | **JVM + library standar** (`java.lang`, `java.util`, dst.): semua yang dibutuhkan untuk *menjalankan* program Java |
| **JDK** | Java Development Kit | **JRE + perkakas pengembang**: `javac` (compiler), `javap`, `jshell`, `jar`, dan lainnya. Dibutuhkan untuk *membuat* program |

Hubungannya bersarang: **JDK ⊃ JRE ⊃ JVM**. Dalam praktik modern, distribusi biasanya hanya menyediakan JDK lengkap; paket JRE terpisah sudah jarang. Sandbox saya sendiri contoh kecilnya: hanya ada runtime tanpa `javac` terpisah.

### Peran JIT compiler dalam lifecycle

Lifecycle program Java: `.java` → (*javac*) → `.class` bytecode → **JVM memuat, memverifikasi, lalu mengeksekusi**. Pada tahap eksekusi, JVM memulai dengan **interpreter** (langsung jalan, tetapi lambat), sambil **memprofil** method mana yang sering dipanggil atau loop mana yang sering berputar (*hot spot*). Method yang panas dikirim ke **JIT (Just-In-Time) compiler**, yang menerjemahkannya ke **kode mesin native** dan mengoptimalkannya (misalnya *inlining* dan membuang pengecekan yang tidak perlu). HotSpot memakai bertingkat: interpreter → **C1** (kompilasi cepat, optimasi ringan) → **C2** (kompilasi lambat, optimasi agresif).

### Mengapa program Java sering terasa lebih cepat setelah beberapa saat?

Karena awalnya kode berjalan di interpreter dan belum dioptimalkan; JVM butuh waktu untuk **mengumpulkan data profil** dan mengompilasi. Setelah bagian yang panas sudah menjadi kode mesin teroptimasi, eksekusinya jauh lebih cepat. Fenomena ini disebut **warm-up**.

### Bukti: `latihan-4/WarmUp.java`

Program memanggil fungsi yang sama 20 kali dan mengukur waktu per putaran. Saya jalankan lima kali dengan JIT aktif, dan sekali dengan `-Xint` (JIT dimatikan, interpreter murni):

| Eksekusi | Putaran 1 (ms) | Putaran 20 (ms) |
|---|---|---|
| JIT aktif, #1 | 38 | 6 |
| JIT aktif, #2 | 16 | 9 |
| JIT aktif, #3 | 41 | 8 |
| JIT aktif, #4 | 40 | 10 |
| JIT aktif, #5 | 37 | 8 |
| `-Xint` (tanpa JIT) | 75 | 74 (rentang seluruh putaran: 73–107) |

Dengan JIT, putaran akhir **±4–6× lebih cepat** daripada putaran awal, dan jauh lebih cepat daripada interpreter murni. Tanpa JIT, waktunya tidak pernah membaik. `-XX:+PrintCompilation` (`latihan-4/printcompilation-cuplikan.txt`) memperlihatkan prosesnya langsung: `WarmUp::kerja` dikompilasi di **tingkat 3 (C1)**, lalu **tingkat 4 (C2)**, dan versi tingkat 3 ditandai `made not entrant` setelah digantikan.

> **Catatan kejujuran:** angka bervariasi antar mesin dan antar eksekusi. Sandbox saya hanya punya **1 core CPU**, sehingga thread kompilasi JIT berebut waktu dengan program, dan saat saya mencoba dengan hanya 10 putaran, sebagian eksekusi belum sempat "turun" ke ±8 ms. Karena itu saya naikkan menjadi 20 putaran. Di mesin multi-core, penurunannya biasanya terjadi lebih cepat. Pola yang konsisten adalah putaran awal lebih lambat daripada putaran akhir, dan `-Xint` selalu lambat.

---

## Latihan 5 — `jshell` dan LTS vs non-LTS

### Tiga ekspresi di `jshell`

Sesi lengkap: `latihan-5/sesi-jshell.txt` (masukan: `latihan-5/masukan.jsh`).

```
jshell> int a = 7
a ==> 7
jshell> a * 6                    // 1) aritmetika
$2 ==> 42
jshell> 10 / 4                   // pembagian integer: pecahan dibuang
$4 ==> 2
jshell> 10 / 4.0                 // salah satu operand double: hasil desimal
$5 ==> 2.5
jshell> "java".toUpperCase()     // 2) method String
$6 ==> "JAVA"
jshell> "Belajar Java".substring(0, 7)
$8 ==> "Belajar"
jshell> String bahasa = "Java"   // 3) membuat variabel
bahasa ==> "Java"
jshell> var versi = 25
versi ==> 25
jshell> bahasa + " " + versi
$12 ==> "Java 25"
```

Pengamatan: `jshell` tidak butuh `;`, tidak butuh `class`/`main`, dan ekspresi tanpa variabel otomatis diberi nama `$N` yang bisa dipakai lagi. `10 / 4` menghasilkan `2` (bukan `2.5`) karena kedua operandnya `int`; topik ini dibahas di Modul 2.

### Riset: LTS vs non-LTS

Informasi berikut diverifikasi dari halaman *Oracle Java SE Support Roadmap* dan halaman dukungan Microsoft Build of OpenJDK per **November 2026**:

| | **LTS** (Long-Term Support) | **Non-LTS** (feature release) |
|---|---|---|
| Jadwal | Ditetapkan setiap **2 tahun**: 17 (2021), 21 (2023), **25 (Sep 2025)**; berikutnya **29 (Sep 2027)** | Setiap **6 bulan** (Maret dan September) |
| Lama dukungan | **Bertahun-tahun**. Oracle: Java 25 Premier Support hingga **Sep 2030**, Extended hingga **Sep 2033** (atau lebih lama); Java 21 hingga Sep 2028 (Extended Sep 2031) | **Sekitar 6 bulan saja**. Mis. Java 26 (Mar 2026) didukung hingga Sep 2026, Java 27 (Sep 2026) hingga Mar 2027, lalu "diganti" rilis berikutnya |
| Perbaikan keamanan | Diterima berkala selama masa dukungan, dan vendor lain (Temurin, Microsoft, dll.) mengikuti | Berhenti begitu rilis baru keluar |
| Fitur | Mengakumulasi fitur dari non-LTS di antaranya | Memuat fitur baru lebih dulu, termasuk fitur *preview* |
| Cocok untuk | **Production** | Mencoba fitur terbaru, proyek yang rutin upgrade tiap 6 bulan |

Dua catatan yang relevan *sekarang* (November 2026):

- **Java 27 (non-LTS) baru dirilis September 2026**, jadi Java 26 sudah usai dukungannya. Jangan memakai keduanya untuk production kecuali Anda memang siap upgrade tiap enam bulan.
- Oracle menyatakan **update Oracle JDK 21 setelah September 2026 ditawarkan di bawah lisensi OTN** (bukan lagi lisensi gratis permisif); pengguna yang ingin tetap di lisensi permisif disarankan pindah ke **Oracle JDK 25 atau lebih baru**. Build OpenJDK dari vendor lain (Temurin, Microsoft, Amazon, dll.) memiliki kebijakan sendiri; cek di vendor yang Anda pakai.

### Versi mana untuk production?

Saya tidak tahu stack, kebijakan lisensi, dan vendor perusahaan Anda, jadi inilah kerangka yang bisa Anda terapkan:

| Situasi | Rekomendasi |
|---|---|
| **Proyek baru** | **Java 25 (LTS)**. Dukungan terpanjang (sampai 2030+), selaras dengan acuan materi ini, dan didukung framework modern |
| **Aplikasi existing di Java 21** | Aman untuk saat ini (dukungan hingga 2028+). Rencanakan migrasi ke 25, dan perhatikan perubahan lisensi Oracle JDK 21 di atas jika Anda memakai build Oracle |
| **Masih di Java 8, 11, atau 17** | Rencanakan upgrade. Java 17 sudah melewati masa Premier Support Oracle (Sep 2026, Extended hingga Sep 2029); Java 8 dan 11 makin sulit didukung library baru |
| **Siap upgrade tiap 6 bulan + pipeline test kuat** | Non-LTS boleh, tetapi ini pengecualian, bukan default |

Pertimbangkan juga: (1) versi minimum yang dituntut framework Anda (materi menyebut Spring Boot 4 minimal Java 17), (2) distribusi dan vendor yang punya kontrak dukungan di perusahaan Anda, dan (3) kebijakan keamanan internal (batas usia versi).
