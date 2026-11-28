# Jawaban Latihan — Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran modul ini deterministik, jadi akan sama di mesin Anda. Untuk Latihan 2, tebak dulu hasilnya sebelum membaca penjelasannya.

---

## Latihan 1 — Hierarki `Karyawan` dan polymorphism

Kode: `latihan-1/DemoKaryawan.java`. Keluaran: `latihan-1/hasil.txt`.

```java
abstract class Karyawan {
    private final String nama;
    Karyawan(String nama) { this.nama = nama; }
    String nama() { return nama; }
    abstract long hitungGaji();                       // kontrak, tanpa isi
}

class KaryawanTetap extends Karyawan {                // gajiPokok + tunjangan
    @Override long hitungGaji() { return gajiPokok + tunjangan; }
}

class KaryawanKontrak extends Karyawan {              // tarifPerJam * jamKerja
    @Override long hitungGaji() { return tarifPerJam * jamKerja; }
}
```

```
Andi   KaryawanTetap    Rp   9.500.000
Budi   KaryawanKontrak  Rp   9.000.000
Citra  KaryawanTetap    Rp  11.500.000
Dewi   KaryawanKontrak  Rp   7.200.000
Total gaji              Rp  37.200.000
```

### Bagian mana yang menunjukkan polymorphism

Satu baris ini:

```java
for (Karyawan k : daftar) {          // tipe variabel: Karyawan (abstract)
    long gaji = k.hitungGaji();      // tipe object berbeda-beda -> rumus berbeda-beda
```

Variabel `k` bertipe `Karyawan`, tetapi pada putaran pertama ia merujuk `KaryawanTetap` (rumus: gaji pokok + tunjangan) dan pada putaran kedua `KaryawanKontrak` (rumus: tarif × jam). Kode pemanggil **tidak punya `if (k instanceof ...)`** dan tidak tahu jenis konkretnya; JVM memilih method berdasarkan **tipe object sebenarnya** saat runtime (*dynamic binding*). Perhatikan juga `List<Karyawan>` yang berisi dua jenis object sekaligus: hal itu hanya mungkin karena keduanya "is-a" `Karyawan`.

**Manfaat praktisnya:** jika besok ditambah `KaryawanMagang`, cukup membuat class baru yang meng-override `hitungGaji()`. Loop dan perhitungan total **tidak perlu diubah sama sekali**. Inilah prinsip *open for extension, closed for modification*.

Dua catatan desain: (1) `Karyawan` dibuat `abstract` karena "karyawan umum" tidak punya rumus gaji yang masuk akal; compiler melarang `new Karyawan(...)` dan memaksa tiap subclass mengimplementasikan `hitungGaji()`. (2) Gaji bertipe `long` (rupiah bulat), bukan `double`, sesuai pelajaran Modul 2 tentang uang.

---

## Latihan 2 — Method `static` bernama sama: *hiding*, bukan overriding

Kode: `latihan-2/MethodHiding.java`. Keluaran: `latihan-2/hasil.txt`. Error: `latihan-2/error-kompilasi.txt`.

```java
class Hewan  { static void info() { println("info() milik Hewan"); }  void bersuara() { ... } }
class Anjing extends Hewan {
    static void info() { println("info() milik Anjing"); }           // hiding
    @Override void bersuara() { println("Anjing: guk guk"); }        // overriding
}

Hewan h = new Anjing();
```

| Pemanggilan | Hasil | Mengapa |
|---|---|---|
| `h.info()` | **`info() milik Hewan`** | Tipe **variabel** `h` adalah `Hewan` |
| `((Anjing) h).info()` | `info() milik Anjing` | Setelah cast, tipe variabelnya `Anjing` |
| `Hewan.info()` / `Anjing.info()` | milik masing-masing | Class disebut langsung |
| `h.bersuara()` *(pembanding, instance)* | **`Anjing: guk guk`** | Tipe **object** sebenarnya adalah `Anjing` |

### Versi mana yang jalan, dan mengapa disebut *hiding*

Yang jalan pada `h.info()` adalah versi **`Hewan`**, walaupun objeknya `Anjing`. Sebabnya:

- Method `static` **milik class, bukan milik object**. Ia tidak ikut *dynamic binding*. Compiler memilihnya saat kompilasi dari **tipe deklarasi variabel**, dan objek di belakangnya tidak dilihat sama sekali. (Menulis `h.info()` pun sebenarnya hanya "pintasan" yang diterjemahkan compiler menjadi `Hewan.info()`.)
- **Overriding** berarti *menggantikan* method induk untuk object subclass, sehingga pemanggilan lewat referensi induk pun menjalankan versi anak. Pada method static hal itu tidak terjadi: `Hewan.info()` tetap ada dan tetap dipakai oleh `Hewan`. Method di `Anjing` hanya **menyembunyikan** (*hide*) nama itu **ketika kita menulis `Anjing.info()`** atau memakai variabel bertipe `Anjing`.

Bukti formalnya ada di `error-kompilasi.txt`: menambahkan `@Override` pada method static ditolak compiler:

```
Salah.java:7: error: static methods cannot be annotated with @Override
```

Compiler menyatakan dengan tegas bahwa method static **tidak bisa di-override**; yang terjadi adalah hiding.

**Praktik baik:** panggil method static **lewat nama class** (`Hewan.info()`), bukan lewat variabel instance, agar tidak mengecoh pembaca (dan sebagian IDE memberi peringatan). Hal yang sama berlaku bagi *field* (lihat `DemoBinding` di modul): field juga tidak polimorfik.

---

## Latihan 3 — `Terbang`, `Berenang`, dan `Bebek`

Kode: `latihan-3/DemoBebek.java`. Keluaran: `latihan-3/hasil.txt`. Error: `latihan-3/error-kompilasi.txt`.

### Konflik yang muncul

Kedua interface memiliki `default void bergerak()`. Jika `Bebek` mengimplementasikan keduanya **tanpa** meng-override, compiler menolak:

```
error: types Terbang and Berenang are incompatible;
  class Bebek inherits unrelated defaults for bergerak() from types Terbang and Berenang
```

Java tidak mau menebak versi mana yang dimaksud (ini bentuk *diamond problem* dalam pewarisan perilaku), sehingga **kewajiban memilih diserahkan ke class pengimplementasi**.

### Penyelesaian

```java
class Bebek implements Terbang, Berenang {
    @Override
    public void bergerak() {
        System.out.println("Bebek bergerak:");
        Terbang.super.bergerak();      // pilih versi Terbang
        Berenang.super.bergerak();     // pilih versi Berenang
    }
}
```

```
Bebek bergerak:
[Terbang] mengepakkan sayap
[Berenang] mengayuh kaki
```

Hal yang perlu diperhatikan:

- Bentuk **`NamaInterface.super.metode()`** memanggil default method milik interface tertentu. Syntax ini hanya berlaku untuk interface yang **langsung** diimplementasikan class tersebut.
- Meng-override **wajib** dilakukan walau kita hanya ingin satu versi. Jika ingin versi `Terbang` saja, isi method cukup `Terbang.super.bergerak();`. Atau tulis logika sendiri sepenuhnya tanpa memanggil keduanya.
- Method hasil override harus **`public`**: method interface selalu public, dan overriding tidak boleh mempersempit akses.
- Saya juga menugaskan `Terbang t = b;` lalu memanggil `t.bergerak()`: tetap method `Bebek` yang jalan. Sekalipun dipanggil lewat tipe interface, **object-nya** `Bebek`, jadi dynamic binding berlaku sama seperti pada class biasa.
- Konflik hanya terjadi untuk **default method**. Jika kedua interface mendeklarasikan method `abstract` bersignature sama, satu implementasi di `Bebek` memenuhi keduanya, tanpa konflik.

---

## Latihan 4 — `Uang` dengan `equals`, `hashCode`, `toString`

Kode: `latihan-4/DemoUang.java`. Keluaran: `latihan-4/hasil.txt`.

```java
final class Uang {
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Uang u)) return false;            // juga menangani null
        return jumlah == u.jumlah && Objects.equals(mata, u.mata);
    }
    @Override public int hashCode() { return Objects.hash(jumlah, mata); }   // field SAMA dengan equals
    @Override public String toString() { return mata + " " + jumlah; }
}
```

### Hasil dengan implementasi benar

| Pemeriksaan | Hasil |
|---|---|
| `a == b` (dua object isi sama) | `false` (referensi berbeda) |
| `a.equals(b)` | `true` |
| `a.equals(c)` (mata uang berbeda) | `false` |
| `a.hashCode() == b.hashCode()` | `true` |
| `a.equals(null)` | `false` (tidak melempar exception) |
| `HashSet` setelah menambah `a`, `b`, `c` | **2 elemen** (`a` dan `b` dianggap satu; `c` terpisah) |
| `set.contains(new Uang(50_000, "IDR"))` | `true` |

### Setelah `hashCode` dihapus (`UangRusak`)

| Pemeriksaan | Hasil |
|---|---|
| `ra.equals(rb)` | `true` (equals bilang sama) |
| `ra.hashCode() == rb.hashCode()` | **`false`** |
| `HashSet` setelah menambah `ra` dan `rb` | **2 elemen** (seharusnya 1) |
| `rusak.contains(new UangRusak(50_000, "IDR"))` | **`false`** (seharusnya `true`) |

### Mengapa itu terjadi

`HashSet` (dan `HashMap`) bekerja dalam dua langkah: **(1)** hitung `hashCode()` untuk menentukan "keranjang" (*bucket*), **(2)** hanya di keranjang itu (dan hanya untuk hash yang sama) memanggil `equals()`. `hashCode()` bawaan `Object` berbasis **identitas object**, jadi dua object terpisah hampir pasti berhash berbeda. Akibatnya `equals` **tidak pernah dipanggil**; set menganggap keduanya berbeda, dan `contains` tidak menemukan object yang "sama" secara logis.

Itulah **kontrak `equals`/`hashCode`**: bila `a.equals(b)`, maka `a.hashCode() == b.hashCode()` **wajib**. Arah sebaliknya tidak wajib (hash sama boleh saja untuk object berbeda, itu disebut *collision*). Bug ini berbahaya karena **tidak menimbulkan error**: program berjalan, hanya datanya diam-diam salah (duplikat, atau data yang "hilang" saat dicari).

Beberapa hal yang membuat implementasi ini benar:

- `equals` dan `hashCode` memakai **field yang sama** (`jumlah`, `mata`). Menambah field di salah satu dan lupa di yang lain melanggar kontrak.
- `Objects.equals(mata, u.mata)` aman terhadap `null`; `Objects.hash(...)` juga.
- `instanceof` dengan pattern matching sekaligus menangani `null` (hasilnya `false`).
- Class dibuat `final` supaya tidak ada subclass yang merusak simetri `equals` (a.equals(b) harus sama dengan b.equals(a)).
- Perlu dicatat: `Uang` yang dipakai sebagai kunci `HashSet`/`HashMap` sebaiknya **immutable**. Jika field berubah setelah masuk set, hash-nya berubah dan object tidak ditemukan lagi. Itu sebabnya semua field saya buat `final`.

Modul 6 akan memperkenalkan `record`, yang membuat ketiga method ini **otomatis dan benar** untuk class data seperti `Uang`.

---

## Latihan 5 — Dari constructor 6 parameter ke Builder

Kode: `latihan-5/DemoMobil.java`. Keluaran: `latihan-5/hasil.txt`.

### Sebelum: constructor 6 parameter

```java
new MobilLama("Toyota", "Avanza", "Putih", 2022, "Manual", 4);     // benar
new MobilLama("Toyota", "Putih", "Avanza", 2022, "Manual", 4);     // TERTUKAR, tapi lolos kompilasi
```

```
Benar    : MobilLama{Toyota Avanza, warna=Putih, 2022, Manual, 4 pintu}
Tertukar : MobilLama{Toyota Putih, warna=Avanza, 2022, Manual, 4 pintu}   <- datanya salah, tanpa error
```

Empat dari enam parameter bertipe `String` dan dua bertipe `int`, sehingga compiler tidak bisa menangkap kesalahan urutan. Pembaca kode juga tidak tahu arti `2022` atau `4` tanpa membuka definisi constructor.

### Sesudah: Builder

```java
Mobil baru = new Mobil.Builder("Toyota", "Avanza")     // hanya yang WAJIB di constructor Builder
        .warna("Putih")
        .tahun(2022)
        .transmisi("Manual")
        .jumlahPintu(4)
        .build();

new Mobil.Builder("Honda", "Brio").build();            // minimal: sisanya memakai default
```

```
Minimal  : Mobil{Honda Brio, warna=Putih, 2024, Manual, 4 pintu}
Ditolak  : jumlahPintu harus 2..5, bukan 0
```

### Perbandingan

| Aspek | Constructor 6 parameter | Builder |
|---|---|---|
| Keterbacaan di pemanggil | Angka dan string tanpa nama | **Tiap nilai diberi nama** (`.tahun(2022)`), terbaca seperti kalimat |
| Salah urutan argumen | Lolos kompilasi, data salah | Mustahil: urutan pemanggilan method bebas dan tiap nilai bernama |
| Parameter opsional | Butuh banyak constructor overload (*telescoping constructor*: 2, 3, 4, 5, 6 parameter) atau `null` | Lewati saja yang tidak perlu; default berada di Builder |
| Validasi | Tersebar atau terduplikasi di tiap constructor | **Satu tempat**, di `build()` |
| Immutability | Bisa, tetapi parameter makin banyak | Object `Mobil` tetap `final` penuh, tanpa setter; Builder yang bisa berubah |
| Jumlah kode | Sedikit | **Lebih banyak** (class Builder + satu method per field) |

### Kapan Builder lebih unggul

- Parameter **banyak** (aturan kasarnya, lebih dari 4) **dan/atau banyak yang opsional**.
- Ada beberapa parameter **bertipe sama berdekatan** (seperti `merek`, `tipe`, `warna`) yang rawan tertukar.
- Object harus **immutable** tetapi dibangun bertahap, atau perlu **validasi gabungan** antar-field sebelum object boleh ada.

**Kapan constructor biasa sudah cukup:** 1–3 parameter yang semuanya wajib (`new Titik(1, 2)`), atau object data sederhana. Untuk kasus itu `record` (Modul 6) jauh lebih ringkas daripada Builder. Biaya Builder adalah kode tambahan, jadi jangan dipakai untuk class kecil.

Satu pola yang saya terapkan dan layak ditiru: **field wajib dijadikan parameter constructor Builder** (`Builder(merek, tipe)`), bukan method opsional. Dengan begitu object yang tidak lengkap tidak mungkin dibuat, dan kelebihan Builder (nama jelas) tetap dipertahankan untuk field opsional.
