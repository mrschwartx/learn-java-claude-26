# 06-fitur-modern — Jawaban Latihan Modul 6

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `DemoLevel.java` (enum `Level` + `EnumMap`) + `hasil.txt` |
| `latihan-2/` | `DemoMahasiswa.java` (record + compact constructor) + `hasil.txt` |
| `latihan-3/` | `DemoHasil.java` (sealed `Hasil`), `DemoHasilMenunggu.java`, `contoh-error/` + hasilnya |
| `latihan-4/` | `DemoFormat.java` (switch pattern), `contoh-error/` (urutan case salah) + hasilnya |
| `latihan-5/` | `DemoPanjang.java` (`@Panjang`, `Validator`, efek `@Retention`) + `hasil.txt` |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH). Semua kode memakai fitur yang sudah final di Java 21 (record pattern dan switch pattern), tanpa fitur preview.
2. Latihan 3: **tebak dulu** apa yang terjadi saat `Menunggu` ditambahkan ke `permits`, baru buka `contoh-error/`.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semua keluaran deterministik (termasuk "acak" di Latihan 1, yang memakai seed tetap), jadi harus identik.
4. Eksperimen: pada Latihan 5, ganti `@Retention(RUNTIME)` pada `@Panjang` menjadi `SOURCE`, jalankan ulang, dan lihat validasinya diam-diam berhenti bekerja.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21).
