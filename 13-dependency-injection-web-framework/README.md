# Modul 13 — Dependency Injection & Web Framework: Jawaban Latihan

**Baca dulu:** sandbox tempat jawaban ini disusun **tidak punya akses ke Maven Central**, sehingga **Spring Boot, Javalin, Quarkus, dan Play tidak dapat dijalankan** di sini. Latihan 1–3 murni Java dan **dijalankan penuh**. Untuk Latihan 4–6 kode framework ditulis dan ditandai **TIDAK DIJALANKAN**; sebagai gantinya saya membuat *stand-in* tanpa framework (JDK `HttpServer`) yang meniru **kontrak HTTP yang sama** agar status code dan aturan validasinya benar-benar teruji. Stand-in itu bukan bukti bahwa anotasi Spring/Javalin Anda benar.

| Latihan | Status | Berkas |
|---|---|---|
| 1. `Pembayaran` dua implementasi, `@Primary`/`@Qualifier` | **Dijalankan** | `latihan-1/DemoPembayaran.java` |
| 2. Deteksi dependensi melingkar | **Dijalankan** | `latihan-2/DemoMelingkar.java` |
| 3. Constructor vs field injection; uji tanpa container | **Dijalankan** | `latihan-3/DemoInjeksi.java` |
| 4. Spring Boot PUT/DELETE + validasi baru | Spring **TIDAK dijalankan** (`spring-toko/`); kontrak HTTP diuji lewat stand-in (16 uji lulus) | `latihan-4/` |
| 5. Javalin vs Spring Boot, tabel perbandingan | Javalin/Spring **TIDAK dijalankan**; baris kode dihitung, stand-in diukur | `latihan-5/` |
| 6. `POST /produk` Javalin 201/400 + test | Javalin **TIDAK dijalankan**; kontrak diuji lewat stand-in (10 uji lulus) | `latihan-6/` |

Penjelasan: `jawaban-latihan.md`.

## Yang perlu Anda lakukan
```bash
./jalankan.sh        # jalankan semua bagian offline dan bandingkan dengan hasil tersimpan
```
Di mesin yang punya internet, coba proyek framework: `cd latihan-4/spring-toko && ./mvnw spring-boot:run` (Anda perlu membuat wrapper atau memakai `mvn`), `cd latihan-5/javalin-demo && mvn test`, `cd latihan-6/javalin-post && mvn test`. Versi dependency hanya contoh dari materi.
