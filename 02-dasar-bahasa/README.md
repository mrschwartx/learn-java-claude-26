# 02-dasar-bahasa — Jawaban Latihan Modul 2

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `Primitif.java` (8 tipe primitif + rentangnya) + `hasil.txt` |
| `latihan-2/` | `Prediksi.java` (`(byte) 130` dan `7 / 2`) + `hasil.txt` |
| `latihan-3/` | `Persegi.java` (luas, keliling, diagonal) + dua berkas masukan + hasilnya |
| `latihan-4/` | `Uang.java` (`double` vs `BigDecimal`) + `hasil.txt` |
| `latihan-5/` | `Scope.java` (scope dan shadowing) + `hasil.txt` + `error-kompilasi.txt` |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` ada di PATH).
2. Kerjakan **prediksi Latihan 2 sendiri dulu** (di kertas) sebelum membaca jawabannya.
3. Jalankan `./jalankan.sh`, lalu bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Hasilnya seharusnya identik; satu-satunya yang bisa berbeda adalah Latihan 3 jika Anda mengubah masukan.
4. Ubah-ubah nilai di kode (misalnya masukan Latihan 3 atau jumlah putaran Latihan 4) dan amati dampaknya.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21). Karena tidak ada data pribadi atau pengukuran waktu pada modul ini, hasilnya deterministik dan sama di semua mesin. Pengecualian: keluaran `Scanner` bergantung locale, lihat catatan di Latihan 3.
