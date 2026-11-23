# 08-exception-io — Jawaban Latihan Modul 8

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `Umur.java` (`ambilUmur` + loop input) + dua berkas masukan + hasilnya |
| `latihan-2/` | `DemoGudang.java` (checked vs unchecked) + `contoh-error/` + hasilnya |
| `latihan-3/` | `DemoKoneksi.java` (try-with-resources, suppressed) + `hasil.txt` |
| `latihan-4/` | `Ringkas.java` (baca per baris), `contoh.txt` + hasil kecil dan hasil 300.000 baris |
| `latihan-5/` | `DemoLaporan.java` (chaining, catch kosong, batas terluar) + `hasil.txt` |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH).
2. Latihan 3: **tebak dulu** exception mana yang menang di tiap skenario (utama atau yang dari `close()`), baru buka `hasil.txt`.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semua keluaran deterministik. Satu kemungkinan perbedaan: stack trace pada akhir Latihan 2 (exception tak tertangani) bisa memuat baris tambahan dari launcher `java` di versi JDK lain; isi pokoknya sama.
4. Eksperimen: pada Latihan 3, ubah urutan deklarasi dua `Koneksi` dan lihat urutan penutupannya ikut berbalik; pada Latihan 4, naikkan jumlah baris file besar dan amati bahwa program tetap berjalan dengan memori kecil.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21).
