# Modul 10: Concurrency: Thread, Virtual Thread & Java Memory Model

[← Modul 9: Functional Programming: Lambda & Stream API](09-functional-programming.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 11: Date & Time, Regex, Cryptography & Networking →](11-date-time-regex-crypto-networking.md)

**Isi modul ini**

- [Teori](#teori)
  - [10.1 Konsep Dasar Concurrency](#101-konsep-dasar-concurrency)
  - [10.2 Threads](#102-threads)
  - [10.3 Race Condition dan Synchronization](#103-race-condition-dan-synchronization)
  - [10.4 `java.util.concurrent` — Perkakas Tingkat Tinggi](#104-javautilconcurrent--perkakas-tingkat-tinggi)
  - [10.5 Java Memory Model dan `volatile`](#105-java-memory-model-dan-volatile)
  - [10.6 Virtual Threads (Java 21+)](#106-virtual-threads-java-21)
  - [10.7 Structured Concurrency dan Scoped Values (Java 25)](#107-structured-concurrency-dan-scoped-values-java-25)
- [Praktik](#praktik)
  - [Membuat Thread, `join`, `interrupt`, dan Status Thread](#membuat-thread-join-interrupt-dan-status-thread)
  - [Race Condition vs `synchronized` vs `Atomic`](#race-condition-vs-synchronized-vs-atomic)
  - [ExecutorService, Future, dan CompletableFuture](#executorservice-future-dan-completablefuture)
  - [Producer–Consumer dengan `BlockingQueue`](#producerconsumer-dengan-blockingqueue)
  - [`volatile` untuk Flag Penghentian](#volatile-untuk-flag-penghentian)
  - [Virtual Threads: 10.000 Tugas Menunggu Bersamaan](#virtual-threads-10000-tugas-menunggu-bersamaan)
  - [Deadlock dan Cara Menghindarinya (Konseptual)](#deadlock-dan-cara-menghindarinya-konseptual)
- [Latihan](#latihan)

---


## Teori

### 10.1 Konsep Dasar Concurrency

- **Process** — program yang sedang berjalan dengan ruang memory sendiri. **Thread** — jalur eksekusi *di dalam* sebuah process; semua thread dalam satu process **berbagi heap** (karena itu murah tetapi rawan konflik data).
- **Concurrency** — beberapa tugas **berjalan bergantian/tumpang-tindih** (satu core pun bisa). **Parallelism** — beberapa tugas **benar-benar berjalan bersamaan** di banyak core.
- **CPU-bound** — pekerjaan dibatasi kecepatan CPU (enkripsi, kompresi, perhitungan). **I/O-bound** — pekerjaan banyak menunggu (HTTP, database, disk). Perbedaan ini menentukan strategi: CPU-bound butuh thread sebanyak core; I/O-bound bisa memiliki ribuan tugas menunggu bersamaan (inilah alasan *virtual thread*, 10.6).

Mengapa thread? Agar aplikasi **responsif** (UI tak membeku), memanfaatkan **banyak core**, dan melayani **banyak request** sekaligus — seperti server web yang Anda bangun di [Modul 13](13-dependency-injection-web-framework.md).

### 10.2 Threads

Cara paling sederhana membuat thread adalah memberi `Runnable` (lambda) ke `Thread`:

```java
Thread t = new Thread(() -> System.out.println("Berjalan di " + Thread.currentThread().getName()));
t.start();      // MEMULAI thread baru. (t.run() hanya memanggil method biasa di thread saat ini!)
t.join();       // tunggu t selesai
```

**Siklus hidup thread** (`Thread.State`): `NEW` → `RUNNABLE` → (`BLOCKED` menunggu lock / `WAITING` menunggu tanpa batas waktu / `TIMED_WAITING` mis. `sleep`) → `TERMINATED`.

| Method | Fungsi |
|---|---|
| `start()` | Mulai thread (hanya sekali per object) |
| `join()` | Tunggu thread lain selesai |
| `Thread.sleep(ms)` | Tidur sementara thread saat ini |
| `interrupt()` | Meminta thread berhenti — mekanisme **kooperatif**; thread yang sedang `sleep`/`wait` menerima `InterruptedException` |
| `setDaemon(true)` | Thread latar belakang yang tidak menahan JVM untuk berhenti |

Penghentian thread di Java bersifat **kooperatif**: thread harus memeriksa `isInterrupted()` atau menangani `InterruptedException` lalu berhenti sendiri. Jangan menelan `InterruptedException` — pulihkan status dengan `Thread.currentThread().interrupt()` atau teruskan.

### 10.3 Race Condition dan Synchronization

**Race condition** terjadi ketika hasil program bergantung pada **urutan eksekusi thread** yang tidak terduga, biasanya karena beberapa thread membaca-mengubah data bersama. Contoh klasik: `counter++` terlihat satu operasi, padahal sebenarnya tiga langkah — **baca → tambah → tulis**. Dua thread bisa membaca nilai yang sama dan menulis balik, sehingga satu penambahan **hilang**.

Solusinya adalah **synchronization** — memastikan hanya satu thread sekaligus yang mengeksekusi bagian kritis:

```java
class Counter {
    private int nilai;
    synchronized void tambah() { nilai++; }       // memakai intrinsic lock milik object ini
    synchronized int baca()    { return nilai; }
}

// atau blok yang lebih sempit:
synchronized (lock) { saldo -= jumlah; }
```

Setiap object Java memiliki **intrinsic lock (monitor)**. `synchronized` menjamin dua hal: **mutual exclusion** (satu thread sekaligus) dan **visibility** (perubahan terlihat oleh thread berikutnya yang mengambil lock yang sama — lihat 10.5).

**Deadlock** terjadi ketika dua thread saling menunggu lock yang dipegang lawannya selamanya:

```
Thread 1: kunci A → menunggu B
Thread 2: kunci B → menunggu A      ← keduanya macet selamanya
```

Pencegahan: selalu ambil lock dalam **urutan yang konsisten**, jaga bagian kritis sekecil mungkin, hindari memanggil kode asing sambil memegang lock, dan gunakan `tryLock` dengan timeout bila memakai `ReentrantLock`.

### 10.4 `java.util.concurrent` — Perkakas Tingkat Tinggi

Hampir selalu lebih baik memakai library `java.util.concurrent` daripada mengelola `Thread` manual.

**ExecutorService** — kumpulan thread (*thread pool*) yang menjalankan tugas yang Anda kirim:

| Factory | Perilaku |
|---|---|
| `Executors.newFixedThreadPool(n)` | n thread tetap — cocok untuk tugas CPU-bound (n ≈ jumlah core) |
| `Executors.newCachedThreadPool()` | Tumbuh sesuai kebutuhan, hati-hati bisa membludak |
| `Executors.newSingleThreadExecutor()` | Satu thread, tugas berurutan |
| `Executors.newVirtualThreadPerTaskExecutor()` | **Satu virtual thread per tugas** (10.6) |

`submit(Callable<T>)` mengembalikan **`Future<T>`**: `get()` menunggu hasil (atau exception), `cancel()` membatalkan. Sejak Java 19, `ExecutorService` adalah `AutoCloseable` sehingga dapat dipakai di try-with-resources (`close()` menunggu semua tugas selesai). Pada versi lama, wajib memanggil `shutdown()`.

**CompletableFuture** — pipeline asinkron yang dapat dirangkai (mirip stream, tapi untuk hasil yang datang di masa depan):

```java
CompletableFuture<String> f = CompletableFuture
        .supplyAsync(() -> ambilNamaPengguna())            // jalan di thread lain
        .thenApply(String::toUpperCase)                    // transformasi
        .thenCombine(CompletableFuture.supplyAsync(() -> ambilKota()), (nama, kota) -> nama + " dari " + kota)
        .exceptionally(e -> "gagal: " + e.getMessage());   // penanganan error
```

**Struktur data & sinkronisasi thread-safe:**

| Perkakas | Fungsi |
|---|---|
| `ConcurrentHashMap` | Map thread-safe berkinerja tinggi (kunci/segmen terkunci halus); punya `merge`, `compute` atomik |
| `CopyOnWriteArrayList` | List thread-safe, cocok untuk **banyak baca, sedikit tulis** |
| `BlockingQueue` (`ArrayBlockingQueue`, `LinkedBlockingQueue`) | Antrean untuk pola **producer–consumer**; `put` menunggu jika penuh, `take` menunggu jika kosong |
| `CountDownLatch` | Tunggu sampai N kejadian selesai |
| `Semaphore` | Batasi jumlah thread yang boleh mengakses resource sekaligus |
| `ReentrantLock` | Lock eksplisit: `tryLock`, timeout, `lockInterruptibly`, condition |
| `AtomicInteger`, `AtomicLong`, `AtomicReference` | Operasi atomik **tanpa lock** (CAS — *compare-and-swap*) |
| `LongAdder` | Counter berkinerja tinggi untuk banyak thread penulis |

### 10.5 Java Memory Model dan `volatile`

**Java Memory Model (JMM)** adalah spesifikasi yang mendefinisikan **kapan perubahan sebuah thread dijamin terlihat oleh thread lain**. Latar belakangnya: CPU modern memiliki **cache per core**, dan compiler/CPU boleh **menyusun ulang instruksi** untuk optimasi. Tanpa aturan, thread B bisa terus membaca nilai **basi** yang sudah diubah thread A.

JMM mendefinisikan relasi **happens-before**: jika aksi A *happens-before* aksi B, maka efek A dijamin terlihat oleh B. Jaminan yang paling penting:

- **Program order** — dalam satu thread, urutan kode berlaku.
- **Monitor lock** — melepas lock (`unlock`/keluar `synchronized`) *happens-before* thread lain mengambil lock yang sama.
- **`volatile`** — menulis variabel volatile *happens-before* setiap pembacaan berikutnya atas variabel itu.
- **Thread start/join** — semua aksi sebelum `t.start()` terlihat di dalam `t`; semua aksi di `t` terlihat setelah `t.join()` kembali.
- **Field `final`** — object yang dibangun dengan benar aman dibagikan antar thread setelah constructor selesai (*safe publication*).

**`volatile`** menjamin dua hal: **visibility** (tulis langsung terlihat thread lain) dan **ordering** (mencegah reordering di sekitarnya). **Tetapi `volatile` tidak menjamin atomicity** — `volatile int n; n++` **tetap** race condition. Gunakan `volatile` untuk **flag status** (`volatile boolean berjalan`) atau publikasi referensi immutable; untuk operasi baca-ubah-tulis, pakai `Atomic*` atau `synchronized`.

```java
class Worker implements Runnable {
    private volatile boolean berjalan = true;     // tanpa volatile, loop bisa tak pernah melihat perubahan
    void hentikan() { berjalan = false; }
    public void run() { while (berjalan) { /* kerja */ } }
}
```

### 10.6 Virtual Threads (Java 21+)

Thread tradisional (**platform thread**) memetakan **1:1** ke thread sistem operasi — mahal (sekitar ratusan KB–1 MB stack) sehingga jumlahnya terbatas (ribuan). Untuk server yang melayani puluhan ribu koneksi I/O, model "satu thread per request" menjadi mahal.

**Virtual thread** (final di **Java 21**, hasil **Project Loom**) adalah thread **ringan yang dikelola JVM**, bukan OS. Jutaan virtual thread dapat dibuat; saat sebuah virtual thread **menunggu I/O**, JVM melepaskannya dari *carrier thread* (platform thread pembawa) sehingga carrier dapat melayani virtual thread lain. Anda menulis kode **blocking biasa yang sederhana**, tetapi mendapat skalabilitas mendekati model asinkron.

```java
Thread vt = Thread.ofVirtual().name("vt-1").start(() -> System.out.println("virtual!"));
vt.join();

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {   // satu virtual thread per tugas
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> { Thread.sleep(Duration.ofSeconds(1)); return "selesai"; });
    }
}   // close() menunggu semua tugas selesai
```

**Pedoman:**

- **Cocok** untuk beban **I/O-bound** dengan banyak tugas bersamaan (web server, pemanggilan HTTP/database).
- **Tidak mempercepat tugas CPU-bound** — jumlah core tetap pembatasnya.
- **Jangan di-pool.** Virtual thread murah; buat satu per tugas. Untuk membatasi konkurensi pada resource terbatas (misal koneksi database), pakai `Semaphore`, bukan pool kecil.
- Hindari menahan `synchronized` lama sambil melakukan I/O pada Java 21–23 (menyebabkan *pinning* — carrier ikut terblokir). **Java 24 menghilangkan pinning pada `synchronized`** (JEP 491); di Java 21 pertimbangkan `ReentrantLock`.
- Gunakan `ThreadLocal` dengan hemat — dengan jutaan thread, memorinya bisa membengkak.
- Framework modern mendukungnya: di Spring Boot cukup `spring.threads.virtual.enabled=true`.

### 10.7 Structured Concurrency dan Scoped Values (Java 25)

Dua fitur hasil Project Loom yang melengkapi virtual thread:

- **`ScopedValue`** (final di **Java 25**) — cara aman membagikan data **immutable** (misal identitas pengguna, trace id) ke seluruh rantai pemanggilan dalam satu *scope*, sebagai pengganti `ThreadLocal` yang lebih ringan dan tidak bocor.
- **Structured concurrency** (`StructuredTaskScope`, masih **preview** di Java 25) — memperlakukan sekelompok subtugas yang di-*fork* sebagai **satu unit kerja**: jika salah satu gagal, yang lain dibatalkan; tidak ada thread "yatim". API-nya masih berubah antar rilis, jadi pakai hanya untuk eksperimen dan baca JEP terbaru sebelum memakainya.

```java
// Java 25+  (ScopedValue final)
static final ScopedValue<String> PENGGUNA = ScopedValue.newInstance();

ScopedValue.where(PENGGUNA, "andi").run(() -> layani());   // di dalam layani(): PENGGUNA.get() → "andi"
```

## Praktik

### Membuat Thread, `join`, `interrupt`, dan Status Thread

```java
// DemoThread.java
public class DemoThread {
    public static void main(String[] args) throws Exception {
        Thread pekerja = new Thread(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] mulai");
            try {
                Thread.sleep(2_000);                       // akan diinterupsi sebelum selesai
                System.out.println("tidak tercetak");
            } catch (InterruptedException e) {
                System.out.println("[pekerja] diinterupsi, berhenti dengan rapi");
                Thread.currentThread().interrupt();        // pulihkan status interrupt
            }
        }, "pekerja");

        System.out.println("Status sebelum start: " + pekerja.getState());
        pekerja.start();
        Thread.sleep(100);
        System.out.println("Status saat sleep   : " + pekerja.getState());

        pekerja.interrupt();
        pekerja.join();                                    // tunggu benar-benar selesai
        System.out.println("Status setelah join : " + pekerja.getState());

        // run() vs start()
        Thread t = new Thread(() -> System.out.println("dijalankan oleh: " + Thread.currentThread().getName()), "thread-baru");
        t.run();      // BUKAN thread baru -> dijalankan oleh main
        t.start();    // thread baru
        t.join();
    }
}
```

### Race Condition vs `synchronized` vs `Atomic`

```java
// RaceCondition.java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class RaceCondition {
    static int tidakAman = 0;
    static int aman = 0;
    static final Object LOCK = new Object();
    static final AtomicInteger atomik = new AtomicInteger();
    static final LongAdder adder = new LongAdder();
    static volatile int volatil = 0;

    static final int THREAD = 4;
    static final int ITERASI = 2_000_000;

    public static void main(String[] args) throws Exception {
        Thread[] semua = new Thread[THREAD];
        java.util.concurrent.CountDownLatch gerbang = new java.util.concurrent.CountDownLatch(1);
        for (int i = 0; i < THREAD; i++) {
            semua[i] = new Thread(() -> {
                try { gerbang.await(); } catch (InterruptedException e) { return; }   // mulai serentak
                for (int j = 0; j < ITERASI; j++) {
                    tidakAman++;                           // race condition
                    volatil++;                             // volatile TIDAK menyelesaikan atomicity
                    synchronized (LOCK) { aman++; }        // mutual exclusion
                    atomik.incrementAndGet();              // CAS tanpa lock
                    adder.increment();
                }
            });
            semua[i].start();
        }
        gerbang.countDown();                               // lepaskan semua thread bersamaan
        for (Thread t : semua) t.join();

        int diharapkan = THREAD * ITERASI;
        System.out.println("Diharapkan      : " + diharapkan);
        System.out.println("Tanpa sinkron   : " + tidakAman + (tidakAman == diharapkan ? "" : "  <-- ada update yang hilang"));
        System.out.println("volatile        : " + volatil + (volatil == diharapkan ? "" : "  <-- volatile tidak atomik"));
        System.out.println("synchronized    : " + aman);
        System.out.println("AtomicInteger   : " + atomik.get());
        System.out.println("LongAdder       : " + adder.sum());
    }
}
```

Jalankan beberapa kali: pada mesin multi-core, angka "Tanpa sinkron" dan "volatile" umumnya **lebih kecil** dari 8.000.000 dan **berbeda di tiap eksekusi**, sedangkan tiga versi aman selalu tepat. Race condition bersifat probabilistik: pada mesin dengan 1 core atau beban sangat kecil, hasilnya bisa kebetulan benar — bukan berarti kodenya aman.

### ExecutorService, Future, dan CompletableFuture

```java
// DemoExecutor.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DemoExecutor {
    public static void main(String[] args) throws Exception {
        // Fixed thread pool + Future
        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            List<Future<Long>> hasil = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                final int n = i * 1_000_000;
                Callable<Long> tugas = () -> {
                    long jumlah = 0;
                    for (int k = 1; k <= n; k++) jumlah += k;
                    return jumlah;
                };
                hasil.add(pool.submit(tugas));
            }
            for (int i = 0; i < hasil.size(); i++) {
                System.out.println("tugas " + (i + 1) + " -> " + hasil.get(i).get());   // get() menunggu
            }
        }

        // Exception di dalam tugas dibungkus ExecutionException
        try (ExecutorService pool = Executors.newSingleThreadExecutor()) {
            Future<Integer> gagal = pool.submit(() -> 10 / (args.length));   // args kosong -> bagi nol
            try {
                gagal.get();
            } catch (ExecutionException e) {
                System.out.println("Penyebab: " + e.getCause());
            }
        }

        // CompletableFuture: rangkai langkah asinkron
        CompletableFuture<String> nama = CompletableFuture.supplyAsync(() -> "andi");
        CompletableFuture<String> kota = CompletableFuture.supplyAsync(() -> "bandung");

        String gabungan = nama
                .thenApply(String::toUpperCase)
                .thenCombine(kota, (n, k) -> n + " dari " + k)
                .join();
        System.out.println(gabungan);

        String aman = CompletableFuture
                .supplyAsync(() -> { if (true) throw new IllegalStateException("layanan mati"); return "ok"; })
                .exceptionally(e -> "fallback karena: " + e.getCause().getMessage())
                .join();
        System.out.println(aman);

        // allOf: tunggu beberapa tugas paralel
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 22);
        CompletableFuture.allOf(a, b).join();
        System.out.println("allOf: " + (a.join() + b.join()));
    }
}
```

### Producer–Consumer dengan `BlockingQueue`

```java
// DemoProdusenKonsumen.java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DemoProdusenKonsumen {
    private static final int SELESAI = -1;      // "poison pill": sinyal berhenti

    public static void main(String[] args) throws Exception {
        BlockingQueue<Integer> antrean = new ArrayBlockingQueue<>(5);   // kapasitas kecil -> produsen kadang menunggu
        long[] total = new long[1];

        Thread produsen = new Thread(() -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    antrean.put(i);                 // menunggu jika antrean penuh
                }
                antrean.put(SELESAI);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread konsumen = new Thread(() -> {
            try {
                while (true) {
                    int x = antrean.take();         // menunggu jika antrean kosong
                    if (x == SELESAI) break;
                    total[0] += x;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        produsen.start();
        konsumen.start();
        produsen.join();
        konsumen.join();                            // join menjamin 'total' terlihat (happens-before)
        System.out.println("Total yang diproses konsumen: " + total[0]);   // 210
    }
}
```

### `volatile` untuk Flag Penghentian

```java
// DemoVolatile.java
public class DemoVolatile {
    static class Worker implements Runnable {
        private volatile boolean berjalan = true;     // hapus 'volatile' -> perilaku tidak terjamin
        private long hitungan = 0;

        void hentikan() { berjalan = false; }
        long hitungan() { return hitungan; }

        @Override
        public void run() {
            while (berjalan) {
                hitungan++;
            }
            System.out.println("Worker berhenti dengan rapi.");
        }
    }

    public static void main(String[] args) throws Exception {
        Worker w = new Worker();
        Thread t = new Thread(w);
        t.start();
        Thread.sleep(300);
        w.hentikan();          // tulis volatile -> terlihat oleh thread worker
        t.join();
        System.out.println("Worker sempat menghitung: " + (w.hitungan() > 0 ? "banyak iterasi" : "0"));
    }
}
```

Tanpa `volatile`, JIT compiler boleh mengoptimasi `while (berjalan)` menjadi loop tak berujung karena thread worker tidak pernah diwajibkan melihat perubahan dari thread lain — contoh nyata mengapa JMM penting.

### Virtual Threads: 10.000 Tugas Menunggu Bersamaan

```java
// DemoVirtualThread.java
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoVirtualThread {
    public static void main(String[] args) throws Exception {
        // Satu virtual thread langsung
        Thread vt = Thread.ofVirtual().name("vt-1").start(() ->
                System.out.println("virtual? " + Thread.currentThread().isVirtual()));
        vt.join();

        // 10.000 tugas I/O-bound (disimulasikan dengan sleep 1 detik)
        AtomicInteger selesai = new AtomicInteger();
        long mulai = System.nanoTime();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    return selesai.incrementAndGet();
                });
            }
        }   // menunggu semua tugas selesai

        long ms = (System.nanoTime() - mulai) / 1_000_000;
        System.out.println("Tugas selesai: " + selesai.get());
        System.out.println("Waktu total  : " + (ms < 5_000 ? "beberapa detik saja (bukan 10.000 detik)" : ms + " ms"));
    }
}
```

Jika dikerjakan berurutan, 10.000 tugas × 1 detik memakan lebih dari **2,7 jam**. Dengan virtual thread, semuanya selesai dalam kira-kira **satu–dua detik** karena seluruh tugas "menunggu" secara bersamaan tanpa menghabiskan ribuan thread OS.

### Deadlock dan Cara Menghindarinya (Konseptual)

```java
// Berpotensi DEADLOCK (jangan dijalankan di production):
//   Thread 1: synchronized(A) { synchronized(B) { ... } }
//   Thread 2: synchronized(B) { synchronized(A) { ... } }

// Solusi: urutan lock KONSISTEN — selalu ambil A dulu, lalu B
void transfer(Akun dari, Akun ke, long jumlah) {
    Akun pertama  = dari.id() < ke.id() ? dari : ke;      // urutan berdasarkan id
    Akun kedua    = dari.id() < ke.id() ? ke : dari;
    synchronized (pertama) {
        synchronized (kedua) {
            dari.kurangi(jumlah);
            ke.tambah(jumlah);
        }
    }
}
```

## Latihan

1. Tulis program dengan dua thread yang masing-masing mencetak angka 1–5 dengan jeda 200 ms, lalu gunakan `join()` agar `main` mencetak "selesai" **setelah** keduanya berhenti. Apa yang terjadi jika memanggil `run()` alih-alih `start()`?
2. Reproduksi race condition pada `int counter` dengan 8 thread × 100.000 kenaikan. Perbaiki dengan tiga cara (`synchronized`, `AtomicInteger`, `ReentrantLock`) dan bandingkan waktunya. Jelaskan mengapa `volatile` saja tidak cukup.
3. Hitung jumlah bilangan prima di bawah 5 juta dengan memecah rentang menjadi 4 bagian dan memakai `ExecutorService` + `Future`. Bandingkan waktu eksekusi dengan versi satu thread dan jelaskan mengapa speed-up tidak selalu persis 4×.
4. Rangkai tiga `CompletableFuture` (ambil harga, ambil kurs, hitung total) dengan `thenCombine` dan `exceptionally`. Simulasikan satu langkah yang gagal dan pastikan hasil akhir tetap bernilai fallback yang masuk akal.
5. Ubah demo virtual thread: bandingkan 10.000 tugas `sleep` 1 detik antara `newFixedThreadPool(200)` dan `newVirtualThreadPerTaskExecutor()`. Jelaskan perbedaan waktunya, mengapa virtual thread **tidak** boleh di-pool, dan jelaskan dengan konsep **happens-before** mengapa `volatile boolean berjalan` bekerja sedangkan `boolean` biasa tidak dijamin.

---

[← Modul 9: Functional Programming: Lambda & Stream API](09-functional-programming.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 11: Date & Time, Regex, Cryptography & Networking →](11-date-time-regex-crypto-networking.md)
