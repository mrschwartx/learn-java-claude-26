# 0103 - Control Flow

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Mengambil keputusan dengan `if`, `else if`, `else`, dan `switch`
- Memakai `switch` expression modern (Java 14+)
- Mengulang proses dengan `for`, `while`, `do-while`, dan enhanced `for`
- Mengontrol loop dengan `break` dan `continue`
- Menyelesaikan masalah logika dasar (FizzBuzz, prime, pattern)

---

## 1. Teori

### 1.1 `if`, `else if`, `else`

**Analogi:** seperti persimpangan jalan. Program cek kondisi satu per satu dari atas, lalu masuk ke **jalur pertama** yang kondisinya `true`.

```java
int score = 82;

if (score >= 90) {
    System.out.println("A");
} else if (score >= 80) {
    System.out.println("B");
} else if (score >= 70) {
    System.out.println("C");
} else {
    System.out.println("D");
}
```

Aturan:
- Kondisi harus bertipe `boolean`. Java tidak seperti JavaScript, jadi `if (1)` itu error.
- Hanya **satu** block yang dijalankan.
- Selalu pakai kurung kurawal `{ }`, walaupun isinya satu baris. Ini mencegah bug saat kode ditambah.
- Urutan kondisi penting: taruh yang paling spesifik di atas.

Jebakan klasik:

```java
if (x = 5) { }    // compile error in Java (assignment, not comparison)
if (x == 5) { }   // correct
```

### 1.2 Nested `if`

```java
if (age >= 17) {
    if (hasId) {
        System.out.println("Allowed");
    }
}
// equivalent, and cleaner:
if (age >= 17 && hasId) {
    System.out.println("Allowed");
}
```

### 1.3 `switch` (Classic)

Cocok untuk membandingkan satu variable dengan banyak nilai konstan. Tipe yang didukung: `byte`, `short`, `int`, `char`, `String`, `enum` (bukan `long`, `double`, `boolean`).

```java
int day = 3;

switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    default:
        System.out.println("Other day");
}
```

**Fall-through:** kalau `break` dilupakan, eksekusi **lanjut ke case berikutnya**. Ini sumber bug paling umum di `switch` classic.

```java
case 6:
case 7:
    System.out.println("Weekend");   // intentional fall-through
    break;
```

### 1.4 `switch` Modern (Java 14+)

Pakai arrow `->`. Tidak ada fall-through, tidak perlu `break`.

**Sebagai statement:**

```java
switch (day) {
    case 1 -> System.out.println("Monday");
    case 2 -> System.out.println("Tuesday");
    case 6, 7 -> System.out.println("Weekend");
    default -> System.out.println("Weekday");
}
```

**Sebagai expression (mengembalikan nilai):**

```java
String dayName = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    case 6, 7 -> "Weekend";
    default -> "Unknown";
};
```

Kalau butuh block dengan banyak statement, pakai `yield`:

```java
String type = switch (day) {
    case 6, 7 -> "Weekend";
    default -> {
        String temp = "Week";
        yield temp + "day";
    }
};
```

### 1.5 `for` Loop

Dipakai saat jumlah iterasi **sudah diketahui**.

```
for (initialization; condition; update) { body }
```

```java
for (int i = 1; i <= 5; i++) {
    System.out.println("Iteration " + i);
}
```

Urutan eksekusi: `initialization` (sekali) → cek `condition` → `body` → `update` → cek `condition` lagi → dst.

Variasi:

```java
for (int i = 10; i > 0; i -= 2) { }      // count down by 2
for (int i = 0, j = 10; i < j; i++, j--) { }   // two variables
```

### 1.6 `while` Loop

Dipakai saat jumlah iterasi **belum pasti**, tergantung kondisi. Kondisi dicek **sebelum** body.

```java
int n = 1;
while (n <= 5) {
    System.out.println(n);
    n++;
}
```

> Jangan lupa mengubah variable kondisi, kalau tidak jadi **infinite loop**.

### 1.7 `do-while` Loop

Body dijalankan **minimal sekali**, kondisi dicek **setelah** body. Cocok untuk menu atau validasi input.

```java
int n = 10;
do {
    System.out.println(n);   // prints once even though the condition is false
    n++;
} while (n < 5);
```

### 1.8 Enhanced `for` (for-each)

Untuk iterasi semua elemen array atau collection tanpa index.

```java
int[] numbers = {10, 20, 30};
for (int num : numbers) {
    System.out.println(num);
}
```

(Array dibahas lebih dalam di materi berikutnya.)

### 1.9 `break` dan `continue`

| Keyword | Efek |
|---|---|
| `break` | keluar dari loop (atau `switch`) sepenuhnya |
| `continue` | lompat ke iterasi berikutnya |

```java
for (int i = 1; i <= 10; i++) {
    if (i == 3) continue;   // skip 3
    if (i == 7) break;      // stop at 7
    System.out.println(i);  // 1 2 4 5 6
}
```

### 1.10 Nested Loop dan Label

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        System.out.print(i * j + " ");
    }
    System.out.println();
}
```

`break` hanya keluar dari loop **terdalam**. Untuk keluar dari loop luar, pakai label:

```java
outer:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (i * j == 4) break outer;
        System.out.println(i + "," + j);
    }
}
```

Label jarang dipakai. Kalau sering butuh, biasanya logikanya bisa dipindah ke method (`return`).

### 1.11 Memilih Loop yang Tepat

| Situasi | Pilihan |
|---|---|
| Jumlah iterasi diketahui | `for` |
| Iterasi semua elemen | enhanced `for` |
| Ulang selama kondisi benar | `while` |
| Harus jalan minimal sekali | `do-while` |

---

## 2. Praktek

### Praktek 2.1: `if-else if-else`

```java
import java.util.Scanner;

public class GradeDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter score (0-100): ");
        int score = scanner.nextInt();

        if (score < 0 || score > 100) {
            System.out.println("Invalid score");
        } else if (score >= 90) {
            System.out.println("Grade A");
        } else if (score >= 80) {
            System.out.println("Grade B");
        } else if (score >= 70) {
            System.out.println("Grade C");
        } else {
            System.out.println("Grade D");
        }
        scanner.close();
    }
}
```

### Praktek 2.2: `switch` Modern

```java
public class SwitchDemo {
    public static void main(String[] args) {
        int day = 6;

        String type = switch (day) {
            case 1, 2, 3, 4, 5 -> "Weekday";
            case 6, 7 -> "Weekend";
            default -> "Invalid day";
        };

        System.out.println("Day " + day + " is " + type);
    }
}
```

### Praktek 2.3: `for` (Tabel Perkalian)

```java
public class MultiplicationTable {
    public static void main(String[] args) {
        int base = 7;
        for (int i = 1; i <= 10; i++) {
            System.out.printf("%d x %d = %d%n", base, i, base * i);
        }
    }
}
```

### Praktek 2.4: `while` (Jumlah Digit)

```java
public class DigitSum {
    public static void main(String[] args) {
        int number = 12345;
        int sum = 0;

        while (number > 0) {
            sum += number % 10;   // take the last digit
            number /= 10;         // drop the last digit
        }

        System.out.println("Digit sum: " + sum);   // 15
    }
}
```

### Praktek 2.5: `do-while` (Menu)

```java
import java.util.Scanner;

public class MenuDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Say Hello");
            System.out.println("2. Say Goodbye");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> System.out.println("Hello!");
                case 2 -> System.out.println("Goodbye!");
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice");
            }
        } while (choice != 0);

        scanner.close();
    }
}
```

### Praktek 2.6: `break` dan `continue`

```java
public class BreakContinueDemo {
    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) continue;   // skip even numbers
            if (i > 7) break;           // stop after 7
            System.out.print(i + " ");  // 1 3 5 7
        }
        System.out.println();
    }
}
```

### Praktek 2.7: Nested Loop (Pattern)

```java
public class PatternDemo {
    public static void main(String[] args) {
        int rows = 5;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }
    }
}
```

---

## 3. Latihan

**Soal 1.** Program meminta nilai angka (0-100) lalu mencetak grade: A (>= 85), B (>= 70), C (>= 55), D (>= 40), E (< 40). Jika di luar 0-100, cetak "Invalid".

**Soal 2.** Program meminta tahun, lalu cek apakah **tahun kabisat**.
Aturan: habis dibagi 400, **atau** habis dibagi 4 tapi tidak habis dibagi 100.

**Soal 3.** Program meminta angka 1-7 lalu mencetak nama hari (1 = Monday ... 7 = Sunday) memakai **switch expression**. Selain itu cetak "Invalid day".

**Soal 4.** Hitung jumlah semua angka dari 1 sampai 100 memakai `for`, lalu kerjakan lagi memakai `while`.

**Soal 5.** FizzBuzz: cetak angka 1 sampai 30. Untuk kelipatan 3 cetak `Fizz`, kelipatan 5 cetak `Buzz`, kelipatan 3 dan 5 cetak `FizzBuzz`.

**Soal 6.** Cetak pola berikut (tinggi 5), memakai nested loop:

```
    *
   ***
  *****
 *******
*********
```

**Soal 7.** Program meminta sebuah angka lalu cek apakah angka tersebut **bilangan prima**.

**Soal 8.** Program meminta angka `n`, lalu mencetak hasil **faktorial** `n!` (misal `5! = 120`). Gunakan `long` sebagai tipe hasil.

**Soal 9.** Buat game tebak angka: program menentukan angka rahasia 1-100 memakai `new Random().nextInt(100) + 1`. User menebak berulang kali, program memberi petunjuk "Too high" / "Too low", dan berhenti saat tebakan benar sambil menampilkan jumlah percobaan. Gunakan `while` atau `do-while`.

**Soal 10 (Bonus).** Cetak semua bilangan prima antara 1 sampai 100. Gunakan `break`/`continue` sebijak mungkin.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
import java.util.Scanner;

public class GradeCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter score (0-100): ");
        int score = scanner.nextInt();

        if (score < 0 || score > 100) {
            System.out.println("Invalid");
        } else if (score >= 85) {
            System.out.println("A");
        } else if (score >= 70) {
            System.out.println("B");
        } else if (score >= 55) {
            System.out.println("C");
        } else if (score >= 40) {
            System.out.println("D");
        } else {
            System.out.println("E");
        }
        scanner.close();
    }
}
```

### Jawaban Soal 2

```java
import java.util.Scanner;

public class LeapYear {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter year: ");
        int year = scanner.nextInt();

        boolean isLeap = (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);

        System.out.println(year + (isLeap ? " is a leap year" : " is not a leap year"));
        scanner.close();
    }
}
```

Uji: 2000 (kabisat), 1900 (bukan), 2024 (kabisat), 2023 (bukan).

### Jawaban Soal 3

```java
import java.util.Scanner;

public class DayName {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter day number (1-7): ");
        int day = scanner.nextInt();

        String name = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Invalid day";
        };

        System.out.println(name);
        scanner.close();
    }
}
```

### Jawaban Soal 4

```java
public class SumOneToHundred {
    public static void main(String[] args) {
        // using for
        int sumFor = 0;
        for (int i = 1; i <= 100; i++) {
            sumFor += i;
        }

        // using while
        int sumWhile = 0;
        int n = 1;
        while (n <= 100) {
            sumWhile += n;
            n++;
        }

        System.out.println("for   : " + sumFor);     // 5050
        System.out.println("while : " + sumWhile);   // 5050
    }
}
```

### Jawaban Soal 5

```java
public class FizzBuzz {
    public static void main(String[] args) {
        for (int i = 1; i <= 30; i++) {
            if (i % 15 == 0) {          // check the most specific condition first
                System.out.println("FizzBuzz");
            } else if (i % 3 == 0) {
                System.out.println("Fizz");
            } else if (i % 5 == 0) {
                System.out.println("Buzz");
            } else {
                System.out.println(i);
            }
        }
    }
}
```

Kunci: kondisi `i % 15 == 0` (atau `% 3 == 0 && % 5 == 0`) harus dicek **pertama**, kalau tidak, angka 15 akan masuk ke `Fizz` duluan.

### Jawaban Soal 6

```java
public class Pyramid {
    public static void main(String[] args) {
        int rows = 5;

        for (int i = 1; i <= rows; i++) {
            // leading spaces
            for (int s = 1; s <= rows - i; s++) {
                System.out.print(" ");
            }
            // stars: 1, 3, 5, 7, 9
            for (int j = 1; j <= 2 * i - 1; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
```

Pola: baris ke-`i` punya `rows - i` spasi dan `2i - 1` bintang.

### Jawaban Soal 7

```java
import java.util.Scanner;

public class PrimeCheck {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number: ");
        int number = scanner.nextInt();

        boolean isPrime = number > 1;   // numbers <= 1 are not prime

        for (int i = 2; i * i <= number; i++) {   // only check up to sqrt(number)
            if (number % i == 0) {
                isPrime = false;
                break;
            }
        }

        System.out.println(number + (isPrime ? " is prime" : " is not prime"));
        scanner.close();
    }
}
```

Optimasi `i * i <= number`: kalau `number` punya faktor, minimal satu faktor pasti `<= sqrt(number)`.

### Jawaban Soal 8

```java
import java.util.Scanner;

public class Factorial {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter n (0-20): ");
        int n = scanner.nextInt();

        if (n < 0) {
            System.out.println("Factorial is not defined for negative numbers");
        } else {
            long result = 1;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }
            System.out.println(n + "! = " + result);
        }
        scanner.close();
    }
}
```

Kenapa `long` dan batas 20? `int` overflow di `13!`. `long` aman sampai `20!`; `21!` sudah overflow. Untuk angka lebih besar nanti pakai `BigInteger`.

### Jawaban Soal 9

```java
import java.util.Random;
import java.util.Scanner;

public class GuessTheNumber {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int secret = new Random().nextInt(100) + 1;   // 1..100
        int attempts = 0;
        int guess;

        do {
            System.out.print("Guess the number (1-100): ");
            guess = scanner.nextInt();
            attempts++;

            if (guess > secret) {
                System.out.println("Too high");
            } else if (guess < secret) {
                System.out.println("Too low");
            } else {
                System.out.println("Correct! Attempts: " + attempts);
            }
        } while (guess != secret);

        scanner.close();
    }
}
```

### Jawaban Soal 10

```java
public class PrimesUpToHundred {
    public static void main(String[] args) {
        for (int n = 2; n <= 100; n++) {
            boolean isPrime = true;

            for (int i = 2; i * i <= n; i++) {
                if (n % i == 0) {
                    isPrime = false;
                    break;   // no need to check further
                }
            }

            if (!isPrime) continue;   // skip non-primes

            System.out.print(n + " ");
        }
        System.out.println();
    }
}
```

Output: `2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97`

---

## Ringkasan Cepat

| Konsep | Kapan dipakai |
|---|---|
| `if / else if / else` | Kondisi bertingkat atau range |
| `switch` | Satu variable vs banyak nilai konstan |
| `for` | Jumlah iterasi diketahui |
| `while` | Ulang selama kondisi true |
| `do-while` | Harus jalan minimal sekali |
| `break` | Keluar dari loop |
| `continue` | Skip ke iterasi berikutnya |
