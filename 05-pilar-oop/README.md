# 05-pilar-oop — Jawaban Latihan Modul 5

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `DemoKaryawan.java` (abstract class + polymorphism) + `hasil.txt` |
| `latihan-2/` | `MethodHiding.java` (static hiding vs overriding) + `contoh-error/` + hasilnya |
| `latihan-3/` | `DemoBebek.java` (konflik default method) + `contoh-error/` + hasilnya |
| `latihan-4/` | `DemoUang.java` (`equals`/`hashCode`/`toString` di `HashSet`) + `hasil.txt` |
| `latihan-5/` | `DemoMobil.java` (constructor 6 parameter vs Builder) + `hasil.txt` |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH).
2. Sebelum membaca jawaban Latihan 2, **tebak sendiri** versi `info()` mana yang jalan.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semua keluaran deterministik, jadi harus identik.
4. Eksperimen: pada `contoh-error/`, perbaiki kodenya sampai terkompilasi; pada Latihan 4, tambahkan field baru ke `equals` tetapi lupa ke `hashCode`, dan perhatikan apa yang terjadi.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21).
