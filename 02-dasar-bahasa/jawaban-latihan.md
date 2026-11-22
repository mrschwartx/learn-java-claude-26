# Jawaban Latihan — Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Berbeda dengan Modul 1, latihan modul ini tidak menanyakan data pribadi dan tidak mengukur waktu, jadi **hasilnya sama di mesin mana pun**. Satu-satunya pengecualian adalah `Scanner` di Latihan 3, yang bergantung pada *locale* (dijelaskan di sana).
>
> Ada baiknya Anda mengerjakan prediksi di Latihan 2 sendiri dulu sebelum membaca jawabannya.

---

## Latihan 1 — Delapan tipe primitif dan rentangnya

Kode: `latihan-1/Primitif.java`. Keluaran lengkap: `latihan-1/hasil.txt`.

```java
byte    b = 100;
short   s = 30_000;
int     i = 2_000_000_000;
long    l = 9_000_000_000L;     // akhiran L wajib
float   f = 3.14159f;           // akhiran f wajib
double  d = 2.718281828459045;
char    c = 'J';
boolean z = true;
```

Rentang yang dicetak lewat konstanta wrapper:

| Tipe | Rentang (keluaran program) |
|---|---|
| `byte` | −128 .. 127 |
| `short` | −32768 .. 32767 |
| `int` | −2147483648 .. 2147483647 |
| `long` | −9223372036854775808 .. 9223372036854775807 |
| `float` | ±3.4028235E38 (nilai positif terkecil `1.4E-45`) |
| `double` | ±1.7976931348623157E308 (nilai positif terkecil `4.9E-324`) |
| `char` | 0 .. 65535 (tanpa tanda) |
| `boolean` | tidak ada rentang numerik, hanya `true` / `false` |

Hal yang patut dicatat:

- **`9_000_000_000L` butuh `L`.** Tanpa akhiran itu, literal dianggap `int` dan compiler menolaknya (`integer number too large`), karena 9 miliar melebihi 2,1 miliar.
- **`3.14159f` butuh `f`.** Literal desimal secara default adalah `double`, dan `float f = 3.14159;` ditolak karena narrowing yang bisa kehilangan presisi.
- **`Float.MIN_VALUE` dan `Double.MIN_VALUE` bukan nilai paling negatif.** Keduanya adalah nilai **positif terkecil** yang bukan nol. Ini berbeda dari `Integer.MIN_VALUE` dan membingungkan banyak orang. Nilai paling negatif adalah `-Float.MAX_VALUE` / `-Double.MAX_VALUE`, itulah yang saya cetak.
- **`char` adalah angka tanpa tanda 16 bit.** `(int) 'J'` = 74. Karena itu `char` bisa dipakai dalam aritmetika (`'J' + 1` adalah `int` 75).
- **`boolean` tidak punya `MIN_VALUE`/`MAX_VALUE`**, dan Java sengaja tidak mengizinkan konversi antara `boolean` dan angka.

---

## Latihan 2 — Prediksi `(byte) 130` dan `7 / 2`

Kode: `latihan-2/Prediksi.java`. Keluaran: `latihan-2/hasil.txt`.

| Ekspresi | Hasil | 
|---|---|
| `int x = 130; byte b = (byte) x;` | **`-126`** |
| `double d = 7 / 2;` | **`3.0`** |

### Mengapa `(byte) 130` menjadi `-126`

`byte` hanya 8 bit (−128..127), sedangkan 130 di luar rentang itu. Casting `int → byte` **membuang semua bit kecuali 8 bit terbawah**, tanpa error dan tanpa peringatan.

```
130 (int, 32 bit) = 00000000 00000000 00000000 10000010
                                                └──────┘ 8 bit terbawah yang dipertahankan
8 bit terbawah    = 1000 0010
```

Bit paling kiri dari byte adalah **bit tanda** (*two's complement*). Pola `10000010` ditafsirkan sebagai −128 + 2 = **−126**. Cara cepat memeriksanya: 130 − 256 = −126 (nilai "berputar" dengan periode 256).

### Mengapa `double d = 7 / 2` menjadi `3.0`, bukan `3.5`

Java mengevaluasi **sisi kanan lebih dulu, lalu menugaskannya**. Tipe sisi kanan ditentukan oleh operand-nya, bukan oleh tipe variabel tujuan:

1. `7 / 2` adalah pembagian **`int` ÷ `int`**, hasilnya `int` → `3` (bagian pecahan dibuang, bukan dibulatkan).
2. Baru setelah itu `3` di-*widen* otomatis ke `double` → `3.0`.

Pecahan sudah hilang pada langkah 1, sebelum konversi ke `double`. Perbaikannya: jadikan salah satu operand desimal, `7 / 2.0` atau `7.0 / 2` atau `(double) 7 / 2` → `3.5`. Hati-hati dengan `(double) (7 / 2)`: kurung itu tetap membagi sebagai `int` dulu, jadi hasilnya `3.0`.

---

## Latihan 3 — Luas, keliling, dan diagonal persegi panjang

Kode: `latihan-3/Persegi.java`. Masukan contoh: `masukan-1.txt` (3 dan 4) dan `masukan-2.txt` (5.5 dan 2.5). Keluaran: `hasil-1.txt`, `hasil-2.txt`.

```java
double luas     = panjang * lebar;
double keliling = 2 * (panjang + lebar);
double diagonal = Math.sqrt(panjang * panjang + lebar * lebar);   // Pythagoras
System.out.println(String.format(Locale.US, "Luas     = %.2f", luas));
```

| Panjang × lebar | Luas | Keliling | Diagonal |
|---|---|---|---|
| 3 × 4 | 12.00 | 14.00 | 5.00 (segitiga 3-4-5) |
| 5.5 × 2.5 | 13.75 | 16.00 | 6.04 (√36,5 ≈ 6,0415) |

Format `%.2f` berarti "dua angka di belakang pemisah desimal". Catatan: saat masukan dialirkan dari file, prompt `Panjang:` dan `Lebar  :` tercetak berurutan di satu baris karena tidak ada Enter yang diketik pengguna. Pada eksekusi interaktif, tampilannya normal (satu prompt per baris).

> **Catatan locale (penting).** `Scanner` dan `printf` mengikuti locale sistem. Di locale Indonesia (`id_ID`), pemisah desimal adalah **koma**: `nextDouble()` akan menolak `5.5` dengan `InputMismatchException`, dan `%.2f` mencetak `13,75`. Agar program berperilaku sama di mesin mana pun, saya memaksa `useLocale(Locale.US)` pada `Scanner` dan `Locale.US` pada `String.format`. Jika Anda ingin mengikuti locale sistem, hapus kedua penanda itu dan ketik masukan dengan koma.

---

## Latihan 4 — Mengapa `double` tidak aman untuk uang

Kode: `latihan-4/Uang.java`. Keluaran: `latihan-4/hasil.txt`.

### Bukti

Menjumlahkan `0.1` sebanyak 10 kali:

| Cara | Hasil | `== 1.0`? |
|---|---|---|
| `double` | `0.9999999999999999` | **`false`** |
| `BigDecimal` (dari `"0.1"`) | `1.0` | `true` (lewat `compareTo`) |

Efeknya membesar seiring jumlah operasi. Setelah 1000 transaksi Rp 0,10: `double` memberi **`99.9999999999986`**, `BigDecimal` memberi **`100.0`**. Di sistem keuangan, selisih sekecil apa pun membuat rekonsiliasi tidak pernah cocok dan perbandingan `==` gagal tanpa alasan yang tampak.

### Penyebab

`double` menyimpan pecahan dalam **basis 2**. Seperti 1/3 yang tidak bisa ditulis tepat dalam desimal (0,3333…), angka seperti 0,1 tidak bisa ditulis tepat dalam biner (0,000110011001100…). Yang disimpan adalah **pendekatan terdekat**, dan kesalahan kecil itu menumpuk di setiap operasi. Ini bukan bug Java; ini sifat standar IEEE 754 yang sama di hampir semua bahasa.

### Perbaikan

```java
BigDecimal total = BigDecimal.ZERO;
BigDecimal sepuluhSen = new BigDecimal("0.1");     // dari String
for (int i = 0; i < 10; i++) total = total.add(sepuluhSen);
```

`BigDecimal` menyimpan angka sebagai **bilangan bulat tanpa batas dan skala desimal**, sehingga 0,1 benar-benar 1 × 10⁻¹. Penjumlahan dan perkalian tepat; pembulatan hanya terjadi saat Anda memintanya (`setScale(2, RoundingMode.HALF_UP)`).

### Mengapa `new BigDecimal(0.1)` berbahaya

Literal `0.1` di kode sudah berupa `double`, **jadi sudah tidak tepat sebelum `BigDecimal` melihatnya**. Konstruktor `BigDecimal(double)` kemudian menyalin nilai biner yang tidak tepat itu secara **persis**, sehingga hasilnya:

```
new BigDecimal(0.1)   = 0.1000000000000000055511151231257827021181583404541015625
new BigDecimal("0.1") = 0.1
```

Anda memakai `BigDecimal` tetapi tetap membawa kesalahan `double`, dan tampak seperti sudah aman. Aturannya:

- Buat dari **String**: `new BigDecimal("0.1")`.
- Jika sumbernya memang `double`, gunakan **`BigDecimal.valueOf(0.1)`**, yang memakai representasi teks terpendek dari `double` (`"0.1"`) sehingga hasilnya `0.1`.
- Atau simpan uang sebagai bilangan bulat satuan terkecil (sen) dalam `long`.

### Bonus: `equals` vs `compareTo` pada `BigDecimal`

`new BigDecimal("1.0").equals(new BigDecimal("1.00"))` bernilai **`false`**, karena `equals` ikut membandingkan **skala** (1 vs 2 digit desimal). Untuk membandingkan **nilai**, pakai `compareTo(...) == 0` (hasilnya `0`).

---

## Latihan 5 — Scope variabel lokal, instance, dan static

Kode: `latihan-5/Scope.java`. Keluaran: `latihan-5/hasil.txt`. Error kompilasi yang disengaja: `latihan-5/error-kompilasi.txt`.

### Perbedaan ketiganya

| | **Lokal** | **Instance (field)** | **Static** |
|---|---|---|---|
| Dideklarasikan | Di dalam method atau blok | Di class, di luar method | Di class dengan `static` |
| Siapa pemiliknya | Satu pemanggilan method | **Setiap object** punya salinan sendiri | **Class** (satu salinan bersama) |
| Hidup | Selama blok dieksekusi | Selama object hidup (sampai tidak ada referensi) | Selama class dimuat |
| Terlihat di | Hanya di blok tempat dideklarasikan | Seluruh method instance class itu | Seluruh class, juga lewat `NamaClass.variabel` |
| Nilai default | **Tidak ada**: wajib diisi sebelum dipakai | Ya (0 / `false` / `null`) | Ya |

Bukti dari program: setelah `a.setNilai(500)`, `a.nilai = 500` tetapi `b.nilai = 10` (instance terpisah per object), sedangkan `jumlahObjek = 2` dilihat sama oleh keduanya (static dipakai bersama).

### Shadowing

*Shadowing* terjadi saat variabel lokal (atau parameter) memakai nama yang sama dengan field, sehingga di dalam scope itu nama tersebut merujuk ke yang lokal:

```java
int nilai = 10;                       // field

void tampilkan() {
    int nilai = 99;                   // lokal, MENUTUPI field
    System.out.println(nilai);        // 99  -> yang lokal
    System.out.println(this.nilai);   // 10  -> field asli
}
```

**Mengakses field aslinya:**

- Field **instance**: `this.nama`.
- Field **static**: `NamaClass.nama` (di contoh: `Scope.jumlahObjek`). Kata `this` tidak dipakai untuk static.

**Pola yang sangat umum, sekaligus jebakannya:** parameter yang menutupi field di setter atau constructor.

```java
void setNilai(int nilai) {
    // nilai = nilai;       // SALAH: menugaskan parameter ke dirinya sendiri, field tidak berubah (tanpa error!)
    this.nilai = nilai;     // BENAR
}
```

Baris yang salah **kompilasi dan berjalan tanpa error**, hanya saja field tidak pernah terisi. Ini salah satu bug klasik pemula; sebagian IDE memberi peringatan *"assignment to itself"*. `this.` pada Modul 4 akan dibahas lebih lengkap.

### Scope blok dan pesan error

Variabel yang dideklarasikan di dalam `if`/`for` hanya hidup di blok itu. Mencoba memakainya di luar menghasilkan error kompilasi (`latihan-5/error-kompilasi.txt`):

```
Err.java:1: error: cannot find symbol
  symbol:   variable y
  location: class Err
```

Yang sama berlaku untuk variabel penghitung `for` (`i` pada `for (int i = ...)`), yang lenyap setelah perulangan selesai.
