# Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface

[← Modul 4: Class, Object, Access Specifier & Package](04-class-object.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation →](06-fitur-modern.md)

**Isi modul ini**

- [Teori](#teori)
  - [5.1 Basics of OOP — Empat Pilar](#51-basics-of-oop--empat-pilar)
  - [5.2 Encapsulation](#52-encapsulation)
  - [5.3 Inheritance](#53-inheritance)
  - [5.4 Method Overloading vs Overriding](#54-method-overloading-vs-overriding)
  - [5.5 Polymorphism: Static vs Dynamic Binding](#55-polymorphism-static-vs-dynamic-binding)
  - [5.6 Abstraction — `abstract class`](#56-abstraction--abstract-class)
  - [5.7 Interfaces](#57-interfaces)
  - [5.8 `final` Keyword](#58-final-keyword)
  - [5.9 Method Penting Warisan `Object`](#59-method-penting-warisan-object)
  - [5.10 Method Chaining](#510-method-chaining)
- [Praktik](#praktik)
  - [Polymorphism dengan Abstract Class](#polymorphism-dengan-abstract-class)
  - [Static vs Dynamic Binding & Urutan Constructor](#static-vs-dynamic-binding--urutan-constructor)
  - [Interface, Default Method, dan Multi-Implementasi](#interface-default-method-dan-multi-implementasi)
  - [`equals` & `hashCode` — Mengapa Keduanya Harus Di-override](#equals--hashcode--mengapa-keduanya-harus-di-override)
  - [Method Chaining & Builder Pattern](#method-chaining--builder-pattern)
- [Latihan](#latihan)

---


## Teori

### 5.1 Basics of OOP — Empat Pilar

**Object-Oriented Programming (OOP)** memodelkan program sebagai kumpulan object yang saling berinteraksi. Empat pilar utamanya:

| Pilar | Inti gagasan | Mekanisme di Java |
|---|---|---|
| **Encapsulation** | Sembunyikan detail internal, buka hanya antarmuka yang aman | `private` field + method `public` |
| **Inheritance** | Class turunan mewarisi atribut & perilaku class induk | `extends` |
| **Polymorphism** | Satu antarmuka, banyak perilaku | Overriding, interface |
| **Abstraction** | Fokus pada "apa", bukan "bagaimana" | `abstract class`, `interface` |

### 5.2 Encapsulation

**Encapsulation** membungkus data dan method yang mengoperasikannya dalam satu unit (class), sambil **membatasi akses langsung** ke data. Manfaatnya: class dapat **menjaga invarian** (aturan yang harus selalu benar), dan implementasi internal bisa diubah tanpa merusak kode yang memakainya.

```java
public class Suhu {
    private double celsius;               // tidak bisa diubah sembarangan dari luar

    public double getCelsius() { return celsius; }

    public void setCelsius(double celsius) {
        if (celsius < -273.15) {
            throw new IllegalArgumentException("Di bawah nol absolut");
        }
        this.celsius = celsius;           // validasi terjadi di satu tempat
    }

    public double getFahrenheit() { return celsius * 9 / 5 + 32; }   // "properti turunan"
}
```

Jika `celsius` dibuat `public`, siapa pun dapat mengisi `-500` dan merusak konsistensi. Dengan getter/setter (atau, lebih baik lagi, **object immutable** tanpa setter), validasi dijamin.

### 5.3 Inheritance

**Inheritance** memungkinkan class **subclass** (anak) mewarisi member class **superclass** (induk) memakai `extends`. Ini mencerminkan hubungan **"is-a"**: `Anjing` *adalah* `Hewan`.

```java
class Hewan {
    protected String nama;
    Hewan(String nama) { this.nama = nama; }
    void bersuara() { System.out.println("..."); }
}

class Anjing extends Hewan {
    Anjing(String nama) {
        super(nama);                      // WAJIB memanggil constructor induk (baris pertama)
    }
    @Override
    void bersuara() { System.out.println(nama + ": guk guk"); }
}
```

Aturan penting:

- Java hanya mendukung **single inheritance** untuk class (satu `extends`). Perilaku jamak dicapai lewat **interface** (5.7).
- **`super(...)`** memanggil constructor induk; **`super.metode()`** memanggil versi induk dari method yang di-override. Jika tidak ditulis, compiler menyisipkan `super()` otomatis.
- Semua class **otomatis mewarisi `java.lang.Object`** — induk teratas seluruh hierarki.
- Member `private` tidak diwarisi secara akses (ada, tapi tidak bisa disentuh langsung dari subclass).

> **Composition over inheritance:** pewarisan menciptakan keterikatan kuat antara induk dan anak. Jika hubungannya adalah **"has-a"** (misalnya `Mobil` *punya* `Mesin`), gunakan **komposisi** — simpan object lain sebagai field. Gunakan `extends` hanya jika memang benar-benar hubungan "is-a" yang stabil.

### 5.4 Method Overloading vs Overriding

| | **Overloading** | **Overriding** |
|---|---|---|
| Definisi | Beberapa method **bernama sama** dengan **parameter berbeda** di class yang sama | Subclass **mengganti implementasi** method induk |
| Signature | Harus berbeda (jumlah/tipe/urutan parameter) | Harus **sama persis** |
| Return type | Bebas | Sama, atau subtipe (*covariant return*) |
| Dipilih saat | **Kompilasi** (berdasarkan tipe variabel) | **Runtime** (berdasarkan tipe object sebenarnya) |
| Anotasi | — | `@Override` (sangat disarankan) |

```java
class Kalkulator {
    int tambah(int a, int b) { return a + b; }
    double tambah(double a, double b) { return a + b; }      // overload
    int tambah(int a, int b, int c) { return a + b + c; }    // overload
}
```

Aturan overriding: **tidak boleh mempersempit akses** (`public` tidak boleh menjadi `protected`), tidak boleh meng-override method `final`, `static`, atau `private`, dan tidak boleh melempar *checked exception* yang lebih luas ([Modul 8](08-exception-io.md)). Selalu tulis **`@Override`** agar compiler memberi error jika signature ternyata tidak cocok.

### 5.5 Polymorphism: Static vs Dynamic Binding

**Polymorphism** ("banyak bentuk") berarti referensi bertipe induk dapat menunjuk object bertipe turunan, dan **method yang dijalankan ditentukan oleh tipe object sebenarnya**:

```java
Hewan h = new Anjing("Rex");   // tipe variabel: Hewan, tipe object: Anjing
h.bersuara();                  // "Rex: guk guk"  → method milik Anjing
```

**Binding** adalah proses menghubungkan pemanggilan method dengan implementasinya:

| | **Static (early) binding** | **Dynamic (late) binding** |
|---|---|---|
| Diputuskan | Saat **kompilasi** | Saat **runtime** |
| Berlaku untuk | Method `static`, `private`, `final`, **overloading**, dan **field** | Method instance yang di-**override** |
| Berdasarkan | Tipe **variabel** (referensi) | Tipe **object** sebenarnya |

Konsekuensi penting: **overloading dipilih berdasarkan tipe variabel**, sedangkan **overriding berdasarkan tipe object**. Field dan method `static` **tidak** bersifat polimorfik (hanya "disembunyikan", bukan di-override).

**Casting dan `instanceof`:** untuk memakai fitur khusus subclass, referensi di-*downcast*. Sejak Java 16, **pattern matching** untuk `instanceof` menggabungkan pengecekan dan cast:

```java
if (h instanceof Anjing a) {      // a otomatis bertipe Anjing di dalam blok
    a.ambilBola();
}
```

### 5.6 Abstraction — `abstract class`

**Abstraction** menyembunyikan kerumitan dan hanya menampilkan kontrak penting. **Abstract class** adalah class yang **tidak bisa diinstansiasi**, boleh berisi method **abstract** (tanpa isi, wajib diimplementasikan subclass) sekaligus method biasa dan field.

```java
abstract class Bentuk {
    abstract double luas();                       // kontrak: tiap bentuk menghitung luasnya sendiri

    void cetakInfo() {                            // perilaku bersama
        System.out.printf("%s: luas=%.2f%n", getClass().getSimpleName(), luas());
    }
}
// new Bentuk();  // ERROR: abstract class tidak bisa di-instansiasi
```

### 5.7 Interfaces

**Interface** adalah **kontrak murni**: mendefinisikan *apa* yang dapat dilakukan sebuah tipe tanpa menentukan *bagaimana*. Sebuah class dapat mengimplementasikan **banyak interface** (`implements A, B`).

```java
interface Pembayaran {
    void bayar(long jumlah);                              // abstract (implisit public)

    default void cetakStruk(long jumlah) {                // default method (Java 8+)
        System.out.println("Struk pembayaran: Rp" + jumlah);
    }

    static boolean valid(long jumlah) {                   // static method
        return jumlah > 0;
    }
}
```

Fitur interface modern:

- **`default` method** — implementasi bawaan, memungkinkan interface berevolusi tanpa merusak class yang sudah ada.
- **`static` method** — fungsi utilitas milik interface.
- **`private` method** (Java 9+) — berbagi kode antar default method.
- Field di interface otomatis `public static final` (konstanta).
- **Functional interface** — interface dengan tepat satu method abstract, dasar lambda ([Modul 9](09-functional-programming.md)).

**Abstract class vs interface:**

| | Abstract class | Interface |
|---|---|---|
| Jumlah yang bisa diwarisi | Satu (`extends`) | Banyak (`implements`) |
| State (field instance) | Boleh | Tidak (hanya konstanta) |
| Constructor | Boleh | Tidak |
| Cocok untuk | Hubungan "is-a" dengan kode & state bersama | Kemampuan/peran ("bisa dibayar", "bisa dibandingkan") |

Jika dua interface punya default method bersignature sama, class pengimplementasi **wajib meng-override** untuk menyelesaikan konflik (*diamond problem*), dan dapat memilih dengan `NamaInterface.super.metode()`.

### 5.8 `final` Keyword

| Dipakai pada | Artinya |
|---|---|
| **Variabel** | Nilainya hanya boleh diisi **sekali** (konstanta). Untuk referensi: referensinya tidak bisa diganti, tapi isi object bisa berubah |
| **Method** | Tidak boleh di-override |
| **Class** | Tidak boleh diwariskan (contoh: `String`, `Integer`) |
| **Parameter** | Tidak boleh diubah di dalam method |

Perhatikan: **`final` bukan berarti immutable.** `final List<String> daftar = new ArrayList<>();` tetap bisa `daftar.add(...)`. Immutable sejati memerlukan class yang seluruh field-nya `private final` dan tidak menyediakan setter (misalnya `record` di [Modul 6](06-fitur-modern.md)).

### 5.9 Method Penting Warisan `Object`

Setiap class mewarisi tiga method yang hampir selalu perlu di-override:

- **`toString()`** — representasi teks object (default hanya `NamaClass@hash`, tidak berguna).
- **`equals(Object)`** — kesetaraan **logis**. Default `equals` sama dengan `==` (kesetaraan referensi).
- **`hashCode()`** — kode hash untuk struktur seperti `HashMap`/`HashSet` ([Modul 7](07-generics-collections.md)).

**Kontrak `equals`/`hashCode`:** jika dua object `equals`, maka **`hashCode`-nya wajib sama**. Melanggar ini membuat object "hilang" di `HashSet`/`HashMap`. Override **keduanya sekaligus**, dengan field yang sama:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Titik t)) return false;
    return x == t.x && y == t.y;
}

@Override
public int hashCode() {
    return java.util.Objects.hash(x, y);
}
```

### 5.10 Method Chaining

**Method chaining** (*fluent interface*) adalah pola di mana method mengembalikan `this`, sehingga pemanggilan dapat dirangkai dalam satu ekspresi. Contoh familiar: `new StringBuilder().append("a").append("b")`. Pola ini menjadi dasar **Builder pattern** yang sangat umum untuk membuat object dengan banyak parameter opsional (mengganti constructor dengan banyak argumen yang membingungkan).

## Praktik

### Polymorphism dengan Abstract Class

```java
// DemoBentuk.java
import java.util.List;

public class DemoBentuk {
    public static void main(String[] args) {
        List<Bentuk> daftar = List.of(
            new Lingkaran(5),
            new PersegiPanjang(4, 6),
            new Lingkaran(1.5)
        );

        double total = 0;
        for (Bentuk b : daftar) {
            b.cetakInfo();               // dynamic binding: method sesuai tipe object
            total += b.luas();
            if (b instanceof Lingkaran l) {              // pattern matching instanceof
                System.out.printf("   (jari-jari %.1f)%n", l.jariJari());
            }
        }
        System.out.printf("Total luas: %.2f%n", total);
    }
}

abstract class Bentuk {
    abstract double luas();

    void cetakInfo() {
        System.out.printf("%s: luas=%.2f%n", getClass().getSimpleName(), luas());
    }
}

class Lingkaran extends Bentuk {
    private final double r;
    Lingkaran(double r) { this.r = r; }
    double jariJari() { return r; }
    @Override double luas() { return Math.PI * r * r; }
}

class PersegiPanjang extends Bentuk {
    private final double p, l;
    PersegiPanjang(double p, double l) { this.p = p; this.l = l; }
    @Override double luas() { return p * l; }
}
```

### Static vs Dynamic Binding & Urutan Constructor

```java
// DemoBinding.java
public class DemoBinding {
    static void sapa(Hewan h)  { System.out.println("sapa(Hewan)"); }
    static void sapa(Anjing a) { System.out.println("sapa(Anjing)"); }

    public static void main(String[] args) {
        Hewan h = new Anjing();

        // OVERRIDING -> dynamic binding: ditentukan tipe OBJECT (Anjing)
        h.bersuara();

        // OVERLOADING -> static binding: ditentukan tipe VARIABEL (Hewan)
        sapa(h);
        sapa(new Anjing());

        // FIELD tidak polimorfik: ditentukan tipe VARIABEL
        System.out.println("h.jenis = " + h.jenis);
        System.out.println("((Anjing) h).jenis = " + ((Anjing) h).jenis);
    }
}

class Hewan {
    String jenis = "hewan";
    Hewan() { System.out.println("constructor Hewan"); }
    void bersuara() { System.out.println("..."); }
}

class Anjing extends Hewan {
    String jenis = "anjing";
    Anjing() {
        super();                 // eksplisit; biasanya dipanggil otomatis
        System.out.println("constructor Anjing");
    }
    @Override void bersuara() { System.out.println("guk guk"); }
}
```

Perhatikan urutan keluaran: constructor **induk** selalu selesai lebih dulu sebelum constructor anak. Dan `sapa(h)` memilih versi `Hewan` meski object aslinya `Anjing`, karena overloading diputuskan saat kompilasi.

### Interface, Default Method, dan Multi-Implementasi

```java
// DemoPembayaran.java
public class DemoPembayaran {
    public static void main(String[] args) {
        Pembayaran[] metode = { new Transfer("BCA"), new EWallet("Dompetku") };
        for (Pembayaran p : metode) {
            if (Pembayaran.valid(150_000)) {
                p.bayar(150_000);
                p.cetakStruk(150_000);
            }
            // pola "bisa juga melakukan X": cek kemampuan tambahan
            if (p instanceof Notifikasi n) {
                n.kirimNotif("Pembayaran berhasil");
            }
        }
    }
}

interface Pembayaran {
    void bayar(long jumlah);

    default void cetakStruk(long jumlah) {
        System.out.println("  Struk: Rp" + jumlah);
    }

    static boolean valid(long jumlah) { return jumlah > 0; }
}

interface Notifikasi {
    default void kirimNotif(String pesan) {
        System.out.println("  Notifikasi: " + pesan);
    }
}

class Transfer implements Pembayaran {
    private final String bank;
    Transfer(String bank) { this.bank = bank; }
    @Override public void bayar(long jumlah) {
        System.out.println("Transfer via " + bank + " sebesar " + jumlah);
    }
}

// satu class, DUA interface: kemampuan jamak tanpa multiple inheritance of state
class EWallet implements Pembayaran, Notifikasi {
    private final String nama;
    EWallet(String nama) { this.nama = nama; }
    @Override public void bayar(long jumlah) {
        System.out.println("E-wallet " + nama + " memotong saldo " + jumlah);
    }
    @Override public void cetakStruk(long jumlah) {       // override default method
        System.out.println("  Struk digital e-wallet: Rp" + jumlah);
    }
}
```

### `equals` & `hashCode` — Mengapa Keduanya Harus Di-override

```java
// DemoEquality.java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DemoEquality {
    public static void main(String[] args) {
        Set<TitikRusak> rusak = new HashSet<>();
        rusak.add(new TitikRusak(1, 2));
        System.out.println("equals saja, contains? " + rusak.contains(new TitikRusak(1, 2)));

        Set<TitikBenar> benar = new HashSet<>();
        benar.add(new TitikBenar(1, 2));
        System.out.println("equals+hashCode, contains? " + benar.contains(new TitikBenar(1, 2)));
        System.out.println(new TitikBenar(3, 4));
    }
}

class TitikRusak {
    final int x, y;
    TitikRusak(int x, int y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object o) {      // hashCode TIDAK di-override: salah!
        return o instanceof TitikRusak t && x == t.x && y == t.y;
    }
}

class TitikBenar {
    final int x, y;
    TitikBenar(int x, int y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object o) {
        return o instanceof TitikBenar t && x == t.x && y == t.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
    @Override public String toString() { return "Titik(" + x + ", " + y + ")"; }
}
```

Perhatikan: pada `TitikRusak`, `contains` mengembalikan `false` padahal `equals` menyatakan kedua titik setara. Penyebabnya, `HashSet` mencari lewat `hashCode` terlebih dahulu; `hashCode` bawaan `Object` berbasis identitas object sehingga dua object berbeda hampir selalu jatuh di "keranjang" yang berbeda dan `equals` tidak pernah sempat dipanggil.

### Method Chaining & Builder Pattern

```java
// DemoBuilder.java
public class DemoBuilder {
    public static void main(String[] args) {
        Pesanan p = new Pesanan.Builder("Andi")
                .menu("Nasi Goreng")
                .porsi(2)
                .pedas(true)
                .catatan("tanpa bawang")
                .build();
        System.out.println(p);

        Pesanan sederhana = new Pesanan.Builder("Budi").menu("Teh").build();
        System.out.println(sederhana);
    }
}

class Pesanan {
    private final String pemesan, menu, catatan;
    private final int porsi;
    private final boolean pedas;

    private Pesanan(Builder b) {
        this.pemesan = b.pemesan;
        this.menu = b.menu;
        this.catatan = b.catatan;
        this.porsi = b.porsi;
        this.pedas = b.pedas;
    }

    @Override public String toString() {
        return "Pesanan{" + pemesan + ", " + menu + ", porsi=" + porsi
             + ", pedas=" + pedas + ", catatan='" + catatan + "'}";
    }

    static class Builder {
        private final String pemesan;
        private String menu = "-", catatan = "";
        private int porsi = 1;
        private boolean pedas;

        Builder(String pemesan) { this.pemesan = pemesan; }
        Builder menu(String m)    { this.menu = m; return this; }     // return this -> chaining
        Builder porsi(int p)      { this.porsi = p; return this; }
        Builder pedas(boolean b)  { this.pedas = b; return this; }
        Builder catatan(String c) { this.catatan = c; return this; }
        Pesanan build()           { return new Pesanan(this); }
    }
}
```

## Latihan

1. Buat hierarki `Karyawan` (abstract) dengan subclass `KaryawanTetap` dan `KaryawanKontrak`, masing-masing meng-override `hitungGaji()`. Simpan beberapa object dalam satu `List<Karyawan>` dan cetak total gaji. Jelaskan bagian mana yang menunjukkan **polymorphism**.
2. Ubah contoh `DemoBinding`: tambahkan method `static` bernama sama di `Hewan` dan `Anjing`, panggil lewat variabel bertipe `Hewan` yang menunjuk `Anjing`. Method versi mana yang jalan, dan mengapa ini disebut *method hiding*, bukan overriding?
3. Rancang interface `Terbang` dan `Berenang` lalu class `Bebek` yang mengimplementasikan keduanya. Tambahkan `default` method bernama sama di kedua interface dan selesaikan konflik yang muncul dengan `Terbang.super.metode()`.
4. Buat class `Uang(long jumlah, String mata)` dengan `equals`, `hashCode`, dan `toString` yang benar. Buktikan dengan `HashSet` bahwa dua object `Uang` berisi sama dianggap satu elemen, lalu hapus `hashCode` dan amati dampaknya.
5. Tulis ulang class dengan 6 parameter constructor (misal `Mobil`: merek, tipe, warna, tahun, transmisi, jumlahPintu) menjadi **Builder** dengan method chaining. Bandingkan keterbacaan kode pemanggilannya dan jelaskan kapan Builder lebih unggul daripada constructor biasa.

---

[← Modul 4: Class, Object, Access Specifier & Package](04-class-object.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation →](06-fitur-modern.md)
