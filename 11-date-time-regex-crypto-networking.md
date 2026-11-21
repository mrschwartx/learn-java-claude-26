# Modul 11: Date & Time, Regex, Cryptography & Networking

[← Modul 10: Concurrency: Thread, Virtual Thread & Java Memory Model](10-concurrency.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 12: Build Tools, Java Modules & Logging →](12-build-tools-modules-logging.md)

**Isi modul ini**

- [Teori](#teori)
  - [11.1 Date and Time (`java.time`)](#111-date-and-time-javatime)
  - [11.2 Regular Expressions](#112-regular-expressions)
  - [11.3 Cryptography](#113-cryptography)
  - [11.4 Networking](#114-networking)
- [Praktik](#praktik)
  - [Date & Time](#date--time)
  - [Regex: Mem-parse Baris Log](#regex-mem-parse-baris-log)
  - [Kriptografi: Hash, Password, HMAC, AES-GCM, dan Tanda Tangan](#kriptografi-hash-password-hmac-aes-gcm-dan-tanda-tangan)
  - [Networking: HTTP Server Mini + `HttpClient`](#networking-http-server-mini--httpclient)
- [Latihan](#latihan)

---


## Teori

### 11.1 Date and Time (`java.time`)

Sejak Java 8, API tanggal-waktu modern ada di package **`java.time`** (JSR-310). API lama (`java.util.Date`, `Calendar`, `SimpleDateFormat`) bermasalah — mutable, tidak thread-safe, bulan berbasis 0 — sehingga **jangan dipakai di kode baru**.

Semua class `java.time` **immutable** dan thread-safe: operasi seperti `plusDays` mengembalikan object baru.

| Class | Mewakili | Contoh |
|---|---|---|
| `LocalDate` | Tanggal tanpa waktu & zona | `2026-11-21` |
| `LocalTime` | Jam tanpa tanggal & zona | `14:30:00` |
| `LocalDateTime` | Tanggal + jam tanpa zona | `2026-11-21T14:30` |
| `ZonedDateTime` | Tanggal + jam + **zona waktu** | `2026-11-21T14:30+07:00[Asia/Jakarta]` |
| `OffsetDateTime` | Tanggal + jam + offset UTC | `...+07:00` |
| `Instant` | **Satu titik di garis waktu** (UTC), cocok untuk timestamp mesin | `2026-11-21T07:30:00Z` |
| `Duration` | Rentang waktu berbasis detik/nano (jam, menit, detik) | `PT2H30M` |
| `Period` | Rentang berbasis kalender (tahun, bulan, hari) | `P1Y2M3D` |
| `ZoneId` | Zona waktu | `Asia/Jakarta` |
| `DateTimeFormatter` | Parsing & formatting | `dd MMM yyyy` |

Pedoman memilih tipe:

- Untuk **menyimpan/mengirim waktu kejadian** (log, audit, database), gunakan **`Instant`** (UTC) lalu ubah ke zona pengguna hanya saat **ditampilkan**.
- Untuk **tanggal kalender murni** (tanggal lahir, jatuh tempo), gunakan `LocalDate`.
- Untuk **jadwal di zona tertentu** (rapat jam 09.00 WIB), gunakan `ZonedDateTime`.
- Jangan pernah mengandalkan zona waktu default server; selalu sebutkan `ZoneId` secara eksplisit. Indonesia memiliki tiga zona: `Asia/Jakarta` (WIB), `Asia/Makassar` (WITA), dan `Asia/Jayapura` (WIT).

Untuk "waktu sekarang" gunakan `LocalDate.now()`, `Instant.now()`, dst. Pada kode yang perlu diuji, injeksikan **`Clock`** (`LocalDate.now(clock)`) agar waktu dapat dikontrol di test ([Modul 15](15-testing-advanced.md)).

### 11.2 Regular Expressions

**Regular expression (regex)** adalah pola untuk mencocokkan dan mengekstrak teks. Di Java tersedia lewat `java.util.regex.Pattern` dan `Matcher`, serta method `String.matches`, `replaceAll`, dan `split`.

**Sintaks yang paling sering dipakai:**

| Pola | Arti |
|---|---|
| `.` | Satu karakter apa saja (kecuali baris baru) |
| `\d` `\w` `\s` | Digit / karakter kata (`[A-Za-z0-9_]`) / spasi. Kebalikannya: `\D` `\W` `\S` |
| `[abc]` `[^abc]` `[a-z]` | Salah satu dari / bukan salah satu dari / rentang |
| `*` `+` `?` | 0 atau lebih / 1 atau lebih / 0 atau 1 |
| `{n}` `{n,m}` | Tepat n kali / antara n dan m kali |
| `^` `$` | Awal / akhir teks (atau baris, dengan flag `MULTILINE`) |
| `\|` | Atau |
| `( )` | **Grup** penangkap |
| `(?<nama> )` | **Grup bernama** |
| `(?: )` | Grup tak menangkap |
| `\b` | Batas kata |

Di string Java, backslash harus digandakan: pola `\d+` ditulis `"\\d+"` (atau pakai *text block*).

```java
Pattern p = Pattern.compile("(?<tahun>\\d{4})-(?<bulan>\\d{2})-(?<hari>\\d{2})");
Matcher m = p.matcher("Rilis: 2026-11-21");
if (m.find()) {                                  // find() = cari di mana saja; matches() = harus cocok seluruhnya
    System.out.println(m.group("tahun"));        // 2026
}
```

Catatan penting:

- **Compile sekali, pakai berulang** — `Pattern.compile` mahal; simpan sebagai konstanta `static final`. `Pattern` thread-safe, `Matcher` **tidak**.
- **`matches()`** mencocokkan **seluruh** string; **`find()`** mencari kemunculan di bagian mana pun.
- **ReDoS (*Regex Denial of Service*)** — pola dengan pengulangan bersarang seperti `(a+)+$` bisa memakan waktu eksponensial pada input jahat (*catastrophic backtracking*) dan melumpuhkan server. Jangan menjalankan regex rumit pada input tak tepercaya tanpa batasan panjang.
- Regex bukan alat untuk semua hal: **jangan** mem-parse HTML/JSON/XML dengan regex; pakai parser yang sesuai. Validasi email yang benar-benar sempurna lewat regex pun hampir mustahil — validasi dasar lalu kirim email konfirmasi.

### 11.3 Cryptography

Java menyediakan kriptografi lewat arsitektur **JCA/JCE** (*Java Cryptography Architecture/Extension*) di package `java.security` dan `javax.crypto`. Prinsip emas: **jangan membuat algoritma kripto sendiri**, gunakan yang standar, dan pilih alat sesuai tujuannya.

| Tujuan | Alat yang tepat | API Java |
|---|---|---|
| Sidik jari data (integritas) | **Hash**: SHA-256 / SHA-3 | `MessageDigest` |
| Integritas + keaslian (kunci rahasia bersama) | **HMAC** | `Mac` (`HmacSHA256`) |
| **Menyimpan password** | **KDF lambat**: Argon2, bcrypt, scrypt, **PBKDF2** (plus *salt*) | `SecretKeyFactory` (`PBKDF2WithHmacSHA256`) |
| Merahasiakan data (1 kunci) | **Enkripsi simetris**: **AES-GCM** | `Cipher` (`AES/GCM/NoPadding`) |
| Tanda tangan digital | **Asimetris**: Ed25519, ECDSA, RSA | `Signature`, `KeyPairGenerator` |
| Angka acak aman | CSPRNG | **`SecureRandom`** (bukan `Random`/`Math.random`) |
| Encoding biner → teks | Bukan enkripsi! | `Base64`, `HexFormat` |

Poin yang sering salah dipahami:

- **Hash bukan enkripsi** — satu arah, tak dapat dibalik. **Jangan** menyimpan password dengan SHA-256 polos: terlalu cepat sehingga mudah di-*brute force*. Gunakan **KDF yang sengaja lambat** dengan **salt acak per pengguna**. JDK menyertakan PBKDF2; untuk **Argon2/bcrypt** gunakan library teruji (misal Spring Security `PasswordEncoder`, Bouncy Castle).
- **Base64 bukan enkripsi** — hanya cara menulis byte sebagai teks.
- **AES wajib memakai mode terautentikasi seperti GCM**, dengan **IV/nonce 12 byte acak dan unik** untuk setiap enkripsi dengan kunci yang sama. Mode ECB **tidak boleh dipakai**. GCM juga mendeteksi data yang dimodifikasi (*tamper*).
- **Jangan memakai `Random`** untuk token/kunci/salt — hasilnya dapat ditebak. Pakai `SecureRandom`.
- **Kunci jangan ditanam di kode.** Simpan di secret manager, variabel lingkungan, atau KMS.
- **Kriptografi pasca-kuantum:** Java 24 menambahkan algoritma standar **ML-KEM** (pertukaran kunci) dan **ML-DSA** (tanda tangan), dan Java 25 menambahkan **KDF API**. Anda belum perlu memakainya sekarang, tetapi layak diketahui arahnya.
- Untuk **TLS** (HTTPS), Java sudah menangani lewat `SSLContext`/`HttpClient` — Anda hampir tidak pernah perlu mengelola kripto TLS secara manual.

### 11.4 Networking

Bahasan jaringan di Java bertingkat, dari TCP mentah hingga HTTP:

| Tingkat | API | Kapan dipakai |
|---|---|---|
| **HTTP client** | `java.net.http.HttpClient` (Java 11+) | Memanggil REST API — **pilihan utama** |
| HTTP server ringan | `com.sun.net.httpserver.HttpServer` (modul `jdk.httpserver`) | Demo, tool kecil, test (Java 18+ juga punya `jwebserver`) |
| **TCP socket** | `Socket`, `ServerSocket` | Protokol kustom |
| UDP | `DatagramSocket` | Data real-time ringan |
| URL/URI | `URI`, `URL`, `URLEncoder` | Mem-parse dan menyusun alamat |

**`HttpClient`** mendukung HTTP/1.1 dan HTTP/2, request **sinkron** (`send`) maupun **asinkron** (`sendAsync` → `CompletableFuture`, [Modul 10](10-concurrency.md)), timeout, dan redirect. Object `HttpClient` **dibuat sekali dan dipakai ulang** (thread-safe, memegang connection pool).

```java
HttpClient klien = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.contoh.com/produk/1"))
        .timeout(Duration.ofSeconds(10))
        .header("Accept", "application/json")
        .GET()
        .build();

HttpResponse<String> res = klien.send(req, HttpResponse.BodyHandlers.ofString());
System.out.println(res.statusCode() + " " + res.body());
```

Selalu periksa **status code** (2xx sukses, 4xx kesalahan klien, 5xx kesalahan server), tetapkan **timeout** (default-nya tak terbatas), dan jangan menyusun URL dari input pengguna dengan konkatenasi mentah (gunakan `URLEncoder`). `HttpClient` tidak mem-parse JSON — gunakan library seperti **Jackson** atau **Gson** untuk itu.

## Praktik

### Date & Time

```java
// DemoWaktu.java
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class DemoWaktu {
    public static void main(String[] args) {
        // LocalDate & Period (nilai tetap agar hasil deterministik)
        LocalDate lahir = LocalDate.of(1998, 5, 17);
        LocalDate acuan = LocalDate.of(2026, 11, 21);
        Period umur = Period.between(lahir, acuan);
        System.out.println("Umur: " + umur.getYears() + " tahun, " + umur.getMonths() + " bulan, " + umur.getDays() + " hari");
        System.out.println("Hari sampai tahun baru: " + ChronoUnit.DAYS.between(acuan, LocalDate.of(2027, 1, 1)));
        System.out.println("Hari dalam seminggu: " + acuan.getDayOfWeek());

        // Immutable: plusDays mengembalikan object BARU
        LocalDate lusa = acuan.plusDays(2);
        System.out.println("acuan tetap " + acuan + ", lusa " + lusa);
        System.out.println("Jumat berikutnya: " + acuan.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)));
        System.out.println("Akhir bulan: " + acuan.with(TemporalAdjusters.lastDayOfMonth()));

        // Duration
        Duration rapat = Duration.between(LocalDateTime.of(2026, 11, 21, 9, 0), LocalDateTime.of(2026, 11, 21, 11, 45));
        System.out.println("Rapat: " + rapat + " = " + rapat.toMinutes() + " menit");

        // Zona waktu: satu Instant, tiga tampilan
        Instant kejadian = Instant.parse("2026-11-21T02:00:00Z");
        for (String zona : new String[] {"Asia/Jakarta", "Asia/Makassar", "Asia/Jayapura"}) {
            System.out.println(zona + " -> " + kejadian.atZone(ZoneId.of(zona)).toLocalTime());
        }
        ZonedDateTime tokyo = ZonedDateTime.of(2026, 11, 21, 9, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
        System.out.println("09:00 Tokyo = " + tokyo.withZoneSameInstant(ZoneId.of("Asia/Jakarta")).toLocalTime() + " WIB");

        // Format & parse
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));
        System.out.println(acuan.format(fmt));

        DateTimeFormatter longgar = DateTimeFormatter.ofPattern("dd/MM/yyyy");   // resolver default: SMART
        System.out.println("SMART  : 31/02/2026 -> " + LocalDate.parse("31/02/2026", longgar));   // dibulatkan diam-diam!

        DateTimeFormatter ketat = DateTimeFormatter.ofPattern("dd/MM/uuuu")      // STRICT butuh 'uuuu', bukan 'yyyy'
                .withResolverStyle(ResolverStyle.STRICT);
        System.out.println("Parse  : " + LocalDate.parse("05/03/2026", ketat));
        try {
            LocalDate.parse("31/02/2026", ketat);
        } catch (DateTimeParseException e) {
            System.out.println("STRICT menolak: " + e.getParsedString());
        }
    }
}
```

### Regex: Mem-parse Baris Log

```java
// DemoRegex.java
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DemoRegex {
    private static final Pattern LOG = Pattern.compile(
            "^(?<waktu>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[(?<level>[A-Z]+)] (?<pesan>.+)$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public static void main(String[] args) {
        String[] baris = {
            "2026-11-21 08:15:02 [INFO] Server mulai di port 8080",
            "2026-11-21 08:15:09 [ERROR] Koneksi database gagal (timeout 5000ms)",
            "baris rusak tanpa format"
        };

        for (String b : baris) {
            Matcher m = LOG.matcher(b);
            if (m.matches()) {
                System.out.printf("%-5s | %s | %s%n", m.group("level"), m.group("waktu").substring(11), m.group("pesan"));
            } else {
                System.out.println("TIDAK COCOK: " + b);
            }
        }

        // find() berulang: ekstrak semua angka
        Matcher angka = Pattern.compile("\\d+").matcher("port 8080, timeout 5000ms, retry 3");
        StringBuilder sb = new StringBuilder();
        while (angka.find()) sb.append(angka.group()).append(' ');
        System.out.println("Angka: " + sb.toString().trim());

        // replaceAll dengan grup: ubah 21/11/2026 -> 2026-11-21
        System.out.println("21/11/2026".replaceAll("(\\d{2})/(\\d{2})/(\\d{4})", "$3-$2-$1"));

        // split dengan regex & validasi
        System.out.println(java.util.Arrays.toString("a, b;c   d".split("[,;\\s]+")));
        for (String e : new String[] {"andi@contoh.com", "salah@", "a.b+tag@mail.co.id"}) {
            System.out.println(e + " -> " + EMAIL.matcher(e).matches());
        }
    }
}
```

### Kriptografi: Hash, Password, HMAC, AES-GCM, dan Tanda Tangan

```java
// DemoKripto.java
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DemoKripto {
    static final SecureRandom RNG = new SecureRandom();

    public static void main(String[] args) throws Exception {
        HexFormat hex = HexFormat.of();

        // 1) HASH: SHA-256 (deterministik; satu karakter beda -> hasil total berbeda)
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        System.out.println("SHA-256('halo') = " + hex.formatHex(sha.digest("halo".getBytes(StandardCharsets.UTF_8))));
        System.out.println("SHA-256('Halo') = " + hex.formatHex(sha.digest("Halo".getBytes(StandardCharsets.UTF_8))));

        // 2) PASSWORD: PBKDF2 + salt acak + iterasi tinggi (BUKAN SHA-256 polos)
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        byte[] h1 = pbkdf2("rahasia123".toCharArray(), salt);
        byte[] h2 = pbkdf2("rahasia123".toCharArray(), salt);
        byte[] h3 = pbkdf2("rahasia124".toCharArray(), salt);
        System.out.println("PBKDF2 sama untuk password+salt sama : " + MessageDigest.isEqual(h1, h2));   // perbandingan waktu-konstan
        System.out.println("PBKDF2 beda untuk password berbeda   : " + !MessageDigest.isEqual(h1, h3));

        // 3) HMAC: integritas + keaslian dengan kunci rahasia bersama
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("kunci-rahasia-bersama".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String tag = Base64.getEncoder().encodeToString(mac.doFinal("pesan penting".getBytes(StandardCharsets.UTF_8)));
        System.out.println("HMAC (base64) = " + tag);

        // 4) AES-GCM: enkripsi terautentikasi
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey kunci = kg.generateKey();

        byte[] iv = new byte[12];                       // 12 byte, ACAK, unik per enkripsi
        RNG.nextBytes(iv);
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, kunci, new GCMParameterSpec(128, iv));
        byte[] cipherText = enc.doFinal("nomor kartu: 1234-5678".getBytes(StandardCharsets.UTF_8));

        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, kunci, new GCMParameterSpec(128, iv));
        System.out.println("Dekripsi: " + new String(dec.doFinal(cipherText), StandardCharsets.UTF_8));

        cipherText[0] ^= 1;                             // simulasi data dimodifikasi penyerang
        try {
            Cipher dec2 = Cipher.getInstance("AES/GCM/NoPadding");
            dec2.init(Cipher.DECRYPT_MODE, kunci, new GCMParameterSpec(128, iv));
            dec2.doFinal(cipherText);
        } catch (AEADBadTagException e) {
            System.out.println("Modifikasi terdeteksi oleh GCM: " + e.getClass().getSimpleName());
        }

        // 5) TANDA TANGAN DIGITAL: Ed25519
        KeyPair pasangan = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        byte[] dokumen = "kontrak v1".getBytes(StandardCharsets.UTF_8);

        Signature ttd = Signature.getInstance("Ed25519");
        ttd.initSign(pasangan.getPrivate());
        ttd.update(dokumen);
        byte[] tandaTangan = ttd.sign();

        Signature verif = Signature.getInstance("Ed25519");
        verif.initVerify(pasangan.getPublic());
        verif.update(dokumen);
        System.out.println("Tanda tangan valid          : " + verif.verify(tandaTangan));

        Signature verif2 = Signature.getInstance("Ed25519");
        verif2.initVerify(pasangan.getPublic());
        verif2.update("kontrak v2".getBytes(StandardCharsets.UTF_8));
        System.out.println("Valid untuk dokumen diubah  : " + verif2.verify(tandaTangan));
    }

    static byte[] pbkdf2(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 310_000, 256);    // iterasi tinggi = lambat = aman
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }
}
```

> Contoh ini untuk **belajar**. Di aplikasi nyata, simpan hasil PBKDF2 bersama *salt* dan jumlah iterasi, dan lebih baik gunakan `PasswordEncoder` dari Spring Security (Argon2/bcrypt) daripada merakit sendiri.

### Networking: HTTP Server Mini + `HttpClient`

```java
// DemoJaringan.java
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class DemoJaringan {
    public static void main(String[] args) throws Exception {
        // Server: port 0 = biarkan OS memilih port kosong
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);

        server.createContext("/halo", ex -> {
            byte[] body = "{\"pesan\":\"Halo dari server Java\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(body); }
        });

        server.createContext("/echo", ex -> {
            byte[] masuk = ex.getRequestBody().readAllBytes();
            byte[] balas = ("echo(" + ex.getRequestMethod() + "): " + new String(masuk, StandardCharsets.UTF_8))
                    .getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, balas.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(balas); }
        });

        server.start();
        int port = server.getAddress().getPort();
        String base = "http://localhost:" + port;

        // Client
        HttpClient klien = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

        HttpResponse<String> get = klien.send(
                HttpRequest.newBuilder(URI.create(base + "/halo")).timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("GET  -> " + get.statusCode() + " " + get.body());

        HttpResponse<String> post = klien.send(
                HttpRequest.newBuilder(URI.create(base + "/echo"))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString("data dari klien"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("POST -> " + post.statusCode() + " " + post.body());

        HttpResponse<String> hilang = klien.send(
                HttpRequest.newBuilder(URI.create(base + "/tidak-ada")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("GET  -> " + hilang.statusCode() + " (path tidak terdaftar)");

        // Asinkron: sendAsync mengembalikan CompletableFuture
        klien.sendAsync(
                HttpRequest.newBuilder(URI.create(base + "/halo")).build(),
                HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::statusCode)
            .thenAccept(kode -> System.out.println("Async -> " + kode))
            .join();

        server.stop(0);
    }
}
```

## Latihan

1. Tulis program yang menerima tanggal lahir (`dd/MM/yyyy`) dan mencetak umur lengkap (tahun, bulan, hari), hari kelahirannya dalam bahasa Indonesia, serta jumlah hari sampai ulang tahun berikutnya. Gunakan resolver `STRICT` agar tanggal seperti `31/02/2026` ditolak (resolver default `SMART` akan membulatkannya diam-diam ke 28 Februari), dan tangani `DateTimeParseException`.
2. Sebuah rapat dijadwalkan pukul 10.00 di `Asia/Jakarta`. Tampilkan waktunya di `Asia/Makassar`, `Asia/Jayapura`, dan `America/New_York`. Mengapa menyimpan waktu kejadian sebagai `Instant` (UTC) lebih aman daripada `LocalDateTime`?
3. Tulis regex dengan grup bernama untuk mengekstrak `method`, `path`, dan `status` dari baris access log seperti `GET /api/produk/12 200 35ms`. Hitung berapa baris yang berstatus 5xx dari sebuah daftar contoh. Jelaskan apa itu ReDoS dan berikan satu contoh pola berbahaya.
4. Buat class `PasswordHasher` dengan method `hash(String)` dan `verifikasi(String, String)` memakai PBKDF2 + salt acak per pengguna, dengan format simpan `iterasi:salt:hash` (Base64). Mengapa SHA-256 polos dan `Random` tidak layak untuk tujuan ini?
5. Kembangkan DemoJaringan: tambahkan endpoint `/waktu` yang mengembalikan waktu server dalam format ISO, lalu buat client yang memanggil endpoint `/halo` dan `/waktu` **secara paralel** dengan `sendAsync` dan menggabungkan hasilnya via `CompletableFuture.allOf`. Tambahkan timeout dan tangani status non-2xx.

---

[← Modul 10: Concurrency: Thread, Virtual Thread & Java Memory Model](10-concurrency.md) · [Daftar Isi](README.md#daftar-isi) · [Modul 12: Build Tools, Java Modules & Logging →](12-build-tools-modules-logging.md)
