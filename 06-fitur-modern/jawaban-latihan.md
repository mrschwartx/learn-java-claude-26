# Jawaban Latihan — Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran modul ini deterministik, termasuk daftar "acak" di Latihan 1 (memakai seed tetap). Semua kode hanya memakai fitur yang sudah final di Java 21, tanpa fitur preview.

---

## Latihan 1 — Enum `Level` dan `EnumMap`

Kode: `latihan-1/DemoLevel.java`. Keluaran: `latihan-1/hasil.txt`.

```java
enum Level {
    DEBUG(10), INFO(20), WARN(30), ERROR(40);

    private final int bobot;
    Level(int bobot) { this.bobot = bobot; }          // constructor enum selalu private
    int bobot() { return bobot; }

    boolean lebihPentingDari(Level lain) { return this.bobot > lain.bobot; }
}
```

| Pemanggilan | Hasil |
|---|---|
| `WARN.lebihPentingDari(INFO)` | `true` |
| `DEBUG.lebihPentingDari(INFO)` | `false` |
| `INFO.lebihPentingDari(INFO)` | `false` (setara bukan "lebih penting") |

### Menghitung dengan `EnumMap`

Daftar 20 entri dibuat dengan `new Random(42)` (seed tetap, jadi hasilnya bisa diulang di mesin mana pun):

```java
Map<Level, Integer> hitung = new EnumMap<>(Level.class);
for (Level l : log) hitung.merge(l, 1, Integer::sum);     // mulai 1 bila belum ada, selain itu +1
```

```
Hasil hitung  : {DEBUG=3, INFO=7, WARN=5, ERROR=5}   (jumlah 20)
```

Hal yang patut dicatat:

- **`merge(kunci, 1, Integer::sum)`** menggantikan pola `if (!map.containsKey(k)) map.put(k, 0); map.put(k, map.get(k) + 1)` yang panjang dan rawan salah.
- **Urutan cetak mengikuti urutan deklarasi enum** (`DEBUG, INFO, WARN, ERROR`), bukan urutan penyisipan. Itu sifat `EnumMap`, yang secara internal berupa array berindeks `ordinal()`. Karena itu ia lebih cepat dan hemat memori daripada `HashMap` untuk kunci enum.
- **Level yang tidak muncul tidak ada di map.** Pada 20 entri tadi semua level muncul, sehingga contohnya tidak kelihatan. Saya menambah demo lima entri pertama (`WARN, DEBUG, WARN, DEBUG, INFO`): hasilnya `{DEBUG=2, INFO=1, WARN=2}` tanpa `ERROR`. Dua solusi: isi semua level dengan `0` di awal (agar laporan selalu lengkap), atau baca dengan `getOrDefault(Level.ERROR, 0)`.
- `lebihPentingDari` memakai **field `bobot`**, bukan `ordinal()`. `ordinal()` bergantung pada urutan penulisan dan akan berubah bila seseorang menyisipkan konstanta baru di tengah. Field eksplisit membuat maknanya tetap stabil.

---

## Latihan 2 — `Mahasiswa` menjadi `record`

Kode: `latihan-2/DemoMahasiswa.java`. Keluaran: `latihan-2/hasil.txt`.

```java
record Mahasiswa(String nim, String nama, double ipk) {

    Mahasiswa {                                           // compact constructor
        if (!(ipk >= 0.0 && ipk <= 4.0)) {
            throw new IllegalArgumentException("ipk harus 0..4, bukan " + ipk);
        }
        nama = nama.strip();
    }

    Mahasiswa(String nim, String nama) { this(nim, nama, 0.0); }    // constructor tambahan
}
```

| Pemeriksaan | Hasil |
|---|---|
| `println(a)` | `Mahasiswa[nim=2301001, nama=Andi, ipk=3.75]` |
| `a.nama()` | `Andi` (accessor bernama sama dengan komponen) |
| `a.equals(a2)` (dua object, isi sama) | `true` |
| `a == a2` | `false` |
| `set.contains(a2)` | `true` (hashCode konsisten dengan equals) |
| ipk `4.0` dan `0.0` | diterima (batas inklusif) |
| ipk `4.01`, `-0.1`, `NaN` | ditolak dengan `IllegalArgumentException` |
| `new Mahasiswa("2301009", "  Citra  ", 3.5)` | nama tersimpan `Citra` (dirapikan) |

### Yang kini tidak perlu Anda tulis manual

Dibandingkan versi class di Modul 4, compiler membuatkan semuanya:

| Dulu (class) | Sekarang (record) |
|---|---|
| Tiga field `private final` | Otomatis dari daftar komponen |
| Constructor yang mengisi tiga field (`this.nim = nim;` dst.) | Constructor kanonik otomatis (compact constructor hanya menambah validasi) |
| Getter | Accessor `nim()`, `nama()`, `ipk()` (tanpa awalan `get`) |
| `toString()` | Otomatis: `Mahasiswa[nim=..., nama=..., ipk=...]` |
| `equals()` dan `hashCode()` | Otomatis, berdasarkan **semua** komponen, dan konsisten satu sama lain (Latihan 4 Modul 5 menunjukkan betapa mudahnya salah menulisnya manual) |
| Kata `final` pada class | Record otomatis `final` |

Satu baris deklarasi menggantikan sekitar 30 baris. Kode di Modul 4 sebelumnya juga memerlukan satu constructor tambahan, yang tetap ada di sini; aturannya, constructor tambahan **wajib** mendelegasikan ke constructor kanonik lewat `this(...)`.

### Beberapa catatan penting

- **`!(ipk >= 0.0 && ipk <= 4.0)` dipilih sengaja**, bukan `ipk < 0 || ipk > 4`. Untuk `NaN`, semua perbandingan bernilai `false`, jadi bentuk kedua **meloloskan NaN**, sedangkan bentuk pertama menolaknya. Hasil di tabel membuktikannya.
- **Compact constructor boleh mengubah parameter** (`nama = nama.strip()`) sebelum disimpan, tetapi tidak boleh menulis `this.nama = ...`: penugasan ke field dilakukan otomatis di akhir blok.
- **Format `toString` berubah** dari `Mahasiswa{...}` (Modul 4) menjadi `Mahasiswa[...]`, dan `ipk` tercetak `0.0`, bukan `0.00`. Jika format lama dibutuhkan, `toString()` tetap boleh di-override.
- **Immutable, tetapi dangkal.** Record tidak punya setter; objek tidak bisa berubah setelah dibuat. Untuk `String` dan `double` itu penuh. Jika komponennya `List`, isinya masih bisa diubah kecuali disalin dengan `List.copyOf` di compact constructor.
- **Konsekuensi desain:** jika di aplikasi Anda `ipk` perlu berubah, buat object baru (`new Mahasiswa(nim, nama, ipkBaru)`); record tidak cocok untuk object yang state-nya terus berubah.

---

## Latihan 3 — Sealed `Hasil` dan switch tanpa `default`

Kode: `latihan-3/DemoHasil.java`, `DemoHasilMenunggu.java`, dan `contoh-error/HasilBertambah.java`.

```java
sealed interface Hasil permits Sukses, Gagal { }
record Sukses(String data) implements Hasil { }
record Gagal(String pesan) implements Hasil { }

static String tampilkan(Hasil h) {
    return switch (h) {
        case Sukses s -> "OK: " + s.data();
        case Gagal g  -> "GAGAL: " + g.pesan();
    };                                              // tidak ada default
}
```

```
OK: 12 baris dimuat
GAGAL: koneksi terputus
```

`switch` ini sah tanpa `default` karena `Hasil` **tertutup**: compiler tahu hanya `Sukses` dan `Gagal` yang mungkin ada, dan keduanya sudah ditangani (*exhaustive*).

### Yang terjadi saat `Menunggu` ditambahkan ke `permits`

Saya menambahkan `record Menunggu(int antrian) implements Hasil` dan `permits Sukses, Gagal, Menunggu`, **tanpa** menyentuh method `tampilkan`:

```
HasilBertambah.java:4: error: the switch expression does not cover all possible input values
        return switch (h) {
```

**Kompilasi gagal**, dan compiler menunjuk persis `switch` yang belum menangani kasus baru. Setelah ditambah `case Menunggu m -> ...`, kode terkompilasi lagi (`latihan-3/hasil-menunggu.txt`).

### Mengapa ini penting

| | Hierarki biasa + `default` | Sealed + tanpa `default` |
|---|---|---|
| Menambah subtipe baru | `switch` lama diam-diam jatuh ke `default` (yang mungkin salah) | **Compiler memaksa** setiap `switch` diperbarui |
| Kapan kesalahan ketahuan | Saat runtime, mungkin di produksi, mungkin tidak pernah | **Saat kompilasi** |

Jadi **jangan menambah `default` pada switch atas sealed type** hanya "supaya aman", karena `default` justru mematikan pengaman ini.

### Batas pengaman ini: kompilasi terpisah

Jaminan di atas berlaku saat semua kode dikompilasi bersama. Jika hierarki sealed diubah (misalnya tambah `Menunggu`) lalu **hanya file hierarkinya yang dikompilasi ulang** sementara kode pemakai `switch`-nya tidak, tidak ada error saat kompilasi. Saya mencobanya: program lama tetap berjalan untuk `Sukses`, dan **melempar `MatchException` saat runtime** begitu menerima object `Menunggu`. Itu tetap lebih baik daripada diam-diam salah, tetapi alasan bagus untuk selalu mengompilasi ulang seluruh proyek (build tool di Modul 12 melakukannya otomatis).

---

## Latihan 4 — `format(Object)` dengan switch pattern

Kode: `latihan-4/DemoFormat.java`. Keluaran: `latihan-4/hasil.txt`. Error: `latihan-4/error-kompilasi.txt`.

```java
static String format(Object o) {
    return switch (o) {
        case null                  -> "kosong (null)";
        case Integer i when i < 0  -> "bilangan negatif " + i;     // lebih spesifik: di atas
        case Integer i             -> "bilangan " + i;             // lebih umum: di bawah
        case List<?> daftar        -> "list dengan " + daftar.size() + " elemen";
        default                    -> "objek lain: " + o;
    };
}
```

| Masukan | Keluaran |
|---|---|
| `null` | kosong (null) |
| `-5` | bilangan negatif -5 |
| `0` dan `42` | bilangan 0 / bilangan 42 |
| `List.of(1, 2, 3)` | list dengan 3 elemen |
| `List.of()` | list dengan 0 elemen |
| `"teks"` dan `3.14` | objek lain: ... |

Catatan: `case null` ada secara eksplisit. **Tanpa `case null`, `switch` pada nilai `null` melempar `NullPointerException`** (perilaku lama `switch` dipertahankan). Dengan `case null`, kita sendiri yang memutuskan penanganannya.

### Mengapa urutan `case` memengaruhi kompilasi

`switch` pattern memeriksa `case` **dari atas ke bawah** dan memilih yang **pertama cocok**. Jika sebuah `case` yang umum diletakkan sebelum yang spesifik, yang spesifik **tidak akan pernah dijalankan**, dan compiler menolaknya (*dominance*). Saya mencobanya dalam dua bentuk:

```java
case Integer i            -> ...        // menangkap SEMUA Integer
case Integer i when i < 0 -> ...        // tidak mungkin tercapai
```
```
error: this case label is dominated by a preceding case label
```

Hal yang sama terjadi jika `default` diletakkan di atas `case Integer i` (pesan error yang sama): `default` cocok dengan segalanya, sehingga case berikutnya mati. Aturannya: **dari yang paling spesifik ke yang paling umum, dan `default` paling akhir.** (Pada `switch` konstanta biasa, urutan tidak berpengaruh karena nilai-nilainya saling lepas; di pattern, satu object bisa cocok dengan beberapa pola, maka urutannya yang menentukan.)

Satu pengamatan lain: `case Integer i when i < 0` memakai **guard `when`** agar satu tipe bisa dibagi dua cabang tanpa `if` di dalam. Jika `when` terpenuhi tetapi cabang tidak cocok, `switch` terus ke `case` berikutnya (di sini `case Integer i` tanpa guard).

---

## Latihan 5 — Annotation `@Panjang` dan peran `@Retention`

Kode: `latihan-5/DemoPanjang.java`. Keluaran: `latihan-5/hasil.txt`.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Panjang {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}

class Pengguna {
    @Wajib @Panjang(min = 2, max = 20) String nama;
    @Wajib(pesan = "email harus ada") String email;
    @Panjang(min = 8, max = 64) String sandi;
}
```

`Validator` diperluas dengan satu blok yang membaca `@Panjang` lewat reflection:

```java
Panjang p = f.getAnnotation(Panjang.class);
if (p != null && nilai instanceof String s) {
    if (s.length() < p.min() || s.length() > p.max()) {
        galat.add(f.getName() + ": panjang harus " + p.min() + ".." + p.max() + ", bukan " + s.length());
    }
}
```

| Kasus | Hasil validasi |
|---|---|
| `("Andi", "andi@contoh.com", "Rahasia123")` | `[]` (valid) |
| nama `"A"` | `[nama: panjang harus 2..20, bukan 1]` |
| nama 30 huruf | `[nama: panjang harus 2..20, bukan 30]` |
| sandi `"abc"` | `[sandi: panjang harus 8..64, bukan 3]` |
| nama `null` | `[nama: wajib diisi]` (**hanya** `@Wajib` yang berbunyi) |
| `("", null, "x")` | empat galat: nama kosong, nama terlalu pendek, email kosong, sandi pendek |

Keputusan desain: **`@Panjang` mengabaikan `null`** (`nilai instanceof String s` bernilai `false` untuk `null`). "Wajib ada" adalah tanggung jawab `@Wajib`; `@Panjang` hanya memeriksa panjang bila ada nilainya. Dengan begitu `sandi` boleh kosong (opsional), tetapi bila diisi harus 8–64 karakter. Pembagian tanggung jawab seperti ini dipakai juga oleh Bean Validation (`@NotNull` terpisah dari `@Size`), yang akan Anda temui di modul Spring. `@Target(FIELD)` membatasi penempatan ke field; memasangnya di method atau class akan menjadi error kompilasi.

### Peran `@Retention(RUNTIME)` dan apa yang terjadi bila diganti `SOURCE`

`@Retention` menentukan **sampai tahap mana annotation dipertahankan**. Saya membuktikannya dengan tiga annotation identik, ketiganya dipasang pada satu field, lalu dibaca lewat reflection:

```
@UjiRuntime : @UjiRuntime()
@UjiClass   : null
@UjiSource  : null
```

| Retention | Disimpan di... | Terbaca via reflection? | Contoh pemakaian |
|---|---|---|---|
| `SOURCE` | Hanya di source; **dibuang compiler** | **Tidak** | `@Override`, `@SuppressWarnings` (dipakai compiler/tool saja) |
| `CLASS` (bawaan bila tidak ditulis) | Ada di file `.class`, tetapi JVM tidak memuatnya | **Tidak** | Tool yang membaca bytecode |
| `RUNTIME` | Ada di `.class` **dan** dimuat JVM | **Ya** | `@Wajib`, `@Panjang`, `@Component`, `@Entity`, `@Test` |

Jika `@Panjang` diganti menjadi `SOURCE`, kode tetap **terkompilasi tanpa peringatan**, tetapi `f.getAnnotation(Panjang.class)` selalu mengembalikan `null`. Hasilnya, `Validator` **diam-diam berhenti memeriksa panjang** (saya mencobanya): kasus "nama pendek", "nama panjang", dan "sandi pendek" di tabel di atas semuanya berubah menjadi `[]` (lolos), sedangkan galat dari `@Wajib` tetap muncul karena annotation itu masih `RUNTIME`. Karena itu kesalahan ini berbahaya: tidak ada error, validasinya hanya berhenti bekerja. Perhatikan pula bahwa `CLASS` adalah **nilai bawaan**: lupa menulis `@Retention` pada annotation kustom akan menghasilkan perilaku yang sama, jadi untuk annotation yang dibaca lewat reflection `@Retention(RUNTIME)` wajib ditulis eksplisit.
