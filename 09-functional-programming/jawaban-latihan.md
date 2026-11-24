# Jawaban Latihan — Modul 9: Functional Programming: Lambda & Stream API

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21, 2 CPU); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran deterministik, **kecuali dua baris berawalan `[BERVARIASI]`** di Latihan 5 yang sengaja menunjukkan hasil yang berbeda antar eksekusi (demo bahaya `parallel`). Subbab 9.8 (Stream Gatherers) memerlukan Java 24+ dan tidak termasuk soal latihan, sehingga tidak dipakai di sini.

---

## Latihan 1 — Empat bentuk method reference

Kode: `latihan-1/EmpatJenis.java`. Keluaran: `latihan-1/hasil.txt`. Error: `latihan-1/error-kompilasi.txt`.

| # | Jenis | Method reference | Lambda setara | Hasil (keduanya sama) |
|---|---|---|---|---|
| 1 | **Static** | `Integer::parseInt` | `s -> Integer.parseInt(s)` | `42` |
| 2 | Instance dari **object tertentu** | `sapaan::concat` | `s -> sapaan.concat(s)` | `Halo, Dunia` |
| 3 | Instance dari **tipe** | `String::toUpperCase` | `s -> s.toUpperCase()` | `JAVA` |
| 4 | **Constructor** | `ArrayList::new` | `() -> new ArrayList<>()` | `[]` (object baru tiap panggilan) |

### Cara membedakan jenis 2 dan 3

Keduanya memanggil method instance, tetapi **dari mana object-nya berasal** berbeda:

- **Jenis 2** (`sapaan::concat`): objeknya **sudah ditentukan** di sisi kiri `::`. Parameter fungsi menjadi **argumen** method (`sapaan.concat(s)`).
- **Jenis 3** (`String::toUpperCase`): sisi kiri berupa **nama tipe**, jadi **parameter pertama** fungsi-lah yang menjadi object yang dipanggil (`s.toUpperCase()`); sisa parameter (bila ada) menjadi argumen.

Karena itu `String::concat` (jenis 3) dan `sapaan::concat` (jenis 2) berarti hal berbeda: yang pertama menjadi `(a, b) -> a.concat(b)` (dua parameter), yang kedua `s -> sapaan.concat(s)` (satu parameter). Pada jenis 2, ekspresi di kiri `::` dievaluasi **sekali saat method reference dibuat** (menurut spesifikasi bahasa; bila hasilnya `null`, `NullPointerException` terjadi saat itu juga, bukan saat dipanggil). Pada demo, `sb::toString` terikat pada object `sb` yang sama; ketika isinya berubah menjadi `AB`, hasil pemanggilan ikut `AB`, karena yang terikat adalah **object-nya**, bukan isi saat itu.

### Constructor reference mengikuti tipe fungsionalnya

`StringBuilder::new` ditulis sama, tetapi memilih constructor berbeda: sebagai `Supplier<StringBuilder>` ia memanggil `new StringBuilder()` (hasil `""`), sebagai `Function<String, StringBuilder>` ia memanggil `new StringBuilder(String)` (hasil `"isi"`). Compiler memilih berdasarkan target type.

### Kapan method reference **tidak** bisa dipakai: ambiguitas

```java
Function<Integer, String> f = Integer::toString;
```
```
error: incompatible types: invalid method reference
    reference to toString is ambiguous
      both method toString(int) in Integer and method toString() in Integer match
```

`Integer` punya `static toString(int)` (jenis 1) **dan** `toString()` instance (jenis 3), dan keduanya cocok untuk `Function<Integer, String>`. Compiler tidak menebak. Solusinya menulis lambda yang jelas (`i -> Integer.toString(i)`) atau `String::valueOf`.

---

## Latihan 2 — `petakan` dan `cariPertama`

Kode: `latihan-2/DemoHof.java`. Keluaran: `latihan-2/hasil.txt`.

```java
static <T, R> List<R> petakan(List<T> data, Function<T, R> fn) { ... }
static <T> T cariPertama(List<T> data, Predicate<T> syarat, T cadangan) { ... }
```

### Fleksibilitas `petakan`: satu method, banyak perilaku

| Pemanggilan | Hasil |
|---|---|
| `petakan(buah, String::length)` | `[4, 4, 6, 3]` (`String` → `Integer`) |
| `petakan(buah, String::toUpperCase)` | `[APEL, KIWI, MANGGA, PIR]` |
| `petakan(angka, x -> x * x)` | `[9, 64, 225, 484, 49]` |
| `petakan(buah, nama -> new Produk(...))` | list `Produk` (tipe hasil berubah lagi) |
| `petakan(angka, x -> x % 2 == 0 ? "genap" : "ganjil")` | `[ganjil, genap, ...]` |
| `petakan(List.<String>of(), String::length)` | `[]` |

Method yang sama menghasilkan tipe yang berbeda-beda (`R` disimpulkan dari lambda), tanpa cast. Inilah inti *higher-order function*: **perilaku disuntikkan dari luar**, sedangkan kerangkanya (loop) ditulis sekali. Kelima baris `petakan` di atas sama dengan `stream().map(...).toList()`, dan memang itulah yang dilakukan `map`.

### `cariPertama`: berbagai syarat, satu cadangan

| Pemanggilan | Hasil |
|---|---|
| genap pertama dari `[3, 8, 15, 22, 7]` | `8` |
| `> 20` pertama | `22` |
| `> 100` (tidak ada) | `-1` (cadangan) |
| berawalan `"m"` pada buah | `mangga` |
| berawalan `"z"` | `(tidak ada)` (cadangan) |
| list kosong | `0` (cadangan) |

### Catatan penting: cadangan *eager* vs *malas*

Pada `cariPertama(angka, x -> x > 20, hitungMahal())`, **argumen dievaluasi sebelum method dipanggil**. Jadi `hitungMahal()` tetap berjalan walau elemennya ketemu dan cadangan tak terpakai (terbukti: pesan "menghitung cadangan yang mahal..." tercetak pada kasus *ditemukan*). Varian `cariPertamaMalas(..., Supplier<T> cadangan)` menjalankannya **hanya bila diperlukan**: tidak tercetak saat elemen ditemukan, tercetak saat tidak. Ini alasan `Optional.orElseGet(Supplier)` ada di samping `orElse(nilai)` (Modul 7): bila cadangan murah (konstanta), `orElse` cukup; bila mahal atau punya efek samping, pakai `Supplier`.

Peningkatan yang lazim di perpustakaan standar: tanda tangan `Function<? super T, ? extends R>` dan `Predicate<? super T>` agar lebih fleksibel (PECS, Modul 7). Untuk latihan ini bentuk sederhana sudah memadai.

---

## Latihan 3 — Pipeline normalisasi nama dan validator

Kode: `latihan-3/DemoPipeline.java`. Keluaran: `latihan-3/hasil.txt`.

### Pipeline dengan `andThen` dan `compose`

```java
Function<String, String> normalisasi  = trim.andThen(kecil).andThen(kapitalTiapKata);     // kiri → kanan
Function<String, String> normalisasi2 = kapitalTiapKata.compose(kecil).compose(trim);     // kanan → kiri
```

| Masukan | Hasil | `andThen` = `compose`? |
|---|---|---|
| `"  bUdI   sAnToSo "` | `Budi Santoso` | ya |
| `"ANI"` | `Ani` | ya |
| `"  rina  kartika  dewi"` | `Rina Kartika Dewi` | ya |
| `"x"` | `X` | ya |
| `""` | `""` | ya |

- **`f.andThen(g)` = g(f(x))**, dibaca urut langkah; **`f.compose(g)` = f(g(x))**, dibaca terbalik. Dua rakitan di atas **sama persis**, hanya penulisannya yang berlawanan. `andThen` lebih mudah dibaca untuk pipeline.
- **Urutan bermakna.** Bila kapital diletakkan *sebelum* lowercase (`trim → kapital → kecil`), hasilnya `budi santoso`: huruf kapital yang baru dibuat langsung dihancurkan. Saya tampilkan di program sebagai peringatan.
- **Spasi ganda di tengah** ditangani oleh `split("\\s+")` (satu atau lebih spasi), jadi `rina  kartika` tidak menghasilkan kata kosong.
- **`toLowerCase(Locale.ROOT)`, bukan `toLowerCase()`.** Tanpa locale, hasil bergantung pada locale mesin; pada locale Turki, `"I".toLowerCase()` menghasilkan `ı` (i tanpa titik), bukan `i`. `Locale.ROOT` membuat perilaku konsisten di semua mesin.
- Tiap langkah kecil dan **dapat diuji sendiri**, itulah manfaat komposisi dibanding satu method panjang.

### Validator: tiga `Predicate` digabung dengan `and`

```java
Predicate<String> tidakKosong = s -> s != null && !s.isBlank();
Predicate<String> tanpaAngka  = s -> s.chars().noneMatch(Character::isDigit);
Predicate<String> minimal3    = s -> s.strip().length() >= 3;
Predicate<String> namaValid   = tidakKosong.and(tanpaAngka).and(minimal3);
```

| Masukan | Valid? | Aturan yang gagal |
|---|---|---|
| `"Budi"` | ya | – |
| `"Siti Aminah"` | ya | – |
| `"Jo"` | tidak | terlalu pendek |
| `""` dan `"   "` | tidak | kosong (juga pendek) |
| `"Ani2"` dan `"x1y2"` | tidak | mengandung angka |
| `null` | tidak | `tidakKosong` |

- **`and` bersifat *short-circuit***: bila predikat pertama `false`, yang berikutnya tidak dipanggil. Inilah yang membuat `null` aman: `tidakKosong` menolaknya lebih dulu, sehingga `tanpaAngka` (yang akan melempar `NullPointerException` pada `null`) tidak pernah dijalankan. **Urutan dalam rantai `and` karena itu penting**: aturan penjaga (null/kosong) harus di depan.
- "Panjang ≥ 3" saya ukur **setelah `strip()`** (jadi `"  ab  "` ditolak). Soal tidak merincinya; ini keputusan yang sebaiknya dinyatakan.
- Pelengkap yang diperagakan: `negate()` (`namaValid.negate()` menandai `"Jo"` sebagai bermasalah) dan `or`.

---

## Latihan 4 — Analisis `Pegawai` dengan stream

Kode: `latihan-4/DemoPegawai.java`. Keluaran: `latihan-4/hasil.txt`.

Data: 8 pegawai dalam tiga divisi (Engineering ×3, Keuangan ×2, Pemasaran ×3).

### (a) Gaji rata-rata per divisi

```java
data.stream().collect(Collectors.groupingBy(Pegawai::divisi, TreeMap::new,
        Collectors.averagingDouble(Pegawai::gaji)));
```

| Divisi | Rata-rata |
|---|---|
| Engineering | Rp15.000.000,00 |
| Keuangan | Rp10.000.000,00 |
| Pemasaran | Rp9.166.666,67 |

### (b) Pegawai bergaji tertinggi di tiap divisi

```java
Collectors.groupingBy(Pegawai::divisi, TreeMap::new,
    Collectors.collectingAndThen(
        Collectors.maxBy(Comparator.comparingDouble(Pegawai::gaji)),
        opt -> opt.orElseThrow()));
```

| Divisi | Tertinggi |
|---|---|
| Engineering | Citra (Rp18.000.000,00) |
| Keuangan | Eko (Rp11.000.000,00) |
| Pemasaran | Gita (Rp10.000.000,00) |

- `maxBy` menghasilkan `Optional<Pegawai>` (karena secara umum kelompok bisa kosong). Karena `groupingBy` hanya membuat kelompok yang punya anggota, `orElseThrow()` aman; `collectingAndThen` membuka bungkus `Optional` itu sehingga nilai map langsung `Pegawai`, bukan `Optional<Pegawai>`.
- **Cara alternatif** `toMap(divisi, p -> p, BinaryOperator.maxBy(...))` memberi hasil yang sama (diverifikasi: `true`). Pilih yang lebih Anda baca.
- **Jika gaji tertinggi seri,** implementasi JDK mempertahankan elemen yang **pertama** ditemui. Data contoh sengaja tanpa seri; bila seri berarti, tentukan aturan pemutusnya (misal `thenComparing(Pegawai::nama)`).

### (c) Nama pegawai di atas rata-rata keseluruhan

```java
double rataSemua = data.stream().mapToDouble(Pegawai::gaji).average().orElse(0);
String diAtas = data.stream().filter(p -> p.gaji() > rataSemua)
                    .map(Pegawai::nama).collect(Collectors.joining(", "));
```

Rata-rata keseluruhan Rp11.562.500,00 → **`Andi, Budi, Citra`**.

- Perlu **dua pipeline**: rata-rata harus diketahui penuh sebelum menyaring, sedangkan stream satu kali jalan tidak bisa "melihat ke depan". Membungkusnya dalam satu pipeline akan memaksa trik yang kurang terbaca.
- `joining(", ")` pada hasil kosong menghasilkan **string kosong** (bukan `null`), terbukti pada uji "tanpa pemenang".
- Saya memverifikasi silang dengan versi loop biasa: hasilnya cocok.
- `average()` pada `DoubleStream` mengembalikan `OptionalDouble` (data bisa kosong), maka `orElse(0)`.
- Gaji bertipe `double` mengikuti soal. Untuk uang sebenarnya, pelajaran Modul 2 tetap berlaku (`long` rupiah atau `BigDecimal`); di sini nilainya bulat sehingga aman, dan keluaran diformat dengan `Locale` Indonesia.

---

## Latihan 5 — FrekuensiKata dengan stream, lalu *lazy*, sekali pakai, dan `parallel`

Kode: `latihan-5/FrekuensiStream.java`. Keluaran: `latihan-5/hasil.txt`.

### Versi stream

```java
Map<String, Long> baru = Arrays.stream(teks.replace(",", "").split(" "))
        .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
```

Hasil: `java -> 3`, lalu `belajar`, `dan`, `itu` masing-masing `2`, lalu `kuat`, `menyenangkan`, `terus` masing-masing `1`. **Identik dengan versi loop Modul 7** (diverifikasi: `true`). `Function.identity()` berarti "kelompokkan per kata itu sendiri", dan `counting()` mengembalikan `Long` (bukan `Integer` seperti `merge` pada versi lama; harap dicatat bila hasilnya dioperkan ke tempat lain).

### Mengapa stream bersifat *lazy*

Operasi intermediate (`filter`, `map`, `peek`, ...) hanya **mendaftarkan** langkah; **tidak ada elemen yang diproses sampai operasi terminal** dipanggil. Pada program, pesan "Pipeline dibangun; belum ada apa pun tercetak" muncul **sebelum** satu pun `peek` tercetak. Jejak sesudah `limit(2).toList()`:

```
sumber 1, filter 1                      <- 1 ganjil: dibuang
sumber 2, filter 2, map 2               <- lolos, hasil pertama (20)
sumber 3, filter 3                      
sumber 4, filter 4, map 4               <- lolos, hasil kedua (40) -> limit(2) terpenuhi, BERHENTI
```

Dua hal yang terlihat dari jejak itu:

1. **Elemen diproses satu per satu melalui seluruh pipeline** (sumber → filter → map, lalu elemen berikutnya), bukan tahap demi tahap untuk seluruh data. Karena itu tidak ada list perantara di antara tahap.
2. **Short-circuit:** elemen 5 sampai 10 **tidak pernah disentuh**. Kemalasan memungkinkan hal ini; bila `filter` memproses seluruh list lebih dulu (seperti loop yang kita tulis manual), pekerjaan sia-sia itu terjadi. Ini juga yang membuat `Stream.iterate(...)` yang tak terbatas aman dipakai bersama `limit`.

### Mengapa stream tidak boleh dipakai ulang

Stream adalah **pipeline sekali jalan di atas sumbernya**, bukan wadah data. Setelah operasi terminal, ia dianggap *dikonsumsi*; memakainya lagi menghasilkan (terbukti di program):

```
IllegalStateException: stream has already been operated upon or closed
```

Alasan desainnya: stream dapat berasal dari sumber yang **tidak bisa dibaca dua kali** (file, soket, generator), sehingga menjamin pemakaian sekali adalah yang paling konsisten. **Solusi:** buat stream **baru** dari sumber yang sama (`sumber.stream()` lagi) atau simpan `Supplier<Stream<T>>`. Stream tidak menyimpan hasil; yang disimpan adalah hasil `collect`/`toList`.

### Kapan `parallelStream()` justru merugikan

**Yang tetap benar** pada stream paralel (deterministik): `toList()` mempertahankan urutan sumber, dan `sum()` pada data yang sama memberi hasil yang sama. Yang berbahaya adalah ketika pemakaiannya salah. Dua demo di bagian `[BERVARIASI]` (hasilnya berbeda tiap mesin dan tiap eksekusi):

| Demo | Hasil pada mesin saya (2 CPU) |
|---|---|
| `parallel().forEach(list::add)` pada `ArrayList`, 50 percobaan × 100.000 elemen | Ukuran **salah** pada sekitar 32–33 dari 50 percobaan, melempar exception 1–4 kali, benar hanya sekitar 13–16 kali (tiga eksekusi) |
| `reduce(0, (a, b) -> a - b)` | sequential `-5050`, **parallel `0`**: berbeda |

Catatan jujur: pada percobaan **tunggal** pertama saya, `forEach(list::add)` kebetulan memberi ukuran yang benar. Baru dengan 50 percobaan kesalahannya terlihat jelas. Itulah sebabnya bug *race condition* berbahaya: bisa lolos uji dan baru gagal di produksi.

Daftar situasi di mana `parallelStream()` merugikan atau salah:

1. **State bersama yang tidak thread-safe** (`list::add`, counter, `HashMap`): hasil salah atau exception. Gunakan `collect(...)`, bukan menambah ke koleksi luar.
2. **Operasi tidak asosiatif** (pengurangan, pembagian): pembagian kerja ke thread mengubah urutan penggabungan, jadi hasil berbeda dari sequential. `reduce` paralel mensyaratkan fungsi asosiatif dan identitas yang benar.
3. **Data kecil:** biaya membagi pekerjaan, menyebar ke thread, dan menggabungkan hasil lebih besar daripada kerja yang dihemat.
4. **Operasi I/O atau pemblokiran:** semua stream paralel memakai **satu `ForkJoinPool.commonPool()` bersama** (di mesin saya paralelismenya hanya 1). Satu tugas yang memblokir menahan thread yang dipakai seluruh aplikasi, termasuk stream paralel lain.
5. **Sumber yang sulit dibagi** (`LinkedList`, `Stream.iterate`) dan **operasi yang bergantung urutan** (`limit`, `findFirst`, `sorted`): paralelisme menjadi mahal atau tak berguna.
6. **Kode yang bergantung pada urutan efek samping** (`forEach` mencetak): urutan tidak terjamin (`forEachOrdered` memulihkannya dengan mengorbankan paralelisme).

**Aturan praktis** dari modul: `parallelStream()` hanya untuk data **besar**, operasi **CPU-intensif**, dan **bebas side effect**, dan **ukur** dulu dengan benchmark yang benar (misalnya JMH, bukan perbandingan waktu satu kali). Bila ragu, sequential adalah pilihan yang benar secara default.
