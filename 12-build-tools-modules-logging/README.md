# Modul 12 — Build Tools, Java Modules & Logging: Jawaban Latihan

**Baca dulu:** sandbox tempat jawaban ini disusun **tidak punya akses ke Maven Central** (diuji: Maven gagal mengunduh plugin paling dasar). Jadi bagian yang butuh dependency/plugin dari internet **ditulis tetapi TIDAK DIJALANKAN**, dan ditandai begitu di semua berkas. Yang benar-benar dijalankan dan dicatat hasilnya: JPMS (`javac`, `jdeps`, `jlink`), Gradle tanpa dependency, demo facade SLF4J, contoh log buruk, dan JUL.

| Latihan | Status | Berkas |
|---|---|---|
| 1. Maven `mvn clean package` | `pom.xml` ditulis, **TIDAK dijalankan**; fase compile+package diemulasi manual dengan `javac`/`jar` | `latihan-1/` |
| 2. SLF4J + Logback + rotasi harian | Project ditulis, **TIDAK dijalankan**; kode `App.java` dijalankan terhadap backend lain | `latihan-2/`, `latihan-5/` |
| 3. `build.gradle.kts` + `implementation`/`api`/`runtimeOnly` | Versi lengkap **TIDAK dijalankan**; Gradle tanpa dependency **dijalankan** | `latihan-3/` |
| 4. Dua modul JPMS, `jdeps`, `jlink` | **Dijalankan penuh** | `latihan-4/` |
| 5. Facade SLF4J + tiga log buruk | **Dijalankan** (terukur) | `latihan-5/` |
| 6. Logback → Log4j2, `JsonTemplateLayout` | Konfigurasi ditulis, **TIDAK dijalankan**; bukti "kode Java tidak berubah" lewat `diff -r` | `latihan-6/` |
| 7. JUL → TinyLog | JUL **dijalankan**; TinyLog ditulis, **TIDAK dijalankan** | `latihan-7/` |
| 8. Bazel `util_lib` + `bazel query` | `BUILD`/`MODULE.bazel` ditulis, **TIDAK dijalankan** (tidak ada Bazel) | `latihan-8/` |

Penjelasan: `jawaban-latihan.md`.

## Yang perlu Anda lakukan
```bash
./jalankan.sh                 # menjalankan bagian yang bisa dijalankan offline, membandingkan dengan hasil tersimpan
SKIP_GRADLE=1 ./jalankan.sh   # lewati Gradle (sekitar 35-70 detik)
```
Di mesin Anda yang punya internet, jalankan sendiri bagian yang saya tandai TIDAK DIJALANKAN: `cd latihan-2/demo-maven && mvn clean package`, dst. Nomor versi dependency hanya contoh dari materi; cek Maven Central.
