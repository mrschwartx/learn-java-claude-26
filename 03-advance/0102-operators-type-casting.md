# 0102 - Operators and Type Casting

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Memakai operator arithmetic, assignment, relational, logical, dan ternary
- Memahami integer division, modulo, dan increment/decrement
- Memahami operator precedence
- Melakukan type casting (widening dan narrowing) dengan aman
- Mengenali bug umum: overflow, precision loss, dan jebakan `+` pada `String`

---

## 1. Teori

### 1.1 Arithmetic Operators

| Operator | Fungsi | Contoh | Hasil |
|---|---|---|---|
| `+` | tambah | `5 + 2` | `7` |
| `-` | kurang | `5 - 2` | `3` |
| `*` | kali | `5 * 2` | `10` |
| `/` | bagi | `5 / 2` | `2` (integer division!) |
| `%` | modulo (sisa bagi) | `5 % 2` | `1` |

**Integer division:** kalau kedua operand `int`, hasilnya `int` dan bagian desimal **dibuang** (bukan dibulatkan).

```java
System.out.println(7 / 2);     // 3
System.out.println(7 / 2.0);   // 3.5
System.out.println(-7 / 2);    // -3 (truncated toward zero)
System.out.println(-7 % 2);    // -1 (sign follows the dividend)
```

Modulo sering dipakai untuk: cek genap/ganjil (`n % 2 == 0`), cek kelipatan, dan wrap-around (jam, indeks).

### 1.2 Assignment Operators

| Operator | Setara dengan |
|---|---|
| `x = 5` | assign |
| `x += 3` | `x = x + 3` |
| `x -= 3` | `x = x - 3` |
| `x *= 3` | `x = x * 3` |
| `x /= 3` | `x = x / 3` |
| `x %= 3` | `x = x % 3` |

Compound assignment otomatis melakukan implicit cast:

```java
byte b = 10;
b += 5;        // OK, implicit cast back to byte
// b = b + 5;  // compile error: int cannot be assigned to byte
```

### 1.3 Increment dan Decrement

| Bentuk | Arti |
|---|---|
| `i++` (post) | pakai nilai `i` dulu, baru tambah 1 |
| `++i` (pre) | tambah 1 dulu, baru pakai nilai `i` |
| `i--` / `--i` | sama, tapi kurang 1 |

```java
int i = 5;
int a = i++;   // a = 5, i = 6
int b = ++i;   // i = 7, b = 7
```

> Jangan menulis ekspresi yang bergantung pada urutan increment dalam satu baris (`i++ + ++i`). Itu valid tapi sulit dibaca dan sering jadi sumber bug.

### 1.4 Relational (Comparison) Operators

Hasilnya selalu `boolean`.

| Operator | Arti |
|---|---|
| `==` | sama dengan |
| `!=` | tidak sama |
| `>` `<` | lebih besar / kecil |
| `>=` `<=` | lebih besar sama / kecil sama |

> Untuk `String` dan object lain, pakai `.equals()`, bukan `==`.

### 1.5 Logical Operators

| Operator | Arti | Catatan |
|---|---|---|
| `&&` | AND | **short-circuit**: kalau kiri `false`, kanan tidak dievaluasi |
| `\|\|` | OR | **short-circuit**: kalau kiri `true`, kanan tidak dievaluasi |
| `!` | NOT | membalik boolean |

Tabel kebenaran:

| A | B | A && B | A \|\| B |
|---|---|---|---|
| true | true | true | true |
| true | false | false | true |
| false | true | false | true |
| false | false | false | false |

Short-circuit berguna untuk menghindari error:

```java
String s = null;
if (s != null && s.length() > 3) {   // safe: s.length() is never called when s is null
    System.out.println("Long text");
}
```

### 1.6 Ternary Operator

```
condition ? valueIfTrue : valueIfFalse
```

```java
int age = 20;
String status = (age >= 17) ? "Adult" : "Minor";
```

Pakai untuk ekspresi sederhana. Jangan di-nest berlapis-lapis karena susah dibaca.

### 1.7 Operator Precedence (dari tinggi ke rendah, ringkas)

1. `()`
2. `++` `--` `!` (unary)
3. `*` `/` `%`
4. `+` `-`
5. `<` `<=` `>` `>=`
6. `==` `!=`
7. `&&`
8. `||`
9. `? :`
10. `=` `+=` dst.

Kalau ragu, pakai tanda kurung. Kode yang jelas lebih baik dari kode yang "pintar".

### 1.8 `String` Concatenation dengan `+`

`+` dievaluasi **kiri ke kanan**. Kalau salah satu operand `String`, hasilnya `String`.

```java
System.out.println(1 + 2 + "3");   // "33"  (1+2=3, then "3" + "3")
System.out.println("1" + 2 + 3);   // "123"
System.out.println('a' + 1);       // 98 (char is promoted to int)
System.out.println("a" + 1);       // "a1"
```

### 1.9 Type Casting

**Widening (implicit, otomatis, aman):** tipe kecil → tipe besar.

```
byte → short → int → long → float → double
              char → int
```

```java
int i = 100;
long l = i;       // OK
double d = l;     // OK
```

**Narrowing (explicit, manual, berisiko):** tipe besar → tipe kecil. Wajib pakai cast `(type)`.

```java
double d = 9.99;
int i = (int) d;        // 9 (decimal truncated, NOT rounded)

int big = 130;
byte b = (byte) big;    // -126 (overflow wraps around!)
```

### 1.10 Binary Numeric Promotion

Dalam operasi aritmatika:
- `byte`, `short`, `char` otomatis dipromosikan ke `int`
- Kalau ada `long`, hasilnya `long`; kalau ada `float`, hasilnya `float`; kalau ada `double`, hasilnya `double`

```java
byte a = 10, b = 20;
// byte c = a + b;      // compile error: a + b is int
byte c = (byte) (a + b); // OK
```

### 1.11 Bug Umum

**Integer overflow** (tidak ada error, nilainya "muter"):

```java
int max = Integer.MAX_VALUE;   // 2147483647
System.out.println(max + 1);   // -2147483648
```

**Floating-point precision:**

```java
System.out.println(0.1 + 0.2);   // 0.30000000000000004
```

Untuk uang, **jangan** pakai `double`. Pakai `BigDecimal` (dibahas nanti).

**Integer division saat hitung rata-rata:**

```java
int total = 7, count = 2;
double wrong = total / count;            // 3.0
double right = (double) total / count;   // 3.5
```

### 1.12 Class `Math` (Bonus)

```java
Math.max(3, 7);        // 7
Math.min(3, 7);        // 3
Math.abs(-5);          // 5
Math.pow(2, 10);       // 1024.0
Math.sqrt(16);         // 4.0
Math.round(3.5);       // 4
Math.ceil(3.1);        // 4.0
Math.floor(3.9);       // 3.0
```

---

## 2. Praktek

### Praktek 2.1: Arithmetic

```java
public class ArithmeticDemo {
    public static void main(String[] args) {
        int a = 17;
        int b = 5;

        System.out.println("a + b = " + (a + b));
        System.out.println("a - b = " + (a - b));
        System.out.println("a * b = " + (a * b));
        System.out.println("a / b = " + (a / b));         // 3
        System.out.println("a % b = " + (a % b));         // 2
        System.out.println("a / (double) b = " + (a / (double) b)); // 3.4
    }
}
```

### Praktek 2.2: Increment Pre vs Post

```java
public class IncrementDemo {
    public static void main(String[] args) {
        int i = 5;
        System.out.println(i++);   // prints 5, then i = 6
        System.out.println(i);     // 6
        System.out.println(++i);   // i = 7, then prints 7
        System.out.println(i--);   // prints 7, then i = 6
        System.out.println(--i);   // i = 5, then prints 5
    }
}
```

### Praktek 2.3: Logical dan Short-Circuit

```java
public class LogicalDemo {
    public static void main(String[] args) {
        int age = 20;
        boolean hasId = true;

        boolean canEnter = age >= 17 && hasId;
        System.out.println("Can enter: " + canEnter);

        String text = null;
        if (text != null && text.length() > 3) {
            System.out.println("Long text");
        } else {
            System.out.println("Null or short text");
        }
    }
}
```

### Praktek 2.4: Ternary

```java
public class TernaryDemo {
    public static void main(String[] args) {
        int number = 7;
        String type = (number % 2 == 0) ? "Even" : "Odd";
        System.out.println(number + " is " + type);
    }
}
```

### Praktek 2.5: Type Casting

```java
public class CastingDemo {
    public static void main(String[] args) {
        // widening
        int i = 42;
        double d = i;
        System.out.println("Widening: " + d);

        // narrowing
        double price = 99.99;
        int truncated = (int) price;
        System.out.println("Narrowing: " + truncated);

        // overflow
        int big = 130;
        byte b = (byte) big;
        System.out.println("Overflow byte: " + b);

        // char arithmetic
        char c = 'a';
        char next = (char) (c + 1);
        System.out.println("Next char: " + next);
        System.out.println("ASCII of 'a': " + (int) c);
    }
}
```

### Praktek 2.6: Jebakan Umum

```java
public class PitfallDemo {
    public static void main(String[] args) {
        System.out.println(10 / 4);                 // 2
        System.out.println(10 / 4.0);               // 2.5
        System.out.println(0.1 + 0.2);              // 0.30000000000000004
        System.out.println(Integer.MAX_VALUE + 1);  // -2147483648
        System.out.println(1 + 2 + "3");            // 33
        System.out.println("1" + 2 + 3);            // 123
    }
}
```

---

## 3. Latihan

**Soal 1.** Prediksi output (jangan langsung dijalankan dulu):

```java
System.out.println(7 / 2);
System.out.println(7 % 2);
System.out.println(7 / 2.0);
System.out.println((double) 7 / 2);
System.out.println((double) (7 / 2));
System.out.println('a' + 1);
System.out.println((char) ('a' + 1));
System.out.println("1" + 2 + 3);
System.out.println(1 + 2 + "3");
System.out.println(-7 / 2);
System.out.println(-7 % 3);
```

**Soal 2.** Prediksi nilai `i`, `j`, dan `k` di akhir:

```java
int i = 5;
int j = i++;
int k = ++i;
```

**Soal 3.** Program yang menerima input total detik (misal `3725`) lalu mencetak dalam format `jam:menit:detik` (misal `1h 2m 5s`).

**Soal 4.** Program yang menerima 3 nilai `int` lalu mencetak rata-rata **dengan 2 digit desimal**.

**Soal 5.** Program yang menerima suhu Celsius (`double`) lalu mengubahnya ke Fahrenheit dan Kelvin.
Rumus: `F = C * 9 / 5 + 32`, `K = C + 273.15`.

**Soal 6.** Program yang menerima sebuah `int`, lalu mencetak apakah angka itu **genap** atau **ganjil** memakai ternary.

**Soal 7.** Kenapa kode ini compile error? Perbaiki dengan dua cara berbeda.

```java
byte a = 10;
byte b = 20;
byte c = a + b;
```

**Soal 8.** Hitung total harga: harga satuan `19999` (int), jumlah `3`, diskon `10%`, lalu pajak `11%` dari harga setelah diskon. Cetak total akhir dengan 2 digit desimal. Hati-hati dengan integer division.

**Soal 9 (Bonus).** Tanpa menjalankan, kenapa `System.out.println(0.1 + 0.2 == 0.3);` mencetak `false`? Bagaimana cara membandingkan dua `double` dengan benar?

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```
3
1
3.5
3.5
3.0
98
b
123
33
-3
-1
```

Penjelasan singkat:
- `(double) (7 / 2)`: pembagian integer dulu (`3`), baru di-cast jadi `3.0`.
- `'a' + 1`: `char` dipromosikan ke `int` (97 + 1 = 98).
- `-7 / 2`: dipotong ke arah nol, jadi `-3`.
- `-7 % 3`: tanda mengikuti yang dibagi, jadi `-1`.

### Jawaban Soal 2

`i = 7`, `j = 5`, `k = 7`.

- `j = i++` → `j` dapat 5, lalu `i` jadi 6.
- `k = ++i` → `i` jadi 7, lalu `k` dapat 7.

### Jawaban Soal 3

```java
import java.util.Scanner;

public class SecondsConverter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter total seconds: ");
        int totalSeconds = scanner.nextInt();

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        System.out.printf("%dh %dm %ds%n", hours, minutes, seconds);
        scanner.close();
    }
}
```

Untuk `3725`: `1h 2m 5s`.

### Jawaban Soal 4

```java
import java.util.Scanner;

public class AverageCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter 3 numbers: ");
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int c = scanner.nextInt();

        double average = (a + b + c) / 3.0;   // 3.0 forces floating-point division

        System.out.printf("Average: %.2f%n", average);
        scanner.close();
    }
}
```

### Jawaban Soal 5

```java
import java.util.Scanner;

public class TemperatureConverter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter temperature in Celsius: ");
        double celsius = scanner.nextDouble();

        double fahrenheit = celsius * 9 / 5 + 32;
        double kelvin = celsius + 273.15;

        System.out.printf("%.2f C = %.2f F = %.2f K%n", celsius, fahrenheit, kelvin);
        scanner.close();
    }
}
```

### Jawaban Soal 6

```java
import java.util.Scanner;

public class EvenOdd {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number: ");
        int number = scanner.nextInt();

        String result = (number % 2 == 0) ? "Even" : "Odd";
        System.out.println(number + " is " + result);
        scanner.close();
    }
}
```

### Jawaban Soal 7

Penyebab: `a + b` dipromosikan jadi `int` (binary numeric promotion), sedangkan `c` bertipe `byte`.

Cara 1: explicit cast.

```java
byte c = (byte) (a + b);
```

Cara 2: ubah tipe `c` jadi `int`.

```java
int c = a + b;
```

### Jawaban Soal 8

```java
public class PriceCalculator {
    public static void main(String[] args) {
        int unitPrice = 19999;
        int quantity = 3;

        double subtotal = unitPrice * quantity;          // 59997.0
        double afterDiscount = subtotal * (1 - 10 / 100.0);   // 10 / 100.0 = 0.1
        double tax = afterDiscount * 11 / 100.0;
        double total = afterDiscount + tax;

        System.out.printf("Subtotal       : %.2f%n", subtotal);
        System.out.printf("After discount : %.2f%n", afterDiscount);
        System.out.printf("Tax (11%%)      : %.2f%n", tax);
        System.out.printf("Total          : %.2f%n", total);
    }
}
```

Jebakannya: `10 / 100` (keduanya `int`) hasilnya `0`, bukan `0.1`. Pakai `100.0` supaya jadi floating-point division. Perhatikan juga `%%` untuk mencetak karakter `%` di `printf`.

Hasil: subtotal `59997.00`, after discount `53997.30`, tax `5939.70`, total `59937.00`.

### Jawaban Soal 9

`0.1` dan `0.2` disimpan sebagai pecahan biner (IEEE 754) yang tidak presisi, jadi `0.1 + 0.2` hasilnya `0.30000000000000004`, bukan tepat `0.3`.

Cara membandingkan dengan benar: pakai toleransi (epsilon).

```java
double a = 0.1 + 0.2;
double b = 0.3;
double epsilon = 1e-9;

System.out.println(Math.abs(a - b) < epsilon);   // true
```

Untuk perhitungan uang, pakai `BigDecimal`.
