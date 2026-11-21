# Modul 1: Pengenalan Java & Lifecycle Program

[Daftar Isi](README.md#daftar-isi) · [Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika →](02-dasar-bahasa.md)

**Isi modul ini**

- [Teori](#teori)
  - [1.1 Apa itu Java?](#11-apa-itu-java)
  - [1.2 JDK, JRE, dan JVM — Apa Bedanya?](#12-jdk-jre-dan-jvm--apa-bedanya)
  - [1.3 Lifecycle of a Program — Dari Source Code ke Eksekusi](#13-lifecycle-of-a-program--dari-source-code-ke-eksekusi)
  - [1.4 Versi Java & LTS (Long-Term Support)](#14-versi-java--lts-long-term-support)
  - [1.5 Distribusi JDK](#15-distribusi-jdk)
  - [1.6 Anatomi Program Java Paling Sederhana](#16-anatomi-program-java-paling-sederhana)
  - [1.7 Java 25: Compact Source File & Instance Main](#17-java-25-compact-source-file--instance-main)
  - [1.8 Perkakas Dasar Java](#18-perkakas-dasar-java)
- [Praktik](#praktik)
  - [Memasang & Memeriksa JDK](#memasang--memeriksa-jdk)
  - [Compile & Run Manual (Melihat Lifecycle Langsung)](#compile--run-manual-melihat-lifecycle-langsung)
  - [Mengintip Bytecode dengan `javap`](#mengintip-bytecode-dengan-javap)
  - [Bereksperimen di `jshell`](#bereksperimen-di-jshell)
- [Latihan](#latihan)

---


## Teori

### 1.1 Apa itu Java?

**Java** adalah bahasa pemrograman **general-purpose, berorientasi objek, dan statically typed** yang dirilis oleh Sun Microsystems pada 1995 (dipimpin James Gosling) dan kini dikelola oleh **Oracle** bersama komunitas **OpenJDK**. Slogan awalnya, **"Write Once, Run Anywhere"**, berarti kode Java yang sama dapat berjalan di Windows, Linux, maupun macOS tanpa ditulis ulang — selama di mesin tersebut terpasang **JVM**.

Java dipakai luas untuk **backend enterprise**, layanan microservice, sistem keuangan, big data, dan banyak infrastruktur yang mungkin sudah Anda pelajari di course sebelumnya: **Apache Kafka**, **Elasticsearch**, dan sebagian besar ekosistem big data berjalan di atas JVM. Mempelajari Java berarti memahami bahasa di balik banyak komponen sistem terdistribusi modern.

Ciri utama Java:

- **Statically typed** — tipe data setiap variabel diperiksa saat kompilasi, sehingga banyak bug tertangkap lebih awal.
- **Object-oriented** — program disusun dari class dan object ([Modul 4](04-class-object.md) dan [Modul 5](05-pilar-oop.md)).
- **Garbage collected** — memory dikelola otomatis oleh JVM, Anda tidak perlu `free()` manual seperti di C/C++.
- **Platform independent** — dikompilasi ke **bytecode**, bukan langsung ke instruksi mesin tertentu.
- **Ekosistem raksasa** — jutaan library di Maven Central, framework matang seperti Spring Boot, dan tooling kelas dunia.

### 1.2 JDK, JRE, dan JVM — Apa Bedanya?

| Komponen | Kepanjangan | Fungsi |
|---|---|---|
| **JVM** | Java Virtual Machine | "Mesin virtual" yang menjalankan bytecode, mengelola memory, dan melakukan optimasi saat runtime |
| **JRE** | Java Runtime Environment | JVM + library standar — cukup untuk **menjalankan** aplikasi Java |
| **JDK** | Java Development Kit | JRE + alat pengembang (`javac`, `jar`, `jshell`, `javadoc`, dll.) — dibutuhkan untuk **membuat** aplikasi Java |

Hubungannya berlapis: **JDK ⊃ JRE ⊃ JVM**. Sebagai developer, Anda cukup memasang **JDK**. Sejak Java 11, distribusi modern umumnya tidak lagi menyediakan paket JRE terpisah.

### 1.3 Lifecycle of a Program — Dari Source Code ke Eksekusi

Inilah salah satu konsep pertama di roadmap Java. Program Java melewati dua tahap besar: **kompilasi** (oleh `javac`) dan **eksekusi** (oleh JVM).

```
Hello.java  ──javac──►  Hello.class  ──java──►  JVM ──► hasil di layar
(source code)           (bytecode)             │
                                               ├─ Class Loader      : memuat .class ke memory
                                               ├─ Bytecode Verifier : memeriksa keamanan bytecode
                                               ├─ Interpreter + JIT : eksekusi & optimasi kode "panas"
                                               └─ Garbage Collector : membersihkan object tak terpakai
```

Langkah-langkahnya:

1. **Write** — Anda menulis source code di file berakhiran `.java`.
2. **Compile** — `javac` memeriksa sintaks dan tipe, lalu menghasilkan **bytecode** di file `.class` (bukan kode mesin).
3. **Load** — saat `java` dijalankan, **class loader** memuat class yang dibutuhkan ke memory.
4. **Verify** — **bytecode verifier** memastikan bytecode valid dan aman sebelum dieksekusi.
5. **Execute** — JVM awalnya **menginterpretasi** bytecode. Bagian kode yang sering dieksekusi ("hot spot") dikompilasi ke kode mesin native oleh **JIT compiler** (*Just-In-Time*) sehingga makin lama berjalan, program makin cepat.
6. **Clean up** — **garbage collector** membebaskan memory dari object yang sudah tidak dirujuk.

Karena bytecode bersifat netral platform, file `.class` yang sama bisa dijalankan di JVM mana pun — inilah kunci "Write Once, Run Anywhere".

### 1.4 Versi Java & LTS (Long-Term Support)

Sejak 2017, Java dirilis setiap **6 bulan** (Maret dan September). Dari rilis-rilis tersebut, sebagian ditetapkan sebagai **LTS** yang didukung bertahun-tahun dan menjadi pilihan utama untuk production.

| Versi | Status | Catatan |
|---|---|---|
| Java 8 | LTS (lama) | Masih banyak dijumpai di sistem legacy |
| Java 11 | LTS | Awal era modern (`var`, `HttpClient`) |
| Java 17 | LTS | Baseline minimum banyak framework modern (Spring Boot 4 minimal Java 17) |
| Java 21 | LTS | Virtual Threads, pattern matching switch, record patterns final |
| **Java 25** | **LTS (September 2025)** | **Versi yang menjadi acuan course ini** |
| Java 26, 27 | Non-LTS | Rilis 6 bulanan; Java 27 dirilis September 2026 |

Oracle merencanakan LTS berikutnya adalah **Java 29** (September 2027), yaitu setiap dua tahun.

> **Catatan:** Course ini memakai **Java 25 (LTS)** sebagai acuan. Fitur yang baru tersedia di versi tertentu selalu diberi tanda versinya (misalnya "Java 21+"). Hampir seluruh kode di course ini juga berjalan di **Java 21**; contoh yang membutuhkan Java 22–25 ditandai jelas di dalam komentar kodenya.

### 1.5 Distribusi JDK

JDK berbasis **OpenJDK** (open source). Banyak vendor menyediakan build-nya, semuanya kompatibel secara spesifikasi:

| Distribusi | Penyedia | Keterangan |
|---|---|---|
| **Eclipse Temurin** | Eclipse Adoptium | Netral vendor, sangat populer, gratis |
| **Oracle JDK** | Oracle | Gratis untuk semua pengguna (Java 21+ di bawah lisensi No-Fee) |
| **Amazon Corretto** | AWS | Cocok jika deploy di AWS (relevan dengan course EKS) |
| **Azul Zulu** | Azul | Tersedia banyak varian dan versi |
| **Microsoft Build of OpenJDK**, **Red Hat**, **BellSoft Liberica** | Masing-masing vendor | Alternatif lain |

Untuk belajar, **Temurin** atau **Oracle JDK** sama baiknya.

### 1.6 Anatomi Program Java Paling Sederhana

```java
// HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Halo, Java!");
    }
}
```

| Bagian | Arti |
|---|---|
| `public class HelloWorld` | Mendeklarasikan **class**. Pada Java klasik, nama file harus sama dengan nama class publik (`HelloWorld.java`) |
| `public static void main(String[] args)` | **Titik masuk** (entry point) program. JVM mencari method ini untuk memulai eksekusi |
| `System.out.println(...)` | Mencetak teks ke layar diikuti baris baru |
| `;` | Setiap statement diakhiri titik koma |
| `{ }` | Kurung kurawal membatasi blok kode |

**Konvensi penamaan** Java (diikuti hampir semua proyek):

- **Class / interface / record / enum**: `PascalCase` — `RekeningBank`
- **Method & variabel**: `camelCase` — `hitungTotal`, `namaDepan`
- **Konstanta**: `UPPER_SNAKE_CASE` — `MAX_RETRY`
- **Package**: huruf kecil semua — `com.contoh.aplikasi`

### 1.7 Java 25: Compact Source File & Instance Main

Menulis `public class ... public static void main(String[] args)` hanya untuk mencetak satu baris terasa berat bagi pemula. Sejak **Java 25** (JEP 512, final), Anda bisa menulis program sederhana jauh lebih ringkas:

```java
// Halo.java  (membutuhkan Java 25+)
void main() {
    IO.println("Halo dari compact source file!");
}
```

- Tidak perlu deklarasi class (class implisit dibuat otomatis).
- `main` boleh berupa **instance method**, tanpa `static`, `public`, maupun `String[] args`.
- Class `IO` (di `java.lang`) menyediakan `IO.println`, `IO.print`, dan `IO.readln` untuk input/output konsol sederhana.

Fitur ini ditujukan untuk **belajar dan skrip kecil**. Aplikasi sungguhan tetap memakai struktur class lengkap — itulah yang kita pakai di sebagian besar course ini agar kode berjalan di Java 21 maupun 25.

### 1.8 Perkakas Dasar Java

| Tool | Fungsi |
|---|---|
| `javac` | Compiler: `.java` → `.class` |
| `java` | Menjalankan program (class, JAR, atau langsung source file tunggal) |
| `jshell` | **REPL** interaktif untuk mencoba potongan kode tanpa membuat file |
| `javap` | Membedah isi `.class` (disassembler bytecode) |
| `jar` | Mengemas class menjadi arsip `.jar` |
| `javadoc` | Membuat dokumentasi HTML dari komentar `/** ... */` |

**IDE** yang umum dipakai: **IntelliJ IDEA** (Community gratis), **Eclipse**, dan **VS Code** dengan *Extension Pack for Java*.

## Praktik

### Memasang & Memeriksa JDK

Cara termudah mengelola banyak versi JDK adalah **SDKMAN!** (Linux/macOS/WSL):

```bash
curl -s "https://get.sdkman.io" | bash
# buka terminal baru, lalu:
sdk list java            # lihat identifier distribusi yang tersedia
sdk install java <identifier-java-25>   # pilih salah satu dari daftar (misal build Temurin 25)
```

Alternatif: unduh installer langsung dari situs Adoptium (adoptium.net) atau Oracle, atau gunakan package manager OS Anda. Setelah terpasang, verifikasi:

```bash
java -version
javac -version
```

Keluarannya kira-kira berbentuk berikut (versi/vendor bisa berbeda):

```
openjdk version "25.0.1" 2025-10-21 LTS
OpenJDK Runtime Environment Temurin-25.0.1+8 (build 25.0.1+8-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.1+8 (build 25.0.1+8-LTS, mixed mode, sharing)
```

### Compile & Run Manual (Melihat Lifecycle Langsung)

Buat file `HelloWorld.java` dari contoh [1.6](#16-anatomi-program-java-paling-sederhana), lalu:

```bash
javac HelloWorld.java     # menghasilkan HelloWorld.class
ls                        # perhatikan file .class baru muncul
java HelloWorld           # JVM memuat class dan menjalankan main()
```

Sejak Java 11 ada jalan pintas untuk file tunggal — kompilasi terjadi di memory tanpa menghasilkan `.class`:

```bash
java HelloWorld.java
```

### Mengintip Bytecode dengan `javap`

```bash
javap -c HelloWorld
```

Potongan keluarannya (nomor `#7` dst. bisa berbeda di mesin Anda):

```
public static void main(java.lang.String[]);
  Code:
     0: getstatic     #7    // Field java/lang/System.out:Ljava/io/PrintStream;
     3: ldc           #13   // String Halo, Java!
     5: invokevirtual #15   // Method java/io/PrintStream.println:(Ljava/lang/String;)V
     8: return
```

Itulah bytecode: instruksi level rendah yang netral platform. Perhatikan bahwa `System.out.println` diterjemahkan menjadi `getstatic` (ambil objek `System.out`), `ldc` (muat konstanta string), dan `invokevirtual` (panggil method).

### Bereksperimen di `jshell`

```bash
jshell
```

```
jshell> int a = 7
a ==> 7

jshell> a * 6
$2 ==> 42

jshell> "java".toUpperCase()
$3 ==> "JAVA"

jshell> /exit
```

`jshell` sangat berguna untuk mencoba cepat potongan kode sepanjang course ini.

## Latihan

1. Pasang JDK 25 (atau minimal 21), lalu jalankan `java -version` dan `javac -version`. Catat vendor dan nomor versi Anda, serta jelaskan apakah versi tersebut LTS atau bukan.
2. Tulis `HelloWorld.java` yang mencetak nama Anda dan tahun lahir di dua baris berbeda. Jalankan dengan dua cara: `javac` + `java`, lalu `java HelloWorld.java`. Apa perbedaan file yang dihasilkan pada kedua cara tersebut?
3. Gunakan `javap -c` pada class Anda. Identifikasi instruksi bytecode yang mewakili pemanggilan `System.out.println` dan jelaskan secara singkat fungsi masing-masing instruksi.
4. Jelaskan dengan kata-kata Anda sendiri perbedaan **JDK**, **JRE**, dan **JVM**, lalu jelaskan peran **JIT compiler** dalam lifecycle program. Mengapa program Java sering terasa lebih cepat setelah berjalan beberapa saat?
5. Buka `jshell`, lalu coba tiga ekspresi: aritmetika sederhana, pemanggilan method `String`, dan pembuatan variabel. Setelah itu, riset singkat: apa perbedaan antara LTS dan non-LTS, dan versi mana yang sebaiknya dipakai untuk aplikasi production di perusahaan Anda?

---

[Daftar Isi](README.md#daftar-isi) · [Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika →](02-dasar-bahasa.md)
