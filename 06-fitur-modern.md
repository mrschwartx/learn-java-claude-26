# Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation

[← Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface](05-pilar-oop.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 7: Generics & Collections Framework →](07-generics-collections.md)

**Isi modul ini**

- [Teori](#teori)
  - [6.1 Enums](#61-enums)
  - [6.2 Record](#62-record)
  - [6.3 Sealed Classes dan Interfaces](#63-sealed-classes-dan-interfaces)
  - [6.4 Pattern Matching](#64-pattern-matching)
  - [6.5 Annotations](#65-annotations)
  - [6.6 Fitur Bahasa Terbaru (Java 22–25)](#66-fitur-bahasa-terbaru-java-2225)
- [Praktik](#praktik)
  - [Enum dengan State Machine dan `EnumMap`](#enum-dengan-state-machine-dan-enummap)
  - [Record, Sealed, dan Pattern Matching](#record-sealed-dan-pattern-matching)
  - [Annotation Kustom + Reflection](#annotation-kustom--reflection)
- [Latihan](#latihan)

---


## Teori

### 6.1 Enums

**Enum** adalah tipe khusus untuk himpunan **konstanta bernama yang tetap** (hari, status, arah mata angin). Jauh lebih aman daripada memakai `int` atau `String` sebagai "kode", karena compiler menjamin hanya nilai yang valid yang bisa dipakai.

```java
enum Hari { SENIN, SELASA, RABU, KAMIS, JUMAT, SABTU, MINGGU }

Hari h = Hari.JUMAT;
System.out.println(h);               // JUMAT
System.out.println(h.ordinal());     // 4  (urutan deklarasi, mulai 0)
System.out.println(h.name());        // "JUMAT"
Hari dari = Hari.valueOf("RABU");    // dari String; IllegalArgumentException jika tidak ada
Hari[] semua = Hari.values();        // array seluruh konstanta
```

Enum di Java sebenarnya adalah **class penuh**: boleh punya field, constructor (selalu `private`), method, dan bahkan method yang berbeda per konstanta.

```java
enum Planet {
    BUMI(5.97e24, 6.371e6),
    MARS(6.42e23, 3.390e6);

    private final double massa, jariJari;
    Planet(double massa, double jariJari) { this.massa = massa; this.jariJari = jariJari; }

    double gravitasi() { return 6.674e-11 * massa / (jariJari * jariJari); }
}
```

Enum bekerja baik dengan `switch` (compiler mengecek kelengkapan pada switch expression) dan memiliki koleksi khusus yang sangat efisien: **`EnumMap`** dan **`EnumSet`** ([Modul 7](07-generics-collections.md)). Enum juga ideal untuk **state machine** sederhana dan implementasi **singleton** yang aman.

### 6.2 Record

**Record** (Java 16+) adalah cara ringkas mendefinisikan **data carrier immutable**. Dengan satu baris, compiler otomatis membuatkan: field `private final`, constructor kanonik, *accessor* (`nama()`, bukan `getNama()`), serta `equals`, `hashCode`, dan `toString`.

```java
record Titik(int x, int y) { }

Titik a = new Titik(1, 2);
System.out.println(a);                    // Titik[x=1, y=2]
System.out.println(a.x());                // 1
System.out.println(a.equals(new Titik(1, 2)));   // true
```

Record dapat menambah **validasi lewat compact constructor**, method biasa, method `static`, dan mengimplementasikan interface:

```java
record Uang(long jumlah, String mata) implements Comparable<Uang> {
    Uang {                                       // compact constructor: tanpa parameter & tanpa assignment
        if (jumlah < 0) throw new IllegalArgumentException("Jumlah negatif");
        mata = mata.toUpperCase();               // boleh menormalkan parameter sebelum disimpan
    }

    static Uang rupiah(long jumlah) { return new Uang(jumlah, "idr"); }

    Uang tambah(Uang lain) { return new Uang(jumlah + lain.jumlah, mata); }

    @Override public int compareTo(Uang o) { return Long.compare(jumlah, o.jumlah); }
}
```

Batasan record: tidak bisa `extends` class lain (implisit turunan `java.lang.Record`), semua field `final`, dan tidak boleh menambah field instance di luar komponen. Record ideal untuk **DTO**, hasil query, kunci `Map`, dan nilai domain kecil. Hati-hati: record **dangkal immutable** — jika komponennya `List`, isinya tetap bisa berubah kecuali Anda menyalinnya dengan `List.copyOf`.

### 6.3 Sealed Classes dan Interfaces

**Sealed** (Java 17+) membatasi **siapa saja yang boleh menjadi turunan** sebuah class/interface lewat klausa `permits`. Dengan itu, hierarki tipe menjadi **tertutup dan diketahui lengkap** oleh compiler.

```java
sealed interface Bentuk permits Lingkaran, Persegi, Segitiga { }

record Lingkaran(double jariJari) implements Bentuk { }
record Persegi(double sisi) implements Bentuk { }
record Segitiga(double alas, double tinggi) implements Bentuk { }
```

Setiap subtipe yang diizinkan harus menyatakan perilaku pewarisan selanjutnya: **`final`** (record dan enum otomatis final), **`sealed`** (membatasi lagi), atau **`non-sealed`** (membuka kembali). Kombinasi sealed + record dikenal sebagai **algebraic data types** — cara memodelkan "salah satu dari beberapa kemungkinan" secara aman.

### 6.4 Pattern Matching

**Pattern matching** menyatukan pengecekan tipe, casting, dan ekstraksi data dalam satu konstruksi.

**(a) `instanceof` pattern** (Java 16) — sudah kita lihat di [Modul 5](05-pilar-oop.md):

```java
if (obj instanceof String s && s.length() > 3) {
    System.out.println(s.toUpperCase());
}
```

**(b) `switch` pattern** (final di Java 21) — `switch` dapat mencocokkan **tipe**, bukan hanya konstanta, dengan *guard* `when`, dan menangani `null`:

```java
static String deskripsi(Object o) {
    return switch (o) {
        case null                         -> "kosong";
        case Integer i when i > 100       -> "bilangan besar " + i;
        case Integer i                    -> "bilangan " + i;
        case String s when s.isBlank()    -> "teks kosong";
        case String s                     -> "teks '" + s + "'";
        case int[] arr                    -> "array int sepanjang " + arr.length;
        default                           -> "objek lain: " + o;
    };
}
```

Urutan `case` penting: pola yang **lebih spesifik harus di atas** pola yang lebih umum (compiler menolak jika sebuah case tak akan pernah tercapai).

**(c) Record pattern** (Java 21) — membongkar record langsung di pola, termasuk bersarang:

```java
record Titik(int x, int y) { }
record Garis(Titik awal, Titik akhir) { }

static String info(Object o) {
    if (o instanceof Garis(Titik(var x1, var y1), Titik(var x2, var y2))) {
        return "Garis dari (" + x1 + "," + y1 + ") ke (" + x2 + "," + y2 + ")";
    }
    return "bukan garis";
}
```

**(d) Exhaustiveness bersama sealed type** — jika sebuah `switch` expression atas tipe sealed mencakup **semua** subtipe yang diizinkan, **tidak perlu `default`**. Bonusnya: jika suatu hari Anda menambah subtipe baru, **setiap `switch` yang belum menanganinya langsung gagal dikompilasi**, sehingga tidak ada kasus yang terlewat diam-diam.

```java
static double luas(Bentuk b) {
    return switch (b) {
        case Lingkaran l            -> Math.PI * l.jariJari() * l.jariJari();
        case Persegi p              -> p.sisi() * p.sisi();
        case Segitiga(double a, double t) -> 0.5 * a * t;    // record pattern
    };                                                       // tanpa default: sudah exhaustive
}
```

### 6.5 Annotations

**Annotation** adalah **metadata** yang ditempelkan pada class, method, field, parameter, dll. dengan `@`. Annotation tidak mengubah logika kode secara langsung, melainkan dibaca oleh **compiler**, **tool**, atau **framework** (lewat *reflection* saat runtime).

**Annotation bawaan:**

| Annotation | Fungsi |
|---|---|
| `@Override` | Compiler memeriksa method benar-benar meng-override |
| `@Deprecated` | Menandai API usang (`forRemoval = true` jika akan dihapus) |
| `@SuppressWarnings("unchecked")` | Menekan warning tertentu — pakai seminimal mungkin |
| `@FunctionalInterface` | Memastikan interface hanya punya satu method abstract |
| `@SafeVarargs` | Menyatakan varargs generic aman |

**Membuat annotation sendiri** memerlukan dua *meta-annotation*:

- **`@Retention`** — sampai kapan annotation dipertahankan: `SOURCE` (dibuang compiler), `CLASS` (ada di `.class`, tak terbaca runtime), atau **`RUNTIME`** (bisa dibaca via reflection).
- **`@Target`** — di mana annotation boleh dipasang (`TYPE`, `METHOD`, `FIELD`, `PARAMETER`, dst.).

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Wajib {
    String pesan() default "wajib diisi";
}
```

Annotation adalah fondasi framework modern: `@Component`, `@GetMapping`, `@Entity`, `@Test` yang akan Anda temui di [Modul 13](13-dependency-injection-web-framework.md) sampai [Modul 15](15-testing-advanced.md) semuanya bekerja dengan mekanisme yang sama seperti contoh praktik di bawah.

### 6.6 Fitur Bahasa Terbaru (Java 22–25)

Beberapa penyempurnaan terbaru yang layak diketahui (kode di bagian ini membutuhkan versi yang disebutkan):

- **Unnamed variables & patterns `_`** (final di Java 22) — untuk variabel yang sengaja tidak dipakai:

  ```java
  // Java 22+
  try { Integer.parseInt(teks); } catch (NumberFormatException _) { /* abaikan */ }
  if (obj instanceof Garis(Titik(var x, _), _)) { /* hanya butuh x */ }
  ```

- **Flexible constructor bodies** (final di Java 25) — Anda boleh menjalankan validasi atau menghitung nilai **sebelum** memanggil `super(...)` / `this(...)`, selama tidak mengakses `this`:

  ```java
  // Java 25+
  class Positif extends Angka {
      Positif(int nilai) {
          if (nilai <= 0) throw new IllegalArgumentException("harus positif");  // sebelum super()
          super(nilai);
      }
  }
  ```

- **Module import declarations** (final di Java 25) — `import module java.base;` mengimpor semua package yang diekspor modul tersebut sekaligus (berguna untuk skrip dan pembelajaran).
- **Primitive types in patterns** masih berstatus *preview* di Java 25 — jangan dipakai di production.

## Praktik

### Enum dengan State Machine dan `EnumMap`

```java
// DemoEnum.java
import java.util.EnumMap;
import java.util.Map;

public class DemoEnum {
    public static void main(String[] args) {
        StatusPesanan s = StatusPesanan.BARU;
        while (s != StatusPesanan.SELESAI) {
            System.out.println(s + " (" + s.label() + ") -> boleh dibatalkan? " + s.bolehDibatalkan());
            s = s.berikutnya();
        }
        System.out.println(s + " (" + s.label() + ")");

        System.out.println("valueOf: " + StatusPesanan.valueOf("DIKIRIM").ordinal());

        // Enum dengan perilaku berbeda per konstanta
        for (Operasi op : Operasi.values()) {
            System.out.println(op + ": 12 dan 4 -> " + op.hitung(12, 4));
        }

        // EnumMap: Map yang sangat efisien dengan kunci enum
        Map<StatusPesanan, Integer> jumlah = new EnumMap<>(StatusPesanan.class);
        jumlah.put(StatusPesanan.DIKIRIM, 3);
        jumlah.put(StatusPesanan.BARU, 7);
        System.out.println(jumlah);          // urut sesuai deklarasi enum
    }
}

enum StatusPesanan {
    BARU("Menunggu pembayaran"),
    DIBAYAR("Sedang dikemas"),
    DIKIRIM("Dalam perjalanan"),
    SELESAI("Pesanan diterima");

    private final String label;
    StatusPesanan(String label) { this.label = label; }
    String label() { return label; }

    boolean bolehDibatalkan() {
        return this == BARU || this == DIBAYAR;
    }

    StatusPesanan berikutnya() {
        return switch (this) {
            case BARU -> DIBAYAR;
            case DIBAYAR -> DIKIRIM;
            case DIKIRIM, SELESAI -> SELESAI;
        };
    }
}

enum Operasi {
    TAMBAH { int hitung(int a, int b) { return a + b; } },
    KURANG { int hitung(int a, int b) { return a - b; } },
    BAGI   { int hitung(int a, int b) { return a / b; } };

    abstract int hitung(int a, int b);
}
```

### Record, Sealed, dan Pattern Matching

```java
// DemoModern.java
import java.util.List;

public class DemoModern {
    public static void main(String[] args) {
        // --- Record ---
        Uang a = Uang.rupiah(15_000);
        Uang b = new Uang(5_000, "idr");
        System.out.println(a + " + " + b + " = " + a.tambah(b));
        System.out.println("a.equals(Uang.rupiah(15000)) -> " + a.equals(Uang.rupiah(15_000)));
        try {
            new Uang(-1, "idr");
        } catch (IllegalArgumentException e) {
            System.out.println("Validasi record: " + e.getMessage());
        }

        // --- Sealed + switch pattern (exhaustive, tanpa default) ---
        List<Bentuk> daftar = List.of(new Lingkaran(2), new Persegi(3), new Segitiga(4, 5));
        for (Bentuk bentuk : daftar) {
            System.out.printf("%-30s luas = %.2f%n", bentuk, luas(bentuk));
        }

        // --- Pattern matching atas Object ---
        Object[] uji = { null, 7, 500, "", "halo", new int[3], 3.14, new Garis(new Titik(0, 0), new Titik(3, 4)) };
        for (Object o : uji) {
            System.out.println(deskripsi(o));
        }
    }

    static double luas(Bentuk b) {
        return switch (b) {
            case Lingkaran l -> Math.PI * l.jariJari() * l.jariJari();
            case Persegi p -> p.sisi() * p.sisi();
            case Segitiga(double alas, double tinggi) -> 0.5 * alas * tinggi;
        };
    }

    static String deskripsi(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i > 100 -> "bilangan besar " + i;
            case Integer i -> "bilangan " + i;
            case String s when s.isBlank() -> "teks kosong";
            case String s -> "teks '" + s + "'";
            case int[] arr -> "array int sepanjang " + arr.length;
            case Garis(Titik(var x1, var y1), Titik(var x2, var y2)) ->
                "garis panjang " + Math.hypot(x2 - x1, y2 - y1);
            default -> "objek lain: " + o;
        };
    }
}

record Uang(long jumlah, String mata) {
    Uang {
        if (jumlah < 0) throw new IllegalArgumentException("Jumlah negatif");
        mata = mata.toUpperCase();
    }
    static Uang rupiah(long jumlah) { return new Uang(jumlah, "idr"); }
    Uang tambah(Uang lain) { return new Uang(jumlah + lain.jumlah, mata); }
}

sealed interface Bentuk permits Lingkaran, Persegi, Segitiga { }
record Lingkaran(double jariJari) implements Bentuk { }
record Persegi(double sisi) implements Bentuk { }
record Segitiga(double alas, double tinggi) implements Bentuk { }

record Titik(int x, int y) { }
record Garis(Titik awal, Titik akhir) { }
```

Coba tambahkan record baru `Trapesium implements Bentuk` ke `permits`, **tanpa** menyentuh method `luas`, lalu kompilasi ulang. Compiler akan langsung memberi tahu bahwa `switch` tidak lagi exhaustive — inilah keamanan yang diberikan sealed type.

### Annotation Kustom + Reflection

```java
// DemoAnnotation.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DemoAnnotation {
    public static void main(String[] args) throws Exception {
        Pengguna valid = new Pengguna("Ani", "ani@contoh.com", 20);
        Pengguna tidakValid = new Pengguna("", null, 17);

        System.out.println("valid      -> " + Validator.validasi(valid));
        System.out.println("tidakValid -> " + Validator.validasi(tidakValid));
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Wajib {
    String pesan() default "wajib diisi";
}

class Pengguna {
    @Wajib String nama;
    @Wajib(pesan = "email harus ada") String email;
    int umur;

    Pengguna(String nama, String email, int umur) {
        this.nama = nama; this.email = email; this.umur = umur;
    }
}

class Validator {
    static List<String> validasi(Object objek) throws IllegalAccessException {
        List<String> galat = new ArrayList<>();
        for (Field f : objek.getClass().getDeclaredFields()) {
            Wajib w = f.getAnnotation(Wajib.class);
            if (w == null) continue;
            f.setAccessible(true);
            Object nilai = f.get(objek);
            if (nilai == null || nilai.toString().isBlank()) {
                galat.add(f.getName() + ": " + w.pesan());
            }
        }
        return galat;
    }
}
```

Inilah cara kerja dasar validasi seperti `@NotNull`/`@NotBlank` di framework: sebuah **annotation** menandai, lalu **kode lain membaca** annotation tersebut lewat reflection. (`throws Exception` di `main` akan dijelaskan di [Modul 8](08-exception-io.md).)

## Latihan

1. Buat enum `Level` (`DEBUG`, `INFO`, `WARN`, `ERROR`) dengan field `bobot` (int) dan method `lebihPentingDari(Level lain)`. Gunakan `EnumMap` untuk menghitung berapa kali tiap level muncul dalam sebuah `List<Level>` acak.
2. Ubah class `Mahasiswa` di Modul 4 menjadi `record`. Tambahkan compact constructor yang memvalidasi `ipk` antara 0 dan 4, lalu jelaskan apa saja yang kini tidak perlu Anda tulis manual.
3. Rancang hierarki sealed `Hasil` dengan dua record `Sukses(String data)` dan `Gagal(String pesan)`. Tulis method `tampilkan(Hasil h)` memakai switch expression tanpa `default`. Apa yang terjadi saat Anda menambah record `Menunggu` ke `permits`?
4. Tulis method `format(Object o)` memakai `switch` pattern yang menangani `null`, `Integer` negatif (guard `when`), `Integer` lainnya, `List<?>` (cetak ukurannya), dan `default`. Mengapa urutan `case` berpengaruh terhadap kompilasi?
5. Buat annotation `@Panjang(min, max)` untuk field `String` dan perluas `Validator` contoh di atas agar ikut memeriksa panjang string. Jelaskan peran `@Retention(RUNTIME)` dan apa yang terjadi jika diganti `SOURCE`.

---

[← Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface](05-pilar-oop.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 7: Generics & Collections Framework →](07-generics-collections.md)
