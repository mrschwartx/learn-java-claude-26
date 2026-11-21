# Course Java: Basic to Advance

> **Catatan sumber:** Kurikulum ini disusun mengikuti **roadmap.sh Java Developer** ([roadmap.sh/java](https://roadmap.sh/java)) berdasarkan file **`java.pdf`** yang Anda unggah — mulai dari *Learn the Basics*, *Basics of OOP*, *Collections*, *Exception Handling*, *Web Frameworks*, *Build Tools*, *Concurrency*, *Functional Programming*, *Database Access*, *Logging Frameworks*, hingga *Testing*. Seluruh topik pada roadmap tercakup di salah satu modul (lihat tabel *Peta Roadmap ke Modul* di bawah).
>
> Isi materi dilengkapi **dokumentasi resmi OpenJDK/Oracle** dan best practice terkini, dengan **Java 25 (LTS)** sebagai versi acuan dan **Spring Boot 4.x** untuk bagian framework.

Java melengkapi seri course Anda dari sudut **bahasa dan aplikasi**: Docker/Kubernetes/EKS menjawab "bagaimana menjalankan aplikasi", Terraform "bagaimana menyediakan infrastrukturnya", Kafka "bagaimana data mengalir", Elasticsearch "bagaimana data dicari", Keycloak "siapa yang boleh akses", Redis "bagaimana membuatnya sangat cepat" — Java menjawab **"bagaimana menulis aplikasi backend yang menyatukan semuanya"**. Pola yang sangat umum: aplikasi Spring Boot → Redis (cache) → PostgreSQL, dengan event lewat Kafka dan pencarian lewat Elasticsearch.

## Daftar Isi

| # | Modul |
|---|-------|
| 1 | [Pengenalan Java & Lifecycle Program](01-pengenalan.md) |
| 2 | [Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika](02-dasar-bahasa.md) |
| 3 | [Kontrol Alur, Array & String](03-kontrol-alur-array-string.md) |
| 4 | [Class, Object, Access Specifier & Package](04-class-object.md) |
| 5 | [Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface](05-pilar-oop.md) |
| 6 | [Enum, Record, Sealed Class, Pattern Matching & Annotation](06-fitur-modern.md) |
| 7 | [Generics & Collections Framework](07-generics-collections.md) |
| 8 | [Exception Handling & I/O](08-exception-io.md) |
| 9 | [Functional Programming: Lambda & Stream API](09-functional-programming.md) |
| 10 | [Concurrency: Thread, Virtual Thread & Java Memory Model](10-concurrency.md) |
| 11 | [Date & Time, Regex, Cryptography & Networking](11-date-time-regex-crypto-networking.md) |
| 12 | [Build Tools, Java Modules & Logging](12-build-tools-modules-logging.md) |
| 13 | [Dependency Injection & Web Framework](13-dependency-injection-web-framework.md) |
| 14 | [Database Access: JDBC, Hibernate & Spring Data JPA](14-database-access.md) |
| 15 | [Testing & Advanced Topics](15-testing-advanced.md) |

## Peta Roadmap ke Modul

| Topik di roadmap.sh Java | Modul |
|---|---|
| Basic Syntax, Data Types, Variables and Scopes, Type Casting, Math Operations | [2](02-dasar-bahasa.md) |
| Lifecycle of a Program | [1](01-pengenalan.md) |
| Conditionals, Loops, Arrays, Strings and Methods | [3](03-kontrol-alur-array-string.md) |
| Classes and Objects, Attributes and Methods, Access Specifiers, Static Keyword, Nested Classes, Packages, Initializer Block, Object Lifecycle, Pass by Value / Reference | [4](04-class-object.md) |
| Basics of OOP, Inheritance, Abstraction, Encapsulation, Interfaces, Method Overloading / Overriding, Static vs Dynamic Binding, Final Keyword, Method Chaining | [5](05-pilar-oop.md) |
| Enums, Record, Annotations | [6](06-fitur-modern.md) |
| Array vs ArrayList, Set, Map, Queue, Stack, Dequeue, Iterator, Generic Collections, Optionals | [7](07-generics-collections.md) |
| Exception Handling, I/O Operations, File Operations | [8](08-exception-io.md) |
| Lambda Expressions, Functional Programming (Composition, High Order Functions, Functional Interfaces), Stream API | [9](09-functional-programming.md) |
| Concurrency, Threads, Virtual Threads, Java Memory Model, `volatile` keyword | [10](10-concurrency.md) |
| Date and Time, Regular Expressions, Cryptography, Networking | [11](11-date-time-regex-crypto-networking.md) |
| Build Tools (Maven, Gradle, Bazel), Modules, Logging Frameworks (Logback, Log4j2, SLF4J, TinyLog) | [12](12-build-tools-modules-logging.md) |
| Dependency Injection, Web Frameworks (Spring Boot, Play Framework, Quarkus, Javalin) | [13](13-dependency-injection-web-framework.md) |
| Database Access (JDBC, Spring Data JPA, Hibernate, EBean) | [14](14-database-access.md) |
| Testing (JUnit, TestNG, Mockito, REST Assured, JMeter, Cucumber-JVM) | [15](15-testing-advanced.md) |

## Cara Menggunakan Materi Ini

1. Ikuti urutan modul 1–15 secara bertahap — konsep OOP di Modul 4–6 dan collections di Modul 7 menjadi fondasi untuk hampir semua modul lanjutan, termasuk Spring Boot dan JPA.
2. Setiap modul berisi **Teori**, **Praktik** (kode Java yang bisa langsung dijalankan, konfigurasi, dan perintah CLI), dan **Latihan**.
3. Contoh kode JDK murni berlabel nama file (misalnya `// DemoStream.java` di Modul 1–11) dapat dijalankan langsung dengan `java NamaFile.java` — tidak perlu build tool. Modul 12–15 memperkenalkan Maven/Gradle dan dependency eksternal.
4. Praktik database (Modul 14) memakai **PostgreSQL di Docker Compose**, sehingga Anda tidak perlu memasang database secara manual.
5. Jangan hanya membaca: ketik dan jalankan setiap contoh, ubah nilainya, dan kerjakan latihannya.

## Prasyarat

- Familiar dengan command line dan Docker dasar (course Docker sebelumnya) — Docker dipakai untuk database di Modul 14 dan Testcontainers di Modul 15.
- Tidak perlu pengalaman Java sebelumnya, tetapi pemahaman logika pemrograman dasar (variabel, kondisi, perulangan) akan mempercepat belajar.
- **JDK 25** (disarankan) atau minimal **JDK 21**. Komputer dengan RAM minimal 8 GB dan editor/IDE seperti IntelliJ IDEA Community atau VS Code.

## Catatan Versi

Java dirilis setiap enam bulan, dan framework di sekitarnya berubah cukup cepat. Materi ini menargetkan **Java 25 (LTS)**; hampir seluruh kode juga berjalan di **Java 21**, dan bagian yang membutuhkan versi lebih baru (misalnya Java 22–25) ditandai jelas di dalam komentar kode. Contoh dependency memakai nomor versi yang berlaku saat materi disusun (Spring Boot 4.x, JUnit 6.x, SLF4J 2.0.x, Logback 1.5.x) — **cek versi terbaru di [Maven Central](https://central.sonatype.com)** sebelum memakainya. Setiap kali dokumentasi resmi berbeda dengan materi ini untuk versi Anda, dokumentasi resmi yang menang.

Contoh kode JDK murni yang berlabel nama file (Modul 1–13 dan 15), termasuk dua modul JPMS di Modul 12, telah dijalankan dan diverifikasi pada Java 21. Contoh yang bergantung pada library eksternal (Spring Boot, JPA, Mockito, Cucumber, dsb.) mengikuti dokumentasi resminya dan ditandai di dalam materi.

## Sumber Referensi

- Roadmap: https://roadmap.sh/java
- Dokumentasi Java (Oracle): https://docs.oracle.com/en/java/javase/
- OpenJDK & daftar JEP: https://openjdk.org
- Spring Boot Reference: https://docs.spring.io/spring-boot/
- Jakarta Persistence / Hibernate ORM: https://hibernate.org/orm/documentation/
- JUnit User Guide: https://docs.junit.org
- Dev.java (tutorial resmi): https://dev.java

## Navigasi

Setiap modul punya tautan *Sebelumnya · Daftar Isi · Berikutnya* di atas dan di bawah, plus daftar isi modul itu sendiri.
