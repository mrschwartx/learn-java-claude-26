# 07-generics-collections — Jawaban Latihan Modul 7

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `Unik.java` (`LinkedHashSet` vs `TreeSet`) + `masukan.txt` + `hasil.txt` |
| `latihan-2/` | `DemoPasangan.java` (`Pasangan<A,B>`, `min`) + `contoh-error/` + hasilnya |
| `latihan-3/` | `Kurung.java` (kurung seimbang, `Stack` vs `ArrayDeque`) + `hasil.txt` |
| `latihan-4/` | `GrupProduk.java` (`computeIfAbsent`, tanpa stream) + `hasil.txt` |
| `latihan-5/` | `DemoLru.java` (cache LRU vs FIFO), `OptionalField.java` + hasilnya |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH).
2. Latihan 5: **tulis prediksi isi cache** (untuk dua mode) sebelum membuka `hasil-lru.txt`.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semua keluaran deterministik. Satu catatan: baris `HashSet` di Latihan 1 adalah contoh urutan yang **tidak dijamin**; JDK lain bisa saja mengurutkannya berbeda, dan itu bukan kesalahan.
4. Eksperimen: ganti `masukan.txt` dengan bilangan Anda sendiri, dan tambahkan `null` ke `ArrayDeque` untuk melihat `NullPointerException`.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21).
