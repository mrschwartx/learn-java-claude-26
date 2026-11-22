# 03-kontrol-alur-array-string — Jawaban Latihan Modul 3

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `Angka.java` (ganjil/genap, tanda, kelipatan 5) + empat berkas masukan + hasilnya |
| `latihan-2/` | `FizzBuzzSwitch.java` dan `FizzBuzzIf.java` (1–100) + hasilnya |
| `latihan-3/` | `TerbesarKedua.java` (tanpa sort vs `Arrays.sort`) + `hasil.txt` |
| `latihan-4/` | `Teks.java` (`hitungVokal`, `balik`, biaya `+` di loop) + `hasil.txt` |
| `latihan-5/` | `StringPool.java` dan `Kasir.java` (struk `printf`) + hasilnya |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` di PATH).
2. Coba kerjakan sendiri dulu, terutama Latihan 3 dan 4 (algoritmanya pendek tetapi penuh jebakan), baru bandingkan dengan jawaban.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semuanya harus identik **kecuali satu baris waktu** di Latihan 4.
4. Eksperimen: ubah `n` di `Teks.java` (misalnya 20.000 → 100.000) dan amati bagaimana waktu penyambungan dengan `+` melonjak jauh lebih cepat daripada `StringBuilder`.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21). Struk kasir memakai `Locale` Indonesia secara eksplisit, sehingga pemisah ribuan selalu titik di mesin mana pun.
