# Modul 4: Class, Object, Access Specifier & Package

[← Modul 3: Kontrol Alur, Array & String](03-kontrol-alur-array-string.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface →](05-pilar-oop.md)

**Isi modul ini**

- [Teori](#teori)
  - [4.1 Classes and Objects](#41-classes-and-objects)
  - [4.2 Attributes and Methods](#42-attributes-and-methods)
  - [4.3 Constructor dan Keyword `this`](#43-constructor-dan-keyword-this)
  - [4.4 Access Specifiers (Access Modifier)](#44-access-specifiers-access-modifier)
  - [4.5 Packages](#45-packages)
  - [4.6 `static` Keyword](#46-static-keyword)
  - [4.7 Initializer Block dan Urutan Inisialisasi](#47-initializer-block-dan-urutan-inisialisasi)
  - [4.8 Nested Classes](#48-nested-classes)
  - [4.9 Object Lifecycle dan Garbage Collection](#49-object-lifecycle-dan-garbage-collection)
  - [4.10 Pass by Value](#410-pass-by-value)
- [Praktik](#praktik)
  - [Class `RekeningBank` dengan Constructor, `static`, dan `this`](#class-rekeningbank-dengan-constructor-static-dan-this)
  - [Urutan Inisialisasi & Pass by Value](#urutan-inisialisasi--pass-by-value)
  - [Nested Class — Static, Inner, Local, dan Anonymous](#nested-class--static-inner-local-dan-anonymous)
  - [Menyusun Project Multi-Package (Kompilasi Manual)](#menyusun-project-multi-package-kompilasi-manual)
- [Latihan](#latihan)

---


## Teori

### 4.1 Classes and Objects

**Class** adalah **cetak biru (blueprint)** yang mendefinisikan data dan perilaku. **Object** adalah **instance** konkret yang dibuat dari class tersebut dengan keyword `new`. Analoginya: class `Mobil` adalah rancangan pabrik; setiap mobil fisik yang keluar dari pabrik adalah sebuah object.

```java
public class Mobil {
    String merek;        // atribut (field)
    int kecepatan;       // atribut

    void gas(int tambahan) {   // method (perilaku)
        kecepatan += tambahan;
    }
}

// Pemakaian:
Mobil m1 = new Mobil();    // membuat object
m1.merek = "Toyota";
m1.gas(40);
Mobil m2 = new Mobil();    // object kedua — datanya terpisah dari m1
```

Variabel `m1` dan `m2` adalah **referensi** (alamat) ke object di heap ([Modul 2](02-dasar-bahasa.md)). Dua variabel dapat merujuk object yang sama: `Mobil m3 = m1;` tidak menyalin object, hanya menyalin referensinya.

### 4.2 Attributes and Methods

**Atribut (field)** menyimpan state object. **Method** mendefinisikan perilaku; bentuk umumnya:

```
[access] [static] tipeKembalian namaMethod(parameter...) { ... }
```

- **`void`** berarti method tidak mengembalikan nilai; selain itu wajib `return` nilai bertipe sesuai.
- **Parameter** adalah variabel lokal method yang diisi **argumen** saat dipanggil.
- **Varargs** (`int... angka`) memungkinkan jumlah argumen bebas; di dalam method diperlakukan sebagai array.

```java
int jumlah(int... angka) {
    int total = 0;
    for (int a : angka) total += a;
    return total;
}
// jumlah() = 0, jumlah(1, 2, 3) = 6
```

### 4.3 Constructor dan Keyword `this`

**Constructor** adalah blok khusus yang dijalankan saat object dibuat: namanya **sama dengan nama class** dan **tanpa return type**. Fungsinya menginisialisasi state awal.

- Jika Anda tidak menulis constructor apa pun, Java menyediakan **default constructor** (tanpa parameter). Begitu Anda menulis satu constructor, default tersebut **hilang**.
- Constructor boleh **di-overload** (beberapa versi dengan parameter berbeda).
- **`this`** merujuk ke object saat ini — dipakai untuk membedakan field dari parameter bernama sama, dan **`this(...)`** memanggil constructor lain di class yang sama (*constructor chaining*).

```java
public class Buku {
    private final String judul;
    private final int halaman;

    public Buku(String judul, int halaman) {
        this.judul = judul;        // this.judul = field, judul = parameter
        this.halaman = halaman;
    }

    public Buku(String judul) {
        this(judul, 100);          // memanggil constructor di atas
    }
}
```

### 4.4 Access Specifiers (Access Modifier)

Access specifier mengatur **siapa yang boleh mengakses** class, field, atau method.

| Modifier | Class sendiri | Package sama | Subclass (package lain) | Semua tempat |
|---|:---:|:---:|:---:|:---:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(default / package-private, tanpa keyword)* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

Prinsip umum: **gunakan akses yang paling sempit yang masih memungkinkan kode berfungsi**. Field biasanya `private`, dan hanya diakses lewat method — inilah dasar *encapsulation* ([Modul 5](05-pilar-oop.md)). Class tingkat atas hanya boleh `public` atau package-private.

### 4.5 Packages

**Package** adalah cara mengelompokkan class terkait dan menghindari bentrok nama (*namespace*). Konvensi nama: **domain terbalik** dalam huruf kecil, misalnya `com.contoh.toko.model`.

- Deklarasi `package com.contoh.toko.model;` harus menjadi baris pertama file.
- **Struktur direktori mengikuti nama package**: `com/contoh/toko/model/Produk.java`.
- Class dari package lain dipakai dengan **`import`** (atau nama lengkapnya / *fully qualified name*).

```java
package com.contoh.toko.service;

import com.contoh.toko.model.Produk;     // import satu class
import java.util.*;                      // import semua class dari package (hindari di kode produksi)
import static java.lang.Math.sqrt;       // static import: pakai sqrt() tanpa "Math."
```

Package `java.lang` (berisi `String`, `Math`, `System`, dst.) di-import otomatis. Package juga menjadi dasar **modul Java** (JPMS) yang dibahas di [Modul 12](12-build-tools-modules-logging.md).

### 4.6 `static` Keyword

Anggota **`static`** dimiliki oleh **class**, bukan oleh object tertentu — hanya ada **satu salinan** yang dibagi semua object.

| | Instance member | `static` member |
|---|---|---|
| Dimiliki oleh | Setiap object | Class |
| Diakses lewat | `objek.anggota` | `NamaClass.anggota` |
| Bisa pakai `this`? | Ya | **Tidak** |
| Bisa akses anggota instance? | Ya | **Tidak** langsung (butuh object) |

```java
public class Pelanggan {
    private static int jumlahPelanggan = 0;   // satu variabel, dibagi semua object
    public static final String NEGARA = "Indonesia";   // konstanta: static final

    private final int id;

    public Pelanggan() {
        id = ++jumlahPelanggan;
    }

    public static int getJumlahPelanggan() {   // static method
        return jumlahPelanggan;
    }
}
```

`main` adalah `static` karena JVM harus memanggilnya **sebelum ada object apa pun**. Class yang hanya berisi method `static` (seperti `Math`) disebut **utility class**.

### 4.7 Initializer Block dan Urutan Inisialisasi

Selain constructor, Java punya dua jenis blok inisialisasi:

- **Static initializer** `static { ... }` — dijalankan **sekali** saat class pertama kali dimuat.
- **Instance initializer** `{ ... }` — dijalankan **setiap kali object dibuat**, *sebelum* isi constructor.

Urutan eksekusi saat sebuah class dipakai pertama kali: **(1) static field & static block → (2) instance field & instance block → (3) constructor**. Pada pewarisan, constructor dan blok milik **superclass** dieksekusi lebih dulu ([Modul 5](05-pilar-oop.md)).

### 4.8 Nested Classes

Class dapat dideklarasikan di dalam class lain. Ada empat jenis:

| Jenis | Ciri | Kapan dipakai |
|---|---|---|
| **Static nested class** | Dideklarasikan `static` di dalam class; tidak butuh instance luar | Class pembantu yang erat terkait (misal `Builder`) |
| **Inner class** | Non-static; terikat pada instance class luar dan bisa mengakses anggotanya | Object yang hanya bermakna bersama object luarnya (misal `Node` iterator) |
| **Local class** | Dideklarasikan di dalam method | Logika pembantu satu method |
| **Anonymous class** | Class tanpa nama, dibuat sekaligus saat dipakai | Implementasi sekali pakai (sebagian besar sudah digantikan lambda — [Modul 9](09-functional-programming.md)) |

### 4.9 Object Lifecycle dan Garbage Collection

Siklus hidup sebuah object:

1. **Creation** — `new` mengalokasikan memory di **heap** lalu menjalankan constructor.
2. **Use** — object diakses lewat referensi.
3. **Unreachable** — tidak ada lagi referensi yang dapat dijangkau program (variabel keluar scope, di-set `null`, dst.).
4. **Garbage collection** — JVM membebaskan memory object yang unreachable, di waktu yang **tidak dapat diprediksi**.

Memory JVM secara garis besar: **stack** menyimpan variabel lokal dan frame pemanggilan method (per thread), sedangkan **heap** menyimpan semua object (dibagi semua thread).

- Memanggil `System.gc()` hanya **saran** ke JVM, bukan perintah.
- Method **`finalize()`** sudah **deprecated for removal** — jangan dipakai. Untuk membersihkan resource non-memory (file, koneksi), gunakan **try-with-resources** ([Modul 8](08-exception-io.md)).
- **Memory leak** tetap mungkin di Java: jika object tak terpakai masih "dirujuk" (misalnya tertahan di `static Map` yang terus bertambah), GC tidak bisa membersihkannya.

### 4.10 Pass by Value

**Java selalu pass-by-value** — tidak ada pass-by-reference. Saat method dipanggil, **nilai argumen disalin** ke parameter:

- Untuk **primitif**: yang disalin adalah nilainya, sehingga perubahan di dalam method tidak memengaruhi variabel asal.
- Untuk **object**: yang disalin adalah **nilai referensinya** (alamat). Parameter dan variabel asal merujuk **object yang sama**, jadi **mengubah isi object** terlihat dari luar — tetapi **mengganti referensi** parameter ke object lain tidak berpengaruh ke luar.

Itulah sebabnya method `swap(a, b)` yang menukar dua referensi atau dua `int` tidak akan berhasil menukar variabel di pemanggil.

## Praktik

### Class `RekeningBank` dengan Constructor, `static`, dan `this`

```java
// DemoRekening.java
public class DemoRekening {
    public static void main(String[] args) {
        RekeningBank a = new RekeningBank("Andi", 500_000);
        RekeningBank b = new RekeningBank("Budi");   // saldo awal 0

        a.setor(250_000);
        a.tarik(100_000);
        b.setor(50_000);

        System.out.println(a);
        System.out.println(b);
        System.out.println("Total rekening: " + RekeningBank.getJumlahRekening());
        System.out.println("Bank: " + RekeningBank.NAMA_BANK);
    }
}

class RekeningBank {
    public static final String NAMA_BANK = "Bank Contoh";
    private static int jumlahRekening = 0;

    private final int nomor;
    private final String pemilik;
    private long saldo;

    RekeningBank(String pemilik, long saldoAwal) {
        this.nomor = ++jumlahRekening;
        this.pemilik = pemilik;
        this.saldo = saldoAwal;
    }

    RekeningBank(String pemilik) {
        this(pemilik, 0);
    }

    void setor(long jumlah) {
        if (jumlah <= 0) throw new IllegalArgumentException("Jumlah harus positif");
        saldo += jumlah;
    }

    void tarik(long jumlah) {
        if (jumlah > saldo) {
            System.out.println("Saldo tidak cukup untuk " + pemilik);
            return;
        }
        saldo -= jumlah;
    }

    static int getJumlahRekening() {
        return jumlahRekening;
    }

    @Override
    public String toString() {
        return "Rekening #" + nomor + " [" + pemilik + "] saldo=" + saldo;
    }
}
```

### Urutan Inisialisasi & Pass by Value

```java
// UrutanInisialisasi.java
public class UrutanInisialisasi {
    public static void main(String[] args) {
        System.out.println("-- main dimulai --");
        new Contoh();
        System.out.println("-- object kedua --");
        new Contoh();
    }
}

class Contoh {
    static int s = init("static field");
    int i = init("instance field");

    static { System.out.println("static block"); }
    { System.out.println("instance block"); }

    Contoh() { System.out.println("constructor"); }

    static int init(String nama) {
        System.out.println("inisialisasi " + nama);
        return 1;
    }
}
```

```java
// PassByValue.java
import java.util.Arrays;

public class PassByValue {
    static void ubahPrimitif(int x) {
        x = 99;                    // hanya mengubah salinan lokal
    }

    static void ubahIsi(int[] arr) {
        arr[0] = 99;               // mengubah object yang sama -> terlihat di luar
    }

    static void gantiReferensi(int[] arr) {
        arr = new int[] {7, 7, 7}; // hanya mengganti salinan referensi -> tidak terlihat di luar
    }

    public static void main(String[] args) {
        int n = 1;
        ubahPrimitif(n);
        System.out.println("n setelah ubahPrimitif     : " + n);

        int[] data = {1, 2, 3};
        ubahIsi(data);
        System.out.println("data setelah ubahIsi       : " + Arrays.toString(data));

        gantiReferensi(data);
        System.out.println("data setelah gantiReferensi: " + Arrays.toString(data));
    }
}
```

Perhatikan bahwa pada `UrutanInisialisasi`, "static block" hanya tampil **sekali** meski dua object dibuat, sedangkan "instance block" dan "constructor" tampil setiap kali. Pada `PassByValue`, perubahan isi array terlihat di luar, tetapi penggantian referensi tidak.

### Nested Class — Static, Inner, Local, dan Anonymous

```java
// DemoNested.java
import java.util.Iterator;

public class DemoNested {
    public static void main(String[] args) {
        Playlist p = new Playlist("Santai");
        p.tambah("Lagu A");
        p.tambah("Lagu B");

        // inner class: butuh instance Playlist
        for (Iterator<String> it = p.iterator(); it.hasNext(); ) {
            System.out.println("Putar: " + it.next());
        }

        // static nested class: tidak butuh instance luar
        Playlist.Info info = new Playlist.Info("Santai", 2);
        System.out.println(info.ringkas());

        // local class
        class Penghitung {
            int hitung(String s) { return s.length(); }
        }
        System.out.println("Panjang: " + new Penghitung().hitung("Nested"));

        // anonymous class
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous class berjalan");
            }
        };
        r.run();
    }
}

class Playlist {
    private final String nama;
    private final java.util.List<String> lagu = new java.util.ArrayList<>();

    Playlist(String nama) { this.nama = nama; }

    void tambah(String judul) { lagu.add(judul); }

    Iterator<String> iterator() { return new PemutarIterator(); }

    // inner class: dapat mengakses field 'lagu' milik Playlist
    private class PemutarIterator implements Iterator<String> {
        private int posisi = 0;
        public boolean hasNext() { return posisi < lagu.size(); }
        public String next() { return lagu.get(posisi++); }
    }

    // static nested class
    static class Info {
        final String nama;
        final int jumlah;
        Info(String nama, int jumlah) { this.nama = nama; this.jumlah = jumlah; }
        String ringkas() { return nama + " (" + jumlah + " lagu)"; }
    }
}
```

### Menyusun Project Multi-Package (Kompilasi Manual)

Susunan direktori:

```
toko/
└── src/
    └── com/contoh/toko/
        ├── model/Produk.java
        └── App.java
```

```java
// src/com/contoh/toko/model/Produk.java
package com.contoh.toko.model;

public class Produk {
    private final String nama;
    private final int harga;

    public Produk(String nama, int harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public String getNama() { return nama; }
    public int getHarga() { return harga; }
}
```

```java
// src/com/contoh/toko/App.java
package com.contoh.toko;

import com.contoh.toko.model.Produk;

public class App {
    public static void main(String[] args) {
        Produk p = new Produk("Kopi", 25_000);
        System.out.println(p.getNama() + " = Rp" + p.getHarga());
    }
}
```

```bash
cd toko
javac -d out src/com/contoh/toko/model/Produk.java src/com/contoh/toko/App.java
java -cp out com.contoh.toko.App
# Keluaran: Kopi = Rp25000
```

Opsi `-d out` menaruh hasil kompilasi di folder `out` mengikuti struktur package, dan `-cp` (classpath) memberi tahu JVM di mana mencari class.

## Latihan

1. Buat class `Mahasiswa` dengan field `nim`, `nama`, dan `ipk` (semuanya `private`), dua constructor (satu lengkap, satu hanya `nim` dan `nama` dengan `ipk` default `0.0` lewat `this(...)`), serta method `toString`. Buat tiga object dan cetak semuanya.
2. Tambahkan field `static int jumlahMahasiswa` yang bertambah setiap object dibuat, dan method `static` untuk membacanya. Jelaskan mengapa method `static` tidak bisa langsung mengakses field `nama` milik instance.
3. Tulis class dengan `static` block, instance block, dan constructor yang masing-masing mencetak pesan. **Prediksi** urutan keluarannya jika membuat tiga object, lalu jalankan dan bandingkan.
4. Buktikan dengan kode bahwa Java pass-by-value: buat method `tukar(int a, int b)` yang gagal menukar nilai, lalu method yang menerima `StringBuilder` dan memodifikasi isinya. Jelaskan mengapa keduanya berperilaku berbeda padahal sama-sama pass-by-value.
5. Susun project dua package (`model` dan `app`) seperti contoh toko, kompilasi dengan `javac -d` dan jalankan dengan `java -cp`. Lalu ubah salah satu method `public` di package `model` menjadi package-private dan amati error kompilasi dari package `app`. Jelaskan hasilnya menggunakan tabel access specifier.

---

[← Modul 3: Kontrol Alur, Array & String](03-kontrol-alur-array-string.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 5: Pilar OOP: Inheritance, Polymorphism, Abstraction & Interface →](05-pilar-oop.md)
