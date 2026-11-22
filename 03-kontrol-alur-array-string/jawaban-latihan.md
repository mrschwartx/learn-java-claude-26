# Jawaban Latihan — Modul 3: Kontrol Alur, Array & String

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Hasilnya deterministik di mesin mana pun, kecuali **satu baris waktu eksekusi di Latihan 4** yang sengaja ditandai `[WAKTU, bervariasi]`.

---

## Latihan 1 — Ganjil/genap, positif/negatif/nol, kelipatan 5

Kode: `latihan-1/Angka.java`. Masukan dan keluaran: `masukan-*.txt`, `hasil-*.txt`.

```java
String paritas;
if (n % 2 == 0) { paritas = "genap"; } else { paritas = "ganjil"; }      // if-else

String tanda = switch (Integer.signum(n)) {                              // switch expression
    case 1  -> "positif";
    case -1 -> "negatif";
    default -> "nol";
};

boolean kelipatanLima = n % 5 == 0;
```

| Masukan | Keluaran |
|---|---|
| `15` | 15 adalah bilangan ganjil, positif, kelipatan 5 |
| `-8` | -8 adalah bilangan genap, negatif, bukan kelipatan 5 |
| `0` | 0 adalah bilangan genap, nol, kelipatan 5 |
| `-7` | -7 adalah bilangan ganjil, negatif, bukan kelipatan 5 |

**Mengapa memilih ini:**

- **`n % 2 == 0` untuk genap, bukan `n % 2 == 1` untuk ganjil.** Di Java, sisa pembagian mengikuti tanda pembilang: `-7 % 2` adalah **`-1`**, bukan `1`. Maka `n % 2 == 1` salah menilai semua bilangan ganjil negatif sebagai "bukan ganjil". Memeriksa `== 0` aman untuk semua tanda.
- **`Integer.signum(n)` untuk `switch`.** `switch` tidak bisa langsung memakai kondisi `n > 0` (hanya nilai konstanta), jadi nilai dipetakan dulu ke `-1 / 0 / 1`. Dengan `default` yang menangani `0`, switch expression otomatis lengkap.
- **Nol** adalah genap *dan* kelipatan 5 (`0 % 5 == 0`), dan itulah yang dicetak. Kasus tepi seperti ini mudah terlewat bila hanya dites dengan angka positif.
- Untuk kelipatan 5 cukup ekspresi `boolean` + ternary, tidak perlu `if` panjang.

---

## Latihan 2 — FizzBuzz 1–100: `switch` vs `if-else`

Kode: `latihan-2/FizzBuzzSwitch.java` dan `FizzBuzzIf.java`. Keduanya mencetak 100 baris dan **keluarannya identik byte demi byte** (diverifikasi dengan `cmp`).

```java
// Versi switch expression                  // Versi if-else
String hasil = switch (i % 15) {            if (i % 15 == 0)      println("FizzBuzz");
    case 0 -> "FizzBuzz";                   else if (i % 3 == 0)  println("Fizz");
    case 3, 6, 9, 12 -> "Fizz";             else if (i % 5 == 0)  println("Buzz");
    case 5, 10 -> "Buzz";                   else                  println(i);
    default -> String.valueOf(i);
};
println(hasil);
```

| Aspek | `switch` | `if-else` |
|---|---|---|
| Keterbacaan | Tabel keputusan yang rapi, tetapi pembaca harus **menyadari trik `i % 15`** dan menghafal bahwa 3, 6, 9, 12 adalah kelipatan 3 yang bukan kelipatan 15 | **Langsung terbaca seperti aturan soalnya** ("kelipatan 15, atau 3, atau 5") |
| Risiko salah | Daftar `case` ditulis manual: salah satu angka terlewat tidak ketahuan compiler. Bila batas diubah (misal Fizz = kelipatan 4), seluruh daftar harus disusun ulang | Kondisinya **generik**, otomatis berlaku untuk rentang berapa pun |
| Urutan | Tidak penting | **Penting**: cek kelipatan 15 harus di atas 3 dan 5, kalau tidak, `15` tercetak "Fizz" |
| Kapan unggul | Pemetaan nilai diskret yang jumlahnya terbatas (hari, kode status) | Aturan yang dinyatakan sebagai **kondisi** (kelipatan, rentang) |

**Kesimpulan:** untuk FizzBuzz versi `if-else` lebih tepat, karena aturannya berbentuk kondisi, bukan daftar nilai. Contoh `switch` pada modul menarik untuk memperkenalkan sintaks panah, tetapi untuk soal ini ia terlalu bergantung pada trik. Moral umumnya: pilih `switch` ketika satu variabel dicocokkan dengan banyak konstanta, bukan sekadar karena sintaksnya lebih baru.

---

## Latihan 3 — Nilai terbesar kedua

Kode: `latihan-3/TerbesarKedua.java`. Keluaran: `latihan-3/hasil.txt`.

### Tanpa mengurutkan (satu putaran)

```java
Integer terbesar = null, kedua = null;
for (int x : a) {
    if (terbesar == null || x > terbesar) {
        kedua = terbesar;          // juara lama turun jadi runner-up
        terbesar = x;
    } else if (x != terbesar && (kedua == null || x > kedua)) {
        kedua = x;
    }
}
```

Intinya: simpan dua "pemenang" sambil menelusuri array sekali. Jika ada nilai yang lebih besar dari `terbesar`, juara lama otomatis menjadi `kedua`. Kompleksitas **O(n)**, array asli tidak berubah.

### Dengan `Arrays.sort`

Urutkan **salinan** (`a.clone()`, agar array asli tidak rusak), lalu ambil elemen sebelum yang terakhir, dengan melewati nilai yang sama dengan terbesar. Kompleksitas **O(n log n)**.

### Hasil pada empat kasus uji

| Array | Kedua (tanpa sort) | Kedua (dengan sort) |
|---|---|---|
| `[42, 17, 93, 8, 56, 12, 71, 30, 64, 25]` | 71 | 71 |
| `[42, 17, 93, 8, 56, 93, 71, 30, 64, 25]` (93 dua kali) | 71 | 71 |
| `[-5, -12, -3, -40, -3, -9, -20, -7, -15, -8]` (semua negatif) | -5 | -5 |
| `[7, 7, 7, 7, 7, 7, 7, 7, 7, 7]` (semua sama) | `null` | `null` |

### Tiga jebakan yang diuji kasus-kasus di atas

1. **Inisialisasi dengan 0 atau `Integer.MIN_VALUE`.** Untuk array semua negatif, awalan `terbesar = 0` tidak pernah tergantikan dan jawabannya salah. Saya memakai `Integer` yang bernilai `null` (belum ada data) sebagai penanda. Cara lain yang sah: mulai dari `a[0]`.
2. **Definisi "kedua".** Dengan terbesar kembar (`93, 93`), jawaban bisa **93** (urutan posisi) atau **71** (nilai berbeda). Soal tidak merinci, jadi saya pilih *nilai berbeda* dan menjelaskannya. Jika Anda butuh definisi lain, ubah perbandingannya.
3. **Tidak ada nilai kedua.** Semua elemen sama (atau array berisi < 2 elemen): mengembalikan `null` lebih jujur daripada angka palsu.

### Perbandingan

| | Tanpa sort | `Arrays.sort` |
|---|---|---|
| Kompleksitas | O(n) | O(n log n) |
| Memori tambahan | O(1) | O(n) (salinan) |
| Kode | Lebih panjang, lebih banyak kasus tepi | Pendek dan jelas |
| Cocok | Data besar, atau hanya butuh 1–2 teratas | Butuh data terurut juga, atau 10 elemen seperti ini (selisihnya tak terasa) |

Untuk 10 angka, keduanya sama-sama instan; pilih yang paling mudah dibaca. Perbedaan baru berarti saat data jutaan, atau saat Anda butuh "k terbesar" (di sana ada struktur seperti heap, di luar cakupan modul ini).

---

## Latihan 4 — `hitungVokal`, `balik`, dan biaya `+` dalam loop

Kode: `latihan-4/Teks.java`. Keluaran: `latihan-4/hasil.txt`.

### `hitungVokal`

```java
static int hitungVokal(String teks) {
    int jumlah = 0;
    for (int i = 0; i < teks.length(); i++) {
        char c = Character.toLowerCase(teks.charAt(i));
        if (c == 'a' || c == 'i' || c == 'u' || c == 'e' || c == 'o') jumlah++;
    }
    return jumlah;
}
```

| Masukan | Hasil |
|---|---|
| `"Belajar Java"` | 5 |
| `"Programming"` | 3 |
| `"AIUEO aiueo"` | 10 (huruf besar ikut terhitung) |
| `""` | 0 |

Huruf diubah ke kecil dulu dengan `Character.toLowerCase`, sehingga `A` dan `a` diperlakukan sama tanpa menulis sepuluh perbandingan.

### `balik` tanpa `StringBuilder.reverse()`

```java
static String balik(String teks) {
    char[] hasil = new char[teks.length()];
    for (int i = 0; i < teks.length(); i++) {
        hasil[i] = teks.charAt(teks.length() - 1 - i);
    }
    return new String(hasil);
}
```

`"Belajar Java"` → `"avaJ rajaleB"`. String kosong dan satu karakter ditangani benar, dan `balik(balik(x))` selalu kembali ke `x` (properti yang berguna sebagai tes).

Saya menaruh hasil di `char[]` (ukurannya sudah diketahui), bukan `hasil = hasil + c` pada `String`, dan itu persis terkait pertanyaan berikutnya. **Keterbatasan:** `charAt` bekerja per `char` (UTF-16). Karakter di luar BMP seperti emoji tersusun dari **dua** `char` (*surrogate pair*), sehingga membalik per `char` akan merusaknya. `StringBuilder.reverse()` menangani kasus itu dengan benar; untuk teks biasa solusi kita sudah cukup.

### Mengapa `+` di dalam loop panjang tidak efisien

`String` itu **immutable**: `s = s + "x"` tidak menambah `"x"` ke `s`, melainkan **membuat String baru** dan menyalin seluruh isi lama ditambah `"x"` ke dalamnya. Pada putaran ke-*i*, ada *i* karakter yang disalin, sehingga totalnya 1 + 2 + … + *n* ≈ **n²/2**. Jadi bila `n` naik 10 kali, kerjanya naik sekitar 100 kali. `StringBuilder` memakai buffer yang bisa tumbuh, sehingga total kerjanya ≈ **n**.

Dari program (n = 20.000, satu karakter per putaran):

| Cara | Karakter disalin/ditulis | Waktu (satu eksekusi saya) |
|---|---|---|
| `+` | ≈ 200.010.000 | ±100 ms |
| `StringBuilder` | ≈ 20.000 | ≈ 0 ms |

Angka karakter di atas dihitung secara matematis dan **pasti**. Kolom waktu adalah **satu pengukuran kasar** di sandbox saya; angkanya akan berbeda di mesin Anda, dan pengukuran satu kali seperti ini bukan *benchmark* yang valid (tidak ada pemanasan JIT). Pola yang bisa Anda percaya: jarak antara keduanya melebar tajam saat `n` dinaikkan.

Catatan: compiler modern mengoptimalkan **satu** ekspresi seperti `"a" + b + "c"` di luar loop, jadi `+` aman dipakai untuk menyambung beberapa bagian sekali jalan. Masalahnya hanya muncul saat menyambung **berulang di dalam loop**, tempat setiap putaran membuat String baru.

---

## Latihan 5 — String pool dan struk kasir

Kode: `latihan-5/StringPool.java` dan `Kasir.java`. Keluaran: `hasil-stringpool.txt`, `hasil-kasir.txt`.

### Mengapa `new String("a") == "a"` bernilai `false`

`==` pada tipe referensi membandingkan **alamat object**, bukan isinya. Hasil dari program:

| Ekspresi | Hasil | Sebab |
|---|---|---|
| `new String("a") == "a"` | `false` | `new` **selalu membuat object baru** di heap, alamatnya berbeda dari literal |
| `new String("a").equals("a")` | `true` | `equals` membandingkan **isi** karakter |
| `"a" == "a"` (dua literal) | `true` | Kedua literal merujuk **satu object yang sama** di string pool |
| `new String("a").intern() == "a"` | `true` | `intern()` mengembalikan versi yang ada di pool |

### Apa itu string pool

**String pool** (*string intern pool*) adalah area khusus di memori JVM yang menyimpan **satu salinan** untuk setiap literal string yang unik. Saat compiler/JVM menemui literal `"a"`, ia memeriksa apakah sudah ada di pool; jika ya, dipakai ulang, jika belum, dibuat dan dimasukkan. Tujuannya menghemat memori dan mempercepat perbandingan literal. Mekanisme ini aman **hanya karena `String` immutable**: tidak ada pihak yang bisa mengubah object yang dibagi bersama.

Dua kasus yang sering mengejutkan (juga ada di `hasil-stringpool.txt`):

- `("ja" + "va")` dari **variabel biasa** dihitung saat *runtime* dan menghasilkan object baru, sehingga `== "java"` bernilai **`false`**.
- Bila variabelnya `final` dan berisi literal (*constant expression*), compiler menggabungkannya saat kompilasi dan hasilnya tetap dari pool, sehingga `== "java"` bernilai **`true`**.

Jadi hasil `==` pada String bergantung pada **detail kompilasi yang tidak boleh diandalkan**. Itulah sebabnya aturan emasnya: **selalu `.equals()`** untuk membandingkan isi.

### Struk kasir dengan `printf`

```
--------------------------------------------
Item           Qty       Harga     Subtotal
--------------------------------------------
Kopi Susu        2      25.000       50.000
Roti Bakar       1      18.500       18.500
Air Mineral      3       5.000       15.000
--------------------------------------------
Total                                 83.500
PPN 11%                                9.185
Total Bayar                           92.685
--------------------------------------------
```

Format yang dipakai:

| Specifier | Arti |
|---|---|
| `%-14s` | teks **rata kiri**, lebar 14 (nama item) |
| `%3d` | bilangan **rata kanan**, lebar 3 (qty) |
| `%,11d` / `%,12d` | bilangan rata kanan dengan **pemisah ribuan**, lebar 11 / 12 |
| `"-".repeat(44)` | garis pemisah selebar tabel (`repeat`, Java 11+) |

Rata kiri untuk teks dan rata kanan untuk angka membuat digit satuan, puluhan, dan ratusan **sejajar** antarbaris sehingga mudah dibandingkan. Lebar kolom dijumlahkan menjadi 44 karakter agar garis pas dengan tabel.

**Dua keputusan teknis yang perlu dicatat:**

- **Locale eksplisit.** `%,d` memakai pemisah sesuai locale (modul menjelaskan: `1,234,567` di `en_US`, `1.234.567` di `id_ID`). Saya memakai `Locale.of("id", "ID")` (tersedia mulai Java 19) agar struk selalu bertitik, di mesin mana pun. Tanpa itu, di sandbox saya akan keluar koma.
- **Uang memakai bilangan bulat.** Rupiah tidak punya sen dalam transaksi sehari-hari, jadi `int`/`long` aman dan tepat (bandingkan dengan peringatan `double` untuk uang di Modul 2). Subtotal memakai `long` agar tidak overflow, dan PPN 11% dibulatkan ke rupiah terdekat dengan `Math.round`. Untuk transaksi dengan pecahan (mata uang bersen, perhitungan bunga), pakai `BigDecimal`.
