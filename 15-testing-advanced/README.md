# Modul 15 — Testing & Advanced Topics: jawaban latihan

Isi folder ini menjawab 5 latihan di `15-testing-advanced.md`. Jawaban tertulis ada di **`jawaban-latihan.md`**.

## Jalankan
```bash
./jalankan.sh            # kompilasi + jalankan semua latihan, bandingkan dengan hasil tersimpan (butuh JDK 21+)
SIMPAN=1 ./jalankan.sh   # tulis ulang hasil tersimpan
```
Latihan 5 memakai PostgreSQL sementara dari Modul 14 (`../14-database-access/alat/pg-up.sh`); bila server PostgreSQL tidak ada, bagian itu dilewati.

## Yang DIJALANKAN vs TIDAK DIJALANKAN (jujur)
Sandbox tidak punya akses Maven Central, jadi **JUnit, Mockito, JaCoCo, PIT, Spring, REST Assured, Testcontainers, JMeter** tidak bisa dipakai.

| Bagian | Status |
|---|---|
| Alat uji buatan sendiri: `alat/MiniUji.java` (mini JUnit), `alat/MiniMock.java` (mock berbasis Proxy), `alat/Cov.java` | Dijalankan |
| L1 TDD `Password` (merah 7 lulus/3 gagal, hijau 10/0) | Dijalankan (MiniUji) |
| L1 versi JUnit 6 `@ParameterizedTest`/`@CsvSource` (`latihan-1/junit/`) | **TIDAK DIJALANKAN** |
| L2 `Clock.fixed` + hari Jumat | Dijalankan |
| L3 test `RekeningBank`, cakupan baris (instrumentasi sendiri), mutation testing mini | Dijalankan; **bukan JaCoCo/PIT asli** |
| L3 versi JUnit (`latihan-3/junit/`) | **TIDAK DIJALANKAN** |
| L4 mock vs fake dengan MiniMock | Dijalankan; **bukan Mockito asli** |
| L4 versi Mockito (`latihan-4/mockito/`) | **TIDAK DIJALANKAN** |
| L5 integration test: HTTP -> aplikasi pengganti (JDK `HttpServer`) -> JDBC -> **PostgreSQL 16 sungguhan** | Dijalankan; **bukan Spring Boot/REST Assured/Testcontainers** |
| L5 load test mini (p50/p95/p99, throughput) | Dijalankan; **bukan JMeter** |
| L5 `spring/ProdukApiIT.java`, `pom-potongan.xml`, `produk.jmx` | **TIDAK DIJALANKAN / tidak divalidasi** |

Angka load test **bervariasi** antar mesin dan antar eksekusi (ditandai di `jawaban-latihan.md`).
