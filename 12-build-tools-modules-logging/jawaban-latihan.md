# Jawaban Latihan Modul 12

Lingkungan: Java 21 (bukan 25), Maven 3.9.11 dan Gradle 8.14.3 terpasang, **tanpa akses Maven Central**, tanpa Bazel. Karena itu `maven.compiler.release` pada `pom.xml` saya set 21 (materi memakai 25).
Legenda: **[DIJALANKAN]** = keluaran asli tersimpan di berkas hasil. **[TIDAK DIJALANKAN]** = ditulis berdasarkan materi/dokumentasi, belum pernah dieksekusi di sini.

## Latihan 1 — `mvn clean package` **[TIDAK DIJALANKAN]**
Percobaan nyata: `mvn clean package` gagal sebelum mulai karena plugin `maven-clean-plugin` tidak bisa diunduh (`Name or service not known`). `pom.xml` (`latihan-1/demo-quickstart`) tetap lengkap dengan `App`, `AppTest`, Surefire dan Jar plugin.

Jawaban dari materi dan dokumentasi Maven (bukan hasil eksekusi):
- `clean` (lifecycle terpisah) menghapus `target/`. `package` menjalankan phase berurutan sampai `package`: `validate → compile → test → package` (Maven juga melewati phase perantara seperti `process-resources`, `test-compile`, `prepare-package`).
- Isi `target/` yang lazim: `classes/`, `test-classes/`, `surefire-reports/`, `maven-status/`, `maven-archiver/`, dan `demo-quickstart-1.0.0.jar`.

**[DIJALANKAN]** emulasi manual fase compile dan package (`bangun-manual.sh`, bukan keluaran Maven): `javac` ke `target/classes`, `jar --main-class` membuat jar yang bisa dijalankan (`Halo, Maven!`). Ini hanya menunjukkan pekerjaan inti kedua phase; Maven menambahkan resource, test, laporan, dan manajemen dependency.

## Latihan 2 — SLF4J + Logback **[TIDAK DIJALANKAN]**
`latihan-2/demo-maven`: `pom.xml` (`slf4j-api` + `logback-classic` scope `runtime`), `App.java` (hanya `org.slf4j`), `logback.xml`:
- logger `com.contoh` = `DEBUG`, root = `INFO`;
- `RollingFileAppender` dengan `SizeAndTimeBasedRollingPolicy` (`%d{yyyy-MM-dd}` = rotasi **harian**, plus 10 MB, `maxHistory` 14, `totalSizeCap` 500 MB).

Yang **sudah** diverifikasi: `App.java` dikompilasi terhadap `slf4j-api` dan berjalan (lihat Latihan 5) — jadi kodenya benar; yang belum diuji adalah `logback.xml` itu sendiri. Pola `%X{requestId}` hanya berfungsi bila backend mendukung MDC (backend demo saya mendukung; milik Maven tidak menampilkannya).

## Latihan 3 — Gradle
- **[DIJALANKAN]** Gradle 8.14.3 `--offline` pada proyek tanpa dependency (`latihan-3/gradle-offline`): `clean build run` mengeksekusi `compileJava → classes → jar → startScripts → distTar/distZip → assemble → check → build → run`, mencetak `Halo dari Gradle (tanpa dependency)`. Jalan kedua tanpa perubahan: **5 task semuanya UP-TO-DATE** — contoh nyata build inkremental. Jar berisi `MANIFEST.MF` dan `App.class` (`isi-jar.txt`).
- **[TIDAK DIJALANKAN]** `latihan-3/build.gradle.kts` = terjemahan penuh `pom.xml` Latihan 2 (dependency SLF4J + Logback).
- Perbandingan baris: `pom.xml` 41 baris vs `build.gradle.kts` 16 baris. Gradle lebih ringkas; XML Maven lebih seragam dan deklaratif (tanpa kode di build file).
- `implementation`: dibutuhkan saat kompilasi dan runtime modul ini, tetapi **tidak bocor** ke konsumen modul. `api` (plugin `java-library`): sama, tetapi **terekspos** ke konsumen (dipakai bila tipe dari dependency muncul di API publik modul). `runtimeOnly`: hanya di classpath runtime, tidak untuk kompilasi — cocok untuk backend logging (`logback-classic`) dan driver JDBC.

## Latihan 4 — JPMS **[DIJALANKAN]**
- (a) Menambahkan `import com.contoh.util.internal.Rahasia;` di `App` → kompilasi gagal: `package com.contoh.util.internal is not visible ... (package ... is declared in module com.contoh.util, which does not export it)` (`error-kompilasi-latihan4.txt`), padahal `Rahasia` `public`. Akses lewat **reflection** juga ditolak saat runtime: `IllegalAccessException ... module com.contoh.util does not export com.contoh.util.internal to module com.contoh.app`; `Module.isExported` mengembalikan `true` untuk `com.contoh.util` dan `false` untuk `.internal`.
- (b) `jdeps -s`: `com.contoh.app -> com.contoh.util`, `com.contoh.app -> java.base`; mode detail menunjukkan paket `java.io`, `java.lang`, `java.lang.invoke`, `java.lang.reflect` dari `java.base`.
- (c) `jlink --add-modules com.contoh.app` menghasilkan runtime yang hanya berisi `com.contoh.app`, `com.contoh.util`, `java.base`. Ukuran (`[BERVARIASI]`, tergantung build JDK): runtime mini sekitar **53 MB** vs JDK penuh sekitar **293 MB** (image `lib/modules`: sekitar 25 MB vs 138 MB). Runtime mini dijalankan dan mencetak keluaran yang sama. Catatan: ini JDK Ubuntu; angka di JDK lain berbeda.

## Latihan 5 — SLF4J sebagai facade **[DIJALANKAN]**
**Peran facade:** SLF4J hanya API; tidak mencetak apa pun. Backend dipilih lewat classpath, sehingga kode tidak berubah ketika backend diganti, dan library pihak ketiga yang memakai SLF4J ikut mengikuti satu backend pilihan aplikasi.

Bukti nyata (`hasil-facade.txt`), `App.class` yang **sama persis** (sha256 sama) dijalankan dengan:
- A. tanpa backend → SLF4J memperingatkan dan memakai NOP: **tidak ada log sama sekali**;
- B. backend milik Maven (SimpleLogger) → teks `[main] INFO …`, stack trace lengkap;
- C. backend-mini buatan saya (`latihan-5/backend-mini`) → satu baris JSON per log, MDC `requestId` terlihat;
- D. dua backend sekaligus → peringatan `multiple SLF4J bindings` dan hanya satu yang dipakai (bukti perlunya satu backend saja).
Peringatan jujur: ini memakai **SLF4J 1.7.36** (satu-satunya yang ada di instalasi Maven), bukan 2.0.17 seperti materi; mekanisme binding SLF4J 2 berbeda (`ServiceLoader`), tetapi prinsip facade-nya sama.

**Tiga log buruk dan perbaikannya (terukur, `hasil-buruk.txt`):**
1. Konkatenasi `log.debug("Data: " + m)`: `toString()` dipanggil **1000 dari 1000** kali walau DEBUG nonaktif. `log.debug("Data: {}", m)`: **0 kali**. Untuk argumen yang mahal dihitung, bungkus `if (log.isDebugEnabled())` (0 kali).
2. Data sensitif: `password=rahasia123 kartu=1234567812345678` tercetak utuh. Perbaikan: jangan catat password sama sekali; samarkan kartu (`****5678`).
3. Log-dan-lempar-ulang di tiga lapisan: **1 kegagalan → 3 entri ERROR** yang sama. Perbaikan: lapisan bawah melempar/membungkus dengan `cause`, dan hanya batas terluar (controller) yang mencatat **sekali**.

## Latihan 6 — Logback → Log4j2 **[TIDAK DIJALANKAN]**
`latihan-6/demo-maven-log4j2`: `pom.xml` (BOM `log4j-bom`, `log4j-slf4j2-impl`, `log4j-core`, `log4j-layout-template-json`; `logback-classic` dihapus), `log4j2.xml` (rolling harian + 10 MB, `.gz`, 7 arsip), baris `JsonTemplateLayout` tersedia dalam komentar.
**[DIJALANKAN]** bukti "tidak ada baris Java yang berubah": `diff -r latihan-2/.../src/main/java latihan-6/.../src/main/java` tidak menunjukkan perbedaan; yang beda hanya `pom.xml` dan berkas konfigurasi (`hasil-diff.txt`). (Saya memakai `diff -r`, bukan `git diff`, karena ini bukan repositori git.) Pemeriksaan satu backend dengan `mvn dependency:tree` perlu Maven Central — belum dijalankan.
Keuntungan log JSON: tiap baris berupa dokumen terstruktur (`@timestamp`, `log.level`, `message`, MDC sebagai field) sehingga Elasticsearch dapat menyaring/mengagregasi per field tanpa parsing regex yang rapuh; `EcsLayout.json` mengikuti skema ECS Elastic.

## Latihan 7 — JUL → TinyLog
- **[DIJALANKAN]** `DemoLogging` (JUL): `INFO`, `FINE` tampil, `FINEST` tidak, `WARNING` dengan parameter, `SEVERE`. Catatan: format `%4$-7s %5$s%n` dari materi **tidak mencetak stack trace** (tidak memuat `%6$s`), jadi exception hanya muncul sebagai pesan.
- **[TIDAK DIJALANKAN]** `latihan-7/tinylog-proyek`: `DemoTinyLog.java` (`Logger.error(e, ...)` exception di depan, argumen lazy lambda), `tinylog.properties` dengan dua writer (console level `debug`, `rolling file` harian/10 MB, 7 cadangan) dan `level@com.contoh = debug`. Nama opsi saya ambil dari materi; belum diverifikasi terhadap TinyLog 2.7.0.
- **Memilih:** Logback = default untuk mayoritas aplikasi (terutama Spring Boot). Log4j2 = bila perlu async logging throughput tinggi (Disruptor) atau layout/filter lebih kaya (`JsonTemplateLayout`). TinyLog = CLI/aplikasi kecil dengan konfigurasi minimal. Dalam semua kasus kode memakai SLF4J (kecuali TinyLog bila memakai API-nya sendiri, yang mengikat kode ke TinyLog).

## Latihan 8 — Bazel **[TIDAK DIJALANKAN]**
`latihan-8/demo-bazel`: `MODULE.bazel`, `BUILD` dengan `util_lib` (baru) → `app_lib` → `app`, plus `java_junit5_test`; nomor versi rules dibiarkan `<versi terbaru>` seperti materi. Kode Java-nya **[DIJALANKAN]**: dikompilasi dan dijalankan di luar Bazel (`Halo, Bazel!`), tetapi file `BUILD` belum pernah diproses Bazel.
Jawaban dari konsep Bazel (bukan hasil eksekusi): `bazel query 'deps(//:app)'` menampilkan `//:app`, `//:app_lib`, `//:util_lib`, dependency Maven (`@maven//...`) dan seterusnya. Mengubah satu file di `util_lib` membuat **`util_lib`** dikompilasi ulang (inputnya berubah); `app_lib` dan `app` ikut dipertimbangkan karena bergantung padanya — `app_lib` hanya dikompilasi ulang bila antarmuka (ABI) `util_lib` berubah, dan `app` (jar/launcher) dirakit ulang karena classpath runtime-nya berubah; target yang tidak bergantung pada `util_lib` tidak disentuh. Detail pemangkasan ABI bergantung versi `rules_java`; verifikasi dengan `bazel build` dan melihat daftar aksi.
