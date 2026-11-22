# 04-class-object — Jawaban Latihan Modul 4

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `Mahasiswa.java` (field private, dua constructor, `toString`) + `hasil.txt` |
| `latihan-2/` | `Mahasiswa.java` (+ counter `static`), `ContohError.java` + hasil dan pesan error |
| `latihan-3/` | `UrutanBlok.java` (static block, instance block, constructor) + `hasil.txt` |
| `latihan-4/` | `PassByValue.java` (`tukar`, `StringBuilder`) + `hasil.txt` |
| `latihan-5/` | Project `toko/` (package `model` dan `app`), `jalankan-latihan5.sh`, hasil sukses dan hasil error |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH).
2. **Tulis prediksi Latihan 3 sendiri dulu** (urutan keluaran untuk tiga object), baru lihat jawabannya.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Seluruh keluaran modul ini deterministik, jadi harus identik.
4. Latihan 5: buka `latihan-5/toko/`, kompilasi dengan tangan (`javac -d out ...`), lalu coba sendiri mengubah modifier lain (`private`, `protected`) dan amati pesan errornya.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21).
