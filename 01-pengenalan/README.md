# 01-pengenalan — Jawaban Latihan Modul 1

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | Keluaran `java -version` dan `javac -version` |
| `latihan-2/` | `HelloWorld.java` + keluaran dua cara menjalankan |
| `latihan-3/` | Keluaran `javap -c` dan cuplikan `javap -v` |
| `latihan-4/` | `WarmUp.java` (demo JIT) + hasil ukur |
| `latihan-5/` | Masukan dan sesi `jshell` |

## Yang perlu Anda lakukan

1. Pasang JDK (bukan hanya JRE), pastikan `java`, `javac`, `javap`, `jshell` ada di PATH.
2. Edit `latihan-2/HelloWorld.java`: isi `NAMA` dan `TAHUN_LAHIR` dengan data Anda.
3. Jalankan `./jalankan.sh`, lalu isi tabel vendor/versi di `jawaban-latihan.md` (Latihan 1) dari `hasil-lokal/1-versi-java.txt`.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21, 1 core CPU), bukan di mesin Anda; angka waktu pada Latihan 4 akan berbeda.
