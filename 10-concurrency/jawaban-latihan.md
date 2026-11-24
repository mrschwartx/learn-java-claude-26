# Jawaban Latihan Modul 10 — Concurrency

Lingkungan uji: Java 21, **2 CPU**. Angka waktu dan hasil race bersifat `[BERVARIASI]`; yang dijamin adalah pola dan penjelasannya.

## Latihan 1 — Dua thread, `join`, `run()` vs `start()`
Dua thread mencetak 1..5 dengan `Thread.sleep(200)`. `main` memanggil `a.join(); b.join();` baru mencetak "selesai", sehingga "selesai" selalu terakhir.
- Urutan **per thread** selalu 1..5; yang berselang-seling antar A dan B bisa berubah.
- Dengan `start()`: total sekitar 1.014 ms (paralel). Dengan `run()`: sekitar 2.003 ms, dan nama thread yang tercetak adalah `main`; `run()` hanyalah pemanggilan method biasa, status thread tetap `NEW`.
- `InterruptedException` tidak ditelan: `Thread.currentThread().interrupt()` lalu `return`. Tanpa `try/catch`, kompilasi gagal (`contoh-error/TanpaTry.java`).

## Latihan 2 — Race condition
`n++` adalah tiga instruksi (`getstatic`, `iadd`, `putstatic`) — lihat `bytecode.txt`; versi `volatile` **sama persis** di bytecode, hanya field-nya bertanda volatile.

Hasil (5 ronde, target 800.000):
| Cara | Benar | Catatan |
|---|---|---|
| tanpa pengaman | 1/5 | hasil 290.494 – 800.000; kebetulan benar satu kali |
| `volatile` | 0/5 | menjamin visibilitas, **bukan** atomisitas |
| `synchronized` | 5/5 | benar, paling lambat di uji ini |
| `AtomicInteger` | 5/5 | CAS tanpa kunci |
| `ReentrantLock` | 5/5 | `unlock()` di `finally` |
| `LongAdder` | 5/5 | tercepat saat kontensi tinggi |

Peringatan jujur: urutan kecepatan antar varian aman (mis. `ReentrantLock` 19 ms vs `synchronized` 137 ms) berubah antar mesin/eksekusi; jangan menyimpulkan satu selalu lebih cepat. Benchmark ini kecil dan tanpa JMH. Satu ronde tanpa pengaman bisa kebetulan benar — itu sebabnya dijalankan 5 ronde.

## Latihan 3 — Prima < 5.000.000
Jumlah prima: **348.513** (satu thread, 4 bagian, dan 40 potongan semuanya sama). Per bagian sama lebar: 96.469 / 86.603 / 83.645 / 81.796.
- Waktu per bagian kira-kira [166, 269, 294, 273] ms: bagian awal lebih ringan karena angka kecil cepat gugur; pembagian sama lebar tidak merata bebannya.
- Speed-up sekitar 1,9x pada 2 CPU; batas atas realistis adalah jumlah core, bukan jumlah bagian. Memecah jadi 40 potongan hanya membantu pemerataan, bukan menambah core.
- `ExecutorService` dipakai dengan try-with-resources (`close()` menunggu tugas selesai); `Future.get()` membawa hasil dan membungkus exception dalam `ExecutionException`.

## Latihan 4 — `CompletableFuture`
- Normal: 150 USD x 15.800 = Rp2.370.000 via `thenCombine`.
- Kurs gagal tanpa penanganan: `join()` melempar `CompletionException` dengan penyebab asli `IllegalStateException`.
- `exceptionally` **pada langkah kurs**: pakai kurs cadangan 16.000 → total Rp2.400.000. `exceptionally` menerima `CompletionException` yang membungkus penyebab asli (tipe tercetak di `hasil.txt`).
- `exceptionally` **di ujung rantai**: hanya bisa memberi fallback untuk total (di sini -1); informasi harga yang sudah berhasil ikut terbuang. Letak penanganan menentukan apa yang bisa diselamatkan.
- `handle()` menerima hasil dan error sekaligus.
- Kurs *lambat* (bukan gagal) tidak memicu `exceptionally` sendiri; perlu `orTimeout`, yang menghasilkan `TimeoutException`.
- Lambda tidak boleh mengubah variabel lokal (`contoh-error/LambdaBerubah.java`).

## Latihan 5 — Virtual thread dan `volatile`
10.000 tugas tidur 1 detik:
- Pool 200 thread: **50.157 ms** (10.000/200 = 50 gelombang x 1 detik, tepat sesuai teori).
- Virtual thread per tugas: **1.111 ms**; rasio sekitar 45x. Keuntungan ini hanya untuk tugas **I/O / menunggu**; untuk tugas CPU murni, virtual thread tidak lebih cepat dari jumlah core. Virtual thread tidak perlu di-pool, dan selalu daemon.

`volatile` dan happens-before (tiap varian diberi batas 1 detik agar tidak menggantung):
- Flag `boolean` biasa: worker **tidak berhenti** dalam 1 detik di uji ini; `volatile` dan `AtomicBoolean` berhenti. `[BERVARIASI]`: hasil flag biasa bergantung JIT; Java Memory Model tidak menjamin worker melihat perubahan, jadi jangan mengandalkannya.
- Menulis data lalu flag `volatile` mempublikasikan data itu: pembaca yang melihat flag `true` dijamin melihat `data = 42` (happens-before).

## Tidak diuji
`StructuredTaskScope` (preview) dan `ScopedValue` dari teori modul tidak dijalankan karena sandbox hanya punya Java 21 dan jawaban latihan tidak membutuhkannya.
