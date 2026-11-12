# 0109 - Collections: List, Set, Map, Queue

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Menjelaskan kenapa collection dibutuhkan dibanding array
- Memakai generics dasar (`List<String>`) dan memahami wrapper class (autoboxing)
- Memilih dan memakai `List`, `Set`, `Map`, `Queue`/`Deque` dengan tepat
- Mengurutkan data dengan `Comparable` dan `Comparator`
- Menghindari jebakan umum (`ConcurrentModificationException`, `remove(int)` vs `remove(Object)`, `Integer ==`, unboxing `null`)
- Menjelaskan hubungan `equals`/`hashCode` dengan `HashSet` dan `HashMap`
- Memilih collection berdasarkan kebutuhan dan kompleksitas operasinya

---

## 1. Teori

### 1.1 Kenapa Collection?

Array punya keterbatasan: ukuran **tetap**, tidak ada method siap pakai (cari, hapus, dll.), dan tidak bisa menjaga keunikan. **Collections Framework** adalah kumpulan struktur data siap pakai di `java.util` dengan ukuran dinamis.

**Analogi:**

| Collection | Analogi |
|---|---|
| `List` | antrean bernomor / daftar belanja (berurutan, boleh ada duplikat) |
| `Set` | himpunan / daftar tamu undangan (tanpa duplikat) |
| `Map` | kamus / buku telepon (kata kunci → nilai) |
| `Queue` | antrean kasir (masuk belakang, keluar depan: FIFO) |
| `Deque` sebagai stack | tumpukan piring (masuk atas, keluar atas: LIFO) |

### 1.2 Hierarki Singkat

```
Iterable
└── Collection
    ├── List      → ArrayList, LinkedList
    ├── Set       → HashSet, LinkedHashSet, TreeSet
    └── Queue     → LinkedList, PriorityQueue
        └── Deque → ArrayDeque, LinkedList

Map (terpisah, bukan turunan Collection)
    → HashMap, LinkedHashMap, TreeMap
```

Semua yang `Collection` bisa diiterasi dengan for-each (`Iterable`). `Map` punya cara iterasinya sendiri.

### 1.3 Generics Dasar

Generics memberi tahu compiler **tipe isi** collection, sehingga salah tipe terdeteksi saat **compile time**, dan tidak perlu cast.

```java
List<String> names = new ArrayList<>();   // <> is the diamond operator
names.add("Budi");
// names.add(42);                         // compile error: int is not a String
String first = names.get(0);              // no cast needed
```

Tanpa generics (raw type, **hindari**):

```java
List names = new ArrayList();
names.add("Budi");
names.add(42);                            // allowed, error appears only at runtime
String s = (String) names.get(1);         // ClassCastException
```

Generics tidak menerima primitive. Pakai **wrapper class**:

| Primitive | Wrapper |
|---|---|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `char` | `Character` |

```java
List<int> bad;              // compile error
List<Integer> ok = new ArrayList<>();
```

### 1.4 Autoboxing dan Jebakannya

Java otomatis mengonversi `int` ↔ `Integer` (**autoboxing/unboxing**).

```java
List<Integer> list = new ArrayList<>();
list.add(5);                // autoboxing: int -> Integer
int x = list.get(0);        // unboxing: Integer -> int
```

**Jebakan 1: `==` pada wrapper.**

```java
Integer a = 127, b = 127;
System.out.println(a == b);        // true  (small values are cached: -128..127)

Integer c = 128, d = 128;
System.out.println(c == d);        // false (different objects)
System.out.println(c.equals(d));   // true
```

Aturan: bandingkan wrapper dengan **`.equals()`**, bukan `==`.

**Jebakan 2: unboxing `null`.**

```java
Map<String, Integer> stock = new HashMap<>();
int qty = stock.get("pen");        // NullPointerException: get returns null, unboxing fails
```

**Jebakan 3: `remove(int)` vs `remove(Object)`.**

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
list.remove(1);                        // removes the element at INDEX 1 -> [10, 30]
list.remove(Integer.valueOf(10));      // removes the VALUE 10          -> [30]
```

### 1.5 `List`

Berurutan, boleh duplikat, diakses lewat **index**.

```java
import java.util.ArrayList;
import java.util.List;

List<String> fruits = new ArrayList<>();

fruits.add("apple");               // [apple]
fruits.add("banana");              // [apple, banana]
fruits.add(1, "cherry");           // [apple, cherry, banana]
fruits.set(0, "avocado");          // [avocado, cherry, banana]

fruits.get(1);                     // "cherry"
fruits.size();                     // 3
fruits.contains("banana");         // true
fruits.indexOf("banana");          // 2 (-1 if absent)
fruits.remove("cherry");           // by value
fruits.remove(0);                  // by index
fruits.isEmpty();                  // false
fruits.clear();                    // remove all
```

Method lain yang sering dipakai: `addAll`, `removeAll`, `subList(from, to)`, `sort`, `toArray`, `List.copyOf`.

**Program ke interface**, bukan ke class konkret:

```java
List<String> list = new ArrayList<>();      // good: variable type is the interface
ArrayList<String> list2 = new ArrayList<>(); // avoid unless you need ArrayList-specific methods
```

Dengan begitu mengganti implementasi (misal ke `LinkedList`) cukup mengubah satu baris.

**`ArrayList` vs `LinkedList`:**

| | `ArrayList` | `LinkedList` |
|---|---|---|
| Struktur | array dinamis | doubly linked list |
| `get(i)` | **O(1)** | O(n) |
| `add` di akhir | O(1) (amortized) | O(1) |
| `add/remove` di tengah | O(n) | O(1) *jika sudah di posisi node*, tapi mencari posisinya O(n) |
| Memory | hemat | boros (tiap node punya 2 pointer) |

Praktiknya: **pakai `ArrayList` hampir selalu.** `LinkedList` jarang lebih cepat di dunia nyata (cache CPU tidak ramah). Kalau butuh queue/stack, pakai `ArrayDeque`.

**List yang tidak bisa diubah:**

```java
List<String> fixed = List.of("a", "b", "c");           // immutable, no nulls allowed
// fixed.add("d");                                      // UnsupportedOperationException

List<String> view = Arrays.asList("a", "b", "c");      // fixed-size: set OK, add/remove not
List<String> mutableCopy = new ArrayList<>(fixed);     // a normal mutable copy
```

### 1.6 Iterasi `List` dan `ConcurrentModificationException`

```java
for (String s : fruits) {                       // enhanced for
    System.out.println(s);
}

for (int i = 0; i < fruits.size(); i++) {       // index-based
    System.out.println(i + ": " + fruits.get(i));
}
```

**Jangan memodifikasi list saat for-each:**

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
for (Integer n : nums) {
    if (n % 2 == 0) {
        nums.remove(n);     // ConcurrentModificationException
    }
}
```

Cara yang benar:

```java
// 1) removeIf (shortest, preferred)
nums.removeIf(n -> n % 2 == 0);

// 2) Iterator.remove()
Iterator<Integer> it = nums.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) {
        it.remove();
    }
}

// 3) build a new list with only the elements you want to keep
```

> `n -> n % 2 == 0` adalah **lambda** (fungsi ringkas). Dibahas tuntas di materi `0110`. Untuk sekarang baca sebagai: "untuk setiap `n`, kondisinya `n` genap".

### 1.7 Mengurutkan: `Comparable` dan `Comparator`

```java
List<String> names = new ArrayList<>(List.of("Sari", "Budi", "Andi"));
Collections.sort(names);                 // natural order: [Andi, Budi, Sari]
names.sort(null);                        // same thing
```

Untuk class buatan sendiri, ada dua cara menentukan urutan:

**1. `Comparable<T>`: urutan alami (satu urutan default), dimasukkan ke dalam class.**

```java
class Student implements Comparable<Student> {
    private final String name;
    private final double gpa;

    Student(String name, double gpa) {
        this.name = name;
        this.gpa = gpa;
    }

    public String getName() { return name; }
    public double getGpa() { return gpa; }

    @Override
    public int compareTo(Student other) {
        return this.name.compareTo(other.name);   // order by name
    }

    @Override
    public String toString() { return name + "(" + gpa + ")"; }
}
```

`compareTo` mengembalikan: **negatif** (this < other), **0** (sama), **positif** (this > other).

**2. `Comparator<T>`: urutan di luar class, bisa banyak versi.**

```java
students.sort(Comparator.comparingDouble(Student::getGpa));                  // gpa ascending
students.sort(Comparator.comparingDouble(Student::getGpa).reversed());       // gpa descending
students.sort(Comparator.comparingDouble(Student::getGpa).reversed()
        .thenComparing(Student::getName));                                   // tie-breaker by name
```

`Student::getGpa` adalah **method reference** (cara ringkas menunjuk method), dibahas di `0110`.

Hindari trik `return a - b` di `compareTo`/`Comparator` untuk `int` (bisa overflow). Pakai `Integer.compare(a, b)` / `Double.compare(a, b)`.

### 1.8 `Set`

**Tanpa duplikat.** Operasi `add` mengembalikan `false` kalau elemen sudah ada.

```java
Set<String> tags = new HashSet<>();
tags.add("java");        // true
tags.add("spring");      // true
tags.add("java");        // false (already present)
tags.size();             // 2
tags.contains("java");   // true
tags.remove("spring");
```

| Implementasi | Urutan | Kecepatan | Catatan |
|---|---|---|---|
| `HashSet` | **tidak terjamin** | O(1) rata-rata | paling umum |
| `LinkedHashSet` | urutan **insert** | O(1) | menjaga urutan masuk |
| `TreeSet` | **terurut** (natural/Comparator) | O(log n) | punya `first()`, `last()`, `floor()`, `ceiling()` |

**Penting:** `HashSet` menentukan "duplikat" lewat **`hashCode()` lalu `equals()`**. Kalau class-mu tidak meng-override keduanya (lihat `0106`), dua object berisi sama dianggap **berbeda**.

```java
Set<Point> points = new HashSet<>();
points.add(new Point(1, 2));
points.add(new Point(1, 2));
System.out.println(points.size());   // 2 without equals/hashCode, 1 with them
```

`TreeSet` memakai `compareTo`/`Comparator` (bukan `equals`) untuk menentukan duplikat.

Operasi himpunan:

```java
Set<Integer> a = new TreeSet<>(Set.of(1, 2, 3, 4));
Set<Integer> b = new TreeSet<>(Set.of(3, 4, 5));

Set<Integer> union = new TreeSet<>(a);
union.addAll(b);                 // [1, 2, 3, 4, 5]

Set<Integer> intersection = new TreeSet<>(a);
intersection.retainAll(b);       // [3, 4]

Set<Integer> difference = new TreeSet<>(a);
difference.removeAll(b);         // [1, 2]
```

Pola umum: **hapus duplikat dari list** sambil menjaga urutan:

```java
List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(list));
```

### 1.9 `Map`

Menyimpan pasangan **key → value**. Key **unik**, value boleh sama.

```java
Map<String, Integer> stock = new HashMap<>();

stock.put("pen", 10);                         // add
stock.put("book", 5);
stock.put("pen", 12);                         // same key: value replaced (old value 10 is returned)

stock.get("pen");                             // 12
stock.get("bag");                             // null (key absent)
stock.getOrDefault("bag", 0);                 // 0
stock.containsKey("book");                    // true
stock.containsValue(5);                       // true
stock.putIfAbsent("bag", 3);                  // add only if key is absent
stock.remove("book");
stock.size();
```

**Iterasi:**

```java
for (String key : stock.keySet()) { }                       // keys only
for (Integer value : stock.values()) { }                    // values only

for (Map.Entry<String, Integer> e : stock.entrySet()) {     // keys and values (preferred)
    System.out.println(e.getKey() + " -> " + e.getValue());
}
```

Hindari loop `keySet()` lalu `get(key)` di dalamnya (dua kali lookup, tidak perlu).

**Method modern yang sangat berguna:**

```java
// counting: merge
Map<String, Integer> count = new HashMap<>();
for (String w : words) {
    count.merge(w, 1, Integer::sum);          // if absent put 1, else old + 1
}

// grouping: computeIfAbsent
Map<Integer, List<String>> byLength = new TreeMap<>();
for (String w : words) {
    byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
}
```

| Implementasi | Urutan key | Kecepatan | Catatan |
|---|---|---|---|
| `HashMap` | **tidak terjamin** | O(1) rata-rata | paling umum, boleh 1 key `null` |
| `LinkedHashMap` | urutan insert | O(1) | |
| `TreeMap` | **terurut** by key | O(log n) | key harus `Comparable`/ada `Comparator`, tidak boleh key `null` |

Aturan penting untuk **key**:
- Key sebaiknya **immutable** (`String`, `Integer`, atau class immutable). Kalau key diubah setelah dimasukkan dan `hashCode`-nya ikut berubah, entry itu "hilang" di dalam map.
- Class yang jadi key **wajib** meng-override `equals` dan `hashCode` (`0106`).

`Map.of(...)` membuat map **immutable** kecil:

```java
Map<String, Integer> prices = Map.of("pen", 5000, "book", 20000);
```

### 1.10 `Queue` dan `Deque`

**Queue (FIFO):**

```java
Queue<String> queue = new ArrayDeque<>();
queue.offer("A");           // enqueue (add to tail)
queue.offer("B");
queue.peek();               // "A"  (look at head, do not remove; null if empty)
queue.poll();               // "A"  (remove head; null if empty)
queue.size();               // 1
```

**Stack (LIFO) memakai `Deque`:**

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);              // add to the top
stack.push(2);
stack.peek();               // 2
stack.pop();                // 2  (throws NoSuchElementException if empty)
```

> Jangan pakai class `java.util.Stack` (warisan lama, lambat karena synchronized, dan mewarisi `Vector`). Pakai `ArrayDeque`.

| Operasi | Melempar exception jika gagal | Mengembalikan nilai khusus |
|---|---|---|
| Tambah | `add(e)` | `offer(e)` |
| Ambil + hapus | `remove()` | `poll()` (`null`) |
| Lihat saja | `element()` | `peek()` (`null`) |

`ArrayDeque` tidak boleh menyimpan `null`.

**`PriorityQueue`**: elemen yang keluar adalah yang **prioritas tertinggi** (terkecil menurut urutan natural/Comparator), bukan yang pertama masuk.

```java
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(5); pq.offer(1); pq.offer(3);
pq.poll();   // 1
pq.poll();   // 3
```

(Iterasi `PriorityQueue` **tidak** terurut, hanya `poll()` yang menjamin urutan.)

### 1.11 Class Utility `Collections`

```java
Collections.sort(list);
Collections.reverse(list);
Collections.shuffle(list);
Collections.max(list);
Collections.min(list);
Collections.frequency(list, "a");             // how many times "a" appears
Collections.unmodifiableList(list);           // read-only VIEW (changes in the original still show)
Collections.emptyList();
Collections.swap(list, 0, 1);
```

Perbedaan **view vs copy:**

```java
List<String> original = new ArrayList<>(List.of("a", "b"));
List<String> view = Collections.unmodifiableList(original);
List<String> copy = List.copyOf(original);

original.add("c");
System.out.println(view);   // [a, b, c]  (view reflects the change)
System.out.println(copy);   // [a, b]     (copy is independent and immutable)
```

### 1.12 Praktik Baik

- **Deklarasikan dengan interface**: `List<String>`, `Map<K,V>`, `Set<T>`.
- **Jangan kembalikan `null` untuk collection**; kembalikan collection kosong (`List.of()` / `Collections.emptyList()`).
- **Defensive copy** saat menerima/mengembalikan collection milik object (supaya encapsulation tidak bocor):

```java
class Course {
    private final List<String> students = new ArrayList<>();

    public List<String> getStudents() {
        return List.copyOf(students);       // caller cannot modify our internal list
    }
}
```

- Beri **kapasitas awal** kalau tahu perkiraan ukuran besar (`new ArrayList<>(10_000)`), menghindari resize berulang.
- Pilih implementasi berdasarkan **operasi dominan** (lihat tabel di bawah).
- Hati-hati **menyimpan object mutable sebagai key** map atau elemen set.

### 1.13 Memilih Collection

| Kebutuhan | Pilihan |
|---|---|
| Daftar berurutan, akses by index | `ArrayList` |
| Tanpa duplikat, urutan tidak penting | `HashSet` |
| Tanpa duplikat, urutan insert terjaga | `LinkedHashSet` |
| Tanpa duplikat, selalu terurut | `TreeSet` |
| Lookup cepat by key | `HashMap` |
| Lookup by key + urutan insert | `LinkedHashMap` |
| Lookup by key + key terurut | `TreeMap` |
| Antrean FIFO | `ArrayDeque` (sebagai `Queue`) |
| Stack LIFO | `ArrayDeque` (sebagai `Deque`) |
| Ambil elemen terkecil/prioritas | `PriorityQueue` |

### 1.14 Ringkasan Kompleksitas

| Struktur | Cari/`contains` | Tambah | Hapus | Catatan |
|---|---|---|---|---|
| `ArrayList` | O(n) | O(1) akhir, O(n) tengah | O(n) | `get(i)` O(1) |
| `LinkedList` | O(n) | O(1) ujung | O(1) di ujung | `get(i)` O(n) |
| `HashSet` / `HashMap` | **O(1)** rata-rata | O(1) | O(1) | tak terurut |
| `TreeSet` / `TreeMap` | O(log n) | O(log n) | O(log n) | terurut |
| `ArrayDeque` | O(n) | O(1) kedua ujung | O(1) kedua ujung | |
| `PriorityQueue` | O(n) | O(log n) | O(log n) `poll` | `peek` O(1) |

Insight praktis: mengecek keberadaan di **list** itu O(n). Kalau kamu sering melakukan `contains` pada data besar, ubah ke **`HashSet`** atau pakai **`HashMap`** untuk lookup. Ini salah satu optimasi paling umum dan paling berdampak.

---

## 2. Praktek

> Semua contoh dalam satu file `Main.java`. Tambahkan `import java.util.*;` di atas.

### Praktek 2.1: `List`

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> fruits = new ArrayList<>();
        fruits.add("apple");
        fruits.add("banana");
        fruits.add("cherry");
        fruits.add(1, "avocado");

        System.out.println(fruits);                      // [apple, avocado, banana, cherry]
        System.out.println(fruits.get(2));               // banana
        System.out.println(fruits.indexOf("cherry"));    // 3
        System.out.println(fruits.contains("mango"));    // false

        fruits.remove("apple");
        fruits.set(0, "AVOCADO");
        System.out.println(fruits);                      // [AVOCADO, banana, cherry]

        for (String f : fruits) {
            System.out.println("- " + f);
        }

        Collections.sort(fruits);
        System.out.println(fruits);                      // [AVOCADO, banana, cherry]
        Collections.reverse(fruits);
        System.out.println(fruits);                      // [cherry, banana, AVOCADO]
    }
}
```

### Praktek 2.2: Jebakan `remove` dan `Integer`

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 1, 2));

        list.remove(1);                       // by index
        System.out.println(list);             // [10, 30, 1, 2]

        list.remove(Integer.valueOf(1));      // by value
        System.out.println(list);             // [10, 30, 2]

        Integer a = 127, b = 127;
        Integer c = 128, d = 128;
        System.out.println(a == b);           // true
        System.out.println(c == d);           // false
        System.out.println(c.equals(d));      // true

        List<Integer> fixed = List.of(1, 2, 3);
        try {
            fixed.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of is immutable");
        }
    }
}
```

### Praktek 2.3: `ConcurrentModificationException` dan Perbaikannya

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

        try {
            for (Integer n : nums) {
                if (n % 2 == 0) {
                    nums.remove(n);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("CME caught");
        }

        List<Integer> fixed = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        fixed.removeIf(n -> n % 2 == 0);
        System.out.println(fixed);            // [1, 3, 5]

        List<Integer> viaIterator = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = viaIterator.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) {
                it.remove();
            }
        }
        System.out.println(viaIterator);      // [1, 3, 5]
    }
}
```

### Praktek 2.4: Sorting Object dengan `Comparable` dan `Comparator`

```java
import java.util.*;

class Student implements Comparable<Student> {
    private final String name;
    private final double gpa;

    Student(String name, double gpa) {
        this.name = name;
        this.gpa = gpa;
    }

    public String getName() { return name; }
    public double getGpa() { return gpa; }

    @Override
    public int compareTo(Student other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name + "(" + gpa + ")";
    }
}

public class Main {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
            new Student("Sari", 3.8),
            new Student("Budi", 3.5),
            new Student("Andi", 3.8),
            new Student("Dewi", 3.2)
        ));

        Collections.sort(students);                                  // natural: by name
        System.out.println(students);   // [Andi(3.8), Budi(3.5), Dewi(3.2), Sari(3.8)]

        students.sort(Comparator.comparingDouble(Student::getGpa));
        System.out.println(students);   // ascending GPA

        students.sort(Comparator.comparingDouble(Student::getGpa).reversed()
                .thenComparing(Student::getName));
        System.out.println(students);   // [Andi(3.8), Sari(3.8), Budi(3.5), Dewi(3.2)]
    }
}
```

### Praktek 2.5: `Set` dan Operasi Himpunan

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> raw = List.of("java", "spring", "java", "sql", "spring", "react");

        Set<String> hash = new HashSet<>(raw);
        Set<String> linked = new LinkedHashSet<>(raw);
        Set<String> tree = new TreeSet<>(raw);

        System.out.println("Hash  : " + hash);     // order not guaranteed
        System.out.println("Linked: " + linked);   // [java, spring, sql, react]
        System.out.println("Tree  : " + tree);     // [java, react, spring, sql]

        System.out.println(linked.add("java"));    // false
        System.out.println(linked.add("docker"));  // true

        Set<Integer> a = new TreeSet<>(Set.of(1, 2, 3, 4));
        Set<Integer> b = new TreeSet<>(Set.of(3, 4, 5));

        Set<Integer> union = new TreeSet<>(a);
        union.addAll(b);
        Set<Integer> inter = new TreeSet<>(a);
        inter.retainAll(b);
        Set<Integer> diff = new TreeSet<>(a);
        diff.removeAll(b);

        System.out.println("Union       : " + union);   // [1, 2, 3, 4, 5]
        System.out.println("Intersection: " + inter);   // [3, 4]
        System.out.println("Difference  : " + diff);    // [1, 2]
    }
}
```

### Praktek 2.6: `Set` dengan `equals`/`hashCode`

```java
import java.util.*;

class PointNoEquals {
    final int x, y;
    PointNoEquals(int x, int y) { this.x = x; this.y = y; }
}

class Point {
    final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

public class Main {
    public static void main(String[] args) {
        Set<PointNoEquals> s1 = new HashSet<>();
        s1.add(new PointNoEquals(1, 2));
        s1.add(new PointNoEquals(1, 2));
        System.out.println(s1.size());   // 2 (treated as different objects)

        Set<Point> s2 = new HashSet<>();
        s2.add(new Point(1, 2));
        s2.add(new Point(1, 2));
        System.out.println(s2.size());   // 1
        System.out.println(s2.contains(new Point(1, 2)));   // true
    }
}
```

### Praktek 2.7: `Map`

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("pen", 10);
        stock.put("book", 5);
        stock.put("bag", 2);
        stock.put("pen", 12);                      // replace

        System.out.println(stock.get("pen"));                // 12
        System.out.println(stock.get("laptop"));             // null
        System.out.println(stock.getOrDefault("laptop", 0)); // 0
        System.out.println(stock.containsKey("book"));       // true

        for (Map.Entry<String, Integer> e : new TreeMap<>(stock).entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        // counting characters with merge
        Map<Character, Integer> freq = new TreeMap<>();
        for (char c : "mississippi".toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        System.out.println(freq);   // {i=4, m=1, p=2, s=4}
    }
}
```

### Praktek 2.8: Grouping dengan `computeIfAbsent`

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> words = List.of("java", "go", "rust", "c", "kotlin", "sql", "php");

        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : words) {
            byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
        }

        for (Map.Entry<Integer, List<String>> e : byLength.entrySet()) {
            System.out.println(e.getKey() + " letters: " + e.getValue());
        }
        // 1 letters: [c]
        // 2 letters: [go]
        // 3 letters: [sql, php]
        // 4 letters: [java, rust]
        // 6 letters: [kotlin]
    }
}
```

### Praktek 2.9: `Queue` dan `Deque` (Stack)

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("Budi");
        queue.offer("Sari");
        queue.offer("Andi");
        System.out.println(queue.peek());   // Budi
        System.out.println(queue.poll());   // Budi
        System.out.println(queue.poll());   // Sari
        System.out.println(queue);          // [Andi]

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.peek());   // 3
        System.out.println(stack.pop());    // 3
        System.out.println(stack.pop());    // 2
        System.out.println(stack);          // [1]

        Queue<Integer> pq = new PriorityQueue<>();
        pq.offer(5); pq.offer(1); pq.offer(3); pq.offer(2);
        StringBuilder sb = new StringBuilder();
        while (!pq.isEmpty()) {
            sb.append(pq.poll()).append(' ');
        }
        System.out.println(sb.toString().trim());   // 1 2 3 5
    }
}
```

### Praktek 2.10: View vs Copy dan Defensive Copy

```java
import java.util.*;

class Course {
    private final List<String> students = new ArrayList<>();

    void enroll(String name) { students.add(name); }

    List<String> getStudents() {
        return List.copyOf(students);    // defensive copy, immutable
    }
}

public class Main {
    public static void main(String[] args) {
        Course course = new Course();
        course.enroll("Budi");
        course.enroll("Sari");

        List<String> list = course.getStudents();
        try {
            list.add("Hacker");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify internal state");
        }

        List<String> original = new ArrayList<>(List.of("a", "b"));
        List<String> view = Collections.unmodifiableList(original);
        List<String> copy = List.copyOf(original);
        original.add("c");
        System.out.println(view);   // [a, b, c]
        System.out.println(copy);   // [a, b]
    }
}
```

---

## 3. Latihan

**Soal 1.** Diberikan `List<Integer> nums = List.of(4, 8, 15, 8, 16, 23, 4, 42, 15)`.
- Buat list baru tanpa duplikat dengan urutan kemunculan pertama dipertahankan
- Cetak jumlah, rata-rata (2 digit desimal), nilai terbesar, dan nilai terkecil (boleh pakai `Collections`)
- Cetak list terurut descending

**Soal 2.** Prediksi output:

```java
List<Integer> list = new ArrayList<>(List.of(5, 10, 15, 20, 1));
list.remove(1);
list.remove(Integer.valueOf(1));
System.out.println(list);

Integer a = 100, b = 100;
Integer c = 1000, d = 1000;
System.out.println(a == b);
System.out.println(c == d);
System.out.println(c.equals(d));

Map<String, Integer> m = new HashMap<>();
m.put("x", 1);
m.put("x", 2);
System.out.println(m.size() + " " + m.get("x"));
System.out.println(m.get("y"));
```

**Soal 3.** Word frequency: dari kalimat `"the quick brown fox jumps over the lazy dog the end"`, hitung frekuensi tiap kata memakai `Map`. Cetak hasilnya (a) terurut berdasarkan **kata** (alfabet), lalu (b) tentukan kata yang **paling sering** muncul. (Bandingkan dengan solusi dua array di materi 0105.)

**Soal 4.** Buat class `Student` (`name`, `gpa`). Buat list berisi minimal 5 student, lalu urutkan dengan:
- (a) GPA terbesar dulu, kalau sama urutkan nama A-Z
- (b) Nama A-Z saja

Pakai `Comparator`.

**Soal 5.** Diberikan dua set `a = {1, 2, 3, 4, 5}` dan `b = {4, 5, 6, 7}`, hitung dan cetak: union, intersection, difference (`a - b`), dan **symmetric difference** (ada di salah satu tapi tidak di keduanya).

**Soal 6.** Buat method `isBalanced(String s)` yang mengecek apakah kurung `()`, `[]`, `{}` seimbang memakai `Deque` sebagai stack.
Contoh: `"{[()]}"` → `true`, `"([)]"` → `false`, `"((("` → `false`.

**Soal 7.** Dari list kata `["apple", "avocado", "banana", "blueberry", "cherry", "apricot", "cranberry"]`, kelompokkan berdasarkan **huruf pertama** menjadi `Map<Character, List<String>>` terurut. Cetak hasilnya.

**Soal 8.** Kode berikut melempar exception. Jelaskan kenapa dan perbaiki dengan **dua cara**:

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
for (Integer n : nums) {
    if (n % 2 == 0) {
        nums.remove(n);
    }
}
```

**Soal 9 (Bonus).** Buat class `Inventory` dengan `Map<String, Integer>` (item → stok, terurut alfabet). Method:
- `add(String item, int qty)`: `qty <= 0` melempar `IllegalArgumentException`; jika item sudah ada, stok ditambah
- `remove(String item, int qty)`: item tidak ada → `IllegalArgumentException`; stok kurang → `IllegalStateException`; jika stok jadi `0`, hapus item dari map
- `getStock(String item)`: kembalikan `0` jika tidak ada
- `report()`: cetak semua item dan stoknya

**Soal 10 (Bonus).** *Two Sum*: diberikan `int[] nums` dan `int target`, kembalikan **indeks** dua angka yang jumlahnya `target` (anggap pasti ada tepat satu solusi). Contoh: `nums = {2, 7, 11, 15}`, `target = 9` → `[0, 1]`. Selesaikan dalam **satu kali lewat** array memakai `HashMap` (O(n)), lalu jelaskan kenapa solusi dua loop bersarang lebih lambat.

**Soal 11 (Bonus).** Buat class `Student` dengan `id` (`int`) dan `name`. Masukkan dua object `Student` dengan `id` dan `name` sama ke `HashSet`. Tunjukkan bahwa `size()` bernilai 2 **sebelum** meng-override `equals`/`hashCode`, lalu 1 **sesudahnya**. Kenapa `hashCode` juga harus di-override, bukan hanya `equals`?

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> nums = List.of(4, 8, 15, 8, 16, 23, 4, 42, 15);

        List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(nums));
        System.out.println("Unique : " + unique);   // [4, 8, 15, 16, 23, 42]

        int sum = 0;
        for (int n : nums) {
            sum += n;
        }
        double average = (double) sum / nums.size();

        System.out.println("Sum    : " + sum);                       // 135
        System.out.printf("Average: %.2f%n", average);               // 15.00
        System.out.println("Max    : " + Collections.max(nums));     // 42
        System.out.println("Min    : " + Collections.min(nums));     // 4

        List<Integer> sorted = new ArrayList<>(nums);                // copy: List.of is immutable
        sorted.sort(Comparator.reverseOrder());
        System.out.println("Desc   : " + sorted);   // [42, 23, 16, 15, 15, 8, 8, 4, 4]
    }
}
```

`LinkedHashSet` adalah trik standar untuk menghapus duplikat **sambil menjaga urutan**. Perhatikan `new ArrayList<>(nums)` sebelum sorting: `List.of(...)` immutable, jadi `sort` langsung pada `nums` akan melempar `UnsupportedOperationException`.

### Jawaban Soal 2

```
[5, 15, 20]
true
false
true
1 2
null
```

- `list.remove(1)` menghapus **index 1** (nilai `10`) → `[5, 15, 20, 1]`. Lalu `remove(Integer.valueOf(1))` menghapus **nilai 1** → `[5, 15, 20]`.
- `100` berada di cache `Integer` (-128..127), jadi `a == b` bernilai `true`. `1000` di luar cache sehingga dua object berbeda: `c == d` bernilai `false`. `c.equals(d)` bernilai `true`. **Selalu pakai `.equals()` untuk wrapper.**
- `put("x", 2)` pada key yang sama **menimpa** nilai, jadi size tetap `1` dan value `2`.
- `m.get("y")` mengembalikan `null` (key tidak ada), dan mencetak `null` tidak melempar error. Error baru muncul bila hasilnya di-unboxing ke `int`.

### Jawaban Soal 3

```java
import java.util.*;

public class WordFrequency {
    public static void main(String[] args) {
        String sentence = "the quick brown fox jumps over the lazy dog the end";
        String[] words = sentence.split("\\s+");

        Map<String, Integer> freq = new TreeMap<>();    // TreeMap: keys sorted alphabetically
        for (String w : words) {
            freq.merge(w, 1, Integer::sum);
        }

        // (a) sorted by word
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }

        // (b) the most frequent word
        String topWord = null;
        int topCount = 0;
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() > topCount) {
                topWord = e.getKey();
                topCount = e.getValue();
            }
        }
        System.out.println("Most frequent: " + topWord + " (" + topCount + ")");   // the (3)
    }
}
```

Bandingkan dengan solusi dua array paralel di materi 0105: di sini seluruh logika pencarian-lalu-tambah cukup satu baris `merge`. Itulah kenapa `Map` ada.

### Jawaban Soal 4

```java
import java.util.*;

class Student {
    private final String name;
    private final double gpa;

    Student(String name, double gpa) {
        this.name = name;
        this.gpa = gpa;
    }

    public String getName() { return name; }
    public double getGpa() { return gpa; }

    @Override
    public String toString() { return name + "(" + gpa + ")"; }
}

public class Main {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
            new Student("Sari", 3.8),
            new Student("Budi", 3.5),
            new Student("Andi", 3.8),
            new Student("Dewi", 3.2),
            new Student("Citra", 3.5)
        ));

        // (a) GPA descending, then name ascending
        students.sort(Comparator.comparingDouble(Student::getGpa).reversed()
                .thenComparing(Student::getName));
        System.out.println(students);   // [Andi(3.8), Sari(3.8), Budi(3.5), Citra(3.5), Dewi(3.2)]

        // (b) name ascending
        students.sort(Comparator.comparing(Student::getName));
        System.out.println(students);   // [Andi(3.8), Budi(3.5), Citra(3.5), Dewi(3.2), Sari(3.8)]
    }
}
```

`reversed()` hanya membalik komparator **sebelumnya** (GPA), lalu `thenComparing` ditambahkan sebagai pemutus seri dengan urutan normal (A-Z). Urutan pemanggilan menentukan hasil.

### Jawaban Soal 5

```java
import java.util.*;

public class SetOps {
    public static void main(String[] args) {
        Set<Integer> a = new TreeSet<>(Set.of(1, 2, 3, 4, 5));
        Set<Integer> b = new TreeSet<>(Set.of(4, 5, 6, 7));

        Set<Integer> union = new TreeSet<>(a);
        union.addAll(b);

        Set<Integer> intersection = new TreeSet<>(a);
        intersection.retainAll(b);

        Set<Integer> difference = new TreeSet<>(a);
        difference.removeAll(b);

        // symmetric difference = union - intersection
        Set<Integer> symmetric = new TreeSet<>(union);
        symmetric.removeAll(intersection);

        System.out.println("Union       : " + union);          // [1, 2, 3, 4, 5, 6, 7]
        System.out.println("Intersection: " + intersection);   // [4, 5]
        System.out.println("Difference  : " + difference);     // [1, 2, 3]
        System.out.println("Symmetric   : " + symmetric);      // [1, 2, 3, 6, 7]
    }
}
```

Selalu **salin dulu** (`new TreeSet<>(a)`) sebelum `addAll/retainAll/removeAll`, karena method-method itu **mengubah** set yang dipanggil, dan kamu masih butuh `a` dan `b` utuh.

### Jawaban Soal 6

```java
import java.util.*;

public class BalancedParentheses {

    public static boolean isBalanced(String s) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            switch (c) {
                case '(', '[', '{' -> stack.push(c);
                case ')', ']', '}' -> {
                    if (stack.isEmpty()) {
                        return false;               // closing with nothing open
                    }
                    char open = stack.pop();
                    if (!matches(open, c)) {
                        return false;
                    }
                }
                default -> { }                      // ignore other characters
            }
        }
        return stack.isEmpty();                     // anything left open means unbalanced
    }

    private static boolean matches(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }

    public static void main(String[] args) {
        System.out.println(isBalanced("{[()]}"));   // true
        System.out.println(isBalanced("([)]"));     // false
        System.out.println(isBalanced("((("));      // false
        System.out.println(isBalanced("a(b)c"));    // true
        System.out.println(isBalanced(")("));       // false
        System.out.println(isBalanced(""));         // true
    }
}
```

Ide intinya: kurung buka **ditumpuk**, kurung tutup harus cocok dengan **yang paling atas** (LIFO). Ini contoh klasik kenapa stack ada. Ada dua kondisi gagal yang mudah terlupa: stack kosong saat menemui kurung tutup, dan stack tidak kosong di akhir.

### Jawaban Soal 7

```java
import java.util.*;

public class GroupByFirstLetter {
    public static void main(String[] args) {
        List<String> words = List.of(
            "apple", "avocado", "banana", "blueberry", "cherry", "apricot", "cranberry");

        Map<Character, List<String>> groups = new TreeMap<>();
        for (String w : words) {
            groups.computeIfAbsent(w.charAt(0), k -> new ArrayList<>()).add(w);
        }

        for (Map.Entry<Character, List<String>> e : groups.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}
```

Output:

```
a -> [apple, avocado, apricot]
b -> [banana, blueberry]
c -> [cherry, cranberry]
```

`computeIfAbsent` artinya: "kalau key belum ada, buat list kosong dulu dan simpan; lalu kembalikan list itu". Tanpa itu kamu harus menulis `if (!map.containsKey(k)) map.put(k, new ArrayList<>());` setiap kali.

### Jawaban Soal 8

Penyebab: for-each memakai `Iterator` di balik layar. Saat `nums.remove(n)` mengubah list dari luar iterator, `modCount` berubah, dan pada `next()` berikutnya iterator mendeteksi perubahan itu lalu melempar **`ConcurrentModificationException`** (fail-fast).

**Cara 1: `removeIf`** (paling ringkas, direkomendasikan):

```java
nums.removeIf(n -> n % 2 == 0);
System.out.println(nums);   // [1, 3, 5]
```

**Cara 2: `Iterator.remove()`** (satu-satunya cara aman menghapus sambil iterasi manual):

```java
Iterator<Integer> it = nums.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) {
        it.remove();
    }
}
```

**Cara 3 (alternatif): bangun list baru berisi yang dipertahankan**:

```java
List<Integer> odds = new ArrayList<>();
for (Integer n : nums) {
    if (n % 2 != 0) {
        odds.add(n);
    }
}
```

Catatan: ada kasus langka di mana CME **tidak** muncul (misalnya menghapus elemen kedua dari belakang), tapi itu kebetulan, bukan jaminan. Jangan pernah bergantung pada itu.

### Jawaban Soal 9

```java
import java.util.*;

class Inventory {
    private final Map<String, Integer> stock = new TreeMap<>();

    public void add(String item, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + qty);
        }
        stock.merge(item, qty, Integer::sum);
    }

    public void remove(String item, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + qty);
        }
        Integer current = stock.get(item);
        if (current == null) {
            throw new IllegalArgumentException("Unknown item: " + item);
        }
        if (current < qty) {
            throw new IllegalStateException(
                    "Insufficient stock for " + item + ": available=" + current + ", requested=" + qty);
        }
        if (current == qty) {
            stock.remove(item);
        } else {
            stock.put(item, current - qty);
        }
    }

    public int getStock(String item) {
        return stock.getOrDefault(item, 0);
    }

    public void report() {
        if (stock.isEmpty()) {
            System.out.println("(empty inventory)");
            return;
        }
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            System.out.printf("%-10s %d%n", e.getKey(), e.getValue());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Inventory inv = new Inventory();
        inv.add("pen", 10);
        inv.add("book", 5);
        inv.add("pen", 5);        // pen = 15

        inv.remove("book", 5);    // book reaches 0 -> removed from the map
        inv.report();             // pen 15

        System.out.println(inv.getStock("book"));   // 0

        try {
            inv.remove("pen", 100);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        try {
            inv.remove("laptop", 1);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

Perhatikan: `Integer current = stock.get(item)` memakai **`Integer`** (bukan `int`) supaya hasil `null` bisa dicek. Memakai `int current = stock.get(item)` akan melempar NPE saat item tidak ada (jebakan unboxing). Perbandingan `current == qty` aman karena salah satu sisi adalah primitive `int` (maka `current` otomatis di-unbox, bukan membandingkan reference).

### Jawaban Soal 10

```java
import java.util.*;

public class TwoSum {

    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();   // value -> index

        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            Integer j = seen.get(complement);
            if (j != null) {
                return new int[]{j, i};
            }
            seen.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two sum solution for target " + target);
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));   // [0, 1]
        System.out.println(Arrays.toString(twoSum(new int[]{3, 2, 4}, 6)));        // [1, 2]
        System.out.println(Arrays.toString(twoSum(new int[]{3, 3}, 6)));           // [0, 1]
    }
}
```

Cara kerja: untuk setiap angka, hitung "pasangan yang dibutuhkan" (`target - nums[i]`). Kalau pasangan itu sudah pernah terlihat, ketemu. Kalau belum, catat angka sekarang beserta indeksnya.

Kenapa dua loop bersarang lebih lambat: untuk setiap elemen, loop dalam memeriksa **semua** elemen lain, jadi sekitar `n × n` perbandingan = **O(n²)**. Dengan `HashMap`, tiap pencarian pasangan adalah **O(1)** rata-rata sehingga totalnya **O(n)**. Untuk `n = 100.000`, itu sekitar 100 ribu langkah vs 10 miliar langkah. Ini contoh klasik **menukar memory dengan waktu**.

### Jawaban Soal 11

```java
import java.util.*;

class StudentNoEquals {
    final int id;
    final String name;
    StudentNoEquals(int id, String name) { this.id = id; this.name = name; }
}

class Student {
    final int id;
    final String name;
    Student(int id, String name) { this.id = id; this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student s = (Student) o;
        return id == s.id && name.equals(s.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

public class Main {
    public static void main(String[] args) {
        Set<StudentNoEquals> before = new HashSet<>();
        before.add(new StudentNoEquals(1, "Budi"));
        before.add(new StudentNoEquals(1, "Budi"));
        System.out.println(before.size());   // 2

        Set<Student> after = new HashSet<>();
        after.add(new Student(1, "Budi"));
        after.add(new Student(1, "Budi"));
        System.out.println(after.size());    // 1
    }
}
```

Kenapa `hashCode` juga wajib: `HashSet` bekerja dalam **dua langkah**. Pertama memakai `hashCode()` untuk memilih "keranjang" (bucket), baru kemudian `equals()` untuk membandingkan isi keranjang itu. Kalau hanya `equals` yang di-override, dua object yang sama isinya tetap punya `hashCode` berbeda (dari `Object`), sehingga **jatuh ke keranjang berbeda** dan `equals` bahkan tidak pernah dipanggil. Hasilnya set menganggap keduanya berbeda, dan `contains` pun bisa gagal. Karena itu kontraknya: **object yang `equals` harus memiliki `hashCode` sama**.
