# Modul 15: Testing & Advanced Topics

[← Modul 14: Database Access: JDBC, Hibernate & Spring Data JPA](14-database-access.md) · [Daftar Isi](README.md#daftar-isi)

**Isi modul ini**

- [Teori](#teori)
  - [15.1 Mengapa Testing dan Seperti Apa Strateginya](#151-mengapa-testing-dan-seperti-apa-strateginya)
  - [15.2 Unit Testing dengan JUnit](#152-unit-testing-dengan-junit)
  - [15.3 TestNG](#153-testng)
  - [15.4 Mocking dengan Mockito](#154-mocking-dengan-mockito)
  - [15.5 Integration Testing](#155-integration-testing)
  - [15.6 Performance Testing dengan JMeter](#156-performance-testing-dengan-jmeter)
  - [15.7 Behavior Testing dengan Cucumber-JVM](#157-behavior-testing-dengan-cucumber-jvm)
  - [15.8 Kualitas Test dan Kode](#158-kualitas-test-dan-kode)
  - [15.9 Topik Lanjutan: Performa JVM dan Arah Platform](#159-topik-lanjutan-performa-jvm-dan-arah-platform)
- [Praktik](#praktik)
  - [Test Pertama: Mini Test Runner (Memahami Cara Kerja JUnit)](#test-pertama-mini-test-runner-memahami-cara-kerja-junit)
  - [JUnit 6: Unit Test, Parameterized, dan Nested](#junit-6-unit-test-parameterized-dan-nested)
  - [Mockito: Menguji `PesananService` dengan Mock](#mockito-menguji-pesananservice-dengan-mock)
  - [Integration Test: `@WebMvcTest`, Testcontainers, dan REST Assured](#integration-test-webmvctest-testcontainers-dan-rest-assured)
  - [TestNG: Padanan dan `@DataProvider`](#testng-padanan-dan-dataprovider)
  - [Cucumber-JVM: Skenario Bergaya Bisnis](#cucumber-jvm-skenario-bergaya-bisnis)
  - [Load Test dengan JMeter](#load-test-dengan-jmeter)
- [Latihan](#latihan)
- [Selamat! 🎉](#selamat-)

---


## Teori

### 15.1 Mengapa Testing dan Seperti Apa Strateginya

**Automated testing** adalah jaring pengaman yang membuat Anda berani mengubah kode: setiap perubahan dapat diverifikasi dalam hitungan detik. Test yang baik juga berfungsi sebagai **dokumentasi hidup** tentang perilaku sistem.

**Piramida testing** menggambarkan proporsi yang sehat:

```
          ▲  E2E / UI / Behavior     — sedikit, lambat, mahal (seluruh sistem)
         ▲▲▲ Integration             — beberapa, menguji kerja sama komponen (DB, HTTP)
       ▲▲▲▲▲▲▲ Unit                  — banyak, cepat, murah (satu class/method)
```

| Jenis | Menguji | Kecepatan | Contoh alat |
|---|---|---|---|
| **Unit test** | Satu unit kecil, terisolasi dari dependency | Milidetik | JUnit, TestNG, Mockito |
| **Integration test** | Beberapa komponen nyata bersama (service + database, API) | Detik | Spring Boot Test, Testcontainers, REST Assured |
| **Behavior / acceptance test** | Perilaku bisnis dari sudut pandang pengguna | Detik–menit | Cucumber-JVM |
| **Performance / load test** | Kecepatan & ketahanan di bawah beban | Menit | JMeter, Gatling |

Ciri unit test yang baik (**FIRST**): **F**ast, **I**ndependent (tak bergantung urutan/test lain), **R**epeatable (hasil sama di mana pun), **S**elf-validating (lulus/gagal otomatis), **T**imely (ditulis bersama kodenya). Susun tiap test dengan pola **Arrange – Act – Assert** (siapkan, jalankan, verifikasi), dan beri nama yang menjelaskan skenario (`tarik_saldoKurang_melemparException`).

### 15.2 Unit Testing dengan JUnit

**JUnit** adalah framework unit testing standar de facto di Java. Course ini memakai **JUnit 6** (rilis akhir September 2025; baseline Java 17; satu nomor versi untuk Platform, Jupiter, dan Vintage). API tulis-test-nya (paket `org.junit.jupiter.api`) **tetap sama dengan JUnit 5**, jadi hampir semua tutorial JUnit 5 masih berlaku.

| Anotasi | Fungsi |
|---|---|
| `@Test` | Menandai method test |
| `@BeforeEach` / `@AfterEach` | Dijalankan sebelum/sesudah **setiap** test |
| `@BeforeAll` / `@AfterAll` | Sekali untuk seluruh class (method `static`) |
| `@DisplayName` | Nama yang mudah dibaca di laporan |
| `@Nested` | Mengelompokkan test dalam inner class |
| `@ParameterizedTest` + `@ValueSource`/`@CsvSource`/`@MethodSource` | Satu test, banyak data |
| `@Disabled` | Menonaktifkan sementara |
| `@Tag` | Memberi label untuk memfilter (misal `slow`) |
| `@TempDir` | Direktori sementara yang dibersihkan otomatis |

**Assertions** yang sering dipakai: `assertEquals(diharapkan, aktual)`, `assertTrue`, `assertNull`, `assertThrows(Tipe.class, () -> ...)`, `assertAll(...)` (kumpulkan banyak asersi), `assertTimeout`. Untuk asersi yang lebih ekspresif banyak tim memakai **AssertJ** (`assertThat(list).hasSize(3).contains("a")`).

JUnit membuat **instance baru dari class test untuk setiap method test**, sehingga state antar test tidak bocor. Test dijalankan oleh **Maven Surefire** atau **Gradle** ([Modul 12](12-build-tools-modules-logging.md)).

### 15.3 TestNG

**TestNG** adalah framework testing alternatif (terinspirasi JUnit dan NUnit) yang populer di dunia QA/otomasi dan proyek enterprise lama. Fitur khasnya: konfigurasi suite dengan **`testng.xml`**, **groups**, **`@DataProvider`** untuk data-driven test, **`dependsOnMethods`** (urutan test), eksekusi **paralel** bawaan, dan laporan HTML.

| | **JUnit (Jupiter)** | **TestNG** |
|---|---|---|
| Popularitas di proyek baru | **Dominan** (default Spring Boot) | Lebih sedikit |
| Parameter test | `@ParameterizedTest` | `@DataProvider` |
| Dependensi antar test | Tidak didorong | `dependsOnMethods`, `dependsOnGroups` |
| Paralel | Konfigurasi | Bawaan, lewat `testng.xml` |
| Ekstensi | Extension model (`@ExtendWith`) | Listener, annotation transformer |
| Pilih jika | Proyek baru, ekosistem Spring | Sudah ada suite TestNG, butuh fitur suite/paralel kuat |

### 15.4 Mocking dengan Mockito

Unit test harus **terisolasi**. Jika `PesananService` bergantung pada database dan layanan email, kita ganti dependency itu dengan **test double** ([Modul 13](13-dependency-injection-web-framework.md) membuat penggantian semudah ini):

| Jenis | Arti |
|---|---|
| **Dummy** | Pengisi parameter; tak pernah dipakai |
| **Stub** | Mengembalikan jawaban yang sudah ditentukan |
| **Fake** | Implementasi sederhana yang sungguhan berjalan (misal repository berbasis `HashMap`) |
| **Mock** | Merekam pemanggilan agar bisa **diverifikasi** ("apakah `kirim()` dipanggil?") |
| **Spy** | Object nyata yang sebagian method-nya dapat dipantau/diganti |

**Mockito** adalah library untuk membuat mock/stub/spy dengan mudah:

| Tujuan | API |
|---|---|
| Membuat mock | `@Mock` atau `mock(Kelas.class)` |
| Menentukan jawaban | `when(x.metode(a)).thenReturn(b)` / `.thenThrow(...)` |
| Method `void` | `doThrow(...).when(x).metode()` |
| Memverifikasi pemanggilan | `verify(x).metode(a)`, `verify(x, times(2))`, `never()`, `verifyNoInteractions(x)` |
| Menangkap argumen | `ArgumentCaptor` |
| Mencocokkan argumen | `any()`, `eq(...)`, `argThat(...)` |
| Menyuntik mock ke object uji | `@InjectMocks` + `@ExtendWith(MockitoExtension.class)` |

Pedoman: **jangan me-mock value object** (record, `String`, `List`) — buat saja yang asli; **jangan me-mock apa yang Anda tidak miliki** secara berlebihan (bungkus library pihak ketiga dengan adapter milik Anda); dan jangan memverifikasi detail implementasi yang tidak penting — terlalu banyak `verify` membuat test rapuh terhadap refactoring. **Fake** sering lebih jelas daripada mock untuk repository.

Waktu juga adalah dependency: gunakan **`Clock`** (diinjeksi) agar test dapat menetapkan waktu (`Clock.fixed(...)`), seperti di [Modul 11](11-date-time-regex-crypto-networking.md).

### 15.5 Integration Testing

**Integration test** memastikan komponen **nyata** bekerja sama: controller + service + database + serializer JSON. Alat utamanya:

- **Spring Boot Test** — `@SpringBootTest` (memuat seluruh konteks aplikasi) atau **test slice** yang lebih ringan: `@WebMvcTest` (lapisan web saja, dengan `MockMvc`), `@DataJpaTest` (lapisan JPA saja). Pada slice, dependency lain diganti mock dengan `@MockitoBean`.
- **Testcontainers** — menjalankan **database/Kafka/Redis sungguhan di container Docker** selama test, sehingga test berjalan melawan teknologi yang sama dengan production (bukan database in-memory yang berperilaku berbeda). Memanfaatkan ilmu course Docker dan sangat relevan untuk Kafka, Elasticsearch, dan Redis dari course Anda sebelumnya.
- **REST Assured** — DSL fluent untuk menguji REST API dari luar (`given().when().then()`), cocok untuk test *black-box* melawan aplikasi yang berjalan.
- **Maven Failsafe** — plugin yang menjalankan integration test (konvensi nama `*IT.java`) pada phase `verify`, terpisah dari unit test Surefire.

### 15.6 Performance Testing dengan JMeter

**Apache JMeter** adalah alat open source untuk **load/performance testing** (aplikasinya sendiri berbasis Java). Anda mendefinisikan **Test Plan**:

- **Thread Group** — jumlah pengguna virtual (*threads*), *ramp-up period*, dan jumlah iterasi.
- **Sampler** — request yang dikirim (HTTP Request, JDBC Request, dst.).
- **Config element** — header HTTP, data CSV (parameter berbeda per pengguna).
- **Assertion** — validasi respons (status 200, isi mengandung teks tertentu).
- **Listener** — mengumpulkan hasil (tabel, grafik, laporan).

Penting: **GUI hanya untuk menyusun dan debug rencana; eksekusi beban sesungguhnya dijalankan lewat mode CLI (non-GUI)** agar tak membebani mesin penguji. Metrik yang dibaca: **throughput** (request/detik), **latency** (khususnya persentil **p95/p99**, bukan hanya rata-rata), dan **error rate**. Alternatif: **Gatling** (skenario sebagai kode Scala/Java/Kotlin) dan **k6**.

### 15.7 Behavior Testing dengan Cucumber-JVM

**Behavior-Driven Development (BDD)** menjembatani bisnis dan teknis: skenario ditulis dalam bahasa mendekati bahasa manusia, bisa dibaca dan disepakati bersama *product owner*. **Cucumber** membaca skenario berformat **Gherkin** (`Given–When–Then`) dari file `.feature`, lalu mencocokkan tiap langkah dengan **step definition** berupa method Java.

Manfaatnya terutama untuk **acceptance test** dan dokumentasi perilaku; untuk logika teknis rinci tetap gunakan unit test biasa — jangan membungkus semuanya dengan Gherkin.

### 15.8 Kualitas Test dan Kode

- **Code coverage** (**JaCoCo**) menunjukkan baris/cabang yang dieksekusi test. Coverage tinggi **tidak menjamin** test berkualitas (baris bisa dieksekusi tanpa asersi bermakna); pakai sebagai petunjuk celah, bukan target mutlak.
- **Mutation testing** (**PIT / Pitest**) mengubah kode sedikit-sedikit (*mutant*) dan memeriksa apakah test gagal — mengukur seberapa tajam test Anda sebenarnya.
- **Analisis statis**: **SpotBugs**, **Checkstyle**, **Error Prone**, **SonarQube** menangkap bug dan pelanggaran gaya sebelum runtime.
- Jalankan semuanya di **CI** (GitHub Actions/GitLab CI) pada setiap *push*.

### 15.9 Topik Lanjutan: Performa JVM dan Arah Platform

**Garbage collector (GC)** — pilihan pada JVM modern:

| GC | Karakter |
|---|---|
| **G1** | **Default**; seimbang antara throughput dan jeda |
| **ZGC** | Jeda sangat rendah (sub-milidetik) pada heap besar; sejak Java 23 mode **generational** menjadi default |
| **Shenandoah** | Jeda rendah (concurrent compaction); tersedia di banyak distribusi |
| Parallel / Serial | Throughput tinggi / aplikasi kecil, satu core |

Parameter yang paling sering disetel: `-Xms`/`-Xmx` (ukuran heap), `-XX:MaxRAMPercentage=75` (di **container**, JVM mengenali batas memori cgroup dan dapat memakai persentase darinya), dan pemilihan GC (`-XX:+UseZGC`). **Ukur dulu, baru setel** — default modern sudah baik.

**Observabilitas & diagnosis:**

- **JFR** (*Java Flight Recorder*) + **JDK Mission Control** — profiler bawaan berbiaya rendah yang aman dipakai di production.
- `jcmd`, `jstack` (thread dump untuk menemukan deadlock), `jmap` / heap dump (menyelidiki **memory leak**, dianalisis dengan Eclipse MAT).
- **Micrometer** (metrik) dan **OpenTelemetry** (trace terdistribusi) — terintegrasi dengan Spring Boot Actuator.

**Startup & footprint:**

- **AppCDS** dan **AOT cache** (*Project Leyden*, mulai Java 24) mempercepat startup dengan menyimpan hasil pemuatan/analisis class.
- **GraalVM Native Image** — mengompilasi aplikasi menjadi executable native: startup milidetik dan memori kecil, dengan kompromi waktu build lebih lama dan keterbatasan reflection dinamis. Didukung Spring Boot dan Quarkus.
- **`jlink`/`jpackage`** — runtime ringkas dan installer ([Modul 12](12-build-tools-modules-logging.md)).
- **Compact object headers** (produk di Java 25, perlu diaktifkan) menurunkan memori per object.

**Arah evolusi platform** (patut diikuti perkembangannya di openjdk.org/projects):

| Proyek | Tema |
|---|---|
| **Loom** | Virtual thread (sudah), structured concurrency & scoped values ([Modul 10](10-concurrency.md)) |
| **Amber** | Penyederhanaan bahasa: record, sealed, pattern matching ([Modul 6](06-fitur-modern.md)) |
| **Valhalla** | *Value classes* — object tanpa identitas dengan performa mendekati primitif |
| **Panama** | **Foreign Function & Memory API** (final di Java 22): memanggil kode native dan mengelola memori off-heap tanpa JNI; Vector API masih tahap incubator |
| **Leyden** | Startup dan warm-up lebih cepat |

## Praktik

### Test Pertama: Mini Test Runner (Memahami Cara Kerja JUnit)

Sebelum memakai JUnit sungguhan, mari lihat cara kerjanya dengan **anotasi + reflection** ([Modul 6](06-fitur-modern.md)): runner mencari method bertanda `@Uji`, membuat instance baru untuk tiap test, lalu menangkap kegagalan. Sekaligus kita latih **Clock** dan **fake** sebagai test double — tanpa library apa pun.

```java
// MiniUji.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MiniUji {
    public static void main(String[] args) throws Exception {
        int lulus = 0, gagal = 0;

        for (Class<?> kelas : List.of(DiskonServiceTest.class, PesananServiceTest.class)) {
            System.out.println(kelas.getSimpleName());
            Method[] metode = kelas.getDeclaredMethods();
            Arrays.sort(metode, Comparator.comparing(Method::getName));

            for (Method m : metode) {
                if (!m.isAnnotationPresent(Uji.class)) continue;
                Object instans = kelas.getDeclaredConstructor().newInstance();   // instance BARU per test (seperti JUnit)
                try {
                    m.invoke(instans);
                    lulus++;
                    System.out.println("  LULUS  " + m.getName());
                } catch (InvocationTargetException e) {
                    gagal++;
                    System.out.println("  GAGAL  " + m.getName() + " -> " + e.getCause().getMessage());
                }
            }
        }
        System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal");
    }
}

// ---- "Framework" uji mini ----
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Uji { }

class Asersi {
    static void samaDengan(Object diharapkan, Object aktual) {
        if (!Objects.equals(diharapkan, aktual)) {
            throw new AssertionError("diharapkan <" + diharapkan + "> tetapi aktual <" + aktual + ">");
        }
    }
}

// ---- Kode yang diuji: logika bergantung pada waktu -> inject Clock ----
class DiskonService {
    private final Clock clock;
    DiskonService(Clock clock) { this.clock = clock; }

    long hitung(long harga) {
        DayOfWeek hari = LocalDate.now(clock).getDayOfWeek();
        boolean akhirPekan = hari == DayOfWeek.SATURDAY || hari == DayOfWeek.SUNDAY;
        return akhirPekan ? harga * 90 / 100 : harga;       // diskon 10% di akhir pekan
    }
}

class DiskonServiceTest {
    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

    @Uji void akhirPekan_diskon10Persen() {
        Clock sabtu = Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), JAKARTA);    // Sabtu
        Asersi.samaDengan(90_000L, new DiskonService(sabtu).hitung(100_000));
    }

    @Uji void hariKerja_tanpaDiskon() {
        Clock senin = Clock.fixed(Instant.parse("2026-11-23T03:00:00Z"), JAKARTA);    // Senin
        Asersi.samaDengan(100_000L, new DiskonService(senin).hitung(100_000));
    }

    @Uji void sengajaGagal_ekspektasiSalah() {                                        // menunjukkan laporan test GAGAL
        Clock sabtu = Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), JAKARTA);
        Asersi.samaDengan(85_000L, new DiskonService(sabtu).hitung(100_000));
    }
}

// ---- Dependency + FAKE (test double) ----
interface Notifikasi { void kirim(String pesan); }

class NotifikasiFake implements Notifikasi {                 // merekam pesan agar bisa diperiksa
    final List<String> terkirim = new ArrayList<>();
    public void kirim(String pesan) { terkirim.add(pesan); }
}

class PesananService {
    private final Notifikasi notif;
    PesananService(Notifikasi notif) { this.notif = notif; }

    void buat(String item) {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("item wajib diisi");
        notif.kirim("Pesanan " + item + " dibuat");
    }
}

class PesananServiceTest {
    @Uji void buat_mengirimNotifikasi() {
        NotifikasiFake fake = new NotifikasiFake();                        // Arrange
        PesananService service = new PesananService(fake);
        service.buat("kopi");                                              // Act
        Asersi.samaDengan(List.of("Pesanan kopi dibuat"), fake.terkirim);  // Assert
    }

    @Uji void buat_itemKosong_melemparException() {
        NotifikasiFake fake = new NotifikasiFake();
        try {
            new PesananService(fake).buat(" ");
            throw new AssertionError("seharusnya melempar IllegalArgumentException");
        } catch (IllegalArgumentException diharapkan) {
            Asersi.samaDengan(0, fake.terkirim.size());                    // notifikasi tidak terkirim
        }
    }
}
```

Perhatikan tiga hal: (1) test `sengajaGagal...` menunjukkan seperti apa laporan **merah**, (2) `Clock.fixed` membuat test **repeatable** meski dijalankan pada hari apa pun, dan (3) `NotifikasiFake` menggantikan dependency tanpa library mocking. Inilah inti yang dikerjakan JUnit dan Mockito — dengan fitur jauh lebih lengkap.

### JUnit 6: Unit Test, Parameterized, dan Nested

Gunakan project Maven/Gradle dari [Modul 12](12-build-tools-modules-logging.md) (sudah memuat `junit-jupiter`). Class yang diuji:

```java
// src/main/java/com/contoh/app/Kalkulator.java
package com.contoh.app;

public class Kalkulator {
    public int tambah(int a, int b) { return a + b; }

    public int bagi(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("pembagi tidak boleh nol");
        return a / b;
    }

    public boolean genap(int n) { return n % 2 == 0; }
}
```

```java
// src/test/java/com/contoh/app/KalkulatorTest.java
package com.contoh.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class KalkulatorTest {
    private Kalkulator kalkulator;

    @BeforeEach
    void siapkan() {
        kalkulator = new Kalkulator();                         // instance segar untuk tiap test
    }

    @Test
    @DisplayName("2 + 3 = 5")
    void tambah_duaBilanganPositif() {
        assertEquals(5, kalkulator.tambah(2, 3));
    }

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({"1, 1, 2", "2, 3, 5", "-1, 1, 0", "0, 0, 0"})
    void tambah_banyakKasus(int a, int b, int hasil) {
        assertEquals(hasil, kalkulator.tambah(a, b));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 100, -8})
    void genap_benar(int n) {
        assertTrue(kalkulator.genap(n));
    }

    @Nested
    @DisplayName("Pembagian")
    class Pembagian {
        @Test
        void bagi_normal() {
            assertEquals(4, kalkulator.bagi(8, 2));
        }

        @Test
        void bagi_dengan_nol_melempar_exception() {
            IllegalArgumentException e =
                    assertThrows(IllegalArgumentException.class, () -> kalkulator.bagi(1, 0));
            assertEquals("pembagi tidak boleh nol", e.getMessage());
        }
    }

    @Test
    void beberapaAsersiSekaligus() {
        assertAll("sifat bilangan",
                () -> assertTrue(kalkulator.genap(10)),
                () -> assertFalse(kalkulator.genap(7)),
                () -> assertEquals(0, kalkulator.tambah(-5, 5)));
    }
}
```

```bash
mvn test                       # Maven
./gradlew test                 # Gradle
```

### Mockito: Menguji `PesananService` dengan Mock

```java
// src/test/java/com/contoh/pesanan/PesananServiceTest.java
package com.contoh.pesanan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PesananServiceTest {

    @Mock Repositori repo;
    @Mock Notifikasi notif;
    @Mock KursClient kurs;
    @InjectMocks PesananService service;                       // mock di atas disuntikkan ke constructor

    @Test
    void buat_menyimpanLaluMengirimNotifikasi() {
        service.buat("kopi");

        verify(repo).simpan("kopi");                           // MOCK: verifikasi interaksi
        verify(notif).kirim("Pesanan kopi dibuat");
    }

    @Test
    void buat_gagalSimpan_tidakMengirimNotifikasi() {
        doThrow(new IllegalStateException("db mati")).when(repo).simpan("teh");

        assertThrows(IllegalStateException.class, () -> service.buat("teh"));

        verifyNoInteractions(notif);                           // notifikasi tidak boleh terkirim
    }

    @Test
    void hargaDalamRupiah_memakaiKurs() {
        when(kurs.ambilKurs("USD")).thenReturn(16_000L);       // STUB: tentukan jawaban

        assertEquals(160_000L, service.hargaRupiah(10, "USD"));
        verify(kurs, never()).ambilKurs("EUR");
    }

    @Test
    void buat_pesanNotifikasiDitangkap() {
        ArgumentCaptor<String> pesan = ArgumentCaptor.forClass(String.class);

        service.buat("susu");

        verify(notif).kirim(pesan.capture());
        assertEquals("Pesanan susu dibuat", pesan.getValue());
    }
}
```

> `KursClient` (interface dengan `long ambilKurs(String mata)`) dan method `hargaRupiah(long harga, String mata)` adalah tambahan kecil pada `PesananService` dari Modul 13 agar contoh stubbing terasa nyata; tambahkan sendiri sebagai bagian latihan.

### Integration Test: `@WebMvcTest`, Testcontainers, dan REST Assured

> Import untuk anotasi *test slice* Spring Boot (`@WebMvcTest`, `@DataJpaTest`, `@AutoConfigureMockMvc`) **dipindahkan ke package baru pada Spring Boot 4** — biarkan IDE menambahkan import sesuai versi Anda, atau cek panduan migrasi.

```java
// Test lapisan web saja (cepat): service diganti mock
@WebMvcTest(ProdukController.class)
class ProdukControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ProdukService service;                        // pengganti @MockBean pada Spring Framework 6.2+

    @Test
    void detail_ditemukan() throws Exception {
        given(service.cari(1)).willReturn(Optional.of(new Produk(1, "Kopi", 25_000)));

        mvc.perform(get("/api/produk/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.nama").value("Kopi"));
    }

    @Test
    void buat_validasiGagal_mengembalikan400() throws Exception {
        mvc.perform(post("/api/produk")
                .contentType("application/json")
                .content("""
                        {"nama":"","harga":-5}
                        """))
           .andExpect(status().isBadRequest());
    }
}
```

```java
// Test repository melawan PostgreSQL SUNGGUHAN di container (Testcontainers)
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)   // jangan ganti dengan DB in-memory
class PelangganRepositoryIT {

    @Container
    @ServiceConnection                                          // Spring Boot otomatis mengarahkan datasource ke container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17");

    @Autowired PelangganRepository repo;

    @Test
    void findByKota_terurutNama() {
        repo.save(new Pelanggan("Dewi", "Bandung"));
        repo.save(new Pelanggan("Ani", "Bandung"));
        repo.save(new Pelanggan("Candra", "Jakarta"));

        var hasil = repo.findByKotaOrderByNamaAsc("Bandung");

        assertEquals(List.of("Ani", "Dewi"), hasil.stream().map(Pelanggan::getNama).toList());
    }
}
```

```java
// REST Assured: test black-box terhadap aplikasi yang berjalan di port acak
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProdukApiIT {

    @LocalServerPort int port;

    @Test
    void buatLaluAmbilProduk() {
        String lokasi = given()
                .baseUri("http://localhost").port(port)
                .contentType("application/json")
                .body("""
                        {"nama":"Kopi Gayo","harga":25000}
                        """)
        .when()
                .post("/api/produk")
        .then()
                .statusCode(201)
                .header("Location", containsString("/api/produk/"))
                .body("nama", equalTo("Kopi Gayo"))
                .extract().header("Location");

        given().baseUri("http://localhost").port(port)
        .when().get(lokasi)
        .then().statusCode(200).body("harga", equalTo(25000));
    }
}
```

### TestNG: Padanan dan `@DataProvider`

```java
// src/test/java/com/contoh/app/KalkulatorNgTest.java
package com.contoh.app;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KalkulatorNgTest {
    private Kalkulator k;

    @BeforeMethod
    public void siapkan() { k = new Kalkulator(); }

    @DataProvider(name = "penjumlahan")
    public Object[][] data() {
        return new Object[][] { {1, 1, 2}, {2, 3, 5}, {-1, 1, 0} };
    }

    @Test(dataProvider = "penjumlahan", groups = "cepat")
    public void tambah(int a, int b, int hasil) {
        assertEquals(k.tambah(a, b), hasil);          // catatan: TestNG memakai urutan (aktual, diharapkan)
    }

    @Test(groups = "cepat")
    public void bagiNolMelempar() {
        expectThrows(IllegalArgumentException.class, () -> k.bagi(1, 0));
    }
}
```

```xml
<!-- testng.xml -->
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Suite Kalkulator">
  <test name="cepat">
    <groups><run><include name="cepat"/></run></groups>
    <classes><class name="com.contoh.app.KalkulatorNgTest"/></classes>
  </test>
</suite>
```

### Cucumber-JVM: Skenario Bergaya Bisnis

```gherkin
# src/test/resources/features/keranjang.feature
Feature: Keranjang belanja

  Scenario: Menambah dua produk yang sama
    Given keranjang kosong
    When saya menambah 2 "Kopi" seharga 25000
    Then total keranjang adalah 50000

  Scenario: Keranjang diberi diskon di atas 100000
    Given keranjang kosong
    When saya menambah 5 "Kopi" seharga 25000
    Then total keranjang adalah 112500
```

```java
// src/test/java/com/contoh/bdd/KeranjangSteps.java
package com.contoh.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class KeranjangSteps {
    private Keranjang keranjang;

    @Given("keranjang kosong")
    public void keranjangKosong() {
        keranjang = new Keranjang();
    }

    @When("saya menambah {int} {string} seharga {int}")
    public void tambah(int jumlah, String nama, int harga) {
        keranjang.tambah(nama, jumlah, harga);
    }

    @Then("total keranjang adalah {int}")
    public void totalAdalah(int diharapkan) {
        assertEquals(diharapkan, keranjang.total());
    }
}
```

```java
// src/test/java/com/contoh/bdd/RunCucumberTest.java — runner via JUnit Platform Suite
package com.contoh.bdd;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class RunCucumberTest { }
```

> Class `Keranjang` (dengan `tambah(nama, jumlah, harga)` dan `total()`, memberi **diskon 10% jika total di atas 100.000**) sengaja tidak disertakan: tulis sendiri sampai kedua skenario hijau.

### Load Test dengan JMeter

Langkah menyusun rencana uji di GUI (`jmeter`):

1. **Test Plan** → tambah **Thread Group**: 50 threads, ramp-up 20 detik, loop 20.
2. Tambah **HTTP Request Defaults** (server `localhost`, port `8080`) dan **HTTP Header Manager** (`Content-Type: application/json`).
3. Tambah **HTTP Request** `GET /api/produk` dan `POST /api/produk` dengan body JSON.
4. Tambah **Response Assertion** (status `200`/`201`) dan **Summary Report** sebagai listener.
5. Simpan sebagai `produk.jmx`.

Jalankan beban sesungguhnya lewat **CLI (non-GUI)** dan hasilkan laporan HTML:

```bash
jmeter -n -t produk.jmx -l hasil.jtl -e -o laporan/
# -n: non-GUI   -t: file rencana   -l: log hasil   -e -o: laporan HTML ke folder 'laporan'

# parameterisasi: di JMeter tulis ${__P(threads,10)} pada jumlah thread, lalu:
jmeter -n -t produk.jmx -Jthreads=100 -l hasil.jtl
```

Buka `laporan/index.html` dan perhatikan **throughput**, **persentil p95/p99** pada tabel respons, dan **error %**. Jalankan sambil memantau CPU/memori aplikasi dengan JFR (`jcmd <pid> JFR.start duration=60s filename=rekaman.jfr`) untuk menemukan *bottleneck*.

## Latihan

1. Tulis class `Password` dengan method `kuat(String)` (minimal 8 karakter, mengandung huruf besar, kecil, dan angka). Tulis lebih dulu test JUnit (`@ParameterizedTest` dengan `@CsvSource` untuk minimal 8 kasus), lihat test gagal (merah), lalu implementasikan hingga hijau (alur **TDD**).
2. Tambahkan test pada `MiniUji`: `DiskonService` harus mengembalikan harga penuh pada hari Jumat. Gunakan `Clock.fixed`. Mengapa menginjeksi `Clock` lebih baik daripada memanggil `LocalDate.now()` langsung?
3. Dari kelas `RekeningBank` di [Modul 4](04-class-object.md), buat test JUnit yang mencakup setor, tarik, saldo tidak cukup, dan jumlah negatif (`assertThrows`). Ukur coverage-nya dengan **JaCoCo** dan jelaskan mengapa coverage 100% belum menjamin tidak ada bug.
4. Uji `PesananService` dengan Mockito: tulis satu test yang memakai `verify` dan satu test yang memakai `fake` buatan sendiri untuk `Repositori`. Bandingkan keduanya dan jelaskan kapan Anda memilih mock dibanding fake. Mengapa me-mock `record` atau `List` tidak disarankan?
5. Buat integration test untuk endpoint `POST /api/produk` (Modul 13) memakai `@SpringBootTest` + **REST Assured**, lalu ganti database-nya dengan **Testcontainers PostgreSQL** (Modul 14). Terakhir, jalankan test beban dengan JMeter dan sajikan p95 latency serta throughput dalam tabel singkat.


## Selamat! 🎉

Anda telah menyelesaikan seluruh materi **Java** dari **Basic hingga Advance** — kurikulum yang disusun mengikuti **roadmap.sh Java Developer** (`java.pdf`) dan dilengkapi dokumentasi resmi OpenJDK serta ekosistem Java terbaru (**Java 25 LTS**).

Dari sintaks dasar, OOP, collections, dan stream, hingga concurrency dengan virtual thread, build tools, dependency injection, Spring Boot, akses database, dan strategi testing — Anda kini memiliki fondasi **pengembang backend Java modern**. Bersama course **Docker**, **Kubernetes**, **Terraform**, **Amazon EKS**, **Apache Kafka**, **Elasticsearch**, **Keycloak**, dan **Redis**, Java adalah bahasa tempat semua komponen itu bertemu: aplikasi yang Anda tulis, kemas, deploy, hubungkan, cari, amankan, dan percepat.

Langkah selanjutnya yang direkomendasikan:
- **Bangun proyek end-to-end** — satu backend Spring Boot yang: menyimpan data di PostgreSQL (Modul 14), men-cache dengan **Redis**, mempublikasikan event ke **Kafka**, mengindeks pencarian ke **Elasticsearch**, diamankan dengan **Keycloak**, dikemas dengan **Docker**, di-deploy ke **EKS/Kubernetes** lewat **Terraform**, dan dilengkapi unit + integration test serta pipeline CI. Inilah arsitektur nyata yang dipakai banyak perusahaan.
- **Perdalam fondamental** lewat buku klasik: *Effective Java* (Joshua Bloch), *Java Concurrency in Practice* (Brian Goetz dkk.), dan *Modern Java in Action*.
- **Perdalam ekosistem Spring** — Spring Security, Spring Data lanjutan, Spring Cloud, Spring Batch, dan arsitektur microservice / event-driven.
- **Eksplor performa & cloud-native** — GraalVM Native Image, tuning JVM dan GC, observabilitas dengan Micrometer/OpenTelemetry.
- **Validasi kemampuan** lewat sertifikasi **Oracle Certified Professional: Java SE Developer** atau sertifikasi **Spring Professional**.
- **Ikuti perkembangan platform** — baca JEP di [openjdk.org](https://openjdk.org), terutama kemajuan Project Valhalla, Loom, dan Leyden; LTS berikutnya (Java 29) dijadwalkan September 2027.

Kembali ke [Daftar Isi](README.md) untuk meninjau ulang modul mana pun yang perlu diperdalam.

---

[← Modul 14: Database Access: JDBC, Hibernate & Spring Data JPA](14-database-access.md) · [Daftar Isi](README.md#daftar-isi)
