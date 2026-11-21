# Modul 9: Functional Programming: Lambda & Stream API

[← Modul 8: Exception Handling & I/O](08-exception-io.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 10: Concurrency: Thread, Virtual Thread & Java Memory Model →](10-concurrency.md)

**Isi modul ini**

- [Teori](#teori)
  - [9.1 Apa itu Functional Programming?](#91-apa-itu-functional-programming)
  - [9.2 Lambda Expressions](#92-lambda-expressions)
  - [9.3 Functional Interfaces](#93-functional-interfaces)
  - [9.4 Higher-Order Functions](#94-higher-order-functions)
  - [9.5 Functional Composition](#95-functional-composition)
  - [9.6 Stream API](#96-stream-api)
  - [9.7 `Optional` Bergaya Fungsional](#97-optional-bergaya-fungsional)
  - [9.8 Stream Gatherers (Java 24+)](#98-stream-gatherers-java-24)
  - [9.9 Kapan (Tidak) Memakai Stream](#99-kapan-tidak-memakai-stream)
- [Praktik](#praktik)
  - [Lambda, Method Reference, HOF, dan Komposisi](#lambda-method-reference-hof-dan-komposisi)
  - [Stream API: Analisis Data Transaksi](#stream-api-analisis-data-transaksi)
- [Latihan](#latihan)

---


## Teori

### 9.1 Apa itu Functional Programming?

**Functional programming (FP)** adalah gaya menulis program yang memperlakukan **fungsi sebagai nilai** — bisa disimpan di variabel, dikirim sebagai argumen, dan dikembalikan dari method. Java tetap bahasa berorientasi objek, tetapi sejak **Java 8** mengadopsi fitur FP secara luas. Prinsip kuncinya:

- **Pure function** — hasil hanya bergantung pada argumen dan tidak mengubah apa pun di luar dirinya (*tanpa side effect*). Mudah diuji dan aman dipakai paralel.
- **Immutability** — data tidak diubah; operasi menghasilkan data baru ([Modul 6](06-fitur-modern.md): `record`, [Modul 7](07-generics-collections.md): `List.of`).
- **First-class functions** — fungsi bisa dioper seperti object biasa.
- **Deklaratif** — menyatakan *apa* yang diinginkan ("ambil yang genap, kuadratkan, jumlahkan") alih-alih *bagaimana* (loop dan variabel penampung).

### 9.2 Lambda Expressions

**Lambda** adalah fungsi anonim ringkas. Sintaksnya: `(parameter) -> ekspresi` atau `(parameter) -> { blok; }`.

```java
Runnable r = () -> System.out.println("Halo");
Comparator<String> byPanjang = (a, b) -> Integer.compare(a.length(), b.length());
Function<Integer, Integer> kuadrat = x -> x * x;          // satu parameter: kurung boleh dihilangkan
BinaryOperator<Integer> tambah = (a, b) -> {              // badan blok: butuh return eksplisit
    int hasil = a + b;
    return hasil;
};
```

Lambda hanya dapat dipakai **di tempat yang mengharapkan functional interface** (9.3) — compiler menyimpulkan tipe parameter dari konteks.

**Variabel yang ditangkap (*capture*)**: lambda boleh memakai variabel lokal dari luar, tetapi variabel itu harus **final atau *effectively final*** (nilainya tidak pernah diubah setelah diisi). Ini sengaja, agar tidak terjadi masalah ketika lambda dijalankan di thread lain.

```java
int pengali = 3;
Function<Integer, Integer> kali = x -> x * pengali;   // OK: pengali tidak berubah
// pengali = 4;                                        // jika baris ini ada → error kompilasi di lambda
```

**Method reference (`::`)** adalah singkatan lambda yang hanya memanggil satu method. Ada empat bentuk:

| Bentuk | Contoh | Setara lambda |
|---|---|---|
| Method **static** | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Method instance dari **object tertentu** | `System.out::println` | `x -> System.out.println(x)` |
| Method instance dari **tipe** (parameter pertama jadi receiver) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| **Constructor** | `ArrayList::new` | `() -> new ArrayList<>()` |

### 9.3 Functional Interfaces

**Functional interface** adalah interface dengan **tepat satu method abstract** (boleh punya `default`/`static`). Lambda adalah implementasi ringkas untuk method abstract tersebut. Anotasi **`@FunctionalInterface`** opsional, tetapi membuat compiler menjaga syarat itu.

Package **`java.util.function`** menyediakan interface siap pakai:

| Interface | Method | Arti | Contoh |
|---|---|---|---|
| `Function<T,R>` | `R apply(T)` | Ubah `T` menjadi `R` | `s -> s.length()` |
| `BiFunction<T,U,R>` | `R apply(T,U)` | Dua input, satu output | `(a, b) -> a + b` |
| `Supplier<T>` | `T get()` | Hasilkan nilai tanpa input | `() -> new ArrayList<>()` |
| `Consumer<T>` | `void accept(T)` | Konsumsi nilai (side effect) | `x -> System.out.println(x)` |
| `Predicate<T>` | `boolean test(T)` | Uji kondisi | `s -> s.isEmpty()` |
| `UnaryOperator<T>` | `T apply(T)` | `Function<T,T>` | `s -> s.trim()` |
| `BinaryOperator<T>` | `T apply(T,T)` | `BiFunction<T,T,T>` | `Integer::sum` |

Ada juga varian **primitif** untuk menghindari *boxing*: `IntFunction`, `IntPredicate`, `ToIntFunction`, `IntBinaryOperator`, `IntSupplier`, dan seterusnya. Anda juga dapat membuat functional interface sendiri:

```java
@FunctionalInterface
interface Validator<T> {
    boolean valid(T nilai);
    default Validator<T> dan(Validator<T> lain) {      // default method boleh ada
        return x -> valid(x) && lain.valid(x);
    }
}
```

### 9.4 Higher-Order Functions

**Higher-order function** adalah fungsi yang **menerima fungsi sebagai parameter** dan/atau **mengembalikan fungsi**. Inilah yang membuat kode generik dan dapat dipakai ulang — perilaku variabel disuntikkan dari luar.

```java
static <T> List<T> saring(List<T> data, Predicate<T> syarat) {     // menerima fungsi
    List<T> hasil = new ArrayList<>();
    for (T x : data) if (syarat.test(x)) hasil.add(x);
    return hasil;
}

static Function<Integer, Integer> pengali(int n) {                 // mengembalikan fungsi
    return x -> x * n;
}
```

`List.sort(Comparator)`, `removeIf(Predicate)`, `Map.computeIfAbsent(key, Function)`, dan seluruh Stream API adalah higher-order function.

### 9.5 Functional Composition

**Composition** menyambung fungsi-fungsi kecil menjadi fungsi yang lebih besar:

| Method | Arti |
|---|---|
| `f.andThen(g)` | Jalankan `f`, lalu `g` pada hasilnya: `g(f(x))` |
| `f.compose(g)` | Jalankan `g` dulu, lalu `f`: `f(g(x))` |
| `Function.identity()` | Fungsi `x -> x` |
| `p.and(q)`, `p.or(q)`, `p.negate()` | Gabung/balik `Predicate` |
| `Comparator.comparing(...).thenComparing(...)` | Gabung kriteria urutan |

```java
Function<String, String> bersih = String::trim;
Function<String, String> kapital = s -> s.substring(0, 1).toUpperCase() + s.substring(1);
Function<String, String> pipeline = bersih.andThen(String::toLowerCase).andThen(kapital);
System.out.println(pipeline.apply("  jAVA  "));       // Java

Predicate<Integer> positif = x -> x > 0;
Predicate<Integer> genap = x -> x % 2 == 0;
Predicate<Integer> positifGanjil = positif.and(genap.negate());
```

### 9.6 Stream API

**Stream** (`java.util.stream`) adalah urutan elemen yang diproses secara **deklaratif** lewat sebuah **pipeline**. Stream **bukan struktur data** — ia tidak menyimpan elemen dan **tidak mengubah sumbernya**.

```
sumber  ──►  operasi intermediate (0..n)  ──►  operasi terminal (1)
list.stream()   filter, map, sorted, ...        collect, forEach, reduce, count, ...
```

Tiga sifat penting:

1. **Lazy** — operasi *intermediate* tidak berjalan sampai ada operasi *terminal*.
2. **Sekali pakai** — stream tidak boleh dipakai ulang setelah operasi terminal (`IllegalStateException`).
3. **Tidak mengubah sumber** — hasil ditampung ke koleksi/nilai baru.

**Operasi intermediate umum:** `filter`, `map`, `flatMap` (ratakan stream bersarang), `distinct`, `sorted`, `limit`, `skip`, `peek`, `takeWhile`, `dropWhile`.

**Operasi terminal umum:** `toList()` (Java 16+), `collect(...)`, `forEach`, `reduce`, `count`, `min`/`max`, `findFirst`/`findAny` (mengembalikan `Optional`), `anyMatch`/`allMatch`/`noneMatch`.

**`Collectors`** adalah kotak perkakas untuk `collect`:

| Collector | Hasil |
|---|---|
| `toList()`, `toSet()` | Koleksi biasa |
| `toMap(keyFn, valFn, merge)` | `Map` (sertakan fungsi *merge* jika kunci bisa duplikat) |
| `groupingBy(fn)` / `groupingBy(fn, downstream)` | `Map<K, List<T>>` atau diagregasi per kelompok |
| `partitioningBy(predicate)` | `Map<Boolean, List<T>>` — dua kelompok |
| `counting()`, `summingInt/Long`, `averagingInt/Double` | Agregasi angka |
| `mapping(fn, downstream)` | Transformasi sebelum diagregasi |
| `joining(sep, prefix, suffix)` | Gabungan `String` |

**Stream primitif** (`IntStream`, `LongStream`, `DoubleStream`) menghindari boxing dan menyediakan `sum()`, `average()`, `range(a, b)`, `rangeClosed(a, b)`, `summaryStatistics()`; konversi dengan `mapToInt`, `boxed()`.

**Parallel stream** (`parallelStream()`) memecah pekerjaan ke banyak thread via *ForkJoinPool* bersama. Hati-hati: **tidak otomatis lebih cepat** — hanya menguntungkan untuk data besar dan operasi CPU-intensif yang bebas side effect. Untuk operasi I/O atau data kecil, justru memperlambat.

### 9.7 `Optional` Bergaya Fungsional

`Optional` ([Modul 7](07-generics-collections.md)) memiliki `map`, `flatMap`, `filter`, dan `or` sehingga bisa dirangkai tanpa `if (x != null)` bertingkat:

```java
String kota = cariPelanggan(id)                    // Optional<Pelanggan>
        .flatMap(Pelanggan::alamat)                // Optional<Alamat>
        .map(Alamat::kota)                         // Optional<String>
        .filter(k -> !k.isBlank())
        .orElse("tidak diketahui");
```

### 9.8 Stream Gatherers (Java 24+)

Operasi intermediate bawaan terbatas. **Stream Gatherers** (final di **Java 24**) memungkinkan operasi intermediate **kustom** dan menyediakan beberapa bawaan di `java.util.stream.Gatherers`, misalnya *windowing*:

```java
// Java 24+
import java.util.stream.Gatherers;

Stream.of(1, 2, 3, 4, 5, 6, 7)
      .gather(Gatherers.windowFixed(3))        // [[1,2,3], [4,5,6], [7]]
      .toList();

Stream.of(1, 2, 3, 4)
      .gather(Gatherers.windowSliding(2))      // [[1,2], [2,3], [3,4]]
      .toList();
```

Cocok untuk kasus seperti rata-rata bergerak atau pengelompokan per jendela yang sebelumnya sulit dengan `Collectors`.

### 9.9 Kapan (Tidak) Memakai Stream

- **Pakai** untuk transformasi data berurutan (filter → map → agregasi), pengelompokan, dan pipeline pendek yang mudah dibaca.
- **Hindari** jika logikanya butuh banyak variabel keadaan, `break`/`continue`, atau exception checked — loop biasa lebih jelas.
- **Hindari side effect** di dalam `map`/`filter` (misal menambah ke list luar); gunakan `collect`.
- Pipeline yang terlalu panjang sulit di-debug — pecah menjadi method dengan nama bermakna.
- `peek` untuk **debugging** saja, bukan untuk logika.

## Praktik

### Lambda, Method Reference, HOF, dan Komposisi

```java
// DemoFungsional.java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DemoFungsional {
    public static void main(String[] args) {
        // --- Functional interface bawaan ---
        Function<String, Integer> panjang = String::length;
        BiFunction<Integer, Integer, Integer> kali = (a, b) -> a * b;
        Supplier<List<String>> pabrikList = ArrayList::new;
        Consumer<String> cetak = System.out::println;
        UnaryOperator<String> besar = String::toUpperCase;
        BinaryOperator<Integer> jumlah = Integer::sum;

        cetak.accept("panjang 'lambda' = " + panjang.apply("lambda"));
        cetak.accept("6 x 7 = " + kali.apply(6, 7));
        cetak.accept("list baru kosong? " + pabrikList.get().isEmpty());
        cetak.accept(besar.apply("method reference"));
        cetak.accept("3 + 4 = " + jumlah.apply(3, 4));

        // --- Higher-order function: menerima fungsi ---
        List<Integer> angka = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        System.out.println("genap  : " + saring(angka, x -> x % 2 == 0));
        System.out.println(">5     : " + saring(angka, x -> x > 5));

        // --- Higher-order function: mengembalikan fungsi ---
        Function<Integer, Integer> kali3 = pengali(3);
        Function<Integer, Integer> kali10 = pengali(10);
        System.out.println("kali3(7) = " + kali3.apply(7) + ", kali10(7) = " + kali10.apply(7));

        // --- Komposisi Function ---
        Function<String, String> kapital = s -> s.substring(0, 1).toUpperCase() + s.substring(1);
        Function<String, String> pipeline = ((Function<String, String>) String::trim)
                .andThen(String::toLowerCase)
                .andThen(kapital);
        System.out.println("pipeline('  jAVA  ') = " + pipeline.apply("  jAVA  "));

        Function<Integer, Integer> tambah2 = x -> x + 2;
        Function<Integer, Integer> kali5 = x -> x * 5;
        System.out.println("andThen: " + tambah2.andThen(kali5).apply(1));    // (1+2)*5 = 15
        System.out.println("compose: " + tambah2.compose(kali5).apply(1));    // (1*5)+2 = 7

        // --- Komposisi Predicate ---
        Predicate<Integer> positif = x -> x > 0;
        Predicate<Integer> genap = x -> x % 2 == 0;
        System.out.println("positif && ganjil: " + saring(List.of(-3, -2, 1, 2, 3), positif.and(genap.negate())));

        // --- Validator buatan sendiri ---
        Validator<String> tidakKosong = s -> !s.isBlank();
        Validator<String> panjangMin = s -> s.length() >= 8;
        Validator<String> password = tidakKosong.dan(panjangMin);
        System.out.println("'rahasia123' valid? " + password.valid("rahasia123"));
        System.out.println("'abc' valid?        " + password.valid("abc"));

        // --- Comparator sebagai komposisi ---
        List<String> kata = new ArrayList<>(List.of("kiwi", "apel", "mangga", "pir", "jeruk"));
        kata.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("urut panjang lalu abjad: " + kata);

        // --- Capture effectively final ---
        int pengaliLuar = 4;
        Function<Integer, Integer> tangkap = x -> x * pengaliLuar;
        System.out.println("capture: " + tangkap.apply(5));
    }

    static <T> List<T> saring(List<T> data, Predicate<T> syarat) {
        List<T> hasil = new ArrayList<>();
        for (T x : data) {
            if (syarat.test(x)) hasil.add(x);
        }
        return hasil;
    }

    static Function<Integer, Integer> pengali(int n) {
        return x -> x * n;
    }
}

@FunctionalInterface
interface Validator<T> {
    boolean valid(T nilai);

    default Validator<T> dan(Validator<T> lain) {
        return x -> valid(x) && lain.valid(x);
    }
}
```

### Stream API: Analisis Data Transaksi

```java
// DemoStream.java
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DemoStream {
    record Transaksi(String id, String kategori, String kota, long jumlah) { }

    public static void main(String[] args) {
        List<Transaksi> data = List.of(
            new Transaksi("T1", "makanan",  "Jakarta",  50_000),
            new Transaksi("T2", "transport", "Bandung", 20_000),
            new Transaksi("T3", "makanan",  "Bandung",  75_000),
            new Transaksi("T4", "hiburan",  "Jakarta", 120_000),
            new Transaksi("T5", "makanan",  "Jakarta",  30_000),
            new Transaksi("T6", "transport", "Jakarta", 15_000));

        // filter + map + terminal
        List<String> besar = data.stream()
                .filter(t -> t.jumlah() > 40_000)
                .map(Transaksi::id)
                .toList();
        System.out.println("ID > 40rb: " + besar);

        // reduce / sum
        long total = data.stream().mapToLong(Transaksi::jumlah).sum();
        System.out.println("Total: " + total);

        // groupingBy + downstream collector (TreeMap agar urutan stabil)
        Map<String, Long> perKategori = data.stream().collect(
                Collectors.groupingBy(Transaksi::kategori, TreeMap::new,
                        Collectors.summingLong(Transaksi::jumlah)));
        System.out.println("Per kategori: " + perKategori);

        Map<String, Double> rataKota = data.stream().collect(
                Collectors.groupingBy(Transaksi::kota, TreeMap::new,
                        Collectors.averagingLong(Transaksi::jumlah)));
        System.out.println("Rata-rata per kota: " + rataKota);

        // partitioningBy
        Map<Boolean, List<String>> terbagi = data.stream().collect(
                Collectors.partitioningBy(t -> t.jumlah() >= 50_000,
                        Collectors.mapping(Transaksi::id, Collectors.toList())));
        System.out.println(">= 50rb: " + terbagi.get(true) + ", lainnya: " + terbagi.get(false));

        // sorted + limit : 3 transaksi terbesar
        System.out.println("Top 3: " + data.stream()
                .sorted((a, b) -> Long.compare(b.jumlah(), a.jumlah()))
                .limit(3)
                .map(t -> t.id() + "=" + t.jumlah())
                .collect(Collectors.joining(", ", "[", "]")));

        // distinct + sorted pada kota
        System.out.println("Kota: " + data.stream().map(Transaksi::kota).distinct().sorted().toList());

        // max menghasilkan Optional
        data.stream().max((a, b) -> Long.compare(a.jumlah(), b.jumlah()))
            .ifPresent(t -> System.out.println("Terbesar: " + t));

        // anyMatch / allMatch
        System.out.println("Ada hiburan? " + data.stream().anyMatch(t -> t.kategori().equals("hiburan")));
        System.out.println("Semua > 10rb? " + data.stream().allMatch(t -> t.jumlah() > 10_000));

        // toMap dengan merge function (key duplikat)
        Map<String, Long> totalKota = data.stream().collect(
                Collectors.toMap(Transaksi::kota, Transaksi::jumlah, Long::sum, TreeMap::new));
        System.out.println("toMap: " + totalKota);

        // flatMap: ratakan list bersarang
        List<List<Integer>> bersarang = List.of(List.of(1, 2), List.of(3), List.of(4, 5, 6));
        System.out.println("flatMap: " + bersarang.stream().flatMap(List::stream).toList());

        // Stream primitif
        System.out.println("Jumlah 1..100 = " + IntStream.rangeClosed(1, 100).sum());
        System.out.println("Statistik: " + IntStream.of(4, 8, 15, 16, 23, 42).summaryStatistics());
        System.out.println("Kuadrat genap: " + IntStream.rangeClosed(1, 10)
                .filter(x -> x % 2 == 0).map(x -> x * x).boxed().toList());

        // Stream tak terbatas + limit, dan takeWhile
        System.out.println("Pangkat 2: " + Stream.iterate(1, x -> x * 2).limit(8).toList());
        System.out.println("takeWhile: " + Stream.of(1, 2, 3, 10, 4).takeWhile(x -> x < 5).toList());

        // Laziness: tidak ada yang tercetak sampai operasi terminal
        Stream<Integer> lazy = Stream.of(1, 2, 3).peek(x -> System.out.println("  diproses " + x));
        System.out.println("Stream dibuat, belum jalan...");
        lazy.map(x -> x * 10).toList();
        System.out.println("Setelah terminal.");

        // Stream sekali pakai
        Stream<String> sekali = Stream.of("a", "b");
        sekali.toList();
        try {
            sekali.toList();
        } catch (IllegalStateException e) {
            System.out.println("Stream tidak bisa dipakai ulang: " + e.getMessage());
        }
    }
}
```

Perhatikan bagian **laziness**: baris `"Stream dibuat, belum jalan..."` muncul **sebelum** satu pun `peek` tercetak, karena pipeline baru berjalan saat operasi terminal `toList()` dipanggil.

## Latihan

1. Tulis lambda dan method reference untuk empat jenis ([9.2](#92-lambda-expressions)): satu untuk method static, satu untuk method instance dari object tertentu, satu untuk method instance dari tipe, dan satu untuk constructor. Jelaskan lambda setara untuk masing-masing.
2. Buat higher-order function `static <T, R> List<R> petakan(List<T> data, Function<T, R> fn)` dan `static <T> T cariPertama(List<T> data, Predicate<T> syarat, T cadangan)`. Panggil dengan lambda yang berbeda-beda untuk menunjukkan fleksibilitasnya.
3. Gunakan `Function.andThen`/`compose` untuk membangun pipeline normalisasi nama: `trim` → `lowercase` → huruf pertama kapital tiap kata. Lalu gabungkan tiga `Predicate` (tidak kosong, tanpa angka, panjang ≥ 3) menjadi satu validator dengan `and`.
4. Dari `record Pegawai(String nama, String divisi, double gaji)` dan 8 data contoh, gunakan stream untuk: (a) gaji rata-rata per divisi, (b) pegawai bergaji tertinggi di tiap divisi, (c) nama semua pegawai bergaji di atas rata-rata keseluruhan dalam satu `String` yang dipisah koma.
5. Ubah program FrekuensiKata di [Modul 7](07-generics-collections.md) menjadi pipeline stream memakai `Collectors.groupingBy` + `counting`. Setelah itu jelaskan mengapa stream bersifat *lazy*, mengapa tidak boleh dipakai ulang, dan kapan `parallelStream()` justru merugikan.

---

[← Modul 8: Exception Handling & I/O](08-exception-io.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 10: Concurrency: Thread, Virtual Thread & Java Memory Model →](10-concurrency.md)
