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
- [Praktik](#praktik)
  - [Project Maven Minimal dengan SLF4J + Logback + JUnit](#project-maven-minimal-dengan-slf4j--logback--junit)
  - [Padanan Gradle (Kotlin DSL)](#padanan-gradle-kotlin-dsl)
  - [Padanan Bazel (Konseptual)](#padanan-bazel-konseptual)
  - [Java Modules: Dua Modul dengan Enkapsulasi Kuat](#java-modules-dua-modul-dengan-enkapsulasi-kuat)
  - [Logging Bawaan JDK (JUL) — Tanpa Dependency](#logging-bawaan-jdk-jul--tanpa-dependency)
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

## Latihan

1. Buat project Maven dengan `maven-archetype-quickstart` atau dari [start.spring.io](https://start.spring.io) tanpa dependency, lalu jalankan `mvn clean package`. Sebutkan phase yang dilewati dan file apa yang muncul di folder `target/`.
2. Tambahkan dependency **SLF4J + Logback** dan ganti seluruh `System.out.println` pada program Anda dengan logger. Atur agar package `com.contoh` mencatat `DEBUG` sementara root `INFO`, dan tulis log ke file dengan rotasi harian.
3. Terjemahkan `pom.xml` project Anda ke `build.gradle.kts`. Bandingkan jumlah baris, keterbacaan, dan jelaskan perbedaan `implementation` vs `api` vs `runtimeOnly`.
4. Rakit dua modul JPMS seperti contoh, lalu: (a) coba mengakses package yang tidak di-export dan catat pesan error-nya, (b) jalankan `jdeps`, (c) coba buat runtime ringkas dengan `jlink --module-path ... --add-modules com.contoh.app --output runtime-mini` dan bandingkan ukurannya dengan JDK penuh.
5. Jelaskan peran SLF4J sebagai **facade** dan mengapa kode aplikasi sebaiknya hanya bergantung pada SLF4J. Lalu tulis tiga contoh log yang **buruk** (konkatenasi string, data sensitif, log-dan-lempar-ulang) beserta perbaikannya.

---

[← Modul 11: Date & Time, Regex, Cryptography & Networking](11-date-time-regex-crypto-networking.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 13: Dependency Injection & Web Framework →](13-dependency-injection-web-framework.md)
