# Modul 8: Exception Handling & I/O

[← Modul 7: Generics & Collections Framework](07-generics-collections.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 9: Functional Programming: Lambda & Stream API →](09-functional-programming.md)

**Isi modul ini**

- [Teori](#teori)
  - [8.1 Apa itu Exception?](#81-apa-itu-exception)
  - [8.2 `try`, `catch`, `finally`](#82-try-catch-finally)
  - [8.3 `throw` dan `throws`](#83-throw-dan-throws)
  - [8.4 try-with-resources dan `AutoCloseable`](#84-try-with-resources-dan-autocloseable)
  - [8.5 Custom Exception dan Exception Chaining](#85-custom-exception-dan-exception-chaining)
  - [8.6 Best Practices](#86-best-practices)
  - [8.7 I/O Operations — Gambaran Umum](#87-io-operations--gambaran-umum)
  - [8.8 File Operations dengan `Path` dan `Files`](#88-file-operations-dengan-path-dan-files)
  - [8.9 Input Standar, Serialization, dan Format Data](#89-input-standar-serialization-dan-format-data)
- [Praktik](#praktik)
  - [Exception: Urutan Eksekusi, Custom Exception, dan Chaining](#exception-urutan-eksekusi-custom-exception-dan-chaining)
  - [try-with-resources dan Suppressed Exception](#try-with-resources-dan-suppressed-exception)
  - [Operasi File dengan `Path` dan `Files`](#operasi-file-dengan-path-dan-files)
  - [Mengolah CSV Sederhana dengan Penanganan Baris Rusak](#mengolah-csv-sederhana-dengan-penanganan-baris-rusak)
- [Latihan](#latihan)

---


## Teori

### 8.1 Apa itu Exception?

**Exception** adalah peristiwa tak normal saat program berjalan yang mengganggu alur eksekusi biasa — file tidak ditemukan, pembagian dengan nol, koneksi database putus, input tidak valid. Tanpa penanganan, exception **menghentikan program** dan mencetak *stack trace*. Dengan **exception handling**, Anda dapat mendeteksi masalah, memulihkan diri, atau gagal dengan pesan yang jelas.

Semua exception adalah object dengan hierarki berikut:

```
Throwable
├── Error                    — masalah fatal JVM (OutOfMemoryError, StackOverflowError); JANGAN ditangkap
└── Exception
    ├── (checked exceptions) — IOException, SQLException, ...   WAJIB ditangani/dideklarasikan
    └── RuntimeException     — (unchecked)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        ├── NumberFormatException
        └── ArithmeticException
```

| | **Checked** | **Unchecked** (`RuntimeException` dan turunannya) |
|---|---|---|
| Dicek compiler? | **Ya** — harus di-`catch` atau dideklarasikan dengan `throws` | Tidak |
| Biasanya menandakan | Kondisi eksternal yang **wajar terjadi** dan bisa dipulihkan (file hilang, jaringan putus) | **Bug pemrograman** (argumen salah, null) |
| Contoh | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |

### 8.2 `try`, `catch`, `finally`

```java
try {
    int hasil = 10 / pembagi;                 // kode yang mungkin gagal
    System.out.println(hasil);
} catch (ArithmeticException e) {             // tangani tipe tertentu
    System.out.println("Tidak bisa dibagi nol: " + e.getMessage());
} finally {
    System.out.println("Selalu dijalankan");  // dipakai untuk pembersihan
}
```

- Blok **`catch`** dicocokkan **berurutan dari atas**; tulis yang **paling spesifik dulu**, yang umum (`Exception`) belakangan.
- **Multi-catch** menggabungkan beberapa tipe yang penanganannya sama: `catch (IOException | SQLException e)`.
- **`finally`** selalu berjalan (kecuali JVM mati) — baik terjadi exception maupun tidak, bahkan setelah `return`. Namun untuk membersihkan resource, **try-with-resources** (8.4) jauh lebih baik.

### 8.3 `throw` dan `throws`

- **`throw`** — *melempar* sebuah object exception: `throw new IllegalArgumentException("umur negatif");`
- **`throws`** — pada deklarasi method, menyatakan method **dapat melempar** checked exception tertentu sehingga pemanggil harus menanganinya.

```java
static String bacaBaris(Path p) throws IOException {      // memberi tahu pemanggil
    return Files.readString(p);
}

static void setUmur(int umur) {                           // unchecked: tak perlu throws
    if (umur < 0) throw new IllegalArgumentException("umur negatif: " + umur);
}
```

Prinsip: **validasi argumen** dengan `IllegalArgumentException`, **state objek yang salah** dengan `IllegalStateException`, dan **tidak ada hasil** lewat `Optional` ([Modul 7](07-generics-collections.md)) alih-alih exception jika hal itu situasi normal.

### 8.4 try-with-resources dan `AutoCloseable`

Resource seperti file, koneksi database, dan socket harus **selalu ditutup**. Menulis `finally` + `close()` manual rawan lupa dan berantakan. **try-with-resources** (Java 7+) menutup resource **otomatis** — setiap object yang mengimplementasikan **`AutoCloseable`** boleh dideklarasikan di dalam tanda kurung:

```java
try (BufferedReader in = Files.newBufferedReader(path);
     BufferedWriter out = Files.newBufferedWriter(tujuan)) {
    String baris;
    while ((baris = in.readLine()) != null) {
        out.write(baris.toUpperCase());
        out.newLine();
    }
}   // in dan out otomatis di-close, urutan TERBALIK dari deklarasi, meski terjadi exception
```

Jika exception terjadi di badan `try` **dan** saat `close()`, exception dari `close()` **ditekan** (*suppressed*) dan tetap bisa dibaca lewat `e.getSuppressed()` — exception utama tidak tertimpa. Untuk resource Anda sendiri, cukup `implements AutoCloseable`.

### 8.5 Custom Exception dan Exception Chaining

Buat exception sendiri agar kesalahan domain punya nama yang jelas. Turunkan dari `Exception` (checked) untuk kondisi yang **harus ditangani pemanggil**, atau `RuntimeException` (unchecked) untuk kesalahan pemrograman/yang tidak realistis dipulihkan.

```java
public class SaldoTidakCukupException extends Exception {
    private final long kekurangan;

    public SaldoTidakCukupException(String pesan, long kekurangan) {
        super(pesan);
        this.kekurangan = kekurangan;
    }
    public long getKekurangan() { return kekurangan; }
}
```

**Exception chaining** membungkus exception tingkat rendah menjadi exception domain **tanpa membuang penyebab aslinya** — selalu teruskan `cause`:

```java
try {
    return repo.cari(id);
} catch (SQLException e) {
    throw new LayananException("Gagal memuat pelanggan " + id, e);   // e = cause, tetap tercatat di stack trace
}
```

### 8.6 Best Practices

- **Tangkap yang spesifik**, bukan `catch (Exception e)` atau (lebih buruk) `Throwable`, kecuali di batas terluar aplikasi.
- **Jangan menelan exception** — `catch (Exception e) { }` kosong menyembunyikan bug. Minimal catat (log) atau lempar ulang.
- **Fail fast** — validasi di awal method, lempar exception dengan **pesan yang informatif** (sertakan nilai yang salah).
- **Jangan pakai exception untuk alur normal** (misal untuk keluar dari loop) — lambat dan membingungkan.
- **Log sekali**: jangan mencatat *dan* melempar ulang di setiap lapisan; hasilnya log ganda. Catat di tempat exception akhirnya ditangani ([Modul 12](12-build-tools-modules-logging.md)).
- Selalu gunakan **try-with-resources** untuk resource; hindari `finally` untuk `close()`.
- **Jangan kembalikan `null`** untuk menandakan error; gunakan exception, `Optional`, atau koleksi kosong.
- Saat membaca **stack trace**: baca dari atas (jenis & pesan exception), lalu cari baris pertama yang menunjuk kode **milik Anda**; bagian `Caused by:` di bawahnya menunjukkan akar masalah.

### 8.7 I/O Operations — Gambaran Umum

**I/O** (*Input/Output*) adalah pertukaran data antara program dan dunia luar: file, konsol, jaringan. Java menyediakan dua generasi API:

| | **`java.io`** (klasik) | **`java.nio.file`** (NIO.2, modern) |
|---|---|---|
| Representasi path | `File` | **`Path`** |
| Operasi file | Method di `File`, terbatas | **`Files`** — kaya fitur, melempar exception informatif |
| Penanganan error | Banyak method mengembalikan `boolean` diam-diam | Melempar `IOException` yang jelas |
| Saran | Dipahami (kode lama) | **Gunakan untuk kode baru** |

Di tingkat *stream*, ada dua keluarga:

- **Byte stream** — `InputStream` / `OutputStream` (data biner: gambar, zip, jaringan).
- **Character stream** — `Reader` / `Writer` (teks; memperhitungkan *charset*).

Selalu **bungkus dengan buffer** (`BufferedReader`, `BufferedWriter`) untuk file besar agar tidak membaca satu karakter per sistem-call. Sejak **Java 18**, charset default adalah **UTF-8** di semua platform; tetap disarankan menuliskan charset secara eksplisit pada kode yang harus portabel.

### 8.8 File Operations dengan `Path` dan `Files`

| Tugas | Cara |
|---|---|
| Membuat path | `Path.of("data", "log.txt")` |
| Cek keberadaan | `Files.exists(p)`, `Files.isDirectory(p)`, `Files.size(p)` |
| Baca seluruh isi teks | `Files.readString(p)` / `Files.readAllLines(p)` |
| Tulis / tambah teks | `Files.writeString(p, teks)` / dengan `StandardOpenOption.APPEND` |
| Baca file besar baris-per-baris | `Files.newBufferedReader(p)` atau `Files.lines(p)` (**wajib ditutup**) |
| Salin / pindah / hapus | `Files.copy`, `Files.move`, `Files.delete`, `Files.deleteIfExists` |
| Buat folder | `Files.createDirectories(p)` |
| Telusuri folder | `Files.list(dir)`, `Files.walk(dir)` (**wajib ditutup**) |
| File sementara | `Files.createTempFile`, `Files.createTempDirectory` |

`Files.readString` dan `Files.readAllLines` memuat **seluruh file ke memory** — cocok untuk file kecil, tapi untuk file berukuran gigabyte gunakan pembacaan **per baris**.

### 8.9 Input Standar, Serialization, dan Format Data

- **`System.in` / `System.out` / `System.err`** adalah stream standar. Untuk membaca input konsol, `Scanner` praktis ([Modul 2](02-dasar-bahasa.md)); `BufferedReader` lebih cepat untuk input besar.
- **Java Serialization** (`ObjectOutputStream`) bawaan **sebaiknya dihindari** — rapuh dan menjadi sumber kerentanan keamanan serius jika memproses data tak tepercaya. Untuk menyimpan/mengirim object, gunakan format terbuka seperti **JSON** (Jackson/Gson), Protocol Buffers, atau Avro.
- Berkas konfigurasi sederhana sering memakai `java.util.Properties`, YAML, atau JSON.

## Praktik

### Exception: Urutan Eksekusi, Custom Exception, dan Chaining

```java
// DemoException.java
public class DemoException {
    public static void main(String[] args) {
        // 1) try/catch/finally dan urutan eksekusi
        System.out.println(bagi(10, 2));
        System.out.println(bagi(10, 0));

        // 2) NumberFormatException (unchecked) dengan penanganan spesifik
        for (String s : new String[] {"42", "4x2", null}) {
            try {
                System.out.println("parse: " + Integer.parseInt(s));
            } catch (NumberFormatException e) {
                System.out.println("Tidak valid -> " + e.getMessage());
            }
        }

        // 3) Custom checked exception
        Dompet d = new Dompet(50_000);
        try {
            d.tarik(30_000);
            d.tarik(40_000);
        } catch (SaldoTidakCukupException e) {
            System.out.println(e.getMessage() + " (kurang " + e.getKekurangan() + ")");
        }

        // 4) Chaining: penyebab asli tetap terbawa
        try {
            muatKonfigurasi("port=abc");
        } catch (KonfigurasiException e) {
            System.out.println(e.getMessage());
            System.out.println("  Caused by: " + e.getCause());
        }
    }

    static int bagi(int a, int b) {
        try {
            System.out.println("mulai bagi");
            return a / b;
        } catch (ArithmeticException e) {
            System.out.println("catch: " + e.getMessage());
            return -1;
        } finally {
            System.out.println("finally (selalu jalan)");
        }
    }

    static void muatKonfigurasi(String baris) throws KonfigurasiException {
        String nilai = baris.split("=")[1];
        try {
            Integer.parseInt(nilai);
        } catch (NumberFormatException e) {
            throw new KonfigurasiException("Konfigurasi 'port' tidak valid", e);
        }
    }
}

class Dompet {
    private long saldo;
    Dompet(long saldo) { this.saldo = saldo; }

    void tarik(long jumlah) throws SaldoTidakCukupException {
        if (jumlah > saldo) {
            throw new SaldoTidakCukupException("Saldo tidak cukup", jumlah - saldo);
        }
        saldo -= jumlah;
        System.out.println("Berhasil tarik " + jumlah + ", sisa " + saldo);
    }
}

class SaldoTidakCukupException extends Exception {
    private final long kekurangan;
    SaldoTidakCukupException(String pesan, long kekurangan) {
        super(pesan);
        this.kekurangan = kekurangan;
    }
    long getKekurangan() { return kekurangan; }
}

class KonfigurasiException extends Exception {
    KonfigurasiException(String pesan, Throwable penyebab) { super(pesan, penyebab); }
}
```

### try-with-resources dan Suppressed Exception

```java
// DemoResource.java
public class DemoResource {
    public static void main(String[] args) {
        try (Sumber a = new Sumber("A", false);
             Sumber b = new Sumber("B", true)) {       // close() B akan melempar exception
            System.out.println("-- badan try --");
            throw new IllegalStateException("kegagalan utama");
        } catch (Exception e) {
            System.out.println("Ditangkap : " + e.getMessage());
            for (Throwable t : e.getSuppressed()) {
                System.out.println("Suppressed: " + t.getMessage());
            }
        }
    }
}

class Sumber implements AutoCloseable {
    private final String nama;
    private final boolean gagalSaatClose;

    Sumber(String nama, boolean gagalSaatClose) {
        this.nama = nama;
        this.gagalSaatClose = gagalSaatClose;
        System.out.println("buka " + nama);
    }

    @Override
    public void close() {
        System.out.println("tutup " + nama);
        if (gagalSaatClose) throw new RuntimeException("gagal menutup " + nama);
    }
}
```

Perhatikan: resource ditutup dengan urutan **terbalik** (B lalu A), dan kegagalan `close()` tidak menimpa exception utama — ia muncul sebagai *suppressed*.

### Operasi File dengan `Path` dan `Files`

```java
// DemoFile.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DemoFile {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("belajar-java");
        Path catatan = dir.resolve("catatan.txt");

        // Tulis & tambah
        Files.writeString(catatan, "baris 1\nbaris 2\n", StandardCharsets.UTF_8);
        Files.writeString(catatan, "baris 3\n", StandardOpenOption.APPEND);

        // Baca
        List<String> baris = Files.readAllLines(catatan);
        System.out.println("Isi: " + baris + ", ukuran = " + Files.size(catatan) + " byte");

        // Baca per baris dengan buffer (aman untuk file besar)
        try (BufferedReader in = Files.newBufferedReader(catatan)) {
            String b;
            int no = 1;
            while ((b = in.readLine()) != null) {
                System.out.println(no++ + ": " + b);
            }
        }

        // Tulis dengan BufferedWriter
        Path laporan = dir.resolve("sub").resolve("laporan.txt");
        Files.createDirectories(laporan.getParent());
        try (BufferedWriter out = Files.newBufferedWriter(laporan)) {
            out.write("Total baris: " + baris.size());
            out.newLine();
        }

        // Salin & pindah
        Files.copy(catatan, dir.resolve("salinan.txt"));
        Files.move(dir.resolve("salinan.txt"), dir.resolve("arsip.txt"));

        // Telusuri direktori (Stream WAJIB ditutup -> try-with-resources)
        try (Stream<Path> isi = Files.walk(dir)) {
            isi.map(p -> dir.relativize(p).toString())
               .sorted()
               .forEach(p -> System.out.println("  [" + (p.isEmpty() ? "." : p) + "]"));
        }

        // Bersihkan: hapus dari yang terdalam
        try (Stream<Path> isi = Files.walk(dir)) {
            isi.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); }
                catch (IOException e) { throw new UncheckedIOException(e); }
            });
        }
        System.out.println("Direktori sementara masih ada? " + Files.exists(dir));

        // Exception pada file yang tidak ada
        try {
            Files.readString(Path.of("tidak-ada.txt"));
        } catch (java.nio.file.NoSuchFileException e) {
            System.out.println("NoSuchFileException: " + e.getMessage());
        }
    }
}
```

> Penggunaan `Stream`, `map`, dan lambda di atas akan dibahas tuntas di [Modul 9](09-functional-programming.md).

### Mengolah CSV Sederhana dengan Penanganan Baris Rusak

```java
// LaporanCsv.java
import java.util.ArrayList;
import java.util.List;

public class LaporanCsv {
    public static void main(String[] args) {
        String csv = """
                nama,qty,harga
                Kopi,2,25000
                Roti,x,12000
                Teh,3
                Susu,1,18000
                """;

        List<String> galat = new ArrayList<>();
        long total = 0;
        String[] baris = csv.strip().split("\\R");        // \\R = pemisah baris apa pun

        for (int i = 1; i < baris.length; i++) {           // lewati header
            try {
                String[] kolom = baris[i].split(",");
                if (kolom.length != 3) {
                    throw new IllegalArgumentException("jumlah kolom " + kolom.length + ", seharusnya 3");
                }
                int qty = Integer.parseInt(kolom[1].trim());
                long harga = Long.parseLong(kolom[2].trim());
                total += qty * harga;
            } catch (IllegalArgumentException e) {         // NumberFormatException termasuk di sini
                galat.add("baris " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println("Total valid: " + total);
        System.out.println("Baris bermasalah:");
        galat.forEach(g -> System.out.println("  - " + g));
    }
}
```

## Latihan

1. Tulis method `ambilUmur(String input)` yang mem-parse umur dan melempar `IllegalArgumentException` dengan pesan jelas bila bukan angka atau di luar 0–150. Panggil dari `main` dalam loop yang terus meminta input sampai valid.
2. Buat checked exception `StokHabisException` dan class `Gudang` dengan method `ambil(String barang, int jumlah)`. Tangani di `main`, lalu ubah menjadi unchecked exception dan diskusikan kapan masing-masing pilihan lebih tepat.
3. Buat class `Koneksi implements AutoCloseable` yang mencetak pesan saat dibuka dan ditutup. Pakai dua instance dalam satu try-with-resources, lalu buktikan urutan penutupannya dan tunjukkan *suppressed exception* ketika `close()` gagal.
4. Tulis program yang membaca file teks besar baris-per-baris dengan `BufferedReader`, menghitung jumlah baris dan kata terpanjang, lalu menulis ringkasannya ke file lain. Mengapa `Files.readAllLines` bukan pilihan tepat untuk file berukuran 5 GB?
5. Bungkus `IOException` menjadi exception domain `LaporanException` dengan **chaining**, lalu cetak stack trace dan temukan bagian `Caused by:`. Jelaskan mengapa menelan exception dengan `catch` kosong dianggap buruk, dan kapan boleh menangkap `Exception` secara umum.

---

[← Modul 7: Generics & Collections Framework](07-generics-collections.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 9: Functional Programming: Lambda & Stream API →](09-functional-programming.md)
