# Modul 7: Generics & Collections Framework

[← Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation](06-fitur-modern.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 8: Exception Handling & I/O →](08-exception-io.md)

**Isi modul ini**

- [Teori](#teori)
  - [7.1 Array vs ArrayList](#71-array-vs-arraylist)
  - [7.2 Generics](#72-generics)
  - [7.3 Collections Framework — Peta Besar](#73-collections-framework--peta-besar)
  - [7.4 List](#74-list)
  - [7.5 Set](#75-set)
  - [7.6 Map](#76-map)
  - [7.7 Queue, Deque, dan Stack](#77-queue-deque-dan-stack)
  - [7.8 Iterator](#78-iterator)
  - [7.9 Mengurutkan: `Comparable` dan `Comparator`](#79-mengurutkan-comparable-dan-comparator)
  - [7.10 Optionals](#710-optionals)
  - [7.11 Immutable Collections dan Sequenced Collections](#711-immutable-collections-dan-sequenced-collections)
  - [7.12 Memilih Collection yang Tepat](#712-memilih-collection-yang-tepat)
- [Praktik](#praktik)
  - [Generics: Class, Method, Bounded Type, dan Wildcard](#generics-class-method-bounded-type-dan-wildcard)
  - [Frekuensi Kata dengan `Map`](#frekuensi-kata-dengan-map)
  - [Stack, Queue, dan PriorityQueue](#stack-queue-dan-priorityqueue)
  - [Cache LRU dengan `LinkedHashMap`](#cache-lru-dengan-linkedhashmap)
  - [Iterator, `removeIf`, `Optional`, dan Sequenced Collections](#iterator-removeif-optional-dan-sequenced-collections)
- [Latihan](#latihan)

---


## Teori

### 7.1 Array vs ArrayList

Anda sudah mengenal array di [Modul 3](03-kontrol-alur-array-string.md). **`ArrayList`** adalah "array yang bisa tumbuh": di dalamnya tetap ada array, tetapi class ini otomatis membuat array yang lebih besar saat penuh.

| | Array (`int[]`, `String[]`) | `ArrayList<E>` |
|---|---|---|
| Ukuran | **Tetap** sejak dibuat | **Dinamis** — bertambah/berkurang |
| Tipe elemen | Primitif **atau** object | **Hanya object** (primitif lewat wrapper, misal `Integer`) |
| Akses | `a[i]`, `a.length` | `list.get(i)`, `list.size()` |
| Tambah/hapus | Tidak ada (harus buat array baru) | `add`, `remove`, `addAll`, `removeIf` |
| Fitur bawaan | Sedikit (lewat `Arrays`) | Banyak: pencarian, sortir, iterasi aman |
| Performa | Sedikit lebih cepat & hemat memory (terutama primitif) | Overhead kecil, boxing untuk primitif |

Gunakan **array** untuk data berukuran pasti (terutama primitif dan performa kritis); gunakan **`ArrayList`** untuk hampir semua kebutuhan koleksi sehari-hari.

### 7.2 Generics

**Generics** memungkinkan class dan method bekerja dengan **tipe yang ditentukan belakangan** sambil tetap **aman tipe saat kompilasi**. Tanpa generics, koleksi menyimpan `Object` dan Anda harus melakukan cast — rawan `ClassCastException` saat runtime. Dengan generics, kesalahan tipe tertangkap oleh compiler:

```java
List<String> nama = new ArrayList<>();   // <> (diamond): compiler menyimpulkan <String>
nama.add("Ani");
// nama.add(42);                         // ERROR kompilasi — bukan String
String pertama = nama.get(0);            // tanpa cast
```

**Class dan method generic:**

```java
class Kotak<T> {                          // T = type parameter
    private T isi;
    void simpan(T isi) { this.isi = isi; }
    T ambil() { return isi; }
}

static <T extends Comparable<T>> T terbesar(List<T> data) {   // generic method + bounded type
    T maks = data.get(0);
    for (T x : data) if (x.compareTo(maks) > 0) maks = x;
    return maks;
}
```

- **Bounded type** (`T extends Comparable<T>`) membatasi tipe yang boleh dipakai, sehingga method `compareTo` dijamin ada.
- **Wildcard** `?` dipakai pada parameter untuk fleksibilitas. Aturan praktis **PECS — Producer Extends, Consumer Super**:
  - `List<? extends Number>` — Anda hanya **membaca** (produsen) angka dari list; bisa berisi `Integer`, `Double`, dst.
  - `List<? super Integer>` — Anda **menulis** (konsumen) `Integer` ke dalamnya; bisa `List<Integer>`, `List<Number>`, atau `List<Object>`.
- **Type erasure** — informasi generic **dihapus saat kompilasi** (menjadi `Object`/batasnya) demi kompatibilitas. Akibatnya: tidak bisa `new T()`, tidak bisa `instanceof List<String>`, dan tidak bisa membuat array generic. Generic **tidak bisa** dipakai dengan primitif (`List<int>` ❌, `List<Integer>` ✅).

### 7.3 Collections Framework — Peta Besar

**Java Collections Framework** adalah sekumpulan interface dan class standar untuk menyimpan dan memproses kelompok object (paket `java.util`). Hierarki utamanya:

```
Iterable
  └── Collection
        ├── List      — berurutan, boleh duplikat       (ArrayList, LinkedList)
        ├── Set       — tanpa duplikat                  (HashSet, LinkedHashSet, TreeSet)
        └── Queue     — antrean                         (PriorityQueue, ArrayDeque)
              └── Deque — antrean dua arah              (ArrayDeque, LinkedList)

Map (terpisah dari Collection) — pasangan key → value   (HashMap, LinkedHashMap, TreeMap)
```

Bekerjalah **dengan interface** pada deklarasi (`List<String> x = new ArrayList<>()`), bukan class konkret — implementasinya bisa diganti tanpa mengubah sisa kode.

### 7.4 List

`List` menyimpan elemen **berurutan** (akses lewat indeks) dan **mengizinkan duplikat**.

| Implementasi | Struktur | Kekuatan | Kelemahan |
|---|---|---|---|
| **`ArrayList`** | Array dinamis | `get(i)` sangat cepat, iterasi cepat | Sisip/hapus di tengah lambat (geser elemen) |
| **`LinkedList`** | Doubly linked list | Sisip/hapus di ujung cepat | `get(i)` lambat, boros memory |

**`ArrayList` adalah pilihan default**; `LinkedList` jarang menang di praktik nyata karena buruknya *cache locality* — untuk antrean/stack, pakai `ArrayDeque` (7.7).

```java
List<String> buah = new ArrayList<>(List.of("apel", "jeruk"));
buah.add("mangga");           // tambah di akhir
buah.add(1, "pisang");        // sisip di indeks 1
buah.remove("jeruk");         // hapus berdasarkan nilai
buah.set(0, "apel merah");    // ganti
System.out.println(buah.contains("mangga") + " " + buah.indexOf("mangga"));
```

> **Jebakan:** untuk `List<Integer>`, `list.remove(1)` menghapus **indeks 1**, bukan nilai `1`. Untuk menghapus nilai gunakan `list.remove(Integer.valueOf(1))`.

### 7.5 Set

`Set` **tidak mengizinkan duplikat** (ditentukan oleh `equals`/`hashCode` — [Modul 5](05-pilar-oop.md)).

| Implementasi | Urutan iterasi | `add/contains/remove` |
|---|---|---|
| **`HashSet`** | Tidak terjamin | O(1) rata-rata |
| **`LinkedHashSet`** | Sesuai urutan **penyisipan** | O(1) |
| **`TreeSet`** | **Terurut** (urutan alami / `Comparator`) | O(log n) |

```java
Set<String> unik = new HashSet<>(List.of("a", "b", "a", "c"));
System.out.println(unik.size());       // 3
System.out.println(unik.add("b"));     // false — sudah ada
TreeSet<Integer> terurut = new TreeSet<>(List.of(5, 1, 9, 3));
System.out.println(terurut);                  // [1, 3, 5, 9]
System.out.println(terurut.first() + " " + terurut.ceiling(4));   // 1 5
```

### 7.6 Map

`Map<K, V>` menyimpan pasangan **key → value**; key **unik**. Cocok untuk pencarian cepat berdasarkan kunci (seperti dictionary).

| Implementasi | Urutan iterasi | Catatan |
|---|---|---|
| **`HashMap`** | Tidak terjamin | Default; O(1) rata-rata; izinkan 1 key `null` |
| **`LinkedHashMap`** | Urutan penyisipan (atau urutan akses) | Dasar untuk **cache LRU** |
| **`TreeMap`** | Terurut menurut key | O(log n); ada `firstKey`, `floorKey`, dll. |
| `ConcurrentHashMap` | Tidak terjamin | Aman untuk multi-thread ([Modul 10](10-concurrency.md)) |

Method yang paling berguna:

```java
Map<String, Integer> stok = new HashMap<>();
stok.put("apel", 10);
stok.putIfAbsent("jeruk", 5);                    // isi hanya jika key belum ada
int n = stok.getOrDefault("mangga", 0);          // 0 — key tidak ada
stok.merge("apel", 3, Integer::sum);             // apel: 10 + 3 = 13
stok.computeIfAbsent("pisang", k -> 8);          // hitung & isi jika belum ada

for (Map.Entry<String, Integer> e : stok.entrySet()) {   // telusuri key + value
    System.out.println(e.getKey() + " = " + e.getValue());
}
```

> Tanda `->` dan `::` di atas adalah **lambda** dan **method reference** — bacalah untuk sementara sebagai "fungsi mini". Pembahasan lengkapnya ada di [Modul 9](09-functional-programming.md).

Kunci `Map` **sebaiknya immutable** (`String`, `Integer`, `record`). Jika key diubah setelah dimasukkan, object-nya bisa "hilang" karena `hashCode` berubah.

### 7.7 Queue, Deque, dan Stack

- **`Queue`** — antrean **FIFO** (*First In, First Out*): `offer` (masuk di belakang), `poll` (keluar dari depan, `null` jika kosong), `peek` (intip depan).
- **`Deque`** (*double-ended queue*, dibaca "deck") — antrean dua arah: tambah/ambil di **kedua ujung**. Dapat berperan sebagai **queue** maupun **stack (LIFO)**.
- **`Stack`** — *last in, first out*. Class `java.util.Stack` bersifat **legacy** (turunan `Vector`, tersinkronisasi dan lambat). Untuk stack modern gunakan **`ArrayDeque`** dengan `push`/`pop`/`peek`.
- **`PriorityQueue`** — elemen keluar menurut **prioritas** (terkecil lebih dulu secara default, atau menurut `Comparator`), bukan urutan masuk. Iterasi **tidak** menjamin urutan terurut.

```java
Deque<Integer> stack = new ArrayDeque<>();      // sebagai STACK
stack.push(1); stack.push(2); stack.push(3);
System.out.println(stack.pop());                // 3  (terakhir masuk, pertama keluar)

Queue<String> antrean = new ArrayDeque<>();     // sebagai QUEUE
antrean.offer("A"); antrean.offer("B");
System.out.println(antrean.poll());             // A  (pertama masuk, pertama keluar)

PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 3));
System.out.println(pq.poll());                  // 1  (terkecil)
```

`ArrayDeque` **tidak mengizinkan elemen `null`**.

### 7.8 Iterator

**`Iterator`** adalah cara seragam menelusuri koleksi apa pun: `hasNext()`, `next()`, dan `remove()`. Loop **for-each** sebenarnya memakai `Iterator` di balik layar (karena itu koleksi harus `Iterable`).

Poin krusial: **jangan memodifikasi koleksi saat for-each berjalan** — Java akan melempar `ConcurrentModificationException`. Ada tiga cara aman menghapus elemen saat iterasi:

```java
List<Integer> angka = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

// 1) Iterator.remove()
Iterator<Integer> it = angka.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) it.remove();
}

// 2) removeIf (paling ringkas, disarankan)
angka.removeIf(x -> x > 4);

// 3) Buat koleksi baru berisi hasil filter (stream — Modul 9)
```

### 7.9 Mengurutkan: `Comparable` dan `Comparator`

- **`Comparable<T>`** — urutan **alami** yang ditanam di dalam class (`compareTo`). Dipakai `Collections.sort`, `TreeSet`, dll.
- **`Comparator<T>`** — urutan **eksternal** yang bisa dibuat sesuka hati tanpa mengubah class.

```java
record Mahasiswa(String nama, double ipk) { }

List<Mahasiswa> daftar = new ArrayList<>(List.of(
    new Mahasiswa("Citra", 3.5), new Mahasiswa("Andi", 3.9), new Mahasiswa("Budi", 3.5)));

daftar.sort(Comparator.comparing(Mahasiswa::nama));                          // A-Z
daftar.sort(Comparator.comparingDouble(Mahasiswa::ipk).reversed());          // IPK tertinggi
daftar.sort(Comparator.comparingDouble(Mahasiswa::ipk).reversed()
                      .thenComparing(Mahasiswa::nama));                      // IPK desc, lalu nama
```

### 7.10 Optionals

`null` adalah sumber `NullPointerException` terbesar — Tony Hoare menyebutnya "kesalahan miliaran dolar". **`Optional<T>`** adalah wadah yang **secara eksplisit** menyatakan "mungkin ada nilai, mungkin tidak", memaksa pemanggil menangani kasus kosong.

```java
Optional<String> hasil = cariEmail("budi");        // method mengembalikan Optional

String email = hasil.orElse("tidak ada");                     // nilai default
String email2 = hasil.orElseGet(() -> buatDefault());         // default dihitung malas
String email3 = hasil.orElseThrow();                          // lempar NoSuchElementException jika kosong
hasil.ifPresent(e -> System.out.println("Kirim ke " + e));
hasil.ifPresentOrElse(e -> kirim(e), () -> catatGagal());
String besar = hasil.map(String::toUpperCase).orElse("-");    // transformasi aman
```

**Aturan penggunaan:**

- Pakai `Optional` sebagai **tipe kembalian** method yang mungkin tidak menemukan hasil.
- **Jangan** pakai untuk field class, parameter method, atau elemen koleksi.
- **Jangan** memanggil `.get()` tanpa `isPresent()` — gunakan `orElse`, `orElseThrow`, atau `map`.
- Untuk koleksi kosong, kembalikan **koleksi kosong**, bukan `Optional<List<...>>` atau `null`.

### 7.11 Immutable Collections dan Sequenced Collections

**Koleksi immutable** (Java 9+) dibuat dengan factory method — tidak bisa diubah, tidak menerima `null`:

```java
List<String> l = List.of("a", "b");
Set<Integer> s = Set.of(1, 2, 3);
Map<String, Integer> m = Map.of("satu", 1, "dua", 2);
List<String> salinan = List.copyOf(sumber);     // salinan immutable
// l.add("c");                                  // UnsupportedOperationException
```

Bedakan dengan `Arrays.asList(...)` (ukuran tetap, tapi elemennya masih bisa di-`set`) dan `Collections.unmodifiableList(x)` (hanya *view* — jika `x` berubah, view ikut berubah).

**Sequenced Collections** (Java 21) menambahkan antarmuka seragam untuk koleksi **berurutan** (`List`, `LinkedHashSet`, `LinkedHashMap`, `TreeSet`, `Deque`, …): `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`, `removeLast()`, dan **`reversed()`** (view terbalik). Sebelumnya, mengambil elemen terakhir `List` harus `list.get(list.size() - 1)`.

### 7.12 Memilih Collection yang Tepat

| Kebutuhan | Pilihan |
|---|---|
| Daftar berurutan, akses via indeks | `ArrayList` |
| Menghapus duplikat / cek keanggotaan cepat | `HashSet` |
| Unik **dan** pertahankan urutan masuk | `LinkedHashSet` |
| Unik **dan** selalu terurut | `TreeSet` |
| Lookup berdasarkan kunci | `HashMap` |
| Map dengan urutan masuk / cache LRU | `LinkedHashMap` |
| Map terurut menurut key / pencarian rentang | `TreeMap` |
| Stack (LIFO) atau antrean (FIFO) | `ArrayDeque` |
| Proses menurut prioritas | `PriorityQueue` |
| Kunci berupa `enum` | `EnumMap` / `EnumSet` |
| Akses banyak thread | `ConcurrentHashMap`, `CopyOnWriteArrayList` |

## Praktik

### Generics: Class, Method, Bounded Type, dan Wildcard

```java
// DemoGenerics.java
import java.util.ArrayList;
import java.util.List;

public class DemoGenerics {
    public static void main(String[] args) {
        Kotak<String> ks = new Kotak<>();
        ks.simpan("halo");
        String s = ks.ambil();                 // tanpa cast
        System.out.println(s);

        System.out.println(terbesar(List.of(3, 9, 2)));
        System.out.println(terbesar(List.of("kiwi", "apel", "mangga")));

        List<Integer> ints = List.of(1, 2, 3);
        List<Double> dbls = List.of(1.5, 2.5);
        System.out.println("jumlah ints   = " + jumlah(ints));
        System.out.println("jumlah doubles= " + jumlah(dbls));

        List<Number> wadah = new ArrayList<>();
        isiAngka(wadah);                       // List<Number> menerima Integer (super)
        System.out.println(wadah);

        // Type erasure: pada runtime kedua list bertipe sama
        System.out.println(new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass());
    }

    static <T extends Comparable<T>> T terbesar(List<T> data) {
        T maks = data.get(0);
        for (T x : data) {
            if (x.compareTo(maks) > 0) maks = x;
        }
        return maks;
    }

    // PECS: "? extends" untuk MEMBACA (producer)
    static double jumlah(List<? extends Number> data) {
        double total = 0;
        for (Number n : data) total += n.doubleValue();
        return total;
    }

    // PECS: "? super" untuk MENULIS (consumer)
    static void isiAngka(List<? super Integer> tujuan) {
        for (int i = 1; i <= 3; i++) tujuan.add(i);
    }
}

class Kotak<T> {
    private T isi;
    void simpan(T isi) { this.isi = isi; }
    T ambil() { return isi; }
}
```

### Frekuensi Kata dengan `Map`

```java
// FrekuensiKata.java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FrekuensiKata {
    public static void main(String[] args) {
        String teks = "java itu menyenangkan dan java itu kuat, belajar java dan belajar terus";
        String[] kata = teks.replace(",", "").split(" ");

        Map<String, Integer> frekuensi = new HashMap<>();
        for (String k : kata) {
            frekuensi.merge(k, 1, Integer::sum);        // tambah 1; mulai dari 1 jika belum ada
        }

        // Urutkan berdasarkan jumlah (menurun), lalu kata (A-Z)
        List<Map.Entry<String, Integer>> daftar = new ArrayList<>(frekuensi.entrySet());
        daftar.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()));

        for (Map.Entry<String, Integer> e : daftar) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        // TreeMap: otomatis terurut menurut key
        System.out.println(new TreeMap<>(frekuensi));
    }
}
```

### Stack, Queue, dan PriorityQueue

```java
// DemoAntrian.java
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class DemoAntrian {
    public static void main(String[] args) {
        // STACK: riwayat undo (LIFO)
        Deque<String> undo = new ArrayDeque<>();
        undo.push("ketik 'a'");
        undo.push("ketik 'b'");
        undo.push("hapus 'b'");
        System.out.println("Undo: " + undo.pop());
        System.out.println("Undo: " + undo.pop());
        System.out.println("Sisa riwayat: " + undo);

        // QUEUE: antrean layanan (FIFO)
        Queue<String> antrean = new ArrayDeque<>();
        antrean.offer("Pelanggan 1");
        antrean.offer("Pelanggan 2");
        antrean.offer("Pelanggan 3");
        System.out.println("Dilayani : " + antrean.poll());
        System.out.println("Berikutnya: " + antrean.peek());

        // PRIORITYQUEUE: tugas dengan prioritas (angka kecil = penting)
        record Tugas(String nama, int prioritas) { }
        PriorityQueue<Tugas> tugas = new PriorityQueue<>(Comparator.comparingInt(Tugas::prioritas));
        tugas.add(new Tugas("Kirim laporan", 2));
        tugas.add(new Tugas("Perbaiki bug produksi", 1));
        tugas.add(new Tugas("Rapikan arsip", 3));
        while (!tugas.isEmpty()) {
            System.out.println("Kerjakan: " + tugas.poll());
        }
    }
}
```

### Cache LRU dengan `LinkedHashMap`

```java
// DemoLru.java
import java.util.LinkedHashMap;
import java.util.Map;

public class DemoLru {
    public static void main(String[] args) {
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");
        cache.get("a");              // "a" baru dipakai -> jadi paling baru
        cache.put("d", "4");         // penuh: "b" (paling lama tak dipakai) dibuang
        System.out.println(cache.keySet());   // [c, a, d]
    }
}

class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int kapasitas;

    LruCache(int kapasitas) {
        super(16, 0.75f, true);      // true = urutkan berdasarkan AKSES, bukan penyisipan
        this.kapasitas = kapasitas;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > kapasitas;   // dipanggil otomatis setelah put()
    }
}
```

Pola ini sama dengan konsep *eviction policy* **LRU** yang dibahas di course Redis — di sini diimplementasikan hanya dengan beberapa baris.

### Iterator, `removeIf`, `Optional`, dan Sequenced Collections

```java
// DemoIteratorOptional.java
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class DemoIteratorOptional {
    public static void main(String[] args) {
        // Modifikasi saat for-each -> ConcurrentModificationException
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (Integer x : a) {
                if (x == 2) a.remove(x);
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("CME: tidak boleh modifikasi saat for-each");
        }

        // Cara benar 1: Iterator.remove()
        List<Integer> b = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (Iterator<Integer> it = b.iterator(); it.hasNext(); ) {
            if (it.next() % 2 == 0) it.remove();
        }
        System.out.println("Setelah hapus genap: " + b);

        // Cara benar 2: removeIf
        b.removeIf(x -> x > 4);
        System.out.println("Setelah removeIf (>4): " + b);

        // Optional
        System.out.println(cari("ani").map(String::toUpperCase).orElse("tidak ada"));
        System.out.println(cari("zzz").map(String::toUpperCase).orElse("tidak ada"));
        cari("budi").ifPresentOrElse(
            e -> System.out.println("Kirim ke " + e),
            () -> System.out.println("tidak ditemukan"));

        // Sequenced Collections (Java 21): elemen pertama/terakhir & tampilan terbalik
        List<String> urut = new ArrayList<>(List.of("a", "b", "c"));
        System.out.println(urut.getFirst() + " " + urut.getLast() + " " + urut.reversed());

        // Immutable
        try {
            List.of(1, 2).add(3);
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of immutable");
        }
    }

    static Optional<String> cari(String nama) {
        return switch (nama) {
            case "ani" -> Optional.of("ani@contoh.com");
            case "budi" -> Optional.of("budi@contoh.com");
            default -> Optional.empty();
        };
    }
}
```

## Latihan

1. Tulis program yang membaca daftar bilangan, lalu menghasilkan: (a) daftar tanpa duplikat dengan urutan masuk dipertahankan, (b) daftar yang terurut naik tanpa duplikat. Pilih `Set` yang tepat untuk masing-masing dan jelaskan alasannya.
2. Buat generic class `Pasangan<A, B>` dengan method `tukar()` yang mengembalikan `Pasangan<B, A>`. Tambahkan generic method `static <T extends Comparable<T>> T min(T a, T b)` dan uji dengan `Integer` dan `String`.
3. Implementasikan pengecekan kurung seimbang (`"{[()]}"` valid, `"{[(])}"` tidak) memakai `ArrayDeque` sebagai stack. Mengapa `ArrayDeque` lebih disarankan daripada `Stack`?
4. Buat `record Produk(String nama, String kategori, int harga)`, simpan 8 produk dalam `List`, lalu kelompokkan ke `Map<String, List<Produk>>` berdasarkan kategori **tanpa stream** (manfaatkan `computeIfAbsent`). Urutkan setiap grup menurut harga menurun dengan `Comparator`.
5. Modifikasi cache LRU agar kapasitasnya 2, lalu jalankan skenario `put(a), put(b), get(a), put(c)` dan prediksi isi cache **sebelum** menjalankan. Setelah itu jelaskan perbedaan `LinkedHashMap` mode urutan-penyisipan vs mode urutan-akses, dan mengapa `Optional` tidak cocok dipakai sebagai field class.

---

[← Modul 6: Enum, Record, Sealed Class, Pattern Matching & Annotation](06-fitur-modern.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 8: Exception Handling & I/O →](08-exception-io.md)
