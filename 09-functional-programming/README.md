# 09-functional-programming — Jawaban Latihan Modul 9

Isi paket ini:

| Berkas / folder | Isi |
|---|---|
| `jawaban-latihan.md` | **Mulai dari sini.** Jawaban dan penjelasan untuk kelima latihan |
| `jalankan.sh` | Menjalankan ulang semua latihan di mesin Anda; hasil di `hasil-lokal/` |
| `latihan-1/` | `EmpatJenis.java` (empat bentuk method reference), `contoh-error/` + hasilnya |
| `latihan-2/` | `DemoHof.java` (`petakan`, `cariPertama`, versi malas dengan `Supplier`) + `hasil.txt` |
| `latihan-3/` | `DemoPipeline.java` (`andThen`/`compose`, tiga `Predicate` digabung) + `hasil.txt` |
| `latihan-4/` | `DemoPegawai.java` (rata-rata per divisi, tertinggi per divisi, di atas rata-rata) + `hasil.txt` |
| `latihan-5/` | `FrekuensiStream.java` (groupingBy + counting, lazy, sekali pakai, parallel) + `hasil.txt` |

## Yang perlu Anda lakukan

1. Pasang JDK 21 atau lebih baru (`java` dan `javac` di PATH). Subbab 9.8 (Stream Gatherers) memerlukan Java 24+ dan **tidak** termasuk soal latihan, jadi tidak dipakai di sini.
2. Latihan 5: **tebak dulu** baris mana saja yang tercetak oleh `peek` pada pipeline `filter → map → limit(2)`, baru lihat bagian *LAZY* di `hasil.txt`.
3. Jalankan `./jalankan.sh` dan bandingkan `hasil-lokal/` dengan folder `latihan-*/`. Semua keluaran deterministik **kecuali** dua baris berawalan `[BERVARIASI]` di Latihan 5 (demo bahaya parallel stream, berbeda tiap mesin dan tiap eksekusi). Jumlah core CPU Anda menentukan seberapa sering kesalahannya muncul.
4. Eksperimen: pada Latihan 5, ganti `ArrayList` dengan `Collections.synchronizedList(...)` atau gunakan `collect(toList())`, lalu ulangi 50 percobaan itu dan lihat kesalahannya hilang.

Semua keluaran di folder `latihan-*` dihasilkan di lingkungan Linux penyusun (OpenJDK 21, 2 CPU).
