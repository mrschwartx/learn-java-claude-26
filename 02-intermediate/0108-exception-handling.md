# 0108 - Exception Handling

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Menjelaskan hierarki `Throwable`, `Error`, `Exception`, dan `RuntimeException`
- Membedakan checked dan unchecked exception
- Memakai `try`, `catch`, `finally`, multi-catch, dan try-with-resources
- Memakai `throw` dan `throws` dengan benar
- Membuat custom exception (termasuk exception chaining)
- Membaca stack trace
- Menghindari anti-pattern (swallowing, catch terlalu luas, exception untuk flow control)

---

## 1. Teori

### 1.1 Apa itu Exception?

**Exception** = kejadian tidak normal saat program berjalan yang mengganggu alur normal (input salah, file tidak ada, koneksi putus, pembagian dengan nol, dst.).

**Analogi:** alarm kebakaran. Tanpa alarm, kamu harus mengecek setiap ruangan satu per satu setiap saat (cek return code di setiap baris). Dengan alarm (exception), kerja normal jalan terus, dan begitu ada masalah, alarm **langsung memotong alur** dan mengarahkan ke prosedur penanganan (`catch`).

Kalau exception tidak ditangani sama sekali, program **berhenti** dan mencetak stack trace.

```java
int[] a = {1, 2, 3};
System.out.println(a[5]);   // program crashes here
System.out.println("Never reached");
```

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 3
	at Main.main(Main.java:4)
```

### 1.2 Hierarki Exception

```
Throwable
├── Error                      (masalah JVM/sistem, JANGAN ditangkap)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── IOException            (checked)
    ├── SQLException           (checked)
    ├── ... (checked lainnya)
    └── RuntimeException       (unchecked)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── ArithmeticException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        ├── NumberFormatException   (turunan IllegalArgumentException)
        └── UnsupportedOperationException
```

| Cabang | Artinya | Ditangani? |
|---|---|---|
| `Error` | kondisi fatal di luar kendali aplikasi | Hampir tidak pernah |
| `Exception` (checked) | kondisi yang **bisa diprediksi** dan dipulihkan (file tidak ada, jaringan putus) | Compiler **memaksa** |
| `RuntimeException` (unchecked) | **bug programmer** (null, index salah, argumen ilegal) | Opsional |

### 1.3 Checked vs Unchecked

| | Checked | Unchecked |
|---|---|---|
| Turunan | `Exception` (bukan `RuntimeException`) | `RuntimeException` |
| Dicek compiler? | **Ya**: harus `try-catch` atau `throws` | Tidak |
| Contoh | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Biasanya menandakan | kegagalan eksternal yang bisa dipulihkan | kesalahan logika/pemakaian |

```java
// checked: compile error if not handled
FileReader fr = new FileReader("data.txt");   // throws FileNotFoundException

// unchecked: compiles fine, may crash at runtime
String s = null;
s.length();
```

### 1.4 `try-catch`

```java
try {
    // code that might throw
    int result = 10 / 0;
    System.out.println("Not reached");
} catch (ArithmeticException e) {
    System.out.println("Error: " + e.getMessage());   // Error: / by zero
}
System.out.println("Program continues");
```

Alur: begitu ada exception di dalam `try`, sisa baris di `try` **dilewati** dan lompat ke `catch` yang cocok. Kalau tidak ada exception, `catch` dilewati.

Method berguna pada object exception:

| Method | Fungsi |
|---|---|
| `getMessage()` | pesan error |
| `getCause()` | exception penyebab (kalau ada) |
| `printStackTrace()` | cetak stack trace (untuk belajar, bukan untuk aplikasi nyata; pakai logger) |
| `getClass().getSimpleName()` | nama jenis exception |

### 1.5 Multiple `catch` dan Multi-Catch

```java
try {
    String text = args[0];
    int n = Integer.parseInt(text);
    System.out.println(100 / n);
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("No argument given");
} catch (NumberFormatException e) {
    System.out.println("Not a number");
} catch (ArithmeticException e) {
    System.out.println("Cannot divide by zero");
}
```

**Aturan urutan:** catch yang **lebih spesifik harus di atas** catch yang lebih umum. Kalau terbalik, compile error.

```java
try {
    // ...
} catch (Exception e) {                    // too general, placed first
} catch (NumberFormatException e) {        // compile error: already caught
}
```

**Multi-catch** (Java 7+) untuk penanganan yang sama:

```java
try {
    // ...
} catch (NumberFormatException | ArithmeticException e) {
    System.out.println("Invalid input: " + e.getMessage());
}
```

Tipe di multi-catch tidak boleh saling turunan satu sama lain.

### 1.6 `finally`

Block yang **selalu dijalankan**, ada exception atau tidak (bahkan setelah `return`). Dipakai untuk **cleanup** (tutup resource).

```java
try {
    System.out.println("try");
    throw new RuntimeException("boom");
} catch (RuntimeException e) {
    System.out.println("catch");
} finally {
    System.out.println("finally");   // always runs
}
```

`finally` baru **tidak** jalan kalau JVM mati (`System.exit()`, crash fatal).

**Jebakan `return` di `finally`:**

```java
static int a() {
    try { return 1; }
    finally { System.out.println("cleanup"); }   // prints, then returns 1
}

static int b() {
    int x = 1;
    try { return x; }
    finally { x = 99; }                          // returns 1 (value already captured)
}

static int c() {
    try { return 1; }
    finally { return 2; }                        // returns 2 and swallows any exception: NEVER do this
}
```

### 1.7 Try-With-Resources

Resource yang perlu ditutup (file, koneksi, stream) harus mengimplementasi `AutoCloseable`. Dengan try-with-resources, `close()` dipanggil **otomatis**, dalam urutan **terbalik** dari pembukaan, bahkan jika terjadi exception.

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
    String line = reader.readLine();
    System.out.println(line);
} catch (IOException e) {
    System.out.println("Cannot read file: " + e.getMessage());
}
// reader is closed automatically here
```

Lebih dari satu resource:

```java
try (Res a = new Res("A"); Res b = new Res("B")) {
    // use a and b
}
// closes B first, then A
```

Selalu pilih try-with-resources daripada `try/finally` manual. Kode manual rawan lupa menutup atau menutupnya salah saat exception.

Membuat resource sendiri:

```java
class Connection implements AutoCloseable {
    @Override
    public void close() {
        System.out.println("Connection closed");
    }
}
```

### 1.8 `throw` dan `throws`

| Keyword | Fungsi |
|---|---|
| `throw` | **melempar** satu object exception (statement) |
| `throws` | **mendeklarasikan** bahwa method bisa melempar exception tertentu (di signature) |

```java
public static double divide(int a, int b) {
    if (b == 0) {
        throw new IllegalArgumentException("Divisor must not be zero");
    }
    return (double) a / b;
}

// checked exception: must be declared or handled
public static String readFirstLine(String path) throws IOException {
    try (BufferedReader r = new BufferedReader(new FileReader(path))) {
        return r.readLine();
    }
}
```

Untuk checked exception, pemanggil punya dua pilihan:
1. **Tangani** dengan `try-catch`
2. **Teruskan** dengan `throws` di method-nya sendiri

Prinsip: tangani exception di **level yang tahu cara menanganinya**. Kalau method tidak tahu harus berbuat apa, biarkan naik ke pemanggil.

### 1.9 Custom Exception

Buat exception sendiri agar kesalahan **bermakna secara domain**.

```java
// checked: caller is forced to handle it
public class InsufficientFundsException extends Exception {
    private final double shortfall;

    public InsufficientFundsException(String message, double shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public double getShortfall() {
        return shortfall;
    }
}

// unchecked: for programming/validation errors
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

Konvensi:
- Nama diakhiri `Exception`
- Sediakan constructor `(String message)` dan `(String message, Throwable cause)`
- Tulis pesan error yang jelas dan spesifik (**dalam bahasa Inggris**, sesuai standar kode di project)

**Checked atau unchecked?**
- Framework modern (Spring) dan sebagian besar kode baru **lebih memilih unchecked**, karena checked exception membuat signature "tercemar" di seluruh layer.
- Pakai **checked** kalau pemanggil realistis bisa **memulihkan** keadaan dan kamu ingin memaksanya mempertimbangkan itu.
- Pakai **unchecked** untuk kesalahan pemakaian, validasi, dan kondisi yang tidak bisa dipulihkan pemanggil.

### 1.10 Exception Chaining

Saat menangkap exception lalu melempar exception lain (biasanya di tingkat abstraksi berbeda), **sertakan penyebab aslinya** sebagai `cause`. Kalau tidak, informasi diagnosis hilang.

```java
public int parsePort(String value) {
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        throw new ConfigException("Invalid port: " + value, e);   // keep the cause
    }
}
```

Stack trace akan menampilkan `Caused by: ...`. Ini disebut **exception translation**: mengubah exception level rendah (SQLException) menjadi exception level domain (UserNotFoundException) tanpa kehilangan jejak.

### 1.11 Membaca Stack Trace

```
Exception in thread "main" com.example.ConfigException: Invalid port: abc
	at com.example.Config.parsePort(Config.java:12)
	at com.example.Main.main(Main.java:5)
Caused by: java.lang.NumberFormatException: For input string: "abc"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.base/java.lang.Integer.parseInt(Integer.java:661)
	at com.example.Config.parsePort(Config.java:10)
	... 1 more
```

Cara membaca:
1. Baris pertama: **jenis dan pesan** exception
2. Baris `at ...`: **jalur pemanggilan**, paling atas = tempat terjadinya, paling bawah = awal program
3. Cari baris **pertama yang berasal dari kodemu** (bukan `java.base`), di situlah biasanya akar masalahnya
4. `Caused by:` menunjukkan penyebab paling dalam. **Penyebab sebenarnya biasanya ada di `Caused by` paling bawah**.

### 1.12 Exception yang Sering Ditemui

| Exception | Penyebab umum |
|---|---|
| `NullPointerException` | memanggil method/field pada `null` |
| `ArrayIndexOutOfBoundsException` | index di luar `0..length-1` |
| `StringIndexOutOfBoundsException` | index salah pada `String` |
| `ClassCastException` | downcast ke tipe yang salah |
| `NumberFormatException` | `Integer.parseInt("abc")` |
| `ArithmeticException` | pembagian **integer** dengan nol |
| `IllegalArgumentException` | argumen method tidak valid |
| `IllegalStateException` | method dipanggil pada saat yang tidak tepat (state object tidak sesuai) |
| `UnsupportedOperationException` | operasi tidak didukung (misal `add` pada `List.of(...)`) |
| `ConcurrentModificationException` | memodifikasi collection saat diiterasi (materi 0109) |
| `StackOverflowError` (Error) | rekursi tanpa base case |
| `OutOfMemoryError` (Error) | heap habis |
| `IOException`, `FileNotFoundException` (checked) | masalah I/O |
| `InputMismatchException` | `Scanner.nextInt()` menerima teks |

Catatan: pembagian `double` dengan nol **tidak** melempar exception.

```java
System.out.println(10 / 0);        // ArithmeticException
System.out.println(10.0 / 0);      // Infinity
System.out.println(0.0 / 0);       // NaN
```

### 1.13 `IllegalArgumentException` vs `IllegalStateException`

| | Maknanya | Contoh |
|---|---|---|
| `IllegalArgumentException` | **argumen**-nya yang salah | `setAge(-5)` |
| `IllegalStateException` | argumen benar, tapi **object sedang di state yang tidak mengizinkan** | `connection.send()` setelah `close()` |

Helper bawaan untuk cek `null`:

```java
import java.util.Objects;

public Student(String name) {
    this.name = Objects.requireNonNull(name, "Name must not be null");
}
```

### 1.14 Best Practice dan Anti-Pattern

**Lakukan:**
- Tangkap exception **spesifik** (`NumberFormatException`), bukan `Exception`
- **Fail fast**: validasi argumen di awal method dan lempar exception secepatnya
- Pesan error berisi **konteks** ("User not found: id=42", bukan "Error")
- Sertakan `cause` saat membungkus exception
- Pakai try-with-resources untuk resource
- Di aplikasi nyata, **log** exception (SLF4J/Logback di Spring), jangan `printStackTrace()`
- Dokumentasikan exception penting di Javadoc (`@throws`)

**Jangan:**

```java
// 1. Swallowing: the worst anti-pattern, errors vanish silently
try {
    doSomething();
} catch (Exception e) {
    // ignore
}

// 2. Catching Throwable/Exception everywhere
catch (Throwable t) { }          // even catches OutOfMemoryError

// 3. Exception for normal control flow
try {
    int i = 0;
    while (true) { array[i++] = 0; }
} catch (ArrayIndexOutOfBoundsException e) { }   // use a normal loop

// 4. Log and rethrow (duplicate noise in logs)
catch (IOException e) {
    log.error("failed", e);
    throw e;
}

// 5. Losing the cause
catch (SQLException e) {
    throw new RuntimeException("DB error");      // original exception lost!
}

// 6. Generic useless messages and types
throw new Exception("error");

// 7. return inside finally
```

### 1.15 Exception dan Overriding (Singkat)

Method yang meng-override **tidak boleh** melempar checked exception yang **lebih luas** dari method parent. Boleh lebih sempit, atau tidak melempar sama sekali. Unchecked exception bebas.

### 1.16 Ringkasan Alur

```
Ada exception di try?
├─ Tidak → catch dilewati → finally → lanjut
└─ Ya → cari catch yang cocok dari atas ke bawah
        ├─ Ketemu → jalankan catch → finally → lanjut
        └─ Tidak ketemu → finally → exception naik ke pemanggil (ulang proses di sana)
                           (kalau sampai main tanpa penanganan: program berhenti)
```

---

## 2. Praktek

> Semua contoh ditulis dalam satu file `Main.java`. Hanya `Main` yang `public`.

### Praktek 2.1: `try-catch` Dasar

```java
public class Main {
    public static void main(String[] args) {
        try {
            int result = 10 / 0;
            System.out.println("Result: " + result);   // skipped
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e.getMessage());
        }
        System.out.println("Program continues normally");

        try {
            String text = null;
            System.out.println(text.length());
        } catch (NullPointerException e) {
            System.out.println("NPE caught: " + e.getMessage());
        }

        try {
            int n = Integer.parseInt("12abc");
            System.out.println(n);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + e.getMessage());
        }
    }
}
```

### Praktek 2.2: Multiple Catch dan Multi-Catch

```java
public class Main {

    static void process(String[] inputs, int index) {
        try {
            String value = inputs[index];
            int number = Integer.parseInt(value);
            System.out.println("100 / " + number + " = " + (100 / number));
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Index " + index + " does not exist");
        } catch (NumberFormatException | ArithmeticException e) {
            System.out.println("Bad value: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String[] data = {"5", "abc", "0"};

        process(data, 0);   // 100 / 5 = 20
        process(data, 1);   // NumberFormatException
        process(data, 2);   // ArithmeticException
        process(data, 9);   // ArrayIndexOutOfBoundsException
    }
}
```

### Praktek 2.3: `finally` dan Urutan Eksekusi

```java
public class Main {

    static int test(int mode) {
        try {
            System.out.println("try " + mode);
            if (mode == 1) {
                throw new IllegalStateException("mode 1");
            }
            if (mode == 2) {
                return 200;
            }
            return 0;
        } catch (IllegalStateException e) {
            System.out.println("catch " + e.getMessage());
            return -1;
        } finally {
            System.out.println("finally " + mode);
        }
    }

    public static void main(String[] args) {
        System.out.println("returned " + test(0));
        System.out.println("returned " + test(1));
        System.out.println("returned " + test(2));
    }
}
```

### Praktek 2.4: Try-With-Resources

```java
class Connection implements AutoCloseable {
    private final String name;

    Connection(String name) {
        this.name = name;
        System.out.println("Open " + name);
    }

    void query(String sql) {
        if (sql.isBlank()) {
            throw new IllegalArgumentException("SQL must not be blank");
        }
        System.out.println(name + " executes: " + sql);
    }

    @Override
    public void close() {
        System.out.println("Close " + name);
    }
}

public class Main {
    public static void main(String[] args) {
        try (Connection c1 = new Connection("DB-1");
             Connection c2 = new Connection("DB-2")) {

            c1.query("SELECT 1");
            c2.query("   ");             // throws
            System.out.println("Not reached");

        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        } finally {
            System.out.println("Finally");
        }
    }
}
```

Output yang diharapkan:

```
Open DB-1
Open DB-2
DB-1 executes: SELECT 1
Close DB-2
Close DB-1
Caught: SQL must not be blank
Finally
```

Perhatikan: resource **ditutup dulu (urutan terbalik)** baru `catch` jalan.

### Praktek 2.5: Custom Checked Exception

```java
class InsufficientFundsException extends Exception {
    private final double shortfall;

    InsufficientFundsException(String message, double shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    double getShortfall() {
        return shortfall;
    }
}

class Account {
    private double balance;

    Account(double balance) {
        this.balance = balance;
    }

    void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException(
                    "Insufficient funds: balance=" + balance + ", requested=" + amount,
                    amount - balance);
        }
        balance -= amount;
    }

    double getBalance() {
        return balance;
    }
}

public class Main {
    public static void main(String[] args) {
        Account account = new Account(1000);

        try {
            account.withdraw(300);
            System.out.println("Balance: " + account.getBalance());   // 700.0
            account.withdraw(900);                                    // throws
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
            System.out.println("You need " + e.getShortfall() + " more");
        }
    }
}
```

### Praktek 2.6: Exception Chaining

```java
class ConfigException extends RuntimeException {
    ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class Main {

    static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid port: " + value, e);   // keep the cause
        }
    }

    public static void main(String[] args) {
        try {
            parsePort("80x");
        } catch (ConfigException e) {
            System.out.println("Message: " + e.getMessage());
            System.out.println("Cause  : " + e.getCause());
        }
    }
}
```

### Praktek 2.7: Input Aman dengan Loop

```java
import java.util.Scanner;

public class Main {

    static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, please try again");
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int age = readInt(scanner, "Enter your age: ");
        System.out.println("Next year you will be " + (age + 1));

        scanner.close();
    }
}
```

Pola `nextLine()` + `parseInt` jauh lebih tahan banting dibanding `nextInt()` langsung, dan sekaligus menghindari jebakan newline yang tertinggal.

---

## 3. Latihan

**Soal 1.** Prediksi output (jejak lengkap):

```java
public class Quiz {
    static void run(int n) {
        try {
            System.out.println("start " + n);
            int r = 10 / n;
            System.out.println("result " + r);
            String s = null;
            if (n == 2) {
                s.length();
            }
        } catch (ArithmeticException e) {
            System.out.println("arith: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("npe");
        } finally {
            System.out.println("finally " + n);
        }
        System.out.println("end " + n);
    }

    public static void main(String[] args) {
        run(0);
        run(2);
        run(5);
    }
}
```

**Soal 2.** Buat method `safeDivide(int a, int b)` yang mengembalikan `double`. Jika `b == 0`, lempar `IllegalArgumentException("Divisor must not be zero")`. Di `main`, panggil beberapa kali dengan `try-catch`. Lalu jawab: apa yang terjadi kalau method memakai `(double) a / b` tanpa pengecekan, dan kenapa?

**Soal 3.** Buat method `parseOrDefault(String text, int defaultValue)` yang mengembalikan hasil `parseInt`, atau `defaultValue` jika teks tidak valid (termasuk `null`). Uji dengan `"42"`, `"abc"`, `""`, `null`, `" 7 "`.

**Soal 4.** Refactor `BankAccount` dari materi 0106: buat checked exception `InsufficientFundsException` (berisi `shortfall`) yang dilempar `withdraw`. `deposit` melempar `IllegalArgumentException` jika amount `<= 0`. Tambahkan `transferTo(BankAccount target, double amount)` yang **atomik**: kalau withdraw gagal, saldo target tidak berubah. Uji di `main`.

**Soal 5.** Buat `ValidationException` (unchecked) dan method `register(String username, String email, int age)` dengan aturan:
- `username`: tidak null/blank, minimal 3 karakter
- `email`: tidak null, mengandung `@`
- `age`: 17 sampai 100

Lempar `ValidationException` dengan pesan jelas pada pelanggaran pertama yang ditemukan. Uji dengan beberapa input valid dan tidak valid.

**Soal 6.** Prediksi nilai kembalian dan output:

```java
public class Quiz {
    static int a() {
        try { return 1; }
        finally { System.out.println("A-finally"); }
    }

    static int b() {
        int x = 1;
        try { return x; }
        finally { x = 99; System.out.println("B-finally"); }
    }

    static int c() {
        try { throw new RuntimeException("boom"); }
        finally { return 3; }
    }

    public static void main(String[] args) {
        System.out.println(a());
        System.out.println(b());
        System.out.println(c());
    }
}
```

**Soal 7.** Kode berikut penuh anti-pattern. Temukan semuanya dan tulis ulang dengan benar:

```java
public class ReportService {

    public int readAge(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
        }
        return 0;
    }

    public void saveUser(String name) throws Exception {
        if (name == null) {
            throw new Exception("error");
        }
        try {
            Object o = null;
            o.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
```

**Soal 8.** Prediksi urutan output:

```java
class Res implements AutoCloseable {
    private final String name;
    Res(String name) { this.name = name; System.out.println("open " + name); }
    @Override public void close() { System.out.println("close " + name); }
}

public class Quiz {
    public static void main(String[] args) {
        try (Res a = new Res("A"); Res b = new Res("B")) {
            System.out.println("body");
            throw new RuntimeException("boom");
        } catch (RuntimeException e) {
            System.out.println("catch " + e.getMessage());
        } finally {
            System.out.println("finally");
        }
    }
}
```

**Soal 9 (Bonus).** Retry: simulasikan operasi tidak stabil (`fetchData()` yang gagal 2 kali pertama dengan `TemporaryFailureException` (checked), lalu berhasil di percobaan ke-3). Buat `fetchWithRetry(int maxAttempts)` yang mencoba ulang sampai `maxAttempts`. Jika semua gagal, lempar `RuntimeException` yang membungkus exception terakhir sebagai `cause`. Uji dengan `maxAttempts = 3` (berhasil) dan `maxAttempts = 2` (gagal).

**Soal 10 (Bonus).** Exception translation: buat `UserRepository.findNameById(int id) throws SQLException` (simulasi: `id <= 0` melempar `SQLException("Connection lost")`). Buat `UserService.getUserName(int id)` yang **menangkap** `SQLException` dan melempar `UserNotAvailableException` (unchecked) dengan **cause**. Di `main`, tangkap exception service, lalu telusuri dan cetak seluruh rantai penyebab (`getCause()` berulang) sampai paling dalam.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```
start 0
arith: / by zero
finally 0
end 0
start 2
result 5
npe
finally 2
end 2
start 5
result 2
finally 5
end 5
```

- `run(0)`: `10 / 0` melempar `ArithmeticException` sebelum `result` dicetak. Catch pertama menangani, `finally` jalan, lalu program lanjut ke `end`.
- `run(2)`: `10 / 2 = 5` aman, lalu `s.length()` pada `null` melempar NPE. `finally` tetap jalan.
- `run(5)`: tidak ada exception, `catch` dilewati. `finally` **tetap** jalan.

### Jawaban Soal 2

```java
public class SafeDivideDemo {

    public static double safeDivide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Divisor must not be zero");
        }
        return (double) a / b;
    }

    public static void main(String[] args) {
        int[][] cases = {{10, 4}, {7, 0}, {-9, 3}};

        for (int[] c : cases) {
            try {
                System.out.println(c[0] + " / " + c[1] + " = " + safeDivide(c[0], c[1]));
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
```

Output: `10 / 4 = 2.5`, `Error: Divisor must not be zero`, `-9 / 3 = -3.0`.

Kalau `(double) a / b` dipakai tanpa pengecekan dan `b == 0`, hasilnya **`Infinity`** (atau `-Infinity`/`NaN`), **tanpa exception**. Alasannya: aturan IEEE 754 untuk floating-point mendefinisikan pembagian dengan nol sebagai nilai khusus. Hanya pembagian **integer** yang melempar `ArithmeticException`. Bahayanya: `Infinity` bisa mengalir diam-diam ke perhitungan berikutnya dan merusak hasil tanpa ada sinyal error. Karena itu validasi eksplisit lebih aman.

### Jawaban Soal 3

```java
public class ParseUtil {

    public static int parseOrDefault(String text, int defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        System.out.println(parseOrDefault("42", -1));     // 42
        System.out.println(parseOrDefault("abc", -1));    // -1
        System.out.println(parseOrDefault("", -1));       // -1
        System.out.println(parseOrDefault(null, -1));     // -1
        System.out.println(parseOrDefault(" 7 ", -1));    // 7
    }
}
```

`Integer.parseInt(null)` melempar `NumberFormatException`, tetapi cek `null` eksplisit membuat maksud kode lebih jelas. `trim()` dipakai agar `" 7 "` tetap valid. Perhatikan bahwa kita hanya menangkap `NumberFormatException` yang spesifik, bukan `Exception`.

### Jawaban Soal 4

```java
class InsufficientFundsException extends Exception {
    private final double shortfall;

    InsufficientFundsException(String message, double shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    double getShortfall() {
        return shortfall;
    }
}

class BankAccount {
    private final String accountNumber;
    private double balance;

    BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = Math.max(initialBalance, 0);
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException(
                    "Insufficient funds in " + accountNumber
                            + ": balance=" + balance + ", requested=" + amount,
                    amount - balance);
        }
        balance -= amount;
    }

    public void transferTo(BankAccount target, double amount) throws InsufficientFundsException {
        if (target == null) {
            throw new IllegalArgumentException("Target account must not be null");
        }
        withdraw(amount);       // if this throws, nothing below runs: target is untouched
        target.deposit(amount);
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount a = new BankAccount("111", 1000);
        BankAccount b = new BankAccount("222", 500);

        try {
            a.transferTo(b, 300);
            System.out.println("A=" + a.getBalance() + ", B=" + b.getBalance());   // A=700.0, B=800.0

            a.transferTo(b, 5000);   // fails
            System.out.println("Not reached");
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
            System.out.println("Shortfall: " + e.getShortfall());                  // 4300.0
        }

        System.out.println("A=" + a.getBalance() + ", B=" + b.getBalance());       // unchanged: 700.0 / 800.0
    }
}
```

Atomik di sini dijamin oleh **urutan**: `withdraw` (yang bisa gagal) dijalankan **sebelum** `deposit`. Kalau `withdraw` melempar exception, baris `deposit` tidak pernah tercapai. Di sistem nyata dengan database, atomisitas dijamin lewat **transaction** (`@Transactional` di Spring).

### Jawaban Soal 5

```java
class ValidationException extends RuntimeException {
    ValidationException(String message) {
        super(message);
    }
}

public class RegistrationDemo {

    static void register(String username, String email, int age) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username must not be blank");
        }
        if (username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters: " + username);
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email is invalid: " + email);
        }
        if (age < 17 || age > 100) {
            throw new ValidationException("Age must be between 17 and 100: " + age);
        }
        System.out.println("Registered: " + username.trim() + " <" + email + ">, age " + age);
    }

    public static void main(String[] args) {
        Object[][] cases = {
            {"budi", "budi@mail.com", 25},
            {"  ", "x@mail.com", 25},
            {"al", "al@mail.com", 25},
            {"sari", "sari-mail.com", 25},
            {"andi", "andi@mail.com", 12},
            {null, "n@mail.com", 30}
        };

        for (Object[] c : cases) {
            try {
                register((String) c[0], (String) c[1], (Integer) c[2]);
            } catch (ValidationException e) {
                System.out.println("Rejected: " + e.getMessage());
            }
        }
    }
}
```

Urutan pengecekan penting: cek `null`/blank **sebelum** memanggil `length()`, kalau tidak akan jadi NPE (bug sendiri). Untuk mengumpulkan **semua** error sekaligus (bukan berhenti di yang pertama), kamu akan memakai `List<String>` di materi Collections.

### Jawaban Soal 6

```
A-finally
1
B-finally
1
3
```

- `a()`: `finally` jalan sebelum method benar-benar kembali, lalu mengembalikan `1`.
- `b()`: nilai `x` (yaitu `1`) sudah "diambil" untuk di-return sebelum `finally` jalan. Mengubah `x = 99` di `finally` tidak memengaruhi hasil, jadi tetap `1`.
- `c()`: `finally` melakukan `return 3`, yang **menimpa** exception yang sedang terbang. `RuntimeException("boom")` hilang tanpa jejak, dan method mengembalikan `3`. Inilah kenapa `return` di dalam `finally` dilarang keras.

### Jawaban Soal 7

Masalah:

1. `catch (Exception e) { }` di `readAge`: **swallowing**. Error tertelan dan `0` dikembalikan seolah input valid, jadi pemanggil tidak bisa membedakan "umur 0" dengan "input rusak".
2. `catch (Exception e)` terlalu luas, padahal yang bisa terjadi hanya `NumberFormatException`.
3. `throws Exception` di `saveUser`: terlalu umum, memaksa pemanggil menangani `Exception` apa pun.
4. `throw new Exception("error")`: tipe generik dan pesan tidak bermakna.
5. Input `null` adalah kesalahan argumen, seharusnya `IllegalArgumentException`/`NullPointerException` dengan pesan jelas.
6. `catch (Throwable t)` menangkap bahkan `OutOfMemoryError`.
7. `t.printStackTrace()`: bukan logging yang benar, dan setelah itu error **tetap tertelan** (method terlihat sukses).
8. Kode di `try` sengaja membuat NPE, tapi NPE adalah bug yang harus diperbaiki, bukan ditangkap.

Perbaikan:

```java
import java.util.Objects;

public class ReportService {

    /**
     * Parses the given text as an age.
     *
     * @throws IllegalArgumentException if the text is not a valid integer
     */
    public int readAge(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a number: " + input, e);
        }
    }

    public void saveUser(String name) {
        Objects.requireNonNull(name, "Name must not be null");
        // real code would call a repository here and let a meaningful exception propagate
    }
}
```

Prinsipnya: **jangan menelan error**. Lempar kembali dalam bentuk yang bermakna (dengan `cause`), atau tangani dengan benar (pulihkan keadaan, tampilkan pesan ke user).

### Jawaban Soal 8

```
open A
open B
body
close B
close A
catch boom
finally
```

Urutan:
1. Resource dibuka berurutan: `A`, lalu `B`.
2. `body` dicetak, lalu exception dilempar.
3. Sebelum `catch` jalan, resource **ditutup dalam urutan terbalik**: `B` lalu `A`.
4. `catch`, kemudian `finally`.

Aturan pegangan: resource yang dibuka paling akhir ditutup paling awal (seperti tumpukan piring).

### Jawaban Soal 9

```java
class TemporaryFailureException extends Exception {
    TemporaryFailureException(String message) {
        super(message);
    }
}

public class RetryDemo {

    private static int calls = 0;

    // simulation: fails on the first 2 calls, succeeds on the 3rd
    static String fetchData() throws TemporaryFailureException {
        calls++;
        if (calls < 3) {
            throw new TemporaryFailureException("Service unavailable (call " + calls + ")");
        }
        return "DATA";
    }

    static String fetchWithRetry(int maxAttempts) {
        TemporaryFailureException lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String result = fetchData();
                System.out.println("Succeeded on attempt " + attempt);
                return result;
            } catch (TemporaryFailureException e) {
                lastError = e;
                System.out.println("Attempt " + attempt + " failed: " + e.getMessage());
            }
        }
        throw new RuntimeException("All " + maxAttempts + " attempts failed", lastError);
    }

    public static void main(String[] args) {
        calls = 0;
        System.out.println("Result: " + fetchWithRetry(3));

        calls = 0;
        try {
            fetchWithRetry(2);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }
    }
}
```

Output skenario 1: gagal di percobaan 1 dan 2, berhasil di ke-3. Skenario 2 (`maxAttempts = 2`): kedua percobaan gagal, lalu `RuntimeException` dilempar dengan `cause` berisi kegagalan terakhir. Di dunia nyata, retry biasanya diberi **jeda** (`Thread.sleep`) dan **backoff** eksponensial, dan hanya dipakai untuk kegagalan sementara (bukan untuk error validasi).

### Jawaban Soal 10

```java
import java.sql.SQLException;

class UserNotAvailableException extends RuntimeException {
    UserNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

class UserRepository {
    String findNameById(int id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("Connection lost");
        }
        return "User-" + id;
    }
}

class UserService {
    private final UserRepository repository = new UserRepository();

    public String getUserName(int id) {
        try {
            return repository.findNameById(id);
        } catch (SQLException e) {
            // translate low-level exception into a domain exception, keep the cause
            throw new UserNotAvailableException("Cannot load user with id=" + id, e);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        UserService service = new UserService();

        System.out.println(service.getUserName(7));   // User-7

        try {
            service.getUserName(-1);
        } catch (UserNotAvailableException e) {
            Throwable current = e;
            int level = 0;
            while (current != null) {
                System.out.println("  ".repeat(level) + current.getClass().getSimpleName()
                        + ": " + current.getMessage());
                current = current.getCause();
                level++;
            }
        }
    }
}
```

Output:

```
User-7
UserNotAvailableException: Cannot load user with id=-1
  SQLException: Connection lost
```

Alasan translation: layer atas (controller/service) tidak seharusnya bergantung pada detail `SQLException` (itu bocoran detail implementasi database). Dengan membungkusnya menjadi exception domain, kamu bebas mengganti database nanti tanpa mengubah layer atas, dan `cause` menjaga informasi diagnosis tetap utuh. Persis pola ini yang dipakai Spring (`DataAccessException`).
