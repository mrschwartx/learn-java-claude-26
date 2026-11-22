# Modul 12: Build Tools, Java Modules & Logging

[← Modul 11: Date & Time, Regex, Cryptography & Networking](11-date-time-regex-crypto-networking.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 13: Dependency Injection & Web Framework →](13-dependency-injection-web-framework.md)

**Isi modul ini**

- [Teori](#teori)
  - [12.1 Mengapa Butuh Build Tool?](#121-mengapa-butuh-build-tool)
  - [12.2 Maven](#122-maven)
  - [12.3 Gradle](#123-gradle)
  - [12.4 Bazel](#124-bazel)
  - [12.5 Maven vs Gradle vs Bazel](#125-maven-vs-gradle-vs-bazel)
  - [12.6 Java Modules (JPMS)](#126-java-modules-jpms)
  - [12.7 Logging](#127-logging)
  - [12.8 Logback, Log4j2, dan TinyLog Lebih Dekat](#128-logback-log4j2-dan-tinylog-lebih-dekat)
- [Praktik](#praktik)
  - [Project Maven Minimal dengan SLF4J + Logback + JUnit](#project-maven-minimal-dengan-slf4j--logback--junit)
  - [Padanan Gradle (Kotlin DSL)](#padanan-gradle-kotlin-dsl)
  - [Padanan Bazel (Konseptual)](#padanan-bazel-konseptual)
  - [Bazel: Dependency Eksternal dan Test](#bazel-dependency-eksternal-dan-test)
  - [Java Modules: Dua Modul dengan Enkapsulasi Kuat](#java-modules-dua-modul-dengan-enkapsulasi-kuat)
  - [Logging Bawaan JDK (JUL) — Tanpa Dependency](#logging-bawaan-jdk-jul--tanpa-dependency)
  - [Log4j2 sebagai Backend SLF4J (Mengganti Logback)](#log4j2-sebagai-backend-slf4j-mengganti-logback)
  - [TinyLog: Konfigurasi `tinylog.properties`](#tinylog-konfigurasi-tinylogproperties)
- [Latihan](#latihan)

---


## Teori

### 12.1 Mengapa Butuh Build Tool?

Di [Modul 4](04-class-object.md) kita mengompilasi dengan `javac` manual. Itu tidak skala untuk proyek nyata yang memiliki ratusan class, puluhan **dependency** (library pihak ketiga yang juga punya dependency sendiri), pengujian, dan proses pengemasan. **Build tool** mengotomatiskan:

- **Dependency management** — mengunduh library (beserta dependency transitifnya) dari repository seperti **Maven Central**.
- **Kompilasi & pengujian** — mengompilasi sumber, menjalankan unit test.
- **Pengemasan** — menghasilkan `.jar` (atau *fat/uber jar*, atau image container).
- **Build yang dapat diulang** — siapa pun, di mesin mana pun (termasuk CI/CD), menghasilkan artefak yang sama.

Tiga build tool yang ada di roadmap Java: **Maven**, **Gradle**, dan **Bazel**.

### 12.2 Maven

**Maven** adalah build tool paling mapan, bersifat **deklaratif** dan berbasis **konvensi**: Anda mendeskripsikan *apa* proyeknya dalam **`pom.xml`** (*Project Object Model*), dan Maven sudah tahu *bagaimana* membangunnya.

**Struktur direktori standar:**

```
proyek/
├── pom.xml
└── src/
    ├── main/java/        ← kode aplikasi
    ├── main/resources/   ← file konfigurasi (application.yml, logback.xml)
    ├── test/java/        ← kode test
    └── test/resources/
```

**Koordinat (GAV)** mengidentifikasi setiap artefak secara unik: **G**roupId (organisasi, `com.contoh`), **A**rtifactId (nama proyek), **V**ersion (`1.0.0`).

**Lifecycle** — rangkaian *phase* berurutan; menjalankan satu phase otomatis menjalankan semua phase sebelumnya:

| Phase | Fungsi |
|---|---|
| `validate` | Validasi proyek |
| `compile` | Kompilasi `src/main` |
| `test` | Jalankan unit test (plugin **Surefire**) |
| `package` | Kemas menjadi `.jar` |
| `verify` | Jalankan integration test (plugin **Failsafe**) |
| `install` | Pasang artefak ke repository lokal (`~/.m2`) |
| `deploy` | Unggah ke repository bersama |

Terpisah ada lifecycle `clean` (hapus folder `target/`). Perintah umum: `mvn clean package`, `mvn test`, `mvn dependency:tree` (lihat pohon dependency).

**Scope dependency:** `compile` (default), `provided` (disediakan runtime, misal servlet API), `runtime` (tak dibutuhkan saat kompilasi), `test` (hanya untuk test).

**Fitur penting lainnya:**

- **`<dependencyManagement>` / BOM** — mengelola versi sekelompok library agar selaras (contoh: `junit-bom`, `spring-boot-dependencies`).
- **Multi-module** — satu proyek induk berisi beberapa submodul.
- **Maven Wrapper** (`mvnw`) — skrip yang mengunduh versi Maven yang tepat, sehingga tim tak perlu memasang Maven manual.
- **Plugin** — semua pekerjaan dilakukan plugin (compiler, surefire, jar, spring-boot-maven-plugin, dst.).

### 12.3 Gradle

**Gradle** memakai skrip **kode** (Kotlin DSL `build.gradle.kts`, atau Groovy `build.gradle`) alih-alih XML. Sifatnya lebih fleksibel dan umumnya **lebih cepat** karena build **inkremental**, **build cache**, dan **Gradle Daemon**. Gradle adalah build tool standar untuk Android dan banyak proyek besar.

Konsep inti:

- **Task** — unit pekerjaan (`compileJava`, `test`, `jar`) yang membentuk graf dependensi.
- **Plugin** — menambah task dan konvensi (`java`, `application`, `org.springframework.boot`).
- **Configuration** untuk dependency — lebih halus daripada scope Maven:

| Configuration | Artinya |
|---|---|
| `implementation` | Dibutuhkan untuk kompilasi & runtime, **tidak bocor** ke konsumen |
| `api` | Seperti `implementation` tetapi **terekspos** ke konsumen (plugin `java-library`) |
| `compileOnly` | Hanya saat kompilasi (setara `provided`) |
| `runtimeOnly` | Hanya saat runtime (misal driver JDBC, `logback-classic`) |
| `testImplementation` | Hanya untuk test |

- **Toolchain** — Gradle dapat memakai versi JDK tertentu secara otomatis, terlepas dari JDK yang terpasang.
- **Version catalog** (`gradle/libs.versions.toml`) — daftar versi library terpusat.
- **Gradle Wrapper** (`gradlew`) — setara `mvnw`.

Perintah umum: `./gradlew build`, `./gradlew test`, `./gradlew run`, `./gradlew dependencies`.

### 12.4 Bazel

**Bazel** (dari Google) berfokus pada build yang **hermetic** (terisolasi dari kondisi mesin), **reproducible**, dan **sangat cepat pada monorepo skala raksasa** berkat *remote cache* dan eksekusi jarak jauh. Banyak bahasa (Java, Go, C++, Python) bisa dibangun dalam satu sistem.

- Unit build didefinisikan di file **`BUILD`** (bahasa Starlark mirip Python) lewat *rule* seperti `java_library`, `java_binary`, `java_test`.
- Versi modern memakai file **`MODULE.bazel`** (sistem **Bzlmod**) untuk mendeklarasikan dependency eksternal, menggantikan `WORKSPACE` lama; dependency Maven biasanya lewat `rules_jvm_external`.
- Kurva belajar **curam** dan integrasi IDE tidak semulus Maven/Gradle — dipilih terutama oleh organisasi besar dengan monorepo multi-bahasa.

**Konsep inti Bazel** yang perlu dipahami sebelum menulis `BUILD`:

| Konsep | Arti |
|---|---|
| **Module / workspace** | Akar proyek, ditandai `MODULE.bazel`; dependency eksternal dideklarasikan di sini |
| **Package** | Folder yang berisi file `BUILD` |
| **Target** | Satu unit build di dalam package, misalnya `java_library(name = "app_lib")` |
| **Label** | Alamat unik target: `//path/ke/package:nama` (dari root), `:nama` (package yang sama), `@maven//:org_slf4j_slf4j_api` (repositori eksternal) |
| **Dependency graph** | Bazel membangun graf antar-target sehingga **hanya target yang terdampak perubahan** yang dibangun ulang |
| **Visibility** | Aturan target mana yang boleh bergantung pada target lain — membantu menjaga arsitektur monorepo |

Perintah yang paling sering dipakai: `bazel build //...` (bangun semua), `bazel test //...` (jalankan semua test), `bazel run //:app`, dan `bazel query 'deps(//:app)'` untuk melihat graf dependency sebuah target.

### 12.5 Maven vs Gradle vs Bazel

| | **Maven** | **Gradle** | **Bazel** |
|---|---|---|---|
| Konfigurasi | XML deklaratif | Kotlin/Groovy DSL (kode) | Starlark (`BUILD`) |
| Kurva belajar | Rendah | Sedang | Tinggi |
| Kecepatan | Baik | Sangat baik (inkremental + cache) | Sangat baik pada skala besar |
| Fleksibilitas | Berbasis konvensi, kaku | Sangat fleksibel | Sangat ketat & eksplisit |
| Ekosistem Java | Paling luas, standar de facto | Luas, dominan di Android | Niche (monorepo besar) |
| Cocok untuk | Mayoritas proyek backend & Spring | Proyek kompleks, multi-modul, Android | Monorepo multi-bahasa raksasa |

Untuk belajar dan sebagian besar proyek, **mulai dengan Maven atau Gradle**; keduanya diterima luas dan didukung penuh oleh Spring Boot.

### 12.6 Java Modules (JPMS)

**Java Platform Module System (JPMS)**, hadir di Java 9 (Project Jigsaw), menambahkan tingkat pengelompokan **di atas package**: **module**. Sebuah module menyatakan secara eksplisit **apa yang dibutuhkannya** dan **apa yang diekspos**, lewat file **`module-info.java`** di root sumber.

```java
module com.contoh.app {
    requires com.contoh.util;          // dependency pada module lain
    requires java.net.http;            // module JDK
    requires transitive java.sql;      // konsumen module ini ikut mendapat java.sql

    exports com.contoh.app.api;        // package yang boleh diakses module lain
    opens com.contoh.app.model;        // izinkan reflection runtime (mis. library JSON/ORM)
    // uses / provides  → mekanisme service (ServiceLoader)
}
```

Manfaat utama:

- **Strong encapsulation** — package yang tidak di-`exports` **tidak dapat diakses** modul lain, bahkan jika class-nya `public`. Berbeda dengan sistem classpath lama di mana semua `public` terbuka.
- **Reliable configuration** — dependency dicek saat kompilasi dan startup; tidak ada lagi "classpath hell" yang baru ketahuan saat runtime.
- **Runtime ringkas** — `jlink` merakit **runtime Java kustom** hanya berisi modul yang dibutuhkan (hemat ukuran image container). `jdeps` menganalisis dependency modul.

Seluruh JDK sendiri sudah modular (`java.base`, `java.sql`, `java.net.http`, ...). Realitanya, **sebagian besar aplikasi (termasuk Spring Boot) tetap berjalan di classpath** dan tidak memakai `module-info`. JPMS paling bermanfaat untuk **library** yang ingin menyembunyikan internalnya, dan untuk aplikasi yang dikemas dengan `jlink`/`jpackage`. Pada modul, `java --module-path` menggantikan `-cp`.

### 12.7 Logging

Mencetak dengan `System.out.println` tidak memadai untuk aplikasi nyata: tak ada **level**, **timestamp**, **nama class/thread**, tak bisa difilter, dialihkan ke file, atau dirotasi. **Logging framework** menyelesaikan semua itu.

**Level log** (dari paling rinci ke paling kritis):

| Level | Dipakai untuk |
|---|---|
| `TRACE` | Detail sangat rinci (alur langkah demi langkah) |
| `DEBUG` | Informasi diagnostik untuk pengembang |
| `INFO` | Peristiwa normal penting (aplikasi mulai, request selesai) |
| `WARN` | Situasi tak biasa yang belum merupakan error |
| `ERROR` | Kegagalan operasi yang perlu perhatian |

Di production biasanya level **`INFO`**; `DEBUG` dinyalakan sementara untuk menyelidiki masalah.

**Lanskap framework** — salah satu sumber kebingungan terbesar:

| Komponen | Peran |
|---|---|
| **SLF4J** (*Simple Logging Facade for Java*) | **Facade/API** yang dipakai kode Anda. Tidak mencetak apa pun sendiri; mendelegasikan ke *backend* |
| **Logback** | Backend yang ditulis penulis yang sama dengan SLF4J; **default Spring Boot**, konfigurasi `logback.xml` |
| **Log4j2** | Backend dari Apache; **async logging berkinerja tinggi**, banyak appender |
| **`java.util.logging` (JUL)** | Logging bawaan JDK; fitur terbatas, jarang dipilih untuk aplikasi besar |
| **TinyLog** | Backend ringan (tanpa konfigurasi rumit, `tinylog.properties`), cocok untuk aplikasi kecil/CLI |

Pola yang disarankan: **kode memakai SLF4J (API) saja**, dan satu backend dipilih lewat dependency. Dengan begitu backend dapat diganti tanpa menyentuh kode. Pastikan hanya **satu backend** ada di classpath.

**Praktik baik logging:**

- Gunakan **parameterized message**: `log.info("Pesanan {} dibuat oleh {}", id, user)` — string baru dirakit hanya jika level aktif, tidak seperti konkatenasi `"..." + id`.
- Untuk exception, berikan **object exception sebagai argumen terakhir** agar stack trace tercatat: `log.error("Gagal memproses {}", id, e)`.
- **Jangan mencatat data sensitif** (password, token, nomor kartu, data pribadi).
- **Log sekali** per kejadian ([Modul 8](08-exception-io.md)); jangan "log lalu lempar ulang" di setiap lapisan.
- Gunakan **MDC** (*Mapped Diagnostic Context*) untuk menempelkan konteks seperti `requestId` pada setiap baris log satu request — sangat berguna di sistem terdistribusi.
- Di lingkungan container/cloud, pertimbangkan **log terstruktur (JSON)** ke stdout agar mudah dikumpulkan dan dicari oleh stack seperti Elasticsearch (course Elasticsearch) lewat agen log.
- Satu pelajaran keamanan: kerentanan **Log4Shell (CVE-2021-44228)** pada Log4j 2 menunjukkan bahwa library logging pun harus **selalu diperbarui** dan dependency dipantau.

### 12.8 Logback, Log4j2, dan TinyLog Lebih Dekat

Tabel di 12.7 menjelaskan *peran* tiap komponen. Bagian ini membahas *cara kerja dan konfigurasi* tiga backend utama agar Anda bisa memilih dan memakainya dengan percaya diri. Ingat: kode aplikasi tetap memakai **SLF4J**; yang berbeda hanya dependency dan file konfigurasinya.

#### Logback

- Konfigurasi di `logback.xml` (atau `logback-spring.xml` di Spring Boot agar bisa memakai `<springProfile>`). Contoh lengkapnya ada di bagian Praktik pertama modul ini.
- Struktur: **appender** (tujuan: console, file, rolling file), **encoder/pattern** (format baris), dan **logger** (level per package).
- Rotasi file memakai `SizeAndTimeBasedRollingPolicy` (rotasi per hari *dan* per ukuran, dengan batas jumlah arsip).
- Cocok sebagai **pilihan default**: sudah terpasang oleh `spring-boot-starter-logging`, dokumentasi luas, dan integrasi SLF4J-nya native (tanpa lapisan penerjemah).

#### Log4j2

Log4j2 adalah penulisan ulang total dari Log4j 1.x di bawah Apache. Arsitekturnya dibagi tiga artefak:

| Artefak | Fungsi |
|---|---|
| `log4j-api` | API yang dipanggil kode (`LogManager.getLogger(...)`) |
| `log4j-core` | Implementasi: appender, layout, filter, konfigurasi |
| `log4j-slf4j2-impl` | **Bridge** agar panggilan SLF4J 2.x diteruskan ke Log4j2 (untuk SLF4J 1.7 pakai `log4j-slf4j-impl`) |

Poin penting:

- **Konfigurasi**: file `log4j2.xml` (juga mendukung `.json`, `.yaml`, `.properties`) di classpath. Strukturnya `Configuration → Appenders → Loggers`. Atribut `monitorInterval="30"` membuat konfigurasi dimuat ulang otomatis tanpa restart.
- **Appender & Layout**: `Console`, `File`, `RollingFile` (dengan `Policies` waktu/ukuran dan `DefaultRolloverStrategy`), serta layout `PatternLayout` atau **`JsonTemplateLayout`** untuk log JSON (template bawaan `EcsLayout.json` kompatibel dengan stack Elastic).
- **Async logging**: keunggulan khas Log4j2. *Async Loggers* memakai ring buffer **LMAX Disruptor**: thread aplikasi hanya menaruh event ke buffer, thread lain yang menulis ke disk. Throughput dan latensi jauh lebih baik pada beban tinggi. Diaktifkan dengan dependency `com.lmax:disruptor` dan properti `log4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`. *Trade-off*: log terakhir bisa hilang jika proses mati mendadak, dan pola yang meminta info lokasi (`%C`, `%L`, `%M`) tetap mahal.
- **Fitur lain**: *lookup* (`${env:NAMA}`, `${sys:nama}`) dan filter berbasis *marker*.
- **Spring Boot**: keluarkan `spring-boot-starter-logging` dari dependency, tambahkan `spring-boot-starter-log4j2`, dan gunakan `log4j2-spring.xml`.
- **Keamanan**: Log4Shell (CVE-2021-44228) dan celah susulannya sudah ditutup; pakai **rilis terbaru** dan jangan pernah memakai versi di bawah 2.17.1.

#### TinyLog

TinyLog (versi 2.x) adalah framework logging yang sengaja dibuat **kecil, cepat, dan minim konfigurasi**:

- Dua artefak: `tinylog-api` dan `tinylog-impl`.
- **API sendiri berbasis static method**: `Logger.info(...)` dari `org.tinylog.Logger` — tidak perlu membuat instance logger per class. Nama class otomatis terdeteksi.
- Placeholder memakai `{}` seperti SLF4J; untuk exception, **object exception diletakkan pertama**: `Logger.error(e, "Gagal memproses {}", id)`.
- **Lazy argument**: `Logger.debug("Hasil: {}", () -> hitungBerat())` — lambda baru dievaluasi jika level DEBUG aktif.
- Konfigurasi di `tinylog.properties`: mendefinisikan satu atau lebih **writer** (`console`, `file`, `rolling file`, `json`, dll.), format baris, dan level (termasuk level per package dengan sintaks `level@com.contoh = debug`).
- Jika proyek lain (atau library) sudah memakai SLF4J, tambahkan bridge `org.tinylog:slf4j-tinylog` agar semua log lewat TinyLog.
- Cocok untuk **aplikasi CLI, tool kecil, dan layanan sederhana**. Ekosistemnya jauh lebih kecil dan tidak menjadi default di framework besar mana pun.

#### Memilih Backend

| | Logback | Log4j2 | TinyLog | JUL |
|---|---|---|---|---|
| Default di Spring Boot | Ya | Tidak (perlu starter khusus) | Tidak | Tidak |
| Async berkinerja tinggi | Ada (`AsyncAppender`) | **Sangat kuat** (Async Loggers + Disruptor) | Writer dengan buffer | Terbatas |
| Format konfigurasi | XML | XML / JSON / YAML / properties | `.properties` | `logging.properties` |
| Log JSON siap pakai | Lewat encoder tambahan | `JsonTemplateLayout` bawaan | Writer `json` | Tidak |
| Cocok untuk | Mayoritas aplikasi Spring Boot | Throughput tinggi, layout/filter kompleks | CLI & aplikasi kecil | Tanpa dependency, demo |

**Aturan praktis:** mulai dengan Logback (default). Pindah ke Log4j2 jika benar-benar membutuhkan async logging berkinerja tinggi atau fitur layout/filter yang lebih kaya. Pilih TinyLog untuk aplikasi kecil yang ingin konfigurasi seminimal mungkin. Apa pun pilihannya, **kode tetap memakai SLF4J** sehingga pindah backend hanya mengubah `pom.xml` dan file konfigurasi.


## Praktik

### Project Maven Minimal dengan SLF4J + Logback + JUnit

```
demo-maven/
├── pom.xml
└── src/main/
    ├── java/com/contoh/app/App.java
    └── resources/logback.xml
```

```xml
<!-- pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.contoh</groupId>
  <artifactId>demo-maven</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>25</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>                                   <!-- BOM: versi seluruh artefak JUnit selaras -->
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>6.1.1</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.17</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.5.18</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>                            <!-- versi dari BOM -->
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.5.4</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.4.2</version>
        <configuration>
          <archive>
            <manifest>
              <mainClass>com.contoh.app.App</mainClass>
            </manifest>
          </archive>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

```java
// src/main/java/com/contoh/app/App.java
package com.contoh.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Aplikasi mulai");

        MDC.put("requestId", "req-001");              // konteks ikut tercatat di setiap log
        try {
            proses("pesanan-42");
            proses("pesanan-0");
        } finally {
            MDC.clear();
        }
        log.debug("Pesan DEBUG ini hanya muncul jika level DEBUG aktif");
    }

    static void proses(String id) {
        log.info("Memproses {}", id);
        try {
            if (id.endsWith("-0")) throw new IllegalArgumentException("id tidak valid: " + id);
        } catch (IllegalArgumentException e) {
            log.error("Gagal memproses {}", id, e);   // exception sebagai argumen TERAKHIR -> stack trace tercatat
        }
    }
}
```

```xml
<!-- src/main/resources/logback.xml -->
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] [%X{requestId}] %logger{20} - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/app.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
      <fileNamePattern>logs/app.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
      <maxFileSize>10MB</maxFileSize>
      <maxHistory>14</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{ISO8601} %-5level [%thread] [%X{requestId}] %logger - %msg%n</pattern>
    </encoder>
  </appender>

  <logger name="com.contoh" level="DEBUG"/>     <!-- level per package -->

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
  </root>
</configuration>
```

```bash
mvn clean package                    # compile → test → jar (target/demo-maven-1.0.0.jar)
mvn dependency:tree                  # lihat dependency transitif
mvn dependency:copy-dependencies     # salin semua dependency ke target/dependency/
java -cp "target/demo-maven-1.0.0.jar:target/dependency/*" com.contoh.app.App
```

> Nomor versi pada contoh ini adalah yang berlaku saat materi disusun. Selalu cek versi terbaru di [Maven Central](https://central.sonatype.com) sebelum memakainya. Contoh ini tidak dapat dijalankan tanpa akses ke Maven Central.

### Padanan Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
plugins {
    java
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }   // Gradle memakai/mengunduh JDK 25
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.contoh.app.App"
}

tasks.test {
    useJUnitPlatform()
}
```

```bash
./gradlew build      # compile + test + jar
./gradlew run        # jalankan aplikasi
./gradlew test --info
```

### Padanan Bazel (Konseptual)

```python
# BUILD
load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

java_library(
    name = "app_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        "@maven//:org_slf4j_slf4j_api",     # dependency eksternal via rules_jvm_external
    ],
)

java_binary(
    name = "app",
    main_class = "com.contoh.app.App",
    runtime_deps = [
        ":app_lib",
        "@maven//:ch_qos_logback_logback_classic",
    ],
)
```

```bash
bazel build //:app
bazel run //:app
bazel test //...
```

### Bazel: Dependency Eksternal dan Test

Dependency Maven di Bazel didaftarkan di `MODULE.bazel` lewat `rules_jvm_external`. Nomor versi di bawah hanyalah contoh — **cek versi terbaru di [Bazel Central Registry](https://registry.bazel.build)** dan Maven Central.

```
demo-bazel/
├── .bazelversion                 # mengunci versi Bazel
├── MODULE.bazel
├── BUILD
└── src/
    ├── main/java/com/contoh/app/App.java
    └── test/java/com/contoh/app/AppTest.java
```

```python
# MODULE.bazel
module(name = "demo_bazel", version = "0.1.0")

bazel_dep(name = "rules_java", version = "<versi terbaru>")
bazel_dep(name = "rules_jvm_external", version = "<versi terbaru>")

maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
maven.install(
    artifacts = [
        "org.slf4j:slf4j-api:2.0.17",
        "ch.qos.logback:logback-classic:1.5.18",
    ],
    repositories = ["https://repo1.maven.org/maven2"],
)
use_repo(maven, "maven")
```

Nama target eksternal diturunkan dari koordinat Maven dengan mengganti `.`, `:` dan `-` menjadi `_`: `org.slf4j:slf4j-api` menjadi `@maven//:org_slf4j_slf4j_api` (persis seperti yang dipakai pada `BUILD` di bagian sebelumnya).

Untuk test: `java_test` bawaan Bazel menjalankan **JUnit 4** secara native. Untuk **JUnit 5/6** gunakan rule `java_junit5_test` dari modul `contrib_rules_jvm`:

```python
# BUILD (tambahan)
load("@contrib_rules_jvm//java:defs.bzl", "java_junit5_test")

java_junit5_test(
    name = "app_test",
    srcs = ["src/test/java/com/contoh/app/AppTest.java"],
    test_class = "com.contoh.app.AppTest",
    deps = [
        ":app_lib",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
    ],
)
```

```bash
bazel build //...                       # bangun semua target
bazel test //...                        # jalankan semua test
bazel query 'deps(//:app)'              # lihat graf dependency target app
bazel build //:app                      # jalankan lagi tanpa perubahan: selesai instan berkat cache
```

> Contoh Bazel di atas mengikuti dokumentasi `rules_java`, `rules_jvm_external`, dan `contrib_rules_jvm`, tetapi **tidak dijalankan** pada lingkungan penyusunan materi (tanpa akses jaringan). Opsi `maven.install` dan nama rule dapat berubah antar versi; rujuk dokumentasi resmi masing-masing rule.

### Java Modules: Dua Modul dengan Enkapsulasi Kuat

Struktur (dua modul dalam satu folder `mods`):

```
mods/
├── com.contoh.util/
│   ├── module-info.java
│   ├── com/contoh/util/Sapa.java
│   └── com/contoh/util/internal/Rahasia.java
└── com.contoh.app/
    ├── module-info.java
    └── com/contoh/app/App.java
```

```java
// mods/com.contoh.util/module-info.java
module com.contoh.util {
    exports com.contoh.util;          // hanya package ini yang terlihat; ".internal" tersembunyi
}
```

```java
// mods/com.contoh.util/com/contoh/util/Sapa.java
package com.contoh.util;

import com.contoh.util.internal.Rahasia;

public class Sapa {
    public static String sapa(String nama) {
        return "Halo, " + nama + "! " + Rahasia.cap();
    }
}
```

```java
// mods/com.contoh.util/com/contoh/util/internal/Rahasia.java
package com.contoh.util.internal;

public class Rahasia {                 // public, tetapi package-nya TIDAK di-export
    public static String cap() { return "[dibuat oleh modul util]"; }
}
```

```java
// mods/com.contoh.app/module-info.java
module com.contoh.app {
    requires com.contoh.util;
}
```

```java
// mods/com.contoh.app/com/contoh/app/App.java
package com.contoh.app;

import com.contoh.util.Sapa;
// import com.contoh.util.internal.Rahasia;   // <- hapus komentar: ERROR "package ... is not visible"

public class App {
    public static void main(String[] args) {
        System.out.println(Sapa.sapa("Java"));
    }
}
```

```bash
javac -d out --module-source-path mods $(find mods -name "*.java")
java --module-path out -m com.contoh.app/com.contoh.app.App
# Keluaran: Halo, Java! [dibuat oleh modul util]

jdeps --module-path out -s out/com.contoh.app      # lihat dependency modul
```

Coba hapus komentar pada baris `import ...internal.Rahasia` dan kompilasi ulang: compiler menolak karena package tersebut tidak di-`exports`, **meskipun class-nya `public`** — itulah *strong encapsulation*.

### Logging Bawaan JDK (JUL) — Tanpa Dependency

```java
// DemoLogging.java
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DemoLogging {
    private static final Logger LOG = Logger.getLogger(DemoLogging.class.getName());

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$-7s %5$s%n");  // format ringkas

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOG.setUseParentHandlers(false);
        LOG.addHandler(handler);
        LOG.setLevel(Level.FINE);                          // FINE ≈ DEBUG

        LOG.info("Aplikasi mulai");
        LOG.fine("Detail diagnostik");                     // tampil karena level FINE
        LOG.finest("Terlalu rinci");                       // TIDAK tampil
        LOG.log(Level.WARNING, "Pengguna {0} mencoba login gagal {1} kali", new Object[] {"andi", 3});
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Gagal parse", e);       // exception sebagai argumen -> stack trace tercatat
        }
    }
}
```

### Log4j2 sebagai Backend SLF4J (Mengganti Logback)

Ambil project `demo-maven` dari praktik pertama modul ini. Tujuannya membuktikan prinsip facade: **kode Java tidak berubah sama sekali**, hanya `pom.xml` dan file konfigurasi.

Pada `pom.xml`, **hapus** `logback-classic` lalu ganti dengan Log4j2 (versi hanyalah contoh — cek Maven Central):

```xml
<properties>
    <log4j.version>2.25.1</log4j.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>                                   <!-- BOM: menyeragamkan versi semua artefak Log4j2 -->
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-bom</artifactId>
            <version>${log4j.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.17</version>
    </dependency>
    <dependency>                                       <!-- bridge SLF4J 2.x -> Log4j2 -->
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j2-impl</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Hapus `src/main/resources/logback.xml`, lalu buat `src/main/resources/log4j2.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="LOG_DIR">logs</Property>
        <Property name="POLA">%d{HH:mm:ss.SSS} %-5level [%t] %c{1} - %msg%n</Property>
    </Properties>

    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${POLA}"/>
        </Console>

        <RollingFile name="File"
                     fileName="${LOG_DIR}/app.log"
                     filePattern="${LOG_DIR}/app-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="${POLA}"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>               <!-- rotasi harian -->
                <SizeBasedTriggeringPolicy size="10 MB"/>  <!-- dan per ukuran -->
            </Policies>
            <DefaultRolloverStrategy max="7"/>             <!-- simpan 7 arsip -->
        </RollingFile>
    </Appenders>

    <Loggers>
        <Logger name="com.contoh" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="File"/>
        </Logger>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

Kode aplikasi tetap memakai SLF4J (termasuk MDC), persis seperti sebelumnya:

```java
// src/main/java/com/contoh/app/App.java  — TIDAK diubah saat pindah dari Logback ke Log4j2
package com.contoh.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        MDC.put("requestId", "req-001");                    // konteks per request
        log.info("Pesanan {} dibuat oleh {}", 1001, "andi");
        log.debug("Detail diagnostik (tampil karena com.contoh = debug)");
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            log.error("Gagal memproses pesanan {}", 1001, e);   // exception terakhir -> stack trace tercatat
        } finally {
            MDC.clear();
        }
    }
}
```

Untuk menampilkan `requestId` di setiap baris, ubah pola menjadi `%d{HH:mm:ss.SSS} %-5level [%X{requestId}] %c{1} - %msg%n`.

Verifikasi bahwa **hanya satu backend** ada di classpath:

```bash
mvn dependency:tree | grep -Ei "logback|log4j|tinylog|slf4j"
```

**Log JSON untuk stack Elastic** — tambahkan `org.apache.logging.log4j:log4j-layout-template-json` dan ganti `PatternLayout` pada appender `Console`:

```xml
<Console name="Console" target="SYSTEM_OUT">
    <JsonTemplateLayout eventTemplateUri="classpath:EcsLayout.json"/>
</Console>
```

**Async Loggers** — tambahkan dependency `com.lmax:disruptor`, lalu buat `src/main/resources/log4j2.component.properties`:

```properties
log4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

Semua logger kini asinkron tanpa mengubah satu pun baris kode atau konfigurasi appender.

> Konfigurasi Log4j2 di atas mengikuti dokumentasi resmi Apache Log4j, tetapi **tidak dijalankan** pada lingkungan penyusunan materi (tanpa akses ke Maven Central). Nomor versi dan nama artefak (terutama bridge SLF4J) harus disesuaikan dengan versi SLF4J dan Log4j2 yang Anda pakai.

### TinyLog: Konfigurasi `tinylog.properties`

TinyLog bisa dipakai lewat **API-nya sendiri** (paling ringkas) atau lewat SLF4J dengan bridge. Dependency (versi contoh — cek Maven Central):

```xml
<dependency>
    <groupId>org.tinylog</groupId>
    <artifactId>tinylog-api</artifactId>
    <version>2.7.0</version>
</dependency>
<dependency>
    <groupId>org.tinylog</groupId>
    <artifactId>tinylog-impl</artifactId>
    <version>2.7.0</version>
</dependency>
<!-- Opsional: jika kode/library lain memakai SLF4J, alihkan lewat TinyLog -->
<dependency>
    <groupId>org.tinylog</groupId>
    <artifactId>slf4j-tinylog</artifactId>
    <version>2.7.0</version>
</dependency>
```

Konfigurasi `src/main/resources/tinylog.properties`:

```properties
# Level global, dan level khusus untuk package tertentu
level = info
level@com.contoh = debug

# Writer 1: console
writer1 = console
writer1.format = {date: HH:mm:ss.SSS} {level|min-size=5} [{thread}] {class-name}: {message}

# Writer 2: file dengan rotasi (harian atau 10 MB), simpan 7 arsip
writer2 = rolling file
writer2.file = logs/app_{count}.log
writer2.latest = logs/app.log
writer2.policies = daily, size: 10mb
writer2.backups = 7
writer2.format = {date: yyyy-MM-dd HH:mm:ss.SSS} {level} {class}: {message}
```

Program memakai `org.tinylog.Logger` — **tanpa** membuat instance logger:

```java
// DemoTinyLog.java
import org.tinylog.Logger;

public class DemoTinyLog {
    public static void main(String[] args) {
        Logger.info("Aplikasi mulai");
        Logger.debug("Detail diagnostik untuk {}", "pengembang");

        // Lazy argument: lambda hanya dievaluasi jika level DEBUG aktif
        Logger.debug("Hasil perhitungan berat: {}", () -> hitungBerat());

        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            Logger.error(e, "Gagal parse {}", "abc");        // exception DI DEPAN, stack trace ikut tercatat
        }
    }

    private static int hitungBerat() {
        return 42;                                           // bayangkan perhitungan mahal
    }
}
```

Eksperimen: ubah `level@com.contoh = debug` menjadi `warn`, jalankan ulang, dan amati baris mana yang menghilang. Lalu bandingkan dengan `DemoLogging` (JUL) di atas: konfigurasi dan pemanggilannya jauh lebih ringkas.

> Konfigurasi TinyLog di atas mengikuti dokumentasi resmi tinylog.org, tetapi **tidak dijalankan** pada lingkungan penyusunan materi. Nama placeholder format dan opsi writer dapat berbeda antar versi; rujuk [dokumentasi TinyLog](https://tinylog.org/v2/configuration/) untuk versi Anda.

## Latihan

1. Buat project Maven dengan `maven-archetype-quickstart` atau dari [start.spring.io](https://start.spring.io) tanpa dependency, lalu jalankan `mvn clean package`. Sebutkan phase yang dilewati dan file apa yang muncul di folder `target/`.
2. Tambahkan dependency **SLF4J + Logback** dan ganti seluruh `System.out.println` pada program Anda dengan logger. Atur agar package `com.contoh` mencatat `DEBUG` sementara root `INFO`, dan tulis log ke file dengan rotasi harian.
3. Terjemahkan `pom.xml` project Anda ke `build.gradle.kts`. Bandingkan jumlah baris, keterbacaan, dan jelaskan perbedaan `implementation` vs `api` vs `runtimeOnly`.
4. Rakit dua modul JPMS seperti contoh, lalu: (a) coba mengakses package yang tidak di-export dan catat pesan error-nya, (b) jalankan `jdeps`, (c) coba buat runtime ringkas dengan `jlink --module-path ... --add-modules com.contoh.app --output runtime-mini` dan bandingkan ukurannya dengan JDK penuh.
5. Jelaskan peran SLF4J sebagai **facade** dan mengapa kode aplikasi sebaiknya hanya bergantung pada SLF4J. Lalu tulis tiga contoh log yang **buruk** (konkatenasi string, data sensitif, log-dan-lempar-ulang) beserta perbaikannya.
6. Ganti Logback pada project latihan 2 dengan **Log4j2** tanpa mengubah satu baris pun kode Java. Buktikan dengan `git diff` bahwa hanya `pom.xml` dan file konfigurasi yang berubah, lalu pastikan hanya satu backend di classpath (`mvn dependency:tree`). Aktifkan `JsonTemplateLayout` dan jelaskan keuntungan log JSON untuk pencarian di Elasticsearch.
7. Tulis ulang `DemoLogging` (JUL) memakai **TinyLog**: dua writer (console level `debug`, file rolling), dan level `com.contoh` berbeda dari level global lewat `level@`. Jelaskan kapan Anda memilih TinyLog, Logback, atau Log4j2 untuk sebuah proyek.
8. Untuk project Bazel di atas, tambahkan satu `java_library` kedua (misalnya `util_lib`) yang dipakai oleh `app_lib`. Jalankan `bazel query 'deps(//:app)'`, ubah satu file di `util_lib`, dan jelaskan target mana saja yang dibangun ulang dan mengapa.

---

[← Modul 11: Date & Time, Regex, Cryptography & Networking](11-date-time-regex-crypto-networking.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 13: Dependency Injection & Web Framework →](13-dependency-injection-web-framework.md)
