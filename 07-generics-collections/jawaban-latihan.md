# Jawaban Latihan — Modul 7: Generics & Collections Framework

> **Cara membaca dokumen ini.** Semua program di bawah benar-benar dijalankan (OpenJDK 21); keluaran lengkapnya ada di folder `latihan-N/`. Seluruh keluaran deterministik. Pengecualian kecil: baris `HashSet` di Latihan 1 sengaja ditampilkan sebagai contoh urutan yang **tidak dijamin** oleh spesifikasi, sehingga JDK lain boleh saja menghasilkan urutan berbeda. Untuk Latihan 5, tulis prediksi Anda dulu sebelum membaca hasilnya.

---

## Latihan 1 — Daftar unik: urutan masuk dan terurut

Kode: `latihan-1/Unik.java`. Masukan: `latihan-1/masukan.txt`. Keluaran: `latihan-1/hasil.txt`.

Masukan: `50 12 7 12 99 7 3 50 1000 3 12`

| Kebutuhan | `Set` yang dipilih | Hasil |
|---|---|---|
| (a) Tanpa duplikat, **urutan masuk dipertahankan** | `LinkedHashSet` | `[50, 12, 7, 99, 3, 1000]` |
| (b) Tanpa duplikat, **terurut naik** | `TreeSet` | `[3, 7, 12, 50, 99, 1000]` |
| (pembanding) Tanpa duplikat saja | `HashSet` | `[50, 99, 3, 7, 1000, 12]` (urutan tidak dijamin) |

```java
Set<Integer> unikUrutMasuk = new LinkedHashSet<>(masukan);   // (a)
Set<Integer> unikTerurut   = new TreeSet<>(masukan);         // (b)
```

### Alasan pemilihan

| | `LinkedHashSet` | `TreeSet` | `HashSet` |
|---|---|---|---|
| Struktur | Hash table + linked list penghubung | Pohon merah-hitam | Hash table |
| Urutan iterasi | **Urutan penyisipan pertama** | **Terurut** (alami atau `Comparator`) | Tidak dijamin |
| `add`/`contains` | O(1) | O(log n) | O(1) |
| Kapan dipilih | Butuh unik **dan** urutan asli | Butuh unik **dan** selalu terurut, atau operasi rentang (`first`, `ceiling`, `subSet`) | Hanya butuh unik/cek keanggotaan, urutan tak penting |

- **(a)** `HashSet` sudah cukup menghapus duplikat tetapi mengacak urutan (perhatikan `50, 99, 3, ...` di atas). Penambahan "linked" pada `LinkedHashSet` hanya memerlukan sedikit memori ekstra untuk mengingat urutan masuk.
- **(b)** `TreeSet` mengurutkan **saat menyisipkan**, jadi tidak perlu langkah sort terpisah. Harganya O(log n) per operasi. Jika Anda hanya perlu mengurutkan sekali di akhir dan tidak butuh set tetap terurut, alternatifnya `new ArrayList<>(new HashSet<>(x))` lalu `Collections.sort`. Untuk soal ini `TreeSet` lebih ringkas.
- **Elemen pertama yang menang.** `LinkedHashSet` mempertahankan posisi kemunculan **pertama**; menambahkan elemen yang sudah ada tidak memindahkannya. Karena itu `12` tetap di posisi kedua walau muncul lagi.

### Bonus: mencatat yang duplikat

`Set.add()` mengembalikan `false` bila elemen sudah ada, sehingga menemukan duplikat cukup satu putaran:

```java
for (int x : masukan) if (!lihat.add(x)) duplikat.add(x);
```
Hasil `[12, 7, 50, 3, 12]`: kelima pengulangan yang dibuang (angka `12` muncul tiga kali, jadi dua pengulangan).

---

## Latihan 2 — `Pasangan<A, B>` dan `min`

Kode: `latihan-2/DemoPasangan.java`. Keluaran: `latihan-2/hasil.txt`. Error: `latihan-2/error-kompilasi.txt`.

```java
class Pasangan<A, B> {
    private final A pertama;
    private final B kedua;
    Pasangan<B, A> tukar() { return new Pasangan<>(kedua, pertama); }   // tipe parameter ditukar
    ...
}

static <T extends Comparable<T>> T min(T a, T b) {
    return a.compareTo(b) <= 0 ? a : b;
}
```

```
p         = (umur, 25)                      <- Pasangan<String, Integer>
p.tukar() = (25, umur)                      <- Pasangan<Integer, String>
min(7, 3)           = 3
min("kiwi", "apel") = apel
min(2.5, 2.4)       = 2.4
```

### Poin penting

- **Tipe hasil `tukar()` ikut berubah**, dan compiler mengetahuinya: `int angka = t.pertama();` tidak memerlukan cast. Tanpa generics, Anda harus menyimpan `Object` dan melakukan cast sendiri, dengan risiko `ClassCastException` saat runtime.
- **`<T extends Comparable<T>>` menjamin `compareTo` ada.** Tanpa batas itu, `a.compareTo(b)` tidak akan terkompilasi karena `T` hanya dianggap `Object`. Integer, String, dan Double lolos karena ketiganya `Comparable` terhadap dirinya sendiri.
- **Batas ini ditegakkan saat kompilasi.** Dua pemanggilan di `contoh-error/MinSalah.java` ditolak:

  ```
  min(new Object(), new Object())
    reason: inference variable T has incompatible bounds
      upper bounds: Comparable<T>
      lower bounds: Object

  min(1, "satu")
    equality constraints: Integer,String   <- Integer dan String tidak bisa disatukan jadi satu T
  ```

  Pesan error generics memang terkenal panjang; kunci membacanya: cari baris `upper bounds` (apa yang disyaratkan) dan `lower bounds` (apa yang Anda berikan).
- **`<= 0` pada `min`:** jika sama, yang dikembalikan `a` (argumen pertama). Itu perilaku yang konsisten ("stabil") dan perlu diputuskan secara sadar.
- **Catatan lanjutan:** versi di perpustakaan standar memakai `Comparable<? super T>` (wildcard dengan `super`, prinsip PECS dari modul) agar tipe yang mewarisi `Comparable` dari induknya tetap diterima. Untuk latihan ini `Comparable<T>` sudah memadai.
- Dan `Pasangan` memiliki `equals`/`hashCode`/`toString` sendiri. Di Java 21, `record Pasangan<A, B>(A pertama, B kedua)` memberi semuanya otomatis, tetapi saya memakai class agar tipe generic terlihat eksplisit sesuai materi.

---

## Latihan 3 — Kurung seimbang dengan `ArrayDeque`

Kode: `latihan-3/Kurung.java`. Keluaran: `latihan-3/hasil.txt`.

### Algoritma

```java
for (char c : teks.toCharArray()) {
    if (c == '(' || c == '[' || c == '{') {
        stack.push(c);                                         // buka: simpan
    } else if (pasangan.containsKey(c)) {                      // tutup: ), ], }
        if (stack.isEmpty() || stack.pop() != pasangan.get(c)) return false;
    }
}
return stack.isEmpty();                                        // masih ada yang terbuka = gagal
```

Gagasannya: kurung yang **terakhir dibuka harus yang pertama ditutup**, persis sifat LIFO sebuah stack. Setiap kurung tutup dicocokkan dengan puncak stack.

| Masukan | Hasil | Mengapa |
|---|---|---|
| `{[()]}` | SEIMBANG | Semua tertutup berurutan |
| `{[(])}` | tidak seimbang | `]` datang saat puncak stack `(` |
| `((` | tidak seimbang | Stack tidak kosong di akhir |
| `))` | tidak seimbang | Kurung tutup datang saat stack kosong |
| `""` | SEIMBANG | Tidak ada kurung, tidak ada yang dilanggar |
| `a(b)[c]{d}` | SEIMBANG | Karakter lain diabaikan |
| `([)]` | tidak seimbang | Bersilang |

Dua kasus tepi yang mudah terlewat: **(1)** kurung tutup di stack kosong (`))`): tanpa `stack.isEmpty() ||`, `pop()` pada stack kosong akan melempar `NoSuchElementException`; **(2)** kurung buka tersisa (`((`): tanpa pemeriksaan akhir `stack.isEmpty()`, kasus ini lolos sebagai "seimbang". Waktu O(n), memori O(n) pada kasus terburuk.

### Mengapa `ArrayDeque` lebih disarankan daripada `Stack`

Saya memperlihatkan perbedaannya dalam kode (bagian bawah `hasil.txt`):

| Aspek | `Stack` (legacy) | `ArrayDeque` |
|---|---|---|
| Warisan | Turunan `Vector` (tersinkronisasi, tiap operasi menanggung biaya lock) | Struktur modern tanpa sinkronisasi |
| Mengekspos operasi non-stack | **Ya**: `add(0, 99)` menyisip di dasar, `get(1)` mengakses acak. Keduanya lolos kompilasi dan **merusak sifat LIFO** | Tidak ada akses acak; hanya operasi di ujung |
| Urutan iterasi setelah `push(1,2,3)` | `[1, 2, 3]` (dari dasar ke puncak) | `[3, 2, 1]` (dari puncak ke dasar) |
| `pop()` saat kosong | `EmptyStackException` | `NoSuchElementException` (`poll()`/`peek()` mengembalikan `null`) |
| Elemen `null` | Boleh | **Ditolak** (`NullPointerException`) |
| Status | Dokumentasi resmi menyarankan `Deque` sebagai gantinya | Pilihan standar untuk stack **dan** queue |

Perhatikan baris urutan iterasi: **mengganti `Stack` dengan `ArrayDeque` mengubah hasil `println(stack)`**. Jika ada kode yang bergantung pada urutan cetak atau iterasi, migrasinya tidak sepenuhnya transparan. Poin lain: karena `ArrayDeque` menolak `null`, `poll()` yang mengembalikan `null` tidak ambigu (tidak mungkin berarti "elemennya memang null").

---

## Latihan 4 — Mengelompokkan `Produk` tanpa stream

Kode: `latihan-4/GrupProduk.java`. Keluaran: `latihan-4/hasil.txt`.

```java
Map<String, List<Produk>> perKategori = new TreeMap<>();
for (Produk p : semua) {
    perKategori.computeIfAbsent(p.kategori(), k -> new ArrayList<>()).add(p);
}

Comparator<Produk> hargaMenurun = Comparator.comparingInt(Produk::harga).reversed();
for (List<Produk> grup : perKategori.values()) grup.sort(hargaMenurun);
```

```
Kebersihan (2 produk)       Makanan (3 produk)            Minuman (3 produk)
  Sampo 170ml     24.000      Roti Tawar        18.500      Kopi Arabika 250g  85.000
  Sabun Mandi      7.500      Keripik Singkong  15.000      Teh Hijau          32.000
                              Mie Instan         3.500      Air Mineral 1,5L    6.000
```

(Tata letak di atas hanya diringkas berdampingan; keluaran program menampilkannya berurutan, satu kategori per blok.)

### Mengapa `computeIfAbsent`

`computeIfAbsent(kunci, fungsi)` berarti: *jika kunci belum ada, panggil fungsi untuk membuat nilainya, simpan, lalu kembalikan nilai itu; jika sudah ada, kembalikan yang ada*. Karena selalu mengembalikan list-nya, penambahan elemen bisa langsung dirangkai (`.add(p)`). Dibandingkan pola lama (ada di akhir kode program, `cara2`):

```java
List<Produk> grup = peta.get(kategori);
if (grup == null) { grup = new ArrayList<>(); peta.put(kategori, grup); }
grup.add(p);
```

Keduanya menghasilkan kunci yang sama (terbukti di dua baris terakhir keluaran), tetapi versi `computeIfAbsent` satu baris dan tidak bisa lupa mem-`put` list baru.

### Detail yang menentukan kebenaran

- **List di dalam map harus mutable.** `new ArrayList<>()` dipakai karena kita memanggil `.add` dan `.sort`. Jika diganti `List.of()`, `add` akan melempar `UnsupportedOperationException`.
- **`TreeMap`** dipilih agar kategori tampil terurut A–Z. Dengan `HashMap`, urutan kategori tidak dijamin.
- **Pengurutan dilakukan setelah pengelompokan**, per grup, dengan `reversed()` pada comparator. Alternatif: urutkan seluruh daftar menurut harga menurun lebih dulu, baru kelompokkan; karena penyisipan mempertahankan urutan, tiap grup otomatis terurut. Keduanya benar; yang pertama lebih eksplisit.
- **Verifikasi:** total semua grup = 8 sama dengan jumlah produk awal, jadi tidak ada produk hilang atau ganda.
- `record Produk` mendapat `equals`/`hashCode`/`toString`/accessor secara otomatis (Modul 6), sehingga `p.harga()` dipakai tanpa getter. Mendeklarasikannya di dalam class (nested) valid karena record otomatis `static`.

---

## Latihan 5 — Cache LRU kapasitas 2, dan `Optional` sebagai field

Kode: `latihan-5/DemoLru.java` dan `OptionalField.java`. Keluaran: `hasil-lru.txt`, `hasil-optional.txt`.

### Prediksi dan hasil untuk `put(a), put(b), get(a), put(c)`

| Langkah | Mode **akses** (LRU) | Mode **penyisipan** (FIFO) |
|---|---|---|
| `put(a)` | `[a]` | `[a]` |
| `put(b)` | `[a, b]` | `[a, b]` |
| `get(a)` | **`[b, a]`** (a dipakai, pindah ke "paling baru") | `[a, b]` (tidak berubah) |
| `put(c)` | kapasitas lewat, **b** (tertua) dibuang → **`[a, c]`** | kapasitas lewat, **a** (tertua) dibuang → **`[b, c]`** |

Hasil program cocok dengan tabel (`a` masih ada pada mode akses, `b` masih ada pada mode penyisipan).

### Perbedaan kedua mode

Konstruktor `LinkedHashMap(kapasitasAwal, loadFactor, accessOrder)`:

- **`accessOrder = false`** (bawaan): urutan entri = **urutan penyisipan pertama**. Membaca atau menimpa nilai tidak mengubah posisi. Elemen "tertua" = yang **pertama masuk**. Hasilnya cache **FIFO**.
- **`accessOrder = true`**: setiap `get`/`put` (juga `getOrDefault`, `putIfAbsent`, dll.) memindahkan entri ke **ujung** daftar. Elemen "tertua" = yang **paling lama tidak disentuh**. Hasilnya cache **LRU**.

`removeEldestEntry` (dipanggil otomatis setelah `put`) cukup menjawab "buang yang tertua bila `size() > kapasitas`"; **definisi "tertua"** sepenuhnya ditentukan mode urutannya. Inilah mengapa mode akses cocok untuk cache: data yang sering dipakai bertahan, yang jarang dipakai tersingkir.

Efek samping yang perlu diwaspadai: pada mode akses, **`get()` adalah modifikasi struktural**. Menelusuri map sambil memanggil `get` pada map yang sama (bahkan di satu thread) dapat melempar `ConcurrentModificationException`. Cache semacam ini juga **tidak thread-safe**; untuk multi-thread, bungkus dengan sinkronisasi atau pakai library cache (Modul 10 membahas dasarnya).

### Mengapa `Optional` tidak cocok sebagai field class

Saya mencoba tiga hal di `OptionalField.java`:

| # | Percobaan | Hasil |
|---|---|---|
| 1 | `new ProfilBurukOptional("Ani", null)` (field `Optional<String>` diisi `null`) | Terkompilasi tanpa peringatan; **`NullPointerException`** saat `email().isPresent()` |
| 2 | Menyerialisasi class `Serializable` yang punya field `Optional` | **`NotSerializableException: java.util.Optional`** |
| 3 | Alternatif: field `String` biasa + getter `Optional.ofNullable(email)` | Berfungsi, dan serialisasi **berhasil** |

Alasannya:

1. **Gagal pada tujuannya sendiri.** `Optional` ada untuk menyingkirkan `null`, tetapi field bertipe `Optional` itu sendiri masih bisa `null`, dan compiler tidak bisa mencegahnya. Anda justru menambah satu lapis ("Optional atau `null`, atau kosong?") tanpa keamanan baru.
2. **Tidak `Serializable`**, jadi merusak class yang perlu diserialisasi (cache, session, beberapa framework). Terbukti pada percobaan 2.
3. **Biaya ekstra:** setiap field membungkus satu object tambahan di heap.
4. **Memang bukan peruntukannya.** Dokumentasi `Optional` menyatakan tujuannya sebagai tipe **kembalian** method yang mungkin tidak punya hasil. Aturan di modul: bukan untuk field, parameter, atau elemen koleksi.

Pola yang disarankan (percobaan 3): **simpan nilai nullable di field (dan dokumentasikan bahwa boleh `null`), lalu bungkus dengan `Optional` pada getter.** Pemanggil tetap dipaksa menangani kasus kosong, sementara object tetap sederhana dan bisa diserialisasi.
