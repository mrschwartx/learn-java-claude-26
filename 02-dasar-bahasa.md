# Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika

[← Modul 1: Pengenalan Java & Lifecycle Program](01-pengenalan.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 3: Kontrol Alur, Array & String →](03-kontrol-alur-array-string.md)

**Isi modul ini**

- [Teori](#teori)
  - [2.1 Basic Syntax](#21-basic-syntax)
  - [2.2 Variabel](#22-variabel)
  - [2.3 Data Types — Tipe Primitif](#23-data-types--tipe-primitif)
  - [2.4 Tipe Referensi vs Primitif, dan Wrapper Class](#24-tipe-referensi-vs-primitif-dan-wrapper-class)
  - [2.5 Variables and Scopes](#25-variables-and-scopes)
  - [2.6 Type Casting](#26-type-casting)
  - [2.7 Operator](#27-operator)
  - [2.8 Math Operations](#28-math-operations)
- [Praktik](#praktik)
  - [Menjelajahi Tipe Data & Batasnya](#menjelajahi-tipe-data--batasnya)
  - [Presisi Floating-Point vs BigDecimal](#presisi-floating-point-vs-bigdecimal)
  - [Kalkulator Konversi Suhu & Scope](#kalkulator-konversi-suhu--scope)
- [Latihan](#latihan)

---


## Teori

### 2.1 Basic Syntax

Beberapa aturan dasar sintaks Java yang berlaku di seluruh course ini:

- Java **case-sensitive**: `Nama`, `nama`, dan `NAMA` adalah tiga identifier berbeda.
- Setiap **statement** diakhiri `;`, dan **blok** kode dibatasi `{ }`.
- **Identifier** (nama variabel, method, class) harus diawali huruf, `_`, atau `$`, tidak boleh sama dengan *keyword* (`class`, `int`, `if`, dst.).
- **Komentar** tidak dieksekusi:

```java
// komentar satu baris

/* komentar
   beberapa baris */

/**
 * Komentar dokumentasi (Javadoc) — dibaca oleh tool javadoc dan IDE.
 */
```

### 2.2 Variabel

**Variabel** adalah nama untuk lokasi di memory yang menyimpan nilai. Karena Java statically typed, setiap variabel **harus punya tipe** yang tidak berubah selama hidupnya.

```java
int umur = 25;                // deklarasi + inisialisasi
String nama = "Budi";
double tinggi;                // deklarasi saja
tinggi = 170.5;               // pengisian nilai belakangan
int a = 1, b = 2, c = 3;      // beberapa variabel sejenis sekaligus
```

**Type inference dengan `var`** (Java 10+): untuk **variabel lokal**, compiler dapat menyimpulkan tipe dari nilai di sisi kanan. Tipenya tetap statis — hanya penulisannya yang lebih ringkas.

```java
var pesan = "Halo";           // disimpulkan sebagai String
var jumlah = 10;              // int
var daftar = new java.util.ArrayList<String>();
// pesan = 5;                 // ERROR: pesan tetap String
```

> **Catatan:** `var` hanya boleh untuk variabel lokal dengan inisialisasi — bukan untuk field, parameter method, atau return type. Gunakan hanya saat tipenya sudah jelas dari konteks; jangan korbankan keterbacaan.

### 2.3 Data Types — Tipe Primitif

Java memiliki **8 tipe primitif**. Nilainya disimpan langsung (bukan lewat referensi) sehingga sangat efisien.

| Tipe | Ukuran | Rentang / Keterangan | Default (field) |
|---|---|---|---|
| `byte` | 8 bit | −128 s.d. 127 | `0` |
| `short` | 16 bit | −32.768 s.d. 32.767 | `0` |
| `int` | 32 bit | ≈ −2,1 miliar s.d. 2,1 miliar (tipe bilangan bulat default) | `0` |
| `long` | 64 bit | ≈ ±9,2 × 10¹⁸ (akhiri literal dengan `L`) | `0L` |
| `float` | 32 bit | Desimal ±7 digit presisi (akhiri literal dengan `f`) | `0.0f` |
| `double` | 64 bit | Desimal ±15 digit presisi (tipe desimal default) | `0.0` |
| `char` | 16 bit | Satu karakter UTF-16, ditulis `'A'` | `'\u0000'` |
| `boolean` | — | `true` atau `false` | `false` |

**Literal** yang perlu dikenal:

```java
long populasi = 280_000_000L;     // underscore memudahkan pembacaan
int biner = 0b1010;               // 10
int heksa = 0xFF;                 // 255
int oktal = 017;                  // 15
float suhu = 36.6f;
double pi = 3.14159;
char huruf = 'J';
char unicode = '\u0041';          // 'A'
boolean aktif = true;
```

### 2.4 Tipe Referensi vs Primitif, dan Wrapper Class

Selain primitif, semua tipe lain adalah **tipe referensi** (class, interface, array, enum, record). Variabel referensi menyimpan **alamat** object di heap, bukan object-nya sendiri. Nilai khusus `null` berarti "tidak merujuk ke object apa pun".

| | Primitif | Referensi |
|---|---|---|
| Menyimpan | Nilai langsung | Alamat ke object di heap |
| Bisa `null`? | Tidak | Ya |
| Punya method? | Tidak | Ya |
| Contoh | `int`, `double` | `String`, `Integer`, `ArrayList` |

Setiap primitif punya **wrapper class**: `Integer`, `Long`, `Double`, `Boolean`, `Character`, `Byte`, `Short`, `Float`. Wrapper dibutuhkan terutama untuk **collections** ([Modul 7](07-generics-collections.md)) yang hanya menerima object. Konversi otomatis disebut **autoboxing** dan **unboxing**:

```java
Integer boxed = 42;        // autoboxing: int -> Integer
int primitif = boxed;      // unboxing:   Integer -> int
```

> **Jebakan klasik:** membandingkan wrapper dengan `==` membandingkan **referensi**, bukan nilai. Karena cache kecil untuk nilai −128..127, hasilnya bisa menipu. **Selalu gunakan `.equals()`** untuk membandingkan object.

### 2.5 Variables and Scopes

**Scope** menentukan bagian program tempat sebuah variabel dapat diakses.

| Jenis variabel | Dideklarasikan di | Hidup selama | Nilai default? |
|---|---|---|---|
| **Local** | Dalam method/blok | Selama blok dieksekusi | **Tidak ada** — wajib diinisialisasi sebelum dipakai |
| **Instance** (field) | Dalam class, di luar method | Selama object hidup | Ya (0/null/false) |
| **Static** (class variable) | Dalam class dengan `static` | Selama class dimuat | Ya |
| **Parameter** | Daftar parameter method | Selama method berjalan | Dari argumen pemanggil |

Aturan penting:

- Variabel **blok** (di dalam `{ }`, `if`, `for`) hanya terlihat di blok itu.
- Variabel lokal **tidak boleh** memakai nama yang sama dengan variabel lokal lain di scope yang tumpang-tindih.
- Variabel lokal **boleh** menutupi (*shadow*) field dengan nama sama — field bisa diakses dengan `this.` ([Modul 4](04-class-object.md)).

```java
int x = 10;
if (x > 5) {
    int y = x * 2;      // y hanya ada di dalam blok if
}
// System.out.println(y);   // ERROR: cannot find symbol
```

### 2.6 Type Casting

**Casting** adalah konversi satu tipe ke tipe lain.

**Widening (implicit)** — dari tipe kecil ke besar, aman, otomatis:

```
byte → short → int → long → float → double
              char → int
```

**Narrowing (explicit)** — dari tipe besar ke kecil, bisa **kehilangan data**, harus ditulis eksplisit dengan `(tipe)`:

```java
double d = 9.99;
int i = (int) d;        // 9 — bagian desimal DIPOTONG (bukan dibulatkan)

int besar = 130;
byte b = (byte) besar;  // -126 — overflow, nilainya "berputar"
```

**Konversi String ↔ angka** menggunakan method, bukan casting:

```java
int n = Integer.parseInt("123");
double x = Double.parseDouble("3.14");
String s1 = String.valueOf(99);
String s2 = Integer.toString(99);
// Integer.parseInt("abc") melempar NumberFormatException ([Modul 8](08-exception-io.md))
```

Perhatikan juga: pembagian dua `int` menghasilkan `int` (bagian desimal dibuang).

```java
int hasil1 = 7 / 2;          // 3
double hasil2 = 7 / 2;       // 3.0  (pembagian int dulu, baru dikonversi)
double hasil3 = 7 / 2.0;     // 3.5  (salah satu operand double)
```

### 2.7 Operator

| Kategori | Operator | Contoh |
|---|---|---|
| Aritmetika | `+` `-` `*` `/` `%` | `10 % 3` → `1` |
| Increment/decrement | `++` `--` | `i++` (pakai dulu, tambah kemudian), `++i` (tambah dulu) |
| Assignment | `=` `+=` `-=` `*=` `/=` `%=` | `x += 5` ≡ `x = x + 5` |
| Perbandingan | `==` `!=` `<` `>` `<=` `>=` | `a >= b` |
| Logika | `&&` `\|\|` `!` | `umur > 17 && punyaKtp` |
| Bitwise | `&` `\|` `^` `~` `<<` `>>` `>>>` | `5 & 3` → `1` |
| Ternary | `kondisi ? a : b` | `nilai >= 60 ? "Lulus" : "Gagal"` |

`&&` dan `||` bersifat **short-circuit**: operand kanan tidak dievaluasi jika hasil sudah pasti dari operand kiri — bermanfaat untuk menghindari error seperti `obj != null && obj.isValid()`.

**Urutan prioritas** (tinggi → rendah, ringkas): `++ --` → `* / %` → `+ -` → perbandingan → `&&` → `||` → ternary → assignment. Jika ragu, gunakan **tanda kurung**.

### 2.8 Math Operations

Class `java.lang.Math` menyediakan fungsi matematika umum:

| Method | Fungsi | Contoh → hasil |
|---|---|---|
| `Math.abs(x)` | Nilai absolut | `Math.abs(-5)` → `5` |
| `Math.pow(a, b)` | Pangkat | `Math.pow(2, 10)` → `1024.0` |
| `Math.sqrt(x)` | Akar kuadrat | `Math.sqrt(144)` → `12.0` |
| `Math.max/min(a, b)` | Terbesar / terkecil | `Math.max(3, 9)` → `9` |
| `Math.round(x)` | Pembulatan terdekat | `Math.round(2.5)` → `3` |
| `Math.floor/ceil(x)` | Bulatkan ke bawah / atas | `Math.floor(2.9)` → `2.0` |
| `Math.floorMod(a, b)` | Modulo yang selalu non-negatif untuk `b` positif | `Math.floorMod(-1, 5)` → `4` |
| `Math.random()` | Acak `[0.0, 1.0)` | — |

**Dua jebakan numerik yang wajib dipahami:**

1. **Integer overflow** — `int` yang melampaui batas **berputar diam-diam** tanpa error. Gunakan `Math.addExact`, `multiplyExact`, dst. untuk melempar `ArithmeticException` saat overflow, atau pakai `long`/`BigInteger`.
2. **Presisi floating-point** — `float`/`double` menyimpan pecahan dalam biner sehingga `0.1 + 0.2` **bukan** persis `0.3`. **Jangan** pakai `double` untuk uang.

Solusinya adalah `java.math.BigDecimal` (desimal presisi tepat) dan `java.math.BigInteger` (bilangan bulat tanpa batas):

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

BigDecimal harga = new BigDecimal("19.99");   // WAJIB konstruktor String, bukan double
BigDecimal total = harga.multiply(new BigDecimal("3"));
BigDecimal dibulatkan = total.setScale(1, RoundingMode.HALF_UP);
```

## Praktik

### Menjelajahi Tipe Data & Batasnya

```java
// TipeData.java
public class TipeData {
    public static void main(String[] args) {
        System.out.println("int   : " + Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE);
        System.out.println("long  : " + Long.MIN_VALUE + " .. " + Long.MAX_VALUE);
        System.out.println("byte  : " + Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE);
        System.out.println("double: " + Double.MAX_VALUE);

        // Overflow int: berputar tanpa error
        int max = Integer.MAX_VALUE;
        System.out.println("MAX + 1 = " + (max + 1));

        // Versi aman: melempar exception
        try {
            Math.addExact(max, 1);
        } catch (ArithmeticException e) {
            System.out.println("addExact: " + e.getMessage());
        }

        // Narrowing cast
        double d = 9.99;
        System.out.println("(int) 9.99 = " + (int) d);
        System.out.println("(byte) 130 = " + (byte) 130);

        // Pembagian integer vs desimal
        System.out.println("7 / 2   = " + (7 / 2));
        System.out.println("7 / 2.0 = " + (7 / 2.0));

        // Integer cache: jebakan == pada wrapper
        Integer a = 127, b = 127, p = 128, q = 128;
        System.out.println("127 == 127 : " + (a == b));
        System.out.println("128 == 128 : " + (p == q));
        System.out.println("128 equals : " + p.equals(q));
    }
}
```

Jalankan dengan `java TipeData.java` dan perhatikan: `MAX + 1` menghasilkan angka **negatif**, dan `128 == 128` bernilai `false` meski `equals` bernilai `true`.

### Presisi Floating-Point vs BigDecimal

```java
// Presisi.java
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Presisi {
    public static void main(String[] args) {
        System.out.println("double : 0.1 + 0.2 = " + (0.1 + 0.2));

        BigDecimal x = new BigDecimal("0.1");
        BigDecimal y = new BigDecimal("0.2");
        System.out.println("BigDecimal: 0.1 + 0.2 = " + x.add(y));

        // Hitung total belanja dengan PPN 11%
        BigDecimal harga = new BigDecimal("19999.99");
        BigDecimal ppn = harga.multiply(new BigDecimal("0.11"))
                              .setScale(2, RoundingMode.HALF_UP);
        System.out.println("Harga: " + harga + ", PPN: " + ppn + ", Total: " + harga.add(ppn));

        System.out.println("round(2.5)  = " + Math.round(2.5));
        System.out.println("round(-2.5) = " + Math.round(-2.5));
        System.out.println("floorMod(-1, 5) = " + Math.floorMod(-1, 5) + ", -1 % 5 = " + (-1 % 5));
    }
}
```

### Kalkulator Konversi Suhu & Scope

```java
// Suhu.java
import java.util.Scanner;

public class Suhu {
    static final double OFFSET = 32.0;   // static: milik class, terlihat di semua method

    static double celsiusKeFahrenheit(double c) {
        double hasil = c * 9 / 5 + OFFSET;   // hasil: variabel lokal method ini
        return hasil;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Masukkan suhu Celsius: ");
        double c = in.nextDouble();

        double f = celsiusKeFahrenheit(c);
        System.out.printf("%.1f °C = %.1f °F%n", c, f);

        if (f > 100) {
            String status = "panas sekali";   // scope: hanya di dalam blok if
            System.out.println("Status: " + status);
        }
        in.close();
    }
}
```

Jalankan dengan `java Suhu.java` lalu ketik, misalnya, `37`.

> **Catatan locale:** `Scanner` mengikuti *locale* sistem Anda. Pada locale Indonesia (`id_ID`), pemisah desimal adalah **koma** (`36,6`), bukan titik. Jika input `36.6` ditolak dengan `InputMismatchException`, ketik `36,6` atau paksa locale dengan `new Scanner(System.in).useLocale(java.util.Locale.US)`.

Coba juga memindahkan baris `System.out.println("Status: ...")` ke luar blok `if` dan amati error kompilasinya.

## Latihan

1. Tulis program yang mendeklarasikan satu variabel untuk setiap 8 tipe primitif, mencetak nilainya, lalu mencetak nilai minimum/maksimum tipe numeriknya memakai konstanta wrapper (`Integer.MAX_VALUE`, dst.).
2. Prediksi hasil dari `int x = 130; byte b = (byte) x;` dan `double d = 7 / 2;` **sebelum** menjalankannya, lalu verifikasi dan jelaskan mengapa hasilnya demikian.
3. Buat program yang meminta panjang dan lebar persegi panjang (bertipe `double`), lalu mencetak luas, keliling, dan panjang diagonal (`Math.sqrt`) dengan format dua angka desimal.
4. Tunjukkan dengan kode mengapa `double` tidak aman untuk perhitungan uang (misalnya menjumlahkan `0.1` sebanyak 10 kali), lalu perbaiki dengan `BigDecimal`. Mengapa konstruktor `new BigDecimal(0.1)` justru berbahaya dibanding `new BigDecimal("0.1")`?
5. Jelaskan perbedaan **scope** variabel lokal, instance, dan static. Tulis contoh kode yang menunjukkan *shadowing* (variabel lokal bernama sama dengan field), lalu jelaskan bagaimana mengakses field aslinya.

---

[← Modul 1: Pengenalan Java & Lifecycle Program](01-pengenalan.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 3: Kontrol Alur, Array & String →](03-kontrol-alur-array-string.md)
