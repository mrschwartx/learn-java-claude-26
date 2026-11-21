# Modul 3: Kontrol Alur, Array & String

[← Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika](02-dasar-bahasa.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 4: Class, Object, Access Specifier & Package →](04-class-object.md)

**Isi modul ini**

- [Teori](#teori)
  - [3.1 Conditionals — `if`, `else if`, `else`](#31-conditionals--if-else-if-else)
  - [3.2 `switch` — Statement dan Expression](#32-switch--statement-dan-expression)
  - [3.3 Loops](#33-loops)
  - [3.4 Arrays](#34-arrays)
  - [3.5 Strings](#35-strings)
  - [3.6 Memformat Output](#36-memformat-output)
- [Praktik](#praktik)
  - [FizzBuzz — Loop & Conditional](#fizzbuzz--loop--conditional)
  - [Statistik Nilai dengan Array](#statistik-nilai-dengan-array)
  - [Palindrom, Hitung Kata & Perbandingan String](#palindrom-hitung-kata--perbandingan-string)
- [Latihan](#latihan)

---


## Teori

### 3.1 Conditionals — `if`, `else if`, `else`

**Conditional** memungkinkan program mengambil keputusan berdasarkan kondisi `boolean`.

```java
int nilai = 78;

if (nilai >= 85) {
    System.out.println("A");
} else if (nilai >= 70) {
    System.out.println("B");
} else if (nilai >= 55) {
    System.out.println("C");
} else {
    System.out.println("D");
}
```

Kondisi `if` **harus** bertipe `boolean` (berbeda dengan C/JavaScript, `if (1)` tidak valid di Java). Untuk percabangan sederhana yang menghasilkan nilai, gunakan operator **ternary**: `String status = (nilai >= 60) ? "Lulus" : "Gagal";`.

> **Praktik baik:** selalu gunakan kurung kurawal `{ }` meski hanya satu statement — mencegah bug saat baris baru ditambahkan di kemudian hari.

### 3.2 `switch` — Statement dan Expression

`switch` cocok jika satu variabel dibandingkan dengan banyak nilai konstanta. Gaya **klasik** memerlukan `break` agar tidak "jatuh" ke case berikutnya (*fall-through*):

```java
switch (hari) {
    case 1:
        System.out.println("Senin");
        break;
    case 6:
    case 7:
        System.out.println("Akhir pekan");
        break;
    default:
        System.out.println("Hari kerja");
}
```

Sejak **Java 14**, ada **switch expression** dengan sintaks panah `->` yang **tidak fall-through**, bisa menghasilkan nilai, dan dicek kelengkapannya oleh compiler:

```java
String jenis = switch (hari) {
    case 6, 7 -> "Akhir pekan";
    case 1, 2, 3, 4, 5 -> "Hari kerja";
    default -> throw new IllegalArgumentException("Hari tidak valid: " + hari);
};

int jumlahHuruf = switch (bulan) {
    case "Februari" -> 28;
    case "April", "Juni", "September", "November" -> 30;
    default -> {
        // blok multi-statement: hasilkan nilai dengan yield
        int hasil = 31;
        yield hasil;
    }
};
```

Gaya panah lebih aman dan ringkas — **gunakan sebagai default** untuk kode baru. Kemampuan `switch` yang lebih canggih (pattern matching) dibahas di [Modul 6](06-fitur-modern.md).

### 3.3 Loops

| Loop | Kapan dipakai |
|---|---|
| `for` | Jumlah iterasi diketahui / butuh indeks |
| enhanced `for` (`for-each`) | Menelusuri **semua** elemen array atau collection |
| `while` | Ulangi selama kondisi benar, jumlah iterasi tidak pasti (kondisi dicek **di awal**) |
| `do-while` | Seperti `while`, tapi minimal berjalan **satu kali** (kondisi dicek **di akhir**) |

```java
for (int i = 1; i <= 5; i++) {              // for klasik
    System.out.print(i + " ");
}

int[] angka = {10, 20, 30};
for (int a : angka) {                       // for-each
    System.out.println(a);
}

int n = 3;
while (n > 0) {                             // while
    System.out.println("Hitung mundur: " + n--);
}

do {                                        // do-while: jalan minimal 1x
    System.out.println("Sekali jalan");
} while (false);
```

**Mengontrol loop:**

- `break` — keluar dari loop terdalam.
- `continue` — lompat ke iterasi berikutnya.
- **Label** — `break namaLabel;` keluar dari loop luar tertentu (jarang dipakai, tapi berguna pada nested loop).

```java
luar:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (i * j == 4) break luar;   // keluar dari kedua loop
        System.out.println(i + "x" + j);
    }
}
```

### 3.4 Arrays

**Array** adalah kumpulan elemen **bertipe sama** dengan **ukuran tetap** yang disimpan berurutan dan diakses via **indeks** (mulai dari `0`). Array adalah **object** — berada di heap dan memiliki properti `length`.

```java
int[] skor = new int[5];                 // 5 elemen, semua 0
String[] nama = {"Ani", "Budi", "Citra"};   // inisialisasi langsung
int[] kuadrat = new int[] {1, 4, 9, 16};

skor[0] = 90;
System.out.println(nama.length);         // 3  (length = properti, tanpa tanda kurung)
System.out.println(nama[2]);             // Citra
// nama[3];                              // ArrayIndexOutOfBoundsException saat runtime
```

**Array multi-dimensi** adalah "array dari array":

```java
int[][] matriks = {
    {1, 2, 3},
    {4, 5, 6}
};
System.out.println(matriks[1][2]);       // 6
System.out.println(matriks.length);      // 2 (jumlah baris)
System.out.println(matriks[0].length);   // 3 (jumlah kolom baris 0)

int[][] jagged = new int[3][];           // jagged array: panjang tiap baris boleh berbeda
jagged[0] = new int[1];
jagged[1] = new int[4];
```

Class utilitas **`java.util.Arrays`** memudahkan operasi umum:

| Method | Fungsi |
|---|---|
| `Arrays.toString(a)` | Cetak isi array 1 dimensi (tanpa ini, `println(a)` hanya mencetak alamat) |
| `Arrays.deepToString(a)` | Cetak array multi-dimensi |
| `Arrays.sort(a)` | Urutkan (naik) |
| `Arrays.fill(a, nilai)` | Isi semua elemen |
| `Arrays.copyOf(a, n)` / `copyOfRange(a, i, j)` | Salin (sekaligus ubah ukuran) |
| `Arrays.equals(a, b)` | Bandingkan **isi** (bukan referensi) |
| `Arrays.binarySearch(a, x)` | Cari pada array **yang sudah terurut** |
| `Arrays.asList(...)` | Bungkus array sebagai `List` berukuran tetap |

Karena ukurannya tetap, array kurang fleksibel. Untuk data yang jumlahnya berubah-ubah, gunakan `ArrayList` dari Collections Framework ([Modul 7](07-generics-collections.md), termasuk perbandingan *Array vs ArrayList*).

### 3.5 Strings

**`String`** adalah rangkaian karakter dan merupakan tipe referensi yang paling sering dipakai. Dua sifat terpenting:

- **Immutable** — object `String` tidak bisa diubah setelah dibuat. Method seperti `toUpperCase()` selalu **mengembalikan String baru**.
- **String pool** — literal string (`"halo"`) disimpan di area khusus dan dibagikan; dua literal identik merujuk object yang sama.

```java
String a = "java";
String b = "java";
String c = new String("java");

System.out.println(a == b);        // true  (sama-sama dari pool)
System.out.println(a == c);        // false (object baru di heap)
System.out.println(a.equals(c));   // true  (isi sama)  <-- gunakan ini!
```

> **Aturan emas:** bandingkan isi string dengan **`.equals()`** (atau `.equalsIgnoreCase()`), **tidak pernah** dengan `==`.

**Method `String` yang sering dipakai:**

| Method | Contoh → hasil |
|---|---|
| `length()` | `"Java".length()` → `4` |
| `charAt(i)` | `"Java".charAt(1)` → `'a'` |
| `substring(a, b)` | `"Pemrograman".substring(0, 3)` → `"Pem"` |
| `indexOf(x)` / `lastIndexOf(x)` | `"banana".indexOf("na")` → `2` |
| `contains(x)`, `startsWith(x)`, `endsWith(x)` | pengecekan → `boolean` |
| `toUpperCase()` / `toLowerCase()` | ubah huruf besar/kecil |
| `trim()` / `strip()` | buang spasi tepi (`strip()` paham Unicode, lebih disarankan) |
| `isEmpty()` / `isBlank()` | kosong / kosong atau hanya spasi (`isBlank`: Java 11+) |
| `replace(a, b)` | ganti semua kemunculan |
| `split(regex)` | pecah menjadi `String[]` |
| `String.join(sep, ...)` | gabung beberapa string |
| `repeat(n)` | ulang n kali (Java 11+) |
| `compareTo(x)` | urutan leksikografis (negatif / 0 / positif) |

**Text block** (Java 15+) untuk string multi-baris tanpa escape yang berantakan:

```java
String json = """
        {
          "nama": "Budi",
          "umur": 25
        }
        """;
```

**`StringBuilder`** — karena `String` immutable, menyambung string berulang di dalam loop membuat banyak object sementara. Untuk itu gunakan `StringBuilder` (mutable, jauh lebih efisien):

```java
StringBuilder sb = new StringBuilder();
for (int i = 1; i <= 5; i++) {
    sb.append(i).append(",");
}
sb.setLength(sb.length() - 1);   // buang koma terakhir
System.out.println(sb);          // 1,2,3,4,5
```

(`StringBuffer` adalah versi thread-safe yang lebih lambat; di kode modern hampir selalu `StringBuilder` yang dipakai.)

### 3.6 Memformat Output

`printf` / `String.format` / `"...".formatted(...)` memakai *format specifier*:

| Specifier | Arti | Contoh |
|---|---|---|
| `%s` | String (apa pun lewat `toString`) | `%s` ← `"Ani"` |
| `%d` | Bilangan bulat | `%05d` ← `42` → `00042` |
| `%f` | Desimal | `%.2f` ← `3.14159` → `3.14` |
| `%,d` | Bilangan dengan pemisah ribuan (karakter pemisah mengikuti *locale*: `1,234,567` di `en_US`, `1.234.567` di `id_ID`) | `%,d` ← `1234567` |
| `%n` | Baris baru yang sesuai OS | — |
| `%-10s` / `%10s` | Rata kiri / kanan lebar 10 | untuk tabel teks |

## Praktik

### FizzBuzz — Loop & Conditional

```java
// FizzBuzz.java
public class FizzBuzz {
    public static void main(String[] args) {
        for (int i = 1; i <= 15; i++) {
            String hasil = switch (i % 15) {
                case 0 -> "FizzBuzz";
                case 3, 6, 9, 12 -> "Fizz";
                case 5, 10 -> "Buzz";
                default -> String.valueOf(i);
            };
            System.out.print(hasil + " ");
        }
        System.out.println();
    }
}
```

### Statistik Nilai dengan Array

```java
// StatistikNilai.java
import java.util.Arrays;

public class StatistikNilai {
    public static void main(String[] args) {
        int[] nilai = {78, 92, 65, 88, 54, 97, 71};

        int total = 0;
        int tertinggi = nilai[0];
        int terendah = nilai[0];
        for (int n : nilai) {
            total += n;
            tertinggi = Math.max(tertinggi, n);
            terendah = Math.min(terendah, n);
        }
        double rata = (double) total / nilai.length;

        System.out.printf("Rata-rata : %.2f%n", rata);
        System.out.println("Tertinggi : " + tertinggi);
        System.out.println("Terendah  : " + terendah);

        int[] terurut = nilai.clone();     // salin agar array asli tidak berubah
        Arrays.sort(terurut);
        System.out.println("Asli      : " + Arrays.toString(nilai));
        System.out.println("Terurut   : " + Arrays.toString(terurut));

        int[][] tabel = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tabel[i][j] = (i + 1) * (j + 1);
            }
        }
        System.out.println("Tabel kali: " + Arrays.deepToString(tabel));
    }
}
```

### Palindrom, Hitung Kata & Perbandingan String

```java
// ManipulasiString.java
public class ManipulasiString {
    static boolean palindrom(String teks) {
        String bersih = teks.toLowerCase().replaceAll("[^a-z0-9]", "");
        return new StringBuilder(bersih).reverse().toString().equals(bersih);
    }

    public static void main(String[] args) {
        System.out.println(palindrom("Katak"));                       // true
        System.out.println(palindrom("A man, a plan, a canal: Panama")); // true
        System.out.println(palindrom("Java"));                        // false

        String kalimat = "  belajar java itu  menyenangkan  ";
        String[] kata = kalimat.strip().split("\\s+");
        System.out.println("Jumlah kata: " + kata.length);
        System.out.println("Digabung  : " + String.join("-", kata));

        String a = "java";
        String c = new String("java");
        System.out.println("== : " + (a == c) + ", equals : " + a.equals(c));

        // Immutability: toUpperCase() mengembalikan String BARU
        String asli = "halo";
        asli.toUpperCase();
        System.out.println("Setelah toUpperCase tanpa disimpan: " + asli);

        // Menyambung string di loop: pakai StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i).append(',');
        sb.setLength(sb.length() - 1);
        System.out.println(sb);

        System.out.println("%-10s|%8s|%n".formatted("Nama", "Harga").trim());
        System.out.printf("%-10s|%,8d|%n", "Kopi", 25000);
        System.out.printf("%-10s|%,8d|%n", "Roti", 1250000);
    }
}
```

## Latihan

1. Tulis program yang meminta sebuah angka lalu mencetak apakah angka tersebut **ganjil/genap**, **positif/negatif/nol**, dan **kelipatan 5 atau bukan**. Gunakan `if-else` untuk satu kasus dan switch expression untuk kasus lain.
2. Ubah contoh FizzBuzz agar berjalan 1–100 dan mencetak setiap hasil di baris baru. Lalu tulis ulang menggunakan `if-else` biasa dan bandingkan keterbacaan kedua versinya.
3. Buat program yang menyimpan 10 angka dalam array lalu mencari **nilai terbesar kedua** tanpa mengurutkan array. Setelah itu, selesaikan dengan `Arrays.sort` dan bandingkan.
4. Tulis method `hitungVokal(String teks)` yang mengembalikan jumlah huruf vokal, lalu method `balik(String teks)` yang membalik string tanpa memakai `StringBuilder.reverse()` (gunakan loop dan `charAt`). Mengapa menyambung string dengan `+` di dalam loop panjang tidak efisien?
5. Jelaskan dengan contoh kode mengapa `new String("a") == "a"` bernilai `false` sementara `.equals` bernilai `true`, dan sebutkan apa itu **string pool**. Kemudian buat program kasir sederhana yang mencetak struk tiga item dengan format tabel rata kiri/kanan memakai `printf`.

---

[← Modul 2: Dasar Bahasa: Variabel, Tipe Data & Operasi Matematika](02-dasar-bahasa.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 4: Class, Object, Access Specifier & Package →](04-class-object.md)
