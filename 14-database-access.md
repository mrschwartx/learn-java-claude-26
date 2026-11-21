# Modul 14: Database Access: JDBC, Hibernate & Spring Data JPA

[← Modul 13: Dependency Injection & Web Framework](13-dependency-injection-web-framework.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 15: Testing & Advanced Topics →](15-testing-advanced.md)

**Isi modul ini**

- [Teori](#teori)
  - [14.1 Gambaran Umum Akses Database di Java](#141-gambaran-umum-akses-database-di-java)
  - [14.2 JDBC (Java Database Connectivity)](#142-jdbc-java-database-connectivity)
  - [14.3 ORM dan JPA](#143-orm-dan-jpa)
  - [14.4 Hibernate](#144-hibernate)
  - [14.5 Spring Data JPA](#145-spring-data-jpa)
  - [14.6 EBean](#146-ebean)
  - [14.7 Transaksi dan Isolation Level](#147-transaksi-dan-isolation-level)
  - [14.8 Memilih Pendekatan](#148-memilih-pendekatan)
- [Praktik](#praktik)
  - [Menjalankan PostgreSQL dengan Docker Compose](#menjalankan-postgresql-dengan-docker-compose)
  - [JDBC Murni: Query, Transaksi, dan Batch](#jdbc-murni-query-transaksi-dan-batch)
  - [JPA + Spring Data JPA: Entity, Repository, dan Service](#jpa--spring-data-jpa-entity-repository-dan-service)
- [Latihan](#latihan)

---


## Teori

### 14.1 Gambaran Umum Akses Database di Java

Hampir semua aplikasi backend menyimpan data di database relasional (PostgreSQL, MySQL, dst.). Java menyediakan lapisan akses bertingkat:

```
Aplikasi Anda
   │
   ├── Spring Data JPA        ← repository otomatis (paling abstrak)
   ├── JPA / Hibernate, EBean ← ORM: object ⇄ baris tabel
   ├── jOOQ, MyBatis, JdbcClient ← SQL tetap terlihat, hasil dipetakan ke object
   └── JDBC                   ← standar dasar: kirim SQL, baca hasil (paling rendah)
        │
   Driver JDBC (postgresql.jar, mysql-connector.jar, ...)
        │
   Database
```

Semua pendekatan di atas pada akhirnya **berujung pada JDBC**. Memahami JDBC memberi dasar untuk men-debug masalah di lapisan atas.

Pengingat konsep database: tabel dengan **primary key** dan **foreign key**, **index** (mempercepat pencarian, memperlambat penulisan), dan **transaksi ACID** — *Atomicity* (semua atau tidak sama sekali), *Consistency*, *Isolation*, *Durability*.

### 14.2 JDBC (Java Database Connectivity)

**JDBC** adalah API standar (`java.sql`, `javax.sql`) untuk berinteraksi dengan database relasional. Komponen utamanya:

| Komponen | Fungsi |
|---|---|
| **Driver** | Implementasi JDBC dari vendor database (JAR terpisah, misal `org.postgresql:postgresql`) |
| `DataSource` | Pabrik koneksi (biasanya berupa *connection pool*) — **disarankan** dibanding `DriverManager` |
| `Connection` | Sesi ke database; tempat transaksi dikelola |
| `PreparedStatement` | Perintah SQL berparameter (`?`) |
| `ResultSet` | Hasil query, dibaca baris demi baris |

**Selalu pakai `PreparedStatement`** dengan parameter `?`, jangan menyambung string SQL dari input pengguna. Ini bukan sekadar kebiasaan baik, melainkan **pertahanan utama terhadap SQL injection**:

```java
// BERBAHAYA — input "x' OR '1'='1" mengubah arti query:
String sql = "SELECT * FROM pengguna WHERE nama = '" + input + "'";

// AMAN — nilai dikirim sebagai parameter, bukan bagian dari SQL:
PreparedStatement ps = c.prepareStatement("SELECT * FROM pengguna WHERE nama = ?");
ps.setString(1, input);
```

**Transaksi** — secara default JDBC berjalan **auto-commit** (setiap statement langsung permanen). Untuk beberapa operasi yang harus berhasil bersama (misal transfer saldo), matikan auto-commit, lalu `commit()` jika semua sukses atau `rollback()` jika ada yang gagal.

**Praktik baik:**

- Tutup `Connection`, `PreparedStatement`, dan `ResultSet` dengan **try-with-resources** ([Modul 8](08-exception-io.md)).
- Gunakan **connection pool** — membuka koneksi database itu mahal. **HikariCP** adalah pool standar (default di Spring Boot).
- Gunakan **batch** (`addBatch` / `executeBatch`) untuk banyak insert/update sekaligus.
- Gunakan tipe `java.time` langsung: `rs.getObject("dibuat", LocalDateTime.class)`.
- Jangan menyimpan koneksi di field `static`; ambil dari pool, pakai, kembalikan (dengan `close()`).

### 14.3 ORM dan JPA

Menulis JDBC manual untuk setiap tabel berarti banyak kode berulang: SQL `INSERT`/`SELECT`, memetakan kolom ke field, mengelola relasi. **ORM** (*Object-Relational Mapping*) mengotomatiskan pemetaan antara **object Java** dan **baris tabel**.

**JPA** (*Jakarta Persistence API*, dahulu *Java Persistence API*) adalah **spesifikasi standar** ORM; **Hibernate** adalah implementasi paling populer. Anda menulis kode terhadap API `jakarta.persistence`, sementara Hibernate yang menghasilkan SQL-nya.

**Konsep inti JPA:**

- **Entity** — class yang dipetakan ke tabel dengan `@Entity`; `@Id` menandai primary key.
- **`EntityManager`** — pintu masuk operasi (`persist`, `find`, `merge`, `remove`, `createQuery`).
- **Persistence context** — "area kerja" yang melacak entity yang sedang dikelola dalam satu transaksi. Berkat ini ada **dirty checking**: jika Anda mengubah field entity yang *managed*, Hibernate **otomatis** menghasilkan `UPDATE` saat commit tanpa perlu memanggil `save`.
- **Siklus hidup entity:** *transient* (baru, belum dikenal) → *managed* (dilacak) → *detached* (konteks sudah ditutup) → *removed*.
- **Relasi:** `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne`. Sisi pemilik relasi memegang foreign key (`@JoinColumn`); sisi sebaliknya memakai `mappedBy`.
- **Fetch type:** **`LAZY`** (data relasi baru diambil saat diakses) vs **`EAGER`** (langsung diambil). **Praktik yang disarankan: `LAZY` untuk semua relasi**, lalu ambil data tambahan secara eksplisit saat memang dibutuhkan.
- **JPQL** — bahasa query berorientasi object (`select p from Pelanggan p where p.kota = :kota`), diterjemahkan menjadi SQL.

**Masalah N+1 — jebakan performa ORM paling terkenal.** Misal Anda mengambil 100 pelanggan (1 query), lalu untuk setiap pelanggan mengakses `getPesanan()` yang `LAZY` (100 query tambahan) — total **101 query** padahal cukup satu. Solusi: `JOIN FETCH` di JPQL, `@EntityGraph`, atau batch fetching. **Biasakan melihat SQL yang dihasilkan** (`logging.level.org.hibernate.SQL=DEBUG`) selama pengembangan.

**Locking:** untuk mencegah dua pengguna saling menimpa data, JPA menyediakan **optimistic locking** (kolom `@Version`; update gagal jika versi berubah — ideal untuk konflik jarang) dan **pessimistic locking** (`SELECT ... FOR UPDATE`; menahan baris untuk konflik sering).

### 14.4 Hibernate

**Hibernate ORM** adalah implementasi JPA terlengkap dan menjadi default di Spring Boot. Selain fitur JPA, ia menyediakan *second-level cache*, *batch fetching*, *natural id*, filter, dan HQL (superset JPQL). Spring Boot 4 memakai **Hibernate 7** dengan Jakarta Persistence 3.2.

Pengaturan yang penting:

| Properti | Nilai & catatan |
|---|---|
| `spring.jpa.hibernate.ddl-auto` | `none` / `validate` untuk **production**. `update`/`create` hanya untuk eksperimen — **jangan** di production karena bisa merusak/mengubah skema tanpa kontrol |
| `spring.jpa.open-in-view` | Setel **`false`**: default `true` menahan koneksi database selama seluruh request web dan menyembunyikan masalah lazy loading |
| `spring.jpa.properties.hibernate.format_sql` | Merapikan SQL di log |
| `logging.level.org.hibernate.SQL` | `DEBUG` untuk melihat SQL |

**Migrasi skema** dikelola dengan **Flyway** atau **Liquibase**: skrip SQL berversi (`V1__init.sql`, `V2__tambah_kolom.sql`) yang dijalankan berurutan dan tercatat — skema database ikut di-*version control* seperti kode.

### 14.5 Spring Data JPA

**Spring Data JPA** menghilangkan hampir semua kode repository. Anda cukup mendeklarasikan **interface**, dan Spring membuat implementasinya saat runtime:

```java
public interface PelangganRepository extends JpaRepository<Pelanggan, Long> {
    List<Pelanggan> findByKotaOrderByNamaAsc(String kota);       // query diturunkan dari NAMA method
    Optional<Pelanggan> findByNamaIgnoreCase(String nama);
    boolean existsByNama(String nama);
    long countByKota(String kota);
    Page<Pelanggan> findByKota(String kota, Pageable pageable);  // paginasi & sorting bawaan

    @Query("select p from Pelanggan p left join fetch p.pesanan where p.kota = :kota")
    List<Pelanggan> cariDenganPesanan(@Param("kota") String kota);   // JPQL kustom (mengatasi N+1)
}
```

`JpaRepository` sudah menyediakan `save`, `findById`, `findAll`, `deleteById`, `count`, dll. Anotasi **`@Transactional`** pada method service membuka transaksi dan melakukan *commit* saat method selesai (atau *rollback* saat terjadi `RuntimeException`).

Catatan penting: `@Transactional` bekerja lewat **proxy**, sehingga **tidak berlaku jika method dipanggil dari dalam class yang sama** (*self-invocation*), dan hanya berlaku pada method yang `public`.

### 14.6 EBean

**Ebean** adalah ORM alternatif untuk Java dan Kotlin dengan filosofi berbeda dari JPA/Hibernate: lebih **sederhana dan "tanpa sesi"**. Tidak ada *persistence context* jangka panjang atau konsep *detached* yang membingungkan; query langsung mengembalikan object, dan object dapat disimpan kapan saja.

Ciri khasnya:

- **Query beans** — class pendamping yang dihasilkan saat kompilasi (annotation processor) sehingga query **type-safe** (`new QPelanggan().kota.eq("Bandung")`) — kesalahan nama field tertangkap compiler.
- **Bytecode enhancement** pada saat build untuk lazy loading dan pelacakan perubahan.
- Mudah mencampur ORM dengan **SQL native** dan memetakan hasil ke **DTO**.
- Dapat dipakai dengan atau tanpa Spring.

```java
@Entity
public class Pelanggan extends Model {              // Model memberi method save(), delete(), dst.
    @Id Long id;
    String nama;
    String kota;
    // getter/setter...
}

// Query dinamis berbasis string
List<Pelanggan> bandung = DB.find(Pelanggan.class)
        .where().eq("kota", "Bandung")
        .orderBy("nama")
        .findList();

// Query bean (type-safe, dihasilkan saat kompilasi)
List<Pelanggan> sama = new QPelanggan().kota.eq("Bandung").orderBy().nama.asc().findList();

Pelanggan baru = new Pelanggan();
baru.setNama("Ani");
baru.save();                                        // INSERT; tanpa EntityManager/sesi
```

> Sintaks dan setup Ebean (annotation processor, enhancement plugin, `DB` default) berubah antar versi — ikuti panduan resmi di [ebean.io](https://ebean.io) untuk versi yang Anda pakai.

### 14.7 Transaksi dan Isolation Level

Saat banyak transaksi berjalan bersamaan, anomali bisa terjadi. **Isolation level** menentukan anomali mana yang dicegah:

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|:---:|:---:|:---:|
| `READ_UNCOMMITTED` | mungkin | mungkin | mungkin |
| `READ_COMMITTED` | **dicegah** | mungkin | mungkin |
| `REPEATABLE_READ` | dicegah | **dicegah** | mungkin* |
| `SERIALIZABLE` | dicegah | dicegah | **dicegah** |

\* Pada beberapa database (misal PostgreSQL) `REPEATABLE_READ` juga mencegah phantom read.

Istilah: **dirty read** = membaca data yang belum di-commit transaksi lain; **non-repeatable read** = membaca baris yang sama dua kali dan hasilnya berbeda; **phantom read** = query yang sama mengembalikan himpunan baris berbeda. Default umumnya `READ_COMMITTED` (PostgreSQL, Oracle) — cukup untuk sebagian besar kasus. Level lebih ketat memberi konsistensi lebih kuat dengan biaya konkurensi.

### 14.8 Memilih Pendekatan

| Pendekatan | Kekuatan | Kelemahan | Cocok untuk |
|---|---|---|---|
| **JDBC / `JdbcClient`** | Kontrol penuh, ringan, SQL jelas | Kode pemetaan manual | Query kompleks, pelaporan, performa kritis |
| **JPA/Hibernate + Spring Data JPA** | Produktif untuk CRUD, relasi, standar luas | Kurva belajar, jebakan (N+1, lazy), SQL "tersembunyi" | Aplikasi bisnis dengan model domain kaya |
| **EBean** | Sederhana, type-safe, tanpa sesi | Komunitas lebih kecil | Tim yang ingin ORM lebih sederhana |
| **jOOQ / MyBatis** | SQL tetap kelas satu, type-safe (jOOQ) | Lebih banyak kode query | Tim yang fasih SQL dan butuh kontrol |

Pola yang sering dipakai di praktik: **JPA untuk operasi CRUD harian, SQL/JDBC untuk laporan dan query berat** — keduanya bisa berdampingan dalam satu aplikasi.

## Praktik

### Menjalankan PostgreSQL dengan Docker Compose

Konsisten dengan course Docker sebelumnya, kita jalankan database di container:

```yaml
# docker-compose.yml
services:
  db:
    image: postgres:17
    environment:
      POSTGRES_DB: toko
      POSTGRES_USER: belajar
      POSTGRES_PASSWORD: rahasia
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

```bash
docker compose up -d
docker compose exec db psql -U belajar -d toko
```

```sql
-- schema.sql (jalankan di psql, atau jadikan V1__init.sql untuk Flyway)
CREATE TABLE rekening (
    id      BIGSERIAL PRIMARY KEY,
    pemilik VARCHAR(100) NOT NULL,
    saldo   BIGINT NOT NULL CHECK (saldo >= 0)
);

CREATE TABLE pelanggan (
    id    BIGSERIAL PRIMARY KEY,
    nama  VARCHAR(100) NOT NULL,
    kota  VARCHAR(50)
);

CREATE TABLE pesanan (
    id           BIGSERIAL PRIMARY KEY,
    pelanggan_id BIGINT NOT NULL REFERENCES pelanggan(id),
    total        BIGINT NOT NULL,
    dibuat       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_pesanan_pelanggan ON pesanan(pelanggan_id);

INSERT INTO rekening (pemilik, saldo) VALUES ('Andi', 500000), ('Budi', 100000);
INSERT INTO pelanggan (nama, kota) VALUES ('Ani', 'Bandung'), ('Candra', 'Jakarta'), ('Dewi', 'Bandung');
INSERT INTO pesanan (pelanggan_id, total) VALUES (1, 50000), (1, 75000), (2, 20000), (3, 120000);
```

### JDBC Murni: Query, Transaksi, dan Batch

Tambahkan dependency driver dan pool (cek versi terbaru di Maven Central): `org.postgresql:postgresql` dan `com.zaxxer:HikariCP`.

```java
// JdbcDemo.java  (butuh driver PostgreSQL + HikariCP di classpath; SKIP pada lingkungan tanpa database)
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.sql.DataSource;

public class JdbcDemo {
    public static void main(String[] args) throws Exception {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/toko");
        cfg.setUsername("belajar");
        cfg.setPassword("rahasia");
        cfg.setMaximumPoolSize(10);

        try (HikariDataSource ds = new HikariDataSource(cfg)) {
            daftarPesananPerKota(ds, "Bandung");
            transfer(ds, 1, 2, 150_000);       // sukses
            try {
                transfer(ds, 2, 1, 9_999_999); // gagal -> rollback
            } catch (IllegalStateException e) {
                System.out.println("Transfer dibatalkan: " + e.getMessage());
            }
        }
    }

    // Query dengan parameter (aman dari SQL injection)
    static void daftarPesananPerKota(DataSource ds, String kota) throws SQLException {
        String sql = """
                SELECT p.nama, o.total, o.dibuat
                FROM pelanggan p JOIN pesanan o ON o.pelanggan_id = p.id
                WHERE p.kota = ?
                ORDER BY o.total DESC
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%-8s Rp%,d  %s%n",
                            rs.getString("nama"),
                            rs.getLong("total"),
                            rs.getObject("dibuat", LocalDateTime.class));
                }
            }
        }
    }

    // Transaksi: kedua UPDATE berhasil bersama, atau tidak sama sekali
    static void transfer(DataSource ds, long dari, long ke, long jumlah) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement kurangi = c.prepareStatement(
                        "UPDATE rekening SET saldo = saldo - ? WHERE id = ? AND saldo >= ?")) {
                    kurangi.setLong(1, jumlah);
                    kurangi.setLong(2, dari);
                    kurangi.setLong(3, jumlah);
                    if (kurangi.executeUpdate() == 0) {
                        throw new IllegalStateException("saldo rekening " + dari + " tidak cukup");
                    }
                }
                try (PreparedStatement tambah = c.prepareStatement(
                        "UPDATE rekening SET saldo = saldo + ? WHERE id = ?")) {
                    tambah.setLong(1, jumlah);
                    tambah.setLong(2, ke);
                    tambah.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
```

> Berkas di atas ditandai **SKIP** karena membutuhkan database dan library eksternal; kode tidak dijalankan pada lingkungan penyusunan materi. Jalankan di mesin Anda setelah `docker compose up -d` dan menambahkan dependency ke `pom.xml`.

### JPA + Spring Data JPA: Entity, Repository, dan Service

Tambahkan `spring-boot-starter-data-jpa` dan `org.postgresql:postgresql` (runtime) ke proyek Spring Boot dari [Modul 13](13-dependency-injection-web-framework.md).

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/toko
    username: belajar
    password: rahasia
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate          # skema dikelola SQL/Flyway, Hibernate hanya memeriksa
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG      # lihat SQL yang dihasilkan (penting untuk mendeteksi N+1)
```

```java
// Pelanggan.java
package com.contoh.toko.pelanggan;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelanggan")
public class Pelanggan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    private String kota;

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL, orphanRemoval = true)   // LAZY secara default
    private List<Pesanan> pesanan = new ArrayList<>();

    protected Pelanggan() { }                       // dibutuhkan JPA

    public Pelanggan(String nama, String kota) {
        this.nama = nama;
        this.kota = kota;
    }

    public void tambahPesanan(Pesanan p) {          // jaga kedua sisi relasi tetap sinkron
        pesanan.add(p);
        p.setPelanggan(this);
    }

    public Long getId() { return id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKota() { return kota; }
    public List<Pesanan> getPesanan() { return pesanan; }
}
```

```java
// Pesanan.java
package com.contoh.toko.pelanggan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "pesanan")
public class Pesanan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)     // @ManyToOne default-nya EAGER, jadi LAZY dinyatakan eksplisit
    @JoinColumn(name = "pelanggan_id")
    private Pelanggan pelanggan;

    private long total;

    private LocalDateTime dibuat = LocalDateTime.now();

    protected Pesanan() { }

    public Pesanan(long total) { this.total = total; }

    void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }
    public long getTotal() { return total; }
}
```

```java
// PelangganRepository.java
package com.contoh.toko.pelanggan;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PelangganRepository extends JpaRepository<Pelanggan, Long> {

    List<Pelanggan> findByKotaOrderByNamaAsc(String kota);

    Optional<Pelanggan> findByNamaIgnoreCase(String nama);

    Page<Pelanggan> findByKota(String kota, Pageable pageable);

    @Query("select distinct p from Pelanggan p left join fetch p.pesanan where p.kota = :kota")
    List<Pelanggan> cariDenganPesanan(@Param("kota") String kota);      // 1 query, bukan N+1
}
```

```java
// PelangganService.java
package com.contoh.toko.pelanggan;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PelangganService {
    private final PelangganRepository repo;

    public PelangganService(PelangganRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Pelanggan daftar(String nama, String kota, long totalPesananPertama) {
        Pelanggan p = new Pelanggan(nama, kota);
        p.tambahPesanan(new Pesanan(totalPesananPertama));
        return repo.save(p);                         // cascade ALL: pesanan ikut tersimpan
    }

    @Transactional
    public void ubahNama(Long id, String namaBaru) {
        Pelanggan p = repo.findById(id).orElseThrow();
        p.setNama(namaBaru);                         // TANPA save(): dirty checking menghasilkan UPDATE saat commit
    }

    // BERMASALAH: N+1 — 1 query pelanggan + 1 query pesanan untuk SETIAP pelanggan
    @Transactional(readOnly = true)
    public long totalSemuaNPlusSatu(String kota) {
        long total = 0;
        for (Pelanggan p : repo.findByKotaOrderByNamaAsc(kota)) {
            for (Pesanan o : p.getPesanan()) {       // lazy load di sini -> query per pelanggan
                total += o.getTotal();
            }
        }
        return total;
    }

    // BENAR: join fetch -> satu query
    @Transactional(readOnly = true)
    public long totalSemua(String kota) {
        List<Pelanggan> data = repo.cariDenganPesanan(kota);
        return data.stream().flatMap(p -> p.getPesanan().stream()).mapToLong(Pesanan::getTotal).sum();
    }
}
```

Panggil `totalSemuaNPlusSatu("Bandung")` lalu `totalSemua("Bandung")` dan **bandingkan jumlah baris `select` di log** — itulah cara paling praktis memahami dan mendeteksi masalah N+1.

> Kode JPA/Spring Data di atas mengikuti API Jakarta Persistence dan Spring Data JPA, tetapi **tidak dijalankan** pada lingkungan penyusunan materi. Jika kompilasi gagal karena perbedaan versi, rujuk dokumentasi versi Spring Boot Anda.

## Latihan

1. Jalankan PostgreSQL via Docker Compose, buat skema di atas, lalu tulis program JDBC yang menampilkan semua pelanggan beserta jumlah pesanan dan total belanjanya (`JOIN` + `GROUP BY`). Mengapa kita memakai `PreparedStatement` dan bukan konkatenasi string?
2. Perluas `transfer` JDBC: tambahkan tabel `riwayat_transfer` dan catat setiap transfer **di dalam transaksi yang sama**. Simulasikan kegagalan setelah UPDATE pertama (lempar exception manual) dan buktikan lewat `psql` bahwa saldo tidak berubah.
3. Gunakan `PreparedStatement.addBatch()` untuk meng-insert 1.000 pelanggan sekaligus, lalu bandingkan waktunya dengan 1.000 `executeUpdate` terpisah dalam auto-commit. Jelaskan penyebab perbedaannya.
4. Buat entity JPA `Pelanggan`/`Pesanan` dan repository seperti contoh, tambahkan `@Version` pada `Pelanggan`, lalu simulasikan dua transaksi yang mengubah pelanggan yang sama untuk menimbulkan `OptimisticLockException`. Jelaskan kapan optimistic locking lebih tepat daripada pessimistic.
5. Aktifkan log SQL, buat 20 pelanggan dengan masing-masing 3 pesanan, lalu hitung jumlah query untuk `totalSemuaNPlusSatu` vs `totalSemua`. Jelaskan apa itu N+1 dan tiga cara mengatasinya (`join fetch`, `@EntityGraph`, batch fetching), serta mengapa `ddl-auto: update` tidak boleh dipakai di production.

---

[← Modul 13: Dependency Injection & Web Framework](13-dependency-injection-web-framework.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 15: Testing & Advanced Topics →](15-testing-advanced.md)
