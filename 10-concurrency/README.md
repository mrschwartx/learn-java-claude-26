# Modul 10 — Concurrency: Jawaban Latihan

Java 21+ (diuji di Java 21; kode tidak memakai fitur preview). Semua program satu berkas, dijalankan dengan `java Nama.java`.

## Isi
| Folder | Latihan | Berkas utama |
|---|---|---|
| `latihan-1/` | Dua thread + `join`, `run()` vs `start()` | `DuaThread.java`, `hasil.txt`, `contoh-error/` |
| `latihan-2/` | Race condition counter 8 x 100.000, lima perbaikan | `Counter.java`, `Bytecode.java`, `bytecode.txt`, `hasil.txt` |
| `latihan-3/` | Prima < 5.000.000 dengan `ExecutorService` + `Future` | `Prima.java`, `hasil.txt` |
| `latihan-4/` | `CompletableFuture`: harga, kurs, total | `DemoHarga.java`, `hasil.txt`, `contoh-error/` |
| `latihan-5/` | Virtual thread vs pool 200 thread; `volatile` dan happens-before | `VirtualThread.java`, `Volatile.java`, `hasil-*.txt` |

Penjelasan lengkap ada di `jawaban-latihan.md`. Output kompilasi gagal yang disengaja ada di `error-kompilasi.txt`.

## Yang perlu Anda lakukan
```bash
./jalankan.sh                # semua latihan (sekitar 1 menit; latihan 5 punya demo 50 detik)
SKIP_LAMBAT=1 ./jalankan.sh  # lewati demo 50 detik
```
Hasil ditulis ke `hasil-lokal/` lalu dibandingkan dengan berkas tersimpan. Baris bertanda `[BERVARIASI]` (waktu, hasil race) memang berubah tiap eksekusi dan tidak ikut dibandingkan.
