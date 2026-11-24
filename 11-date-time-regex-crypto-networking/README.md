# Modul 11 — Date & Time, Regex, Cryptography & Networking: Jawaban Latihan

Java 21+ (diuji di Java 21). Semua program satu berkas: `java Nama.java`. Tidak butuh koneksi internet (server HTTP berjalan di loopback).

| Folder | Latihan | Berkas utama |
|---|---|---|
| `latihan-1/` | Umur, hari lahir, ulang tahun berikutnya; resolver `STRICT` | `Umur.java`, `YyyyStrict.java`, `hasil*.txt`, `contoh-error/` |
| `latihan-2/` | Rapat 10.00 WIB di beberapa zona; `Instant` vs `LocalDateTime` | `Rapat.java`, `hasil.txt` |
| `latihan-3/` | Regex access log (grup bernama), hitung 5xx, ReDoS | `AccessLog.java`, `hasil.txt` |
| `latihan-4/` | `PasswordHasher` PBKDF2; kenapa bukan SHA-256 polos / `Random` | `PasswordHasher.java`, `SHA256Polos.java`, `RandomLemah.java`, `hasil-*.txt`, `contoh-error/` |
| `latihan-5/` | Endpoint `/waktu`, client paralel `sendAsync` + `allOf`, timeout, non-2xx | `Jaringan.java`, `hasil.txt` |

Penjelasan lengkap: `jawaban-latihan.md`. Error kompilasi yang disengaja: `error-kompilasi.txt`.

## Yang perlu Anda lakukan
```bash
./jalankan.sh
```
Hasil ditulis ke `hasil-lokal/` dan dibandingkan dengan berkas tersimpan. Baris `[BERVARIASI]` (waktu) tidak ikut dibandingkan.
