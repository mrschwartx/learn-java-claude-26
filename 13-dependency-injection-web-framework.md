# Modul 13: Dependency Injection & Web Framework

[← Modul 12: Build Tools, Java Modules & Logging](12-build-tools-modules-logging.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 14: Database Access: JDBC, Hibernate & Spring Data JPA →](14-database-access.md)

**Isi modul ini**

- [Teori](#teori)
  - [13.1 Dependency Injection (DI)](#131-dependency-injection-di)
  - [13.2 DI di Spring Framework](#132-di-di-spring-framework)
  - [13.3 Lanskap Web Framework Java](#133-lanskap-web-framework-java)
  - [13.4 Spring Boot](#134-spring-boot)
  - [13.5 Quarkus, Play, dan Javalin — Ringkasan Cara Kerja](#135-quarkus-play-dan-javalin--ringkasan-cara-kerja)
  - [13.6 Memilih Framework](#136-memilih-framework)
- [Praktik](#praktik)
  - [DI Manual vs Container: Membangun `MiniContainer`](#di-manual-vs-container-membangun-minicontainer)
  - [Spring Boot: REST API Produk](#spring-boot-rest-api-produk)
  - [Javalin, Quarkus, dan Play — Versi Singkat](#javalin-quarkus-dan-play--versi-singkat)
- [Latihan](#latihan)

---


## Teori

### 13.1 Dependency Injection (DI)

Hampir setiap class bergantung pada class lain. Misalnya `PesananService` membutuhkan sesuatu untuk menyimpan data dan sesuatu untuk mengirim notifikasi. Ada dua cara memperolehnya:

```java
// Cara 1: class MEMBUAT dependency-nya sendiri (kaku, sulit diuji)
class PesananService {
    private final Repositori repo = new RepositoriPostgres();     // terikat erat ke implementasi
    private final Notifikasi notif = new NotifikasiEmail();
}

// Cara 2: dependency DIBERIKAN dari luar (Dependency Injection)
class PesananService {
    private final Repositori repo;
    private final Notifikasi notif;
    PesananService(Repositori repo, Notifikasi notif) {           // constructor injection
        this.repo = repo;
        this.notif = notif;
    }
}
```

**Inversion of Control (IoC)** adalah prinsip di baliknya: kontrol atas *pembuatan dan perangkaian* object dibalik — bukan class yang mencari dependency-nya, melainkan pihak luar yang **menyuntikkannya**. Manfaatnya:

- **Loose coupling** — class bergantung pada **interface**, bukan implementasi konkret; implementasi dapat diganti tanpa mengubah class.
- **Testability** — pada unit test, dependency asli (database, email) dapat diganti **tiruan** (mock/fake) ([Modul 15](15-testing-advanced.md)).
- **Konfigurasi terpusat** — perangkaian object ada di satu tempat.

Anda bisa melakukan DI **secara manual** (memanggil `new` di `main`). Ketika aplikasi membesar, perangkaian manual menjadi melelahkan, sehingga dipakai **DI container**: sebuah framework yang membuat object (disebut **bean**), memetakan dependency-nya, dan mengelola siklus hidupnya — contoh: **Spring**, **Jakarta CDI** (dipakai Quarkus dan Jakarta EE), **Google Guice**, dan **Dagger** (DI saat kompilasi).

**Jenis injeksi:**

| Jenis | Contoh | Penilaian |
|---|---|---|
| **Constructor injection** | Parameter constructor | **Disarankan** — dependency wajib jelas, object selalu valid, field bisa `final`, mudah diuji |
| Setter injection | Method `setX(...)` | Untuk dependency opsional |
| Field injection | `@Autowired` langsung di field | **Dihindari** — sulit diuji tanpa container, menyembunyikan dependency |

Anotasi ([Modul 6](06-fitur-modern.md)) dan reflection adalah mesin di balik container DI: container membaca anotasi seperti `@Component`, memeriksa constructor lewat reflection, lalu membangun object. Anda akan membuktikannya sendiri lewat contoh praktik `MiniContainer`.

### 13.2 DI di Spring Framework

**Spring Framework** adalah fondasi ekosistem Java enterprise. Intinya adalah **`ApplicationContext`** (IoC container) yang mengelola **bean**.

| Anotasi | Fungsi |
|---|---|
| `@Component` | Menandai class sebagai bean yang ditemukan lewat *component scanning* |
| `@Service`, `@Repository`, `@Controller`/`@RestController` | Spesialisasi `@Component` yang menyatakan peran lapisan (service / akses data / web) |
| `@Configuration` + `@Bean` | Mendefinisikan bean lewat method (untuk class pihak ketiga yang tak bisa diberi anotasi) |
| `@Autowired` | Meminta injeksi (opsional pada constructor tunggal) |
| `@Value("${nama.properti}")`, `@ConfigurationProperties` | Menyuntikkan konfigurasi |
| `@Profile("dev")` | Bean hanya aktif pada profil tertentu |
| `@Primary`, `@Qualifier` | Memilih bean jika ada beberapa kandidat |

**Scope bean:** `singleton` (**default** — satu instance per container), `prototype` (instance baru tiap diminta), serta `request` dan `session` untuk aplikasi web. Karena bean singleton dipakai banyak thread sekaligus ([Modul 10](10-concurrency.md)), **bean sebaiknya stateless**.

### 13.3 Lanskap Web Framework Java

Roadmap menyebut empat framework web, dengan catatan **Spring Boot direkomendasikan**:

| Framework | Karakter | Kapan dipilih |
|---|---|---|
| **Spring Boot** | Paling populer & lengkap; konvensi-di-atas-konfigurasi, ekosistem sangat luas (Data, Security, Cloud, Kafka, …) | Pilihan default untuk backend enterprise |
| **Quarkus** | "Supersonic Subatomic Java"; **build-time DI**, startup cepat, memori kecil, dukungan **GraalVM native image** kuat, mode `dev` dengan *live reload* | Microservice/serverless/container dengan startup cepat |
| **Play Framework** | Reaktif, **stateless**, non-blocking; ditulis dengan Scala, dapat dipakai dengan Java atau Scala; berbasis **sbt** | Aplikasi web reaktif; komunitas lebih kecil |
| **Javalin** | **Ringan** (lebih "library" daripada framework), API sederhana, Java & Kotlin, tanpa magic anotasi | Layanan kecil, belajar dasar HTTP, prototipe |
| Micronaut, Helidon | Alternatif modern (DI saat kompilasi) | Mirip Quarkus |

Semuanya pada dasarnya melayani **HTTP request** dan memetakan **URL + metode HTTP** ke kode Anda. Pengingat singkat konsep REST:

| Metode | Arti | Status sukses umum |
|---|---|---|
| `GET` | Baca | `200 OK` |
| `POST` | Buat | `201 Created` |
| `PUT` / `PATCH` | Ganti / ubah sebagian | `200 OK` / `204 No Content` |
| `DELETE` | Hapus | `204 No Content` |

Kesalahan klien: `400 Bad Request`, `401/403` (autentikasi/otorisasi), `404 Not Found`, `409 Conflict`, `422`. Kesalahan server: `500` dan seterusnya.

### 13.4 Spring Boot

**Spring Boot** menghilangkan konfigurasi boilerplate Spring dengan tiga ide utama:

1. **Starter** — satu dependency membawa seperangkat library yang selaras (`spring-boot-starter-webmvc` untuk web, `...-data-jpa`, `...-validation`, `...-actuator`, dst.).
2. **Auto-configuration** — Boot menyiapkan bean secara otomatis berdasarkan **dependency yang ada di classpath** dan properti Anda (misal: ada driver PostgreSQL + JPA → otomatis membuat `DataSource` dan `EntityManager`).
3. **Embedded server** — Tomcat (secara default) tertanam di dalam JAR; aplikasi dijalankan dengan `java -jar`, tanpa memasang server terpisah. Cocok untuk Docker/Kubernetes.

Versi acuan course ini: **Spring Boot 4.x** (berbasis **Spring Framework 7**, Jakarta EE 11, Java minimal 17 dan mendukung Java 25). Hal yang berubah dibanding Boot 3 dan patut diperhatikan saat membaca tutorial lama: starter web kini bernama **`spring-boot-starter-webmvc`** (sebelumnya `spring-boot-starter-web`), starter test dipecah per modul (misal `spring-boot-starter-webmvc-test`), dan Boot 4 beralih ke **Jackson 3** untuk JSON (package `tools.jackson`). Selalu rujuk dokumentasi resmi versi yang Anda pakai.

**Anatomi aplikasi Boot** — lapisan yang umum:

```
Controller (HTTP)  →  Service (logika bisnis)  →  Repository (akses data, Modul 14)
```

Fitur yang sering dipakai: `@RestController` + `@GetMapping`/`@PostMapping`, `@PathVariable`/`@RequestParam`/`@RequestBody`, validasi **Jakarta Validation** (`@Valid`, `@NotBlank`, `@Positive`), penanganan error global dengan `@RestControllerAdvice` (mengembalikan `ProblemDetail`, standar RFC 9457), konfigurasi lewat `application.yml`, profil lingkungan, dan **Actuator** untuk endpoint kesehatan/metrik (`/actuator/health`) — penting untuk probe Kubernetes dan monitoring.

### 13.5 Quarkus, Play, dan Javalin — Ringkasan Cara Kerja

- **Quarkus** memakai standar **Jakarta REST** (`@Path`, `@GET`) dan **CDI** (`@ApplicationScoped`, `@Inject`). Berbeda dengan Spring, banyak pekerjaan reflection dipindah ke **waktu build**, menghasilkan startup milidetik pada native image. Perintah utama: `quarkus dev` (live coding), `quarkus build`.
- **Play** mendefinisikan URL di file **`conf/routes`** dan menulis controller yang mengembalikan `Result`. Dijalankan dengan **sbt** (`sbt run`). Sejak Play 3 mesin aktornya memakai Apache **Pekko**.
- **Javalin** hanya butuh beberapa baris: buat aplikasi, daftarkan handler `(ctx) -> ...`, lalu `start(port)`. Cocok untuk memahami HTTP tanpa lapisan abstraksi.

### 13.6 Memilih Framework

| Kebutuhan | Saran |
|---|---|
| Tim besar, ekosistem lengkap, banyak lowongan & dokumentasi | **Spring Boot** |
| Startup cepat, footprint kecil, native image / serverless | **Quarkus** (atau Spring Boot + GraalVM) |
| Aplikasi mikro/sederhana, ingin mengerti dasar-dasarnya | **Javalin** |
| Sudah memakai Scala / sistem reaktif penuh | **Play** |

Pilihan yang aman untuk karier backend Java: **kuasai Spring Boot lebih dulu**, lalu pelajari alternatif lain sebagai pembanding.

## Praktik

### DI Manual vs Container: Membangun `MiniContainer`

```java
// DemoContainer.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoContainer {
    public static void main(String[] args) {
        // 1) DI MANUAL: kita merangkai sendiri dengan 'new'
        PesananService manual = new PesananService(new RepositoriMemori(), new NotifikasiEmail());
        manual.buat("kopi");

        // 2) DI lewat CONTAINER: container membaca constructor lalu merangkai otomatis
        MiniContainer produksi = new MiniContainer(PesananService.class, RepositoriMemori.class, NotifikasiEmail.class);
        produksi.ambil(PesananService.class).buat("teh");

        // 3) Ganti implementasi TANPA mengubah PesananService (inilah kekuatan loose coupling)
        MiniContainer pengujian = new MiniContainer(PesananService.class, RepositoriMemori.class, NotifikasiPalsu.class);
        pengujian.ambil(PesananService.class).buat("susu");

        // Singleton: container memberi instance yang sama
        System.out.println("Singleton? " + (produksi.ambil(PesananService.class) == produksi.ambil(PesananService.class)));

        // Bean tak terdaftar -> gagal jelas
        try {
            new MiniContainer(PesananService.class).ambil(PesananService.class);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

// ---- Anotasi & container sederhana (mirip @Component + ApplicationContext Spring) ----
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Komponen { }

class MiniContainer {
    private final List<Class<?>> terdaftar = new ArrayList<>();
    private final Map<Class<?>, Object> singleton = new HashMap<>();

    MiniContainer(Class<?>... kelas) {
        for (Class<?> k : kelas) {
            if (!k.isAnnotationPresent(Komponen.class)) {
                throw new IllegalArgumentException(k.getSimpleName() + " tidak ditandai @Komponen");
            }
            terdaftar.add(k);
        }
    }

    @SuppressWarnings("unchecked")
    <T> T ambil(Class<T> tipe) {
        Class<?> impl = terdaftar.stream()
                .filter(tipe::isAssignableFrom)                       // cari implementasi yang cocok dengan interface
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tidak ada bean untuk " + tipe.getSimpleName()));

        Object ada = singleton.get(impl);
        if (ada != null) return (T) ada;

        try {
            Constructor<?> ctor = impl.getDeclaredConstructors()[0];
            Class<?>[] tipeParam = ctor.getParameterTypes();
            Object[] argumen = new Object[tipeParam.length];
            for (int i = 0; i < tipeParam.length; i++) {
                argumen[i] = ambil(tipeParam[i]);                     // rekursif: selesaikan dependency dulu
            }
            ctor.setAccessible(true);
            Object baru = ctor.newInstance(argumen);
            singleton.put(impl, baru);
            return (T) baru;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Gagal membuat " + impl.getSimpleName(), e);
        }
    }
}

// ---- Kontrak (interface) & implementasi ----
interface Repositori { void simpan(String item); }
interface Notifikasi { void kirim(String pesan); }

@Komponen
class RepositoriMemori implements Repositori {
    private final List<String> data = new ArrayList<>();
    public void simpan(String item) { data.add(item); System.out.println("  [repo] disimpan: " + item); }
}

@Komponen
class NotifikasiEmail implements Notifikasi {
    public void kirim(String pesan) { System.out.println("  [email] " + pesan); }
}

@Komponen
class NotifikasiPalsu implements Notifikasi {
    public void kirim(String pesan) { System.out.println("  [palsu] (tidak benar-benar mengirim) " + pesan); }
}

@Komponen
class PesananService {
    private final Repositori repo;
    private final Notifikasi notif;

    PesananService(Repositori repo, Notifikasi notif) {           // constructor injection
        this.repo = repo;
        this.notif = notif;
    }

    void buat(String item) {
        repo.simpan(item);
        notif.kirim("Pesanan " + item + " dibuat");
    }
}
```

Perhatikan bahwa `PesananService` **tidak pernah** menyebut `new` untuk dependency-nya dan hanya mengenal **interface**. Container nyata seperti Spring bekerja dengan prinsip yang sama, ditambah banyak fitur (scope, siklus hidup, AOP, deteksi dependensi melingkar, dst.).

### Spring Boot: REST API Produk

Buat proyek dengan **[start.spring.io](https://start.spring.io)**: Project *Maven*, Language *Java*, Spring Boot **4.x**, Java **25**, dependency **Spring Web**, **Validation**, dan **Actuator**. Atau tulis `pom.xml` seperti berikut:

```xml
<!-- pom.xml (ringkas) -->
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.0</version>
  <relativePath/>
</parent>

<properties>
  <java.version>25</java.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

```java
// src/main/java/com/contoh/toko/TokoApplication.java
package com.contoh.toko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication      // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class TokoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TokoApplication.class, args);
    }
}
```

```java
// src/main/java/com/contoh/toko/produk/Produk.java
package com.contoh.toko.produk;

public record Produk(long id, String nama, long harga) { }
```

```java
// src/main/java/com/contoh/toko/produk/ProdukRequest.java
package com.contoh.toko.produk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProdukRequest(
        @NotBlank(message = "nama wajib diisi") String nama,
        @Positive(message = "harga harus positif") long harga) { }
```

```java
// src/main/java/com/contoh/toko/produk/ProdukService.java
package com.contoh.toko.produk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ProdukService {
    private final Map<Long, Produk> data = new ConcurrentHashMap<>();   // bean singleton -> harus thread-safe
    private final AtomicLong urutan = new AtomicLong();

    public List<Produk> semua() {
        return new ArrayList<>(data.values());
    }

    public Optional<Produk> cari(long id) {
        return Optional.ofNullable(data.get(id));
    }

    public Produk buat(ProdukRequest req) {
        long id = urutan.incrementAndGet();
        Produk baru = new Produk(id, req.nama(), req.harga());
        data.put(id, baru);
        return baru;
    }
}
```

```java
// src/main/java/com/contoh/toko/produk/ProdukController.java
package com.contoh.toko.produk;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produk")
public class ProdukController {
    private final ProdukService service;

    public ProdukController(ProdukService service) {      // constructor injection (tanpa @Autowired)
        this.service = service;
    }

    @GetMapping
    public List<Produk> semua() {
        return service.semua();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produk> detail(@PathVariable long id) {
        return service.cari(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produk> buat(@Valid @RequestBody ProdukRequest req) {
        Produk baru = service.buat(req);
        return ResponseEntity.created(URI.create("/api/produk/" + baru.id())).body(baru);   // 201 + header Location
    }
}
```

```java
// src/main/java/com/contoh/toko/PenangananError.java
package com.contoh.toko;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PenangananError {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validasi(MethodArgumentNotValidException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Data tidak valid");
        pd.setProperty("galat", e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList());
        return pd;
    }
}
```

```yaml
# src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: toko
  threads:
    virtual:
      enabled: true            # request ditangani virtual thread (Modul 10)

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Jalankan dan uji dengan `curl`:

```bash
./mvnw spring-boot:run

curl -s localhost:8080/api/produk
curl -s -X POST localhost:8080/api/produk \
     -H "Content-Type: application/json" \
     -d '{"nama":"Kopi Gayo","harga":25000}'
curl -s localhost:8080/api/produk/1
curl -i localhost:8080/api/produk/99                      # 404
curl -s -X POST localhost:8080/api/produk \
     -H "Content-Type: application/json" \
     -d '{"nama":"","harga":-5}'                          # 400 + ProblemDetail
curl -s localhost:8080/actuator/health                    # {"status":"UP"}
```

```bash
./mvnw clean package
java -jar target/toko-0.0.1-SNAPSHOT.jar                  # jar tunggal berisi Tomcat tertanam
```

> Contoh Spring Boot di atas ditulis sesuai dokumentasi Spring Boot 4.x tetapi **tidak dijalankan** pada lingkungan penyusunan materi (tanpa akses ke Maven Central). Jika versi Boot Anda berbeda, sesuaikan nama starter/dependency dengan dokumentasi resmi.

### Javalin, Quarkus, dan Play — Versi Singkat

```java
// Javalin (6.x) — dependency: io.javalin:javalin dan slf4j-simple (untuk log)
import io.javalin.Javalin;

public class JavalinApp {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.useVirtualThreads = true)
                .get("/halo", ctx -> ctx.result("Halo dari Javalin"))
                .get("/produk/{id}", ctx -> ctx.result("Produk #" + ctx.pathParam("id")))
                .start(7070);
    }
}
```

```java
// Quarkus — buat proyek: quarkus create app com.contoh:demo-quarkus --extension=rest
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/halo")
public class HaloResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String halo() {
        return "Halo dari Quarkus";
    }

    @GET
    @Path("/{nama}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sapa(@PathParam("nama") String nama) {
        return "Halo, " + nama;
    }
}
// jalankan: quarkus dev   → http://localhost:8080/halo
```

```java
// Play Framework (Java) — app/controllers/HaloController.java
package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class HaloController extends Controller {
    public Result halo() {
        return ok("Halo dari Play");
    }
}
```

```
# Play — conf/routes
GET     /halo        controllers.HaloController.halo()
# jalankan: sbt run   → http://localhost:9000/halo
```

## Latihan

1. Tambahkan interface `Pembayaran` beserta dua implementasi ke contoh `DemoContainer`, lalu suntikkan ke `PesananService`. Sebelum itu, **prediksi** apa yang terjadi jika kedua implementasi didaftarkan sekaligus, lalu jelaskan bagaimana Spring menyelesaikan kasus ini dengan `@Primary`/`@Qualifier`.
2. Tambahkan deteksi **dependensi melingkar** (A butuh B, B butuh A) pada `MiniContainer` sehingga melempar exception yang jelas alih-alih `StackOverflowError`. Mengapa constructor injection membuat masalah ini langsung ketahuan?
3. Jelaskan, dengan merujuk contoh Anda, tiga alasan constructor injection lebih disarankan daripada field injection `@Autowired`, dan tunjukkan bagaimana Anda akan menguji `PesananService` secara manual tanpa container apa pun.
4. Bangun REST API Spring Boot di atas, lalu lengkapi dengan `PUT /api/produk/{id}` dan `DELETE /api/produk/{id}`. Pastikan status code yang dikembalikan tepat (200/204/404), dan tambahkan satu aturan validasi baru pada `ProdukRequest`.
5. Implementasikan endpoint yang sama (`GET /halo` dan `GET /produk/{id}`) dengan Javalin atau Quarkus, lalu buat tabel perbandingan Spring Boot vs pilihan Anda dari sisi: jumlah baris kode, waktu startup, ukuran artefak, dan kemudahan menambah fitur (validasi, keamanan, akses database).

---

[← Modul 12: Build Tools, Java Modules & Logging](12-build-tools-modules-logging.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 14: Database Access: JDBC, Hibernate & Spring Data JPA →](14-database-access.md)
