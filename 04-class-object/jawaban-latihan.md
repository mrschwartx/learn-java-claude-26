# Jawaban Latihan — Modul 4: Class, Object, Access Specifier & Package

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran modul ini deterministik, jadi akan sama di mesin Anda. Prediksi Latihan 3 sebaiknya Anda tulis sendiri dulu sebelum membaca jawabannya.

---

## Latihan 1 — Class `Mahasiswa`

Kode: `latihan-1/Mahasiswa.java`. Keluaran: `latihan-1/hasil.txt`.

```java
private final String nim;
private final String nama;
private final double ipk;

public Mahasiswa(String nim, String nama, double ipk) {   // constructor lengkap
    this.nim = nim;
    this.nama = nama;
    this.ipk = ipk;
}

public Mahasiswa(String nim, String nama) {               // constructor singkat
    this(nim, nama, 0.0);                                  // constructor chaining
}
```

```
Mahasiswa{nim=2301001, nama=Andi, ipk=3.75}
Mahasiswa{nim=2301002, nama=Budi, ipk=0.00}
Mahasiswa{nim=2301003, nama=Citra, ipk=3.92}
```

Poin yang perlu dicatat:

- **`this(...)` harus menjadi baris pertama constructor.** Seluruh logika inisialisasi tetap berada di satu tempat (constructor lengkap), sehingga constructor singkat tidak menduplikasi kode.
- **Field `private final`.** `private` menutup akses langsung dari luar (dasar encapsulation); `final` menjamin field hanya diisi sekali, di constructor, sehingga object tidak bisa berubah diam-diam sesudahnya. Saya tidak membuat setter karena soal tidak memintanya.
- **`toString()` memakai `String.format(Locale.US, ...)`** agar IPK selalu tercetak `3.75` (titik), bukan `3,75`, di mesin dengan locale Indonesia.
- **Tanpa `toString`,** `println(a)` hanya akan mencetak sesuatu seperti `Mahasiswa@1b6d3586` (nama class dan hash), yang tidak berguna.

---

## Latihan 2 — Field dan method `static`

Kode: `latihan-2/Mahasiswa.java`. Keluaran: `latihan-2/hasil.txt`. Error yang disengaja: `ContohError.java` dan `error-kompilasi.txt`.

```java
private static int jumlahMahasiswa = 0;          // satu salinan, dibagi semua object

public Mahasiswa(String nim, String nama, double ipk) {
    ...
    jumlahMahasiswa++;
}

public static int getJumlahMahasiswa() { return jumlahMahasiswa; }
```

```
Sebelum membuat object : 0
Setelah dua object     : 2
Setelah tiga object    : 3
```

**Catatan penempatan penghitung:** `jumlahMahasiswa++` hanya ada di **constructor lengkap**. Constructor singkat meneruskan ke sana lewat `this(...)`, sehingga penghitung naik tepat satu kali per object. Jika kita juga menaruhnya di constructor singkat, `Budi` akan terhitung dua kali. Ini keuntungan nyata dari constructor chaining.

**Penghitung bernilai 0 sebelum ada object** karena field `static` hidup sejak class dimuat, tidak bergantung pada object apa pun. Membacanya lewat `a.getJumlahMahasiswa()` diperbolehkan compiler, tetapi menyesatkan (seolah milik `a`); tulis `Mahasiswa.getJumlahMahasiswa()`.

### Mengapa method `static` tidak bisa langsung mengakses `nama`

Pesan compiler (`latihan-2/error-kompilasi.txt`):

```
ContohError.java:6: error: non-static variable nama cannot be referenced from a static context
```

Alasannya: `nama` adalah field **instance**, artinya setiap object punya salinan sendiri (`Andi`, `Budi`, `Citra`). Method `static` dimiliki oleh **class** dan bisa dipanggil **tanpa object sama sekali** (`Mahasiswa.getJumlahMahasiswa()`). Di dalamnya tidak ada `this`, jadi pertanyaan "`nama` milik object yang mana?" tidak punya jawaban. Pada saat pemanggilan, bisa jadi belum ada satu object pun, atau ada tiga.

Cara yang benar bila method `static` memang butuh data instance: **berikan object-nya sebagai parameter** (`static String ambilNama(Mahasiswa m) { return m.nama; }`), atau jadikan method itu method instance. Arah sebaliknya selalu boleh: method instance bebas mengakses anggota `static`.

---

## Latihan 3 — Urutan `static` block, instance block, constructor

Kode: `latihan-3/UrutanBlok.java`. Keluaran: `latihan-3/hasil.txt`.

### Prediksi

Aturannya dari modul: **static (sekali, saat class pertama dipakai) → instance field dan instance block (tiap object, urut sesuai penulisan) → constructor**. Maka untuk tiga object:

- Object ke-1: semua tahap, termasuk bagian *static*.
- Object ke-2 dan ke-3: **hanya** instance field, instance block, constructor.

### Hasil sebenarnya (cocok dengan prediksi)

```
-- main dimulai (class Contoh belum dimuat) --
-- membuat object ke-1 --
inisialisasi static field          <- hanya SEKALI
static block                       <- hanya SEKALI
inisialisasi instance field
instance block
constructor
-- membuat object ke-2 --
inisialisasi instance field
instance block
constructor
-- membuat object ke-3 --
inisialisasi instance field
instance block
constructor
```

Dua pengamatan:

1. **Static dijalankan malas (*lazy*) saat class pertama kali dipakai**, bukan saat program mulai. Perhatikan baris `-- main dimulai --` tercetak **sebelum** "inisialisasi static field". Class `Contoh` baru dimuat dan diinisialisasi ketika `new Contoh()` pertama dieksekusi.
2. **Di dalam kelompok yang sama, urutan mengikuti urutan penulisan di kode.** Di class `Contoh`, field `s` ditulis sebelum static block, maka "inisialisasi static field" tercetak lebih dulu. Hal yang sama berlaku bagi `i` dan instance block. Jika Anda menukar posisi barisnya, urutan keluaran ikut berubah. Coba sendiri.

Yang tidak tercakup di latihan ini namun akan penting nanti (Modul 5): pada pewarisan, bagian static dan constructor **superclass** dijalankan lebih dulu.

---

## Latihan 4 — Membuktikan Java pass-by-value

Kode: `latihan-4/PassByValue.java`. Keluaran: `latihan-4/hasil.txt`.

```
Sebelum tukar: p=1, q=2
  di dalam tukar: a=2, b=1
Setelah tukar: p=1, q=2   <- tidak tertukar

Setelah tambahkan: Halo Dunia   <- isi object berubah
  di dalam ganti: sb=Baru
Setelah ganti    : Halo Dunia   <- tidak berubah

Setelah tukarReferensi: x=X, y=Y   <- tidak tertukar
Object sama dengan milik pemanggil? true
```

### Mengapa `tukar(int a, int b)` gagal

Di dalam method, `a` dan `b` memang tertukar (baris `di dalam tukar: a=2, b=1` membuktikannya), tetapi keduanya hanyalah **salinan** nilai `p` dan `q`. Salinan itu lenyap saat method selesai, sementara `p` dan `q` tidak pernah tersentuh.

### Mengapa `StringBuilder` bisa dimodifikasi, padahal sama-sama pass-by-value

Yang disalin ke parameter **tetap nilai**, tetapi untuk tipe object, nilai itu adalah **alamat (referensi)**. Jadi `sb` di pemanggil dan `sb` di method adalah **dua variabel berbeda yang menyimpan alamat yang sama** (terbukti: `Object sama ... ? true`). Perbedaannya ada pada **apa yang kita lakukan dengan salinan itu**:

| Operasi di dalam method | Efek di pemanggil | Alasan |
|---|---|---|
| `sb.append(" Dunia")` | **Terlihat** | Mengikuti alamat, lalu mengubah **isi** object yang dirujuk keduanya |
| `sb = new StringBuilder("Baru")` | **Tidak terlihat** | Hanya mengubah **isi salinan variabel** (alamat) agar menunjuk object lain; variabel milik pemanggil masih menunjuk object lama |
| `tukarReferensi(x, y)` | **Tidak terlihat** | Menukar dua salinan alamat, bukan variabel asalnya |

Analogi: Anda memberi teman **fotokopi alamat rumah**. Teman bisa mengecat rumah itu (terlihat oleh Anda). Tetapi bila ia mencoret fotokopi dan menulis alamat lain, alamat di tangan Anda tidak berubah.

Karena itu, "Java pass-by-reference untuk object" adalah anggapan keliru. Jika benar pass-by-reference, `ganti` dan `tukarReferensi` akan berhasil, dan keduanya gagal. Tes sederhana: **jika method bisa mengganti variabel milik pemanggil, itu pass-by-reference. Java tidak bisa.** (Cara menukar dua nilai di Java adalah dengan mengembalikan hasilnya, atau menukar elemen dalam array/list.)

---

## Latihan 5 — Project dua package dan access specifier

Kode: `latihan-5/toko/` (package `com.contoh.toko.model` dan `com.contoh.toko.app`). Skrip: `latihan-5/jalankan-latihan5.sh`. Keluaran: `5a-berhasil.txt`, `5b-error-package-private.txt`.

```
toko/src/com/contoh/toko/
├── model/Produk.java       package com.contoh.toko.model
└── app/App.java            package com.contoh.toko.app   (import ...model.Produk)
```

### Kompilasi dan jalan

```
$ javac -d out src/com/contoh/toko/model/Produk.java src/com/contoh/toko/app/App.java
$ java -cp out com.contoh.toko.app.App
Kopi = Rp25000

$ find out -name "*.class"
out/com/contoh/toko/app/App.class
out/com/contoh/toko/model/Produk.class
```

`-d out` membuat folder `out` mengikuti struktur package (itulah mengapa ada `com/contoh/toko/...` di dalamnya), dan `-cp out` memberi tahu JVM di mana mencarinya. Perhatikan nama yang dijalankan adalah **nama lengkap class** (`com.contoh.toko.app.App`), bukan nama file.

### Mengubah `public` menjadi package-private

Saya menghapus kata `public` dari `getNama()` di `Produk` (`String getNama() { ... }`), lalu mengompilasi ulang:

```
src/com/contoh/toko/app/App.java:8: error: getNama() is not public in Produk; cannot be accessed from outside package
        System.out.println(p.getNama() + " = Rp" + p.getHarga());
                            ^
```

### Penjelasan dengan tabel access specifier

| Modifier | Class sendiri | Package sama | Subclass (package lain) | Semua tempat |
|---|:---:|:---:|:---:|:---:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(package-private)* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

- `Produk` ada di package `...model`, sedangkan pemanggilnya, `App`, ada di package **lain** (`...app`) dan **bukan subclass** `Produk`. Pada tabel, itu jatuh ke kolom **"Semua tempat"**.
- Hanya `public` yang memiliki ✅ di kolom itu. Package-private (baris kedua) hanya ✅ untuk "Class sendiri" dan "Package sama", sehingga akses dari `app` ditolak.
- Kesimpulannya, **`import` tidak memberi hak akses.** `import com.contoh.toko.model.Produk` hanya membuat nama class bisa ditulis singkat; boleh-tidaknya memanggil anggotanya tetap ditentukan modifier. Kebetulan class `Produk` sendiri `public`, jadi `import` dan `new Produk(...)` tetap lolos; yang ditolak hanya method yang bukan `public`.

Pesan error juga menunjukkan bahwa kompilasi gagal di **tempat pemanggilan** (`App.java`), bukan di `Produk.java` yang kita ubah. Itu pola umum: mengurangi akses sebuah anggota bisa merusak kode di package lain, dan itulah alasan prinsip *"gunakan akses paling sempit yang masih berfungsi"* diterapkan sejak awal (menyempitkan belakangan jauh lebih sulit daripada memulai sempit).

Eksperimen tambahan (tidak termasuk soal): ubah menjadi `private` akan menghasilkan error `getNama() has private access in Produk`; ubah menjadi `protected` menghasilkan `getNama() has protected access in Produk` (keduanya sudah saya coba) karena `App` bukan subclass `Produk`. Pesan terakhir ini menarik karena `protected` sering dikira "lebih longgar dari package-private", padahal untuk pemanggil di package lain yang bukan subclass, hasilnya sama-sama ditolak.
