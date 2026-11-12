# 0101 - Program Structure, Variables, and Data Types

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Menjelaskan perbedaan JDK, JRE, dan JVM
- Menulis, compile, dan menjalankan program Java sederhana
- Mendeklarasikan variable dengan tipe data yang tepat
- Memakai `String`, `var`, dan `final`
- Membaca input dari keyboard dengan `Scanner`

---

## 1. Teori

### 1.1 JDK, JRE, JVM

| Istilah | Fungsi |
|---|---|
| **JVM** (Java Virtual Machine) | Menjalankan bytecode (`.class`). Inilah yang bikin Java "write once, run anywhere". |
| **JRE** (Java Runtime Environment) | JVM + library standar. Cukup untuk *menjalankan* program. |
| **JDK** (Java Development Kit) | JRE + compiler (`javac`) + tools lain. Dibutuhkan untuk *membuat* program. |

**Analogi:** JDK itu seperti dapur lengkap (kompor, pisau, bahan). JRE itu meja makan dan piring. JVM itu orang yang makan. Kalau kamu cuma mau makan, cukup meja makan. Kalau mau masak, butuh dapur.

### 1.2 Alur Compile dan Run

```
Main.java --(javac)--> Main.class (bytecode) --(java)--> JVM menjalankan
```

```bash
javac Main.java   # compile
java Main         # run
```

### 1.3 Struktur Program Java

```java
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

| Bagian | Arti |
|---|---|
| `public class Main` | Deklarasi class. Nama file harus `Main.java`. |
| `public static void main(String[] args)` | Entry point. JVM mulai dari sini. |
| `System.out.println(...)` | Cetak ke console + pindah baris. |
| `;` | Wajib di akhir setiap statement. |
| `{ }` | Block (scope) kode. |

Java itu **case-sensitive**: `Main` berbeda dengan `main`.

### 1.4 Comment

```java
// single-line comment

/* multi-line
   comment */

/** Javadoc comment, used to generate documentation */
```

### 1.5 Variable

**Analogi:** variable itu **kotak berlabel dengan tipe tetap**. Kotak angka tidak bisa diisi teks.

```java
int age = 25;          // declaration + initialization
int score;             // declaration only
score = 90;            // assignment later
```

**Naming convention:**
- Variable dan method: `camelCase` (`studentName`)
- Class: `PascalCase` (`StudentService`)
- Constant: `UPPER_SNAKE_CASE` (`MAX_RETRY`)
- Tidak boleh diawali angka, tidak boleh pakai keyword (`class`, `int`, dst.)
- Boleh diawali huruf, `_`, atau `$` (tapi praktik umum: huruf saja)

### 1.6 Primitive Data Types

| Type | Ukuran | Range / Isi | Contoh |
|---|---|---|---|
| `byte` | 8 bit | -128 s/d 127 | `byte b = 100;` |
| `short` | 16 bit | -32.768 s/d 32.767 | `short s = 1000;` |
| `int` | 32 bit | sekitar -2,1 miliar s/d 2,1 miliar | `int age = 25;` |
| `long` | 64 bit | sangat besar | `long big = 10000000000L;` |
| `float` | 32 bit | desimal, presisi ~7 digit | `float f = 3.14f;` |
| `double` | 64 bit | desimal, presisi ~15 digit | `double d = 3.14159;` |
| `char` | 16 bit | satu karakter Unicode | `char c = 'A';` |
| `boolean` | - | `true` atau `false` | `boolean ok = true;` |

Aturan penting:
- Integer literal default-nya `int`. Untuk `long`, tambahkan `L`.
- Desimal literal default-nya `double`. Untuk `float`, tambahkan `f`.
- `char` pakai **single quote** `'A'`. `String` pakai **double quote** `"A"`.
- Boleh pakai underscore agar angka mudah dibaca: `1_000_000`.

### 1.7 Default Value

Variable di dalam **method** (local variable) **wajib diinisialisasi** sebelum dipakai. Field di dalam class punya default value (`0`, `0.0`, `false`, `'\u0000'`, `null`). Ini akan dibahas lagi di materi OOP.

### 1.8 `String`

`String` adalah **class** (reference type), bukan primitive. Nilainya **immutable** (tidak bisa diubah setelah dibuat).

```java
String name = "Budi";
String full = name + " Santoso";   // concatenation creates a NEW string

name.length();          // 4
name.toUpperCase();     // "BUDI"
name.charAt(0);         // 'B'
name.substring(1, 3);   // "ud"
name.contains("ud");    // true
name.equals("Budi");    // true
```

> Bandingkan isi `String` dengan `.equals()`, **bukan** `==`. `==` membandingkan reference (alamat objek), bukan isi.

### 1.9 `var` (Java 10+)

Compiler menyimpulkan tipe dari nilai. Hanya untuk **local variable** dan wajib langsung diinisialisasi.

```java
var count = 10;       // int
var title = "Java";   // String
// var x;             // compile error: cannot infer type
```

Tipe tetap statis. `count` tidak bisa diisi `"abc"` setelahnya.

### 1.10 `final` (Constant)

```java
final double PI = 3.14159;
// PI = 3.14;  // compile error: cannot assign a value to final variable
```

### 1.11 Output: `print`, `println`, `printf`

```java
System.out.print("No newline");
System.out.println("With newline");
System.out.printf("Name: %s, Age: %d, GPA: %.2f%n", "Budi", 20, 3.456);
```

| Format | Untuk |
|---|---|
| `%s` | String |
| `%d` | integer (`byte`, `short`, `int`, `long`) |
| `%f` / `%.2f` | desimal / 2 digit di belakang koma |
| `%c` | char |
| `%b` | boolean |
| `%n` | newline (portable antar OS) |

### 1.12 Input: `Scanner`

```java
import java.util.Scanner;

Scanner scanner = new Scanner(System.in);
String name = scanner.nextLine();
int age = scanner.nextInt();
double height = scanner.nextDouble();
scanner.close();
```

> **Jebakan klasik:** setelah `nextInt()` atau `nextDouble()`, ada karakter newline yang tertinggal di buffer. Kalau langsung `nextLine()`, hasilnya string kosong. Solusi: panggil `scanner.nextLine();` sekali untuk "membuang" newline tersebut.

---

## 2. Praktek

### Praktek 2.1: Hello World

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

### Praktek 2.2: Semua Primitive Type

```java
public class PrimitiveDemo {
    public static void main(String[] args) {
        byte myByte = 100;
        short myShort = 30_000;
        int myInt = 2_000_000_000;
        long myLong = 9_000_000_000L;
        float myFloat = 3.14f;
        double myDouble = 3.141592653589793;
        char myChar = 'J';
        boolean myBoolean = true;

        System.out.println("byte    : " + myByte);
        System.out.println("short   : " + myShort);
        System.out.println("int     : " + myInt);
        System.out.println("long    : " + myLong);
        System.out.println("float   : " + myFloat);
        System.out.println("double  : " + myDouble);
        System.out.println("char    : " + myChar);
        System.out.println("boolean : " + myBoolean);
    }
}
```

### Praktek 2.3: `String` Methods

```java
public class StringDemo {
    public static void main(String[] args) {
        String text = "  Java Programming  ";

        System.out.println("Original   : [" + text + "]");
        System.out.println("Trimmed    : [" + text.trim() + "]");
        System.out.println("Upper      : " + text.toUpperCase());
        System.out.println("Length     : " + text.length());
        System.out.println("Contains   : " + text.contains("Java"));
        System.out.println("Replace    : " + text.replace("Java", "Kotlin"));

        String a = "hello";
        String b = "hello";
        String c = new String("hello");
        System.out.println(a == b);        // true (string pool)
        System.out.println(a == c);        // false (different object)
        System.out.println(a.equals(c));   // true (same content)
    }
}
```

### Praktek 2.4: `printf`

```java
public class PrintfDemo {
    public static void main(String[] args) {
        String name = "Budi";
        int age = 20;
        double gpa = 3.4567;

        System.out.printf("Name: %s%n", name);
        System.out.printf("Age : %d%n", age);
        System.out.printf("GPA : %.2f%n", gpa);
    }
}
```

### Praktek 2.5: Input dengan `Scanner`

```java
import java.util.Scanner;

public class InputDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();

        System.out.printf("Hello, %s! Next year you will be %d.%n", name, age + 1);

        scanner.close();
    }
}
```

---

## 3. Latihan

**Soal 1.** Dari deklarasi berikut, mana yang **compile error**? Jelaskan alasannya.

```java
int a = 10.5;
long b = 100;
float c = 3.14;
char d = "A";
boolean e = 1;
String f = 'hello';
double g = 10;
byte h = 200;
```

**Soal 2.** Mana nama variable yang **valid** di Java?

```
a) myName      b) 2ndPlace     c) _count      d) class
e) total$      f) first-name   g) MAX_VALUE   h) my name
```

**Soal 3.** Buat program yang menyimpan biodata kamu (nama `String`, umur `int`, tinggi `double`, status menikah `boolean`, inisial `char`) lalu cetak dengan `printf` dalam format rapi.

**Soal 4.** Tukar nilai dua variable `int a = 5` dan `int b = 9` memakai variable bantu (`temp`), lalu cetak hasilnya.

**Soal 5.** Buat program yang meminta input nama dan kota asal user, lalu cetak: `Hello, <name> from <city>!`.

**Soal 6.** Prediksi output kode berikut, lalu jalankan untuk verifikasi:

```java
String s1 = "java";
String s2 = "java";
String s3 = new String("java");
System.out.println(s1 == s2);
System.out.println(s1 == s3);
System.out.println(s1.equals(s3));
```

**Soal 7 (Bonus).** Program meminta input nama lengkap (`nextLine`) **setelah** meminta umur (`nextInt`). Kenapa nama kosong? Perbaiki.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

| Baris | Status | Alasan |
|---|---|---|
| `int a = 10.5;` | Error | `10.5` adalah `double`, tidak bisa masuk `int` tanpa cast (lossy). |
| `long b = 100;` | OK | `int` otomatis naik ke `long`. |
| `float c = 3.14;` | Error | `3.14` default `double`. Harus `3.14f`. |
| `char d = "A";` | Error | `"A"` adalah `String`. Harus `'A'`. |
| `boolean e = 1;` | Error | Java tidak mengenal 1/0 sebagai boolean. Harus `true`/`false`. |
| `String f = 'hello';` | Error | Single quote hanya untuk satu `char`. Harus `"hello"`. |
| `double g = 10;` | OK | `int` otomatis naik ke `double` (hasil `10.0`). |
| `byte h = 200;` | Error | Di luar range `byte` (-128 s/d 127). |

### Jawaban Soal 2

Valid: **a, c, e, g**.

- b) diawali angka
- d) `class` adalah keyword
- f) `-` tidak boleh
- h) ada spasi

### Jawaban Soal 3

```java
public class Biodata {
    public static void main(String[] args) {
        String name = "Budi Santoso";
        int age = 24;
        double height = 170.5;
        boolean isMarried = false;
        char initial = 'B';

        System.out.println("===== BIODATA =====");
        System.out.printf("Name     : %s%n", name);
        System.out.printf("Initial  : %c%n", initial);
        System.out.printf("Age      : %d years%n", age);
        System.out.printf("Height   : %.1f cm%n", height);
        System.out.printf("Married  : %b%n", isMarried);
    }
}
```

### Jawaban Soal 4

```java
public class SwapDemo {
    public static void main(String[] args) {
        int a = 5;
        int b = 9;

        System.out.println("Before: a=" + a + ", b=" + b);

        int temp = a;
        a = b;
        b = temp;

        System.out.println("After : a=" + a + ", b=" + b);
    }
}
```

### Jawaban Soal 5

```java
import java.util.Scanner;

public class GreetingApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your city: ");
        String city = scanner.nextLine();

        System.out.println("Hello, " + name + " from " + city + "!");

        scanner.close();
    }
}
```

### Jawaban Soal 6

```
true
false
true
```

- `s1 == s2` → `true`: literal yang sama berbagi satu objek di **string pool**.
- `s1 == s3` → `false`: `new String(...)` selalu membuat objek baru di heap.
- `s1.equals(s3)` → `true`: isinya sama.

### Jawaban Soal 7

Penyebab: `nextInt()` hanya membaca angka, newline (`\n`) dari tombol Enter tertinggal di buffer. `nextLine()` berikutnya langsung membaca newline itu sehingga hasilnya string kosong.

```java
import java.util.Scanner;

public class ScannerFix {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // consume the leftover newline

        System.out.print("Enter full name: ");
        String name = scanner.nextLine();

        System.out.println(name + " is " + age + " years old.");
        scanner.close();
    }
}
```
