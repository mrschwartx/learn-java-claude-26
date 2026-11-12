# 0104 - Methods

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Membuat dan memanggil method dengan parameter dan return value
- Membedakan `void` dan method yang mengembalikan nilai
- Memakai method overloading dan varargs
- Memahami bahwa Java selalu **pass-by-value** (termasuk untuk object)
- Memahami scope variable
- Menulis method rekursif sederhana dan mengenali bahaya `StackOverflowError`

---

## 1. Teori

### 1.1 Apa itu Method?

**Analogi:** method itu seperti **resep masakan**. Kamu tulis sekali (langkah-langkahnya), lalu bisa dipakai berkali-kali dengan bahan (parameter) yang berbeda, dan menghasilkan sesuatu (return value).

Manfaat method:
- **Reuse**: tidak menulis kode yang sama berulang kali (prinsip DRY)
- **Readability**: kode dipecah jadi bagian kecil yang punya nama jelas
- **Testability**: bagian kecil lebih mudah dites

### 1.2 Anatomi Method

```java
public static int add(int a, int b) {
    int result = a + b;
    return result;
}
```

| Bagian | Arti |
|---|---|
| `public` | access modifier (siapa yang boleh memanggil) |
| `static` | method milik class, bukan milik object (dibahas lebih dalam di OOP) |
| `int` | return type (tipe nilai yang dikembalikan) |
| `add` | nama method (`camelCase`, diawali kata kerja) |
| `(int a, int b)` | parameter list |
| `return result;` | mengembalikan nilai ke pemanggil |

Memanggil method:

```java
int total = add(3, 4);   // 3 and 4 are arguments
```

> **Parameter** = variable di deklarasi method. **Argument** = nilai yang dikirim saat memanggil.

### 1.3 `void` Method

Method yang tidak mengembalikan nilai.

```java
public static void greet(String name) {
    System.out.println("Hello, " + name + "!");
}
```

`return;` (tanpa nilai) boleh dipakai di `void` method untuk keluar lebih awal:

```java
public static void printPositive(int n) {
    if (n <= 0) {
        return;   // exit early
    }
    System.out.println(n);
}
```

### 1.4 Return Value

- Method dengan return type non-`void` **wajib** mengembalikan nilai di **semua jalur** eksekusi.
- Setelah `return`, method langsung selesai.

```java
// compile error: missing return statement when n == 0
public static String sign(int n) {
    if (n > 0) {
        return "positive";
    } else if (n < 0) {
        return "negative";
    }
}

// fixed
public static String sign(int n) {
    if (n > 0) return "positive";
    if (n < 0) return "negative";
    return "zero";
}
```

### 1.5 Method Overloading

Beberapa method dengan **nama sama** tapi **parameter list berbeda** (jumlah atau tipe).

```java
public static int add(int a, int b) { return a + b; }
public static double add(double a, double b) { return a + b; }
public static int add(int a, int b, int c) { return a + b + c; }
```

Aturan:
- Yang membedakan hanya **parameter list** (jumlah, tipe, urutan tipe).
- **Return type saja tidak cukup** untuk membedakan overload.
- Java memilih method yang paling cocok berdasarkan argument saat compile time.

```java
add(1, 2);        // int version
add(1.5, 2.5);    // double version
add(1, 2.5);      // double version (int is widened to double)
```

### 1.6 Varargs

Menerima jumlah argument yang bebas. Di dalam method, varargs diperlakukan sebagai **array**.

```java
public static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}

sum();            // 0
sum(1, 2, 3);     // 6
```

Aturan: varargs harus **parameter terakhir**, dan hanya boleh **satu** per method.

```java
public static String join(String separator, String... parts) { ... }   // OK
```

### 1.7 Pass-by-Value

**Java SELALU pass-by-value.** Method menerima **salinan** dari argument.

**Primitive:** salinan nilai. Mengubah parameter tidak memengaruhi variable asli.

```java
static void change(int x) {
    x = 99;
}

int a = 5;
change(a);
System.out.println(a);   // 5 (unchanged)
```

**Object / array:** yang disalin adalah **reference** (alamat), bukan objeknya. Jadi:
- Mengubah **isi** objek lewat reference itu **terlihat** oleh pemanggil.
- Mengganti reference ke objek lain **tidak berpengaruh** ke pemanggil.

```java
static void modify(int[] arr) {
    arr[0] = 99;            // modifies the SAME array: caller sees it
}

static void reassign(int[] arr) {
    arr = new int[]{1, 2, 3};   // only changes the local copy of the reference
}

int[] data = {10, 20, 30};
modify(data);
System.out.println(data[0]);   // 99

reassign(data);
System.out.println(data[0]);   // still 99
```

**Analogi:** reference itu seperti **alamat rumah di secarik kertas**. Kamu kasih fotokopi kertas itu ke teman. Dia bisa datang ke rumah dan mengubah isinya (kamu lihat perubahannya). Tapi kalau dia mencoret kertas fotokopinya dan menulis alamat lain, kertas aslimu tetap sama.

### 1.8 Scope Variable

Variable hanya hidup di dalam block `{ }` tempat dia dideklarasikan.

```java
public static void demo() {
    int x = 10;                  // visible in the whole method

    for (int i = 0; i < 3; i++) {
        int y = i * 2;           // visible only inside the loop body
    }

    // System.out.println(i);    // compile error: i is out of scope
    // System.out.println(y);    // compile error: y is out of scope
}
```

Aturan:
- Parameter dan local variable hanya hidup selama method berjalan.
- Tidak boleh mendeklarasikan variable dengan nama yang sama di scope yang saling bertumpuk (dalam satu method).

### 1.9 Rekursi

Method yang **memanggil dirinya sendiri**. Wajib punya:
1. **Base case**: kondisi berhenti
2. **Recursive case**: memanggil diri sendiri dengan masalah yang **lebih kecil**

```java
public static long factorial(int n) {
    if (n <= 1) {            // base case
        return 1;
    }
    return n * factorial(n - 1);   // recursive case
}
```

Jejak `factorial(4)`:

```
factorial(4) = 4 * factorial(3)
             = 4 * (3 * factorial(2))
             = 4 * (3 * (2 * factorial(1)))
             = 4 * (3 * (2 * 1))
             = 24
```

Bahaya: tanpa base case (atau base case tidak pernah tercapai), program akan melempar `StackOverflowError`.

Rekursi vs loop:
- Rekursi cocok untuk struktur yang secara alami bertingkat (tree, folder, divide and conquer).
- Untuk hitungan sederhana, loop biasanya lebih efisien dan lebih aman.
- Fibonacci rekursif naif itu **sangat lambat** (banyak hitungan berulang), jangan dipakai untuk `n` besar.

### 1.10 Method `main` dan Memanggil Antar Method

Method `static` hanya bisa memanggil method `static` lain secara langsung. Itu sebabnya semua method di materi ini dideklarasikan `static` supaya bisa dipanggil dari `main`. Setelah masuk OOP, kita akan pakai method non-static (instance method).

### 1.11 Best Practice

- Satu method, **satu tugas**.
- Nama method berupa kata kerja: `calculateTotal`, `isValid`, `printReport`.
- Method boolean sebaiknya diawali `is`, `has`, `can`: `isPrime`, `hasPermission`.
- Hindari parameter terlalu banyak (lebih dari 3-4 biasanya tanda perlu di-refactor).
- Hindari method yang terlalu panjang (jika perlu scroll, pecah).
- Hindari side effect tersembunyi: method bernama `calculate...` sebaiknya tidak sekaligus mencetak atau mengubah data lain.

### 1.12 Javadoc

```java
/**
 * Calculates the area of a rectangle.
 *
 * @param length the length of the rectangle
 * @param width  the width of the rectangle
 * @return the area as a double
 */
public static double rectangleArea(double length, double width) {
    return length * width;
}
```

---

## 2. Praktek

### Praktek 2.1: Method Dasar

```java
public class BasicMethods {

    public static void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }

    public static int square(int n) {
        return n * n;
    }

    public static boolean isAdult(int age) {
        return age >= 17;
    }

    public static void main(String[] args) {
        greet("Budi");
        System.out.println("Square of 7: " + square(7));
        System.out.println("Is 15 an adult? " + isAdult(15));
        System.out.println("Is 20 an adult? " + isAdult(20));
    }
}
```

### Praktek 2.2: Overloading

```java
public class OverloadDemo {

    public static int add(int a, int b) {
        System.out.println("int version");
        return a + b;
    }

    public static double add(double a, double b) {
        System.out.println("double version");
        return a + b;
    }

    public static int add(int a, int b, int c) {
        System.out.println("three int version");
        return a + b + c;
    }

    public static void main(String[] args) {
        System.out.println(add(1, 2));
        System.out.println(add(1.5, 2.5));
        System.out.println(add(1, 2, 3));
        System.out.println(add(1, 2.5));   // which version is called?
    }
}
```

### Praktek 2.3: Varargs

```java
public class VarargsDemo {

    public static int sum(int... numbers) {
        int total = 0;
        for (int n : numbers) {
            total += n;
        }
        return total;
    }

    public static double average(int... numbers) {
        if (numbers.length == 0) {
            return 0;
        }
        return (double) sum(numbers) / numbers.length;
    }

    public static void main(String[] args) {
        System.out.println(sum());
        System.out.println(sum(5));
        System.out.println(sum(1, 2, 3, 4));
        System.out.printf("Average: %.2f%n", average(80, 90, 75));
    }
}
```

### Praktek 2.4: Pass-by-Value

```java
import java.util.Arrays;

public class PassByValueDemo {

    static void changePrimitive(int x) {
        x = 99;
    }

    static void changeArrayContent(int[] arr) {
        arr[0] = 99;
    }

    static void reassignArray(int[] arr) {
        arr = new int[]{7, 7, 7};
    }

    public static void main(String[] args) {
        int a = 5;
        changePrimitive(a);
        System.out.println("a = " + a);

        int[] data = {10, 20, 30};
        changeArrayContent(data);
        System.out.println("after changeArrayContent: " + Arrays.toString(data));

        reassignArray(data);
        System.out.println("after reassignArray     : " + Arrays.toString(data));
    }
}
```

### Praktek 2.5: Rekursi

```java
public class RecursionDemo {

    public static long factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    public static int sumDigits(int n) {
        if (n < 10) {
            return n;
        }
        return (n % 10) + sumDigits(n / 10);
    }

    public static int fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {
        System.out.println("5! = " + factorial(5));
        System.out.println("sumDigits(12345) = " + sumDigits(12345));
        for (int i = 0; i < 10; i++) {
            System.out.print(fibonacci(i) + " ");
        }
        System.out.println();
    }
}
```

### Praktek 2.6: Program Kecil dengan Banyak Method

```java
import java.util.Scanner;

public class TemperatureApp {

    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 9 / 5 + 32;
    }

    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public static void printMenu() {
        System.out.println("\n=== TEMPERATURE CONVERTER ===");
        System.out.println("1. Celsius to Fahrenheit");
        System.out.println("2. Fahrenheit to Celsius");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            printMenu();
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Celsius: ");
                    double c = scanner.nextDouble();
                    System.out.printf("%.2f C = %.2f F%n", c, celsiusToFahrenheit(c));
                }
                case 2 -> {
                    System.out.print("Enter Fahrenheit: ");
                    double f = scanner.nextDouble();
                    System.out.printf("%.2f F = %.2f C%n", f, fahrenheitToCelsius(f));
                }
                case 0 -> System.out.println("Bye!");
                default -> System.out.println("Invalid choice");
            }
        } while (choice != 0);

        scanner.close();
    }
}
```

---

## 3. Latihan

**Soal 1.** Buat method `isEven(int n)` yang mengembalikan `boolean`. Panggil dari `main` untuk beberapa angka.

**Soal 2.** Buat method `max(int a, int b, int c)` yang mengembalikan nilai terbesar dari tiga angka **tanpa** memakai `Math.max`.

**Soal 3.** Buat dua method overload bernama `area`:
- `area(double radius)` untuk luas lingkaran
- `area(double length, double width)` untuk luas persegi panjang

Lalu cetak hasilnya dengan 2 digit desimal.

**Soal 4.** Buat method `isPrime(int n)` yang mengembalikan `boolean`, lalu buat method `printPrimes(int limit)` yang mencetak semua bilangan prima dari 2 sampai `limit` dengan memanggil `isPrime`.

**Soal 5.** Prediksi output program berikut, lalu jelaskan kenapa:

```java
public class Quiz {
    static void a(int x) { x = x + 10; }
    static void b(int[] arr) { arr[0] = arr[0] + 10; }
    static void c(int[] arr) { arr = new int[]{100}; }

    public static void main(String[] args) {
        int n = 1;
        int[] data = {1};
        a(n);
        b(data);
        c(data);
        System.out.println(n);
        System.out.println(data[0]);
    }
}
```

**Soal 6.** Buat method rekursif `power(int base, int exp)` yang menghitung `base` pangkat `exp` (anggap `exp >= 0`). Contoh: `power(2, 10) = 1024`.

**Soal 7.** Buat method varargs `average(double... values)` yang mengembalikan rata-rata. Jika tidak ada argument, kembalikan `0`.

**Soal 8.** Buat method `reverseNumber(int n)` yang membalik angka memakai loop (`12345` menjadi `54321`). Lalu buat method `isPalindromeNumber(int n)` yang memanfaatkan `reverseNumber`.

**Soal 9.** Method berikut compile error. Temukan dan perbaiki semua masalahnya:

```java
public static int grade(int score) {
    if (score >= 80) {
        return "A";
    } else if (score >= 60) {
        return "B";
    }
}
```

**Soal 10 (Bonus).** Buat method rekursif `gcd(int a, int b)` (FPB) memakai algoritma Euclid: `gcd(a, b) = gcd(b, a % b)`, base case `b == 0` mengembalikan `a`.

**Soal 11 (Bonus).** Buat method `fibonacciIterative(int n)` memakai loop. Bandingkan kecepatannya dengan versi rekursif untuk `n = 40` memakai `System.nanoTime()`. Apa yang kamu amati?

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
public class EvenCheck {

    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static void main(String[] args) {
        System.out.println(isEven(4));    // true
        System.out.println(isEven(7));    // false
        System.out.println(isEven(-2));   // true
        System.out.println(isEven(0));    // true
    }
}
```

Catatan: `n % 2 == 0` benar untuk bilangan negatif juga. Hindari `n % 2 == 1` untuk cek ganjil karena `-3 % 2` hasilnya `-1`.

### Jawaban Soal 2

```java
public class MaxOfThree {

    public static int max(int a, int b, int c) {
        int result = a;
        if (b > result) {
            result = b;
        }
        if (c > result) {
            result = c;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(max(3, 9, 5));   // 9
        System.out.println(max(10, 2, 7));  // 10
        System.out.println(max(-1, -5, -3)); // -1
    }
}
```

### Jawaban Soal 3

```java
public class AreaCalculator {

    public static double area(double radius) {
        return Math.PI * radius * radius;
    }

    public static double area(double length, double width) {
        return length * width;
    }

    public static void main(String[] args) {
        System.out.printf("Circle (r=7)        : %.2f%n", area(7));
        System.out.printf("Rectangle (5 x 3.5) : %.2f%n", area(5, 3.5));
    }
}
```

Hasil: circle `153.94`, rectangle `17.50`.

### Jawaban Soal 4

```java
public class PrimeUtil {

    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void printPrimes(int limit) {
        for (int i = 2; i <= limit; i++) {
            if (isPrime(i)) {
                System.out.print(i + " ");
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        printPrimes(50);
    }
}
```

Output: `2 3 5 7 11 13 17 19 23 29 31 37 41 43 47`

Perhatikan: `return false` di dalam loop langsung mengakhiri method, jadi tidak perlu variable flag seperti versi sebelumnya.

### Jawaban Soal 5

Output:

```
1
11
```

- `a(n)`: primitive, yang diubah hanya salinan `x`. `n` tetap `1`.
- `b(data)`: reference disalin, tapi menunjuk array yang sama. `arr[0]` diubah jadi `11`, dan `data[0]` ikut berubah.
- `c(data)`: `arr` hanya salinan reference. Mengarahkannya ke array baru tidak mengubah `data` milik `main`. `data[0]` tetap `11`.

### Jawaban Soal 6

```java
public class PowerDemo {

    public static long power(int base, int exp) {
        if (exp == 0) {
            return 1;                      // base case
        }
        return base * power(base, exp - 1);   // recursive case
    }

    public static void main(String[] args) {
        System.out.println(power(2, 10));   // 1024
        System.out.println(power(5, 3));    // 125
        System.out.println(power(9, 0));    // 1
    }
}
```

Versi lebih efisien (exponentiation by squaring, bonus pengetahuan):

```java
public static long fastPower(long base, int exp) {
    if (exp == 0) return 1;
    long half = fastPower(base, exp / 2);
    return (exp % 2 == 0) ? half * half : half * half * base;
}
```

### Jawaban Soal 7

```java
public class AverageVarargs {

    public static double average(double... values) {
        if (values.length == 0) {
            return 0;
        }
        double total = 0;
        for (double v : values) {
            total += v;
        }
        return total / values.length;
    }

    public static void main(String[] args) {
        System.out.println(average());                  // 0.0
        System.out.println(average(10));                // 10.0
        System.out.println(average(80, 90, 75.5));      // 81.833...
    }
}
```

### Jawaban Soal 8

```java
public class NumberUtil {

    public static int reverseNumber(int n) {
        int reversed = 0;
        while (n > 0) {
            int lastDigit = n % 10;
            reversed = reversed * 10 + lastDigit;
            n /= 10;
        }
        return reversed;
    }

    public static boolean isPalindromeNumber(int n) {
        return n == reverseNumber(n);
    }

    public static void main(String[] args) {
        System.out.println(reverseNumber(12345));        // 54321
        System.out.println(isPalindromeNumber(12321));   // true
        System.out.println(isPalindromeNumber(12345));   // false
    }
}
```

Catatan: untuk angka yang sangat besar, `reversed` bisa overflow `int`. Untuk kasus nyata, pakai `long`.

### Jawaban Soal 9

Masalahnya ada tiga:

1. Return type `int` tapi mengembalikan `String` (`"A"`, `"B"`).
2. Jalur `score < 60` tidak mengembalikan apa-apa (missing return).
3. Nama method `grade` yang mengembalikan huruf lebih cocok bertipe `String`.

Perbaikan:

```java
public static String grade(int score) {
    if (score >= 80) {
        return "A";
    } else if (score >= 60) {
        return "B";
    }
    return "C";
}
```

### Jawaban Soal 10

```java
public class GcdDemo {

    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;               // base case
        }
        return gcd(b, a % b);       // recursive case
    }

    public static void main(String[] args) {
        System.out.println(gcd(48, 18));   // 6
        System.out.println(gcd(17, 5));    // 1
        System.out.println(gcd(100, 25));  // 25
    }
}
```

Jejak `gcd(48, 18)`: `gcd(18, 12)` → `gcd(12, 6)` → `gcd(6, 0)` → `6`.

### Jawaban Soal 11

```java
public class FibonacciCompare {

    public static long fibRecursive(int n) {
        if (n <= 1) return n;
        return fibRecursive(n - 1) + fibRecursive(n - 2);
    }

    public static long fibIterative(int n) {
        if (n <= 1) return n;
        long prev = 0;
        long curr = 1;
        for (int i = 2; i <= n; i++) {
            long next = prev + curr;
            prev = curr;
            curr = next;
        }
        return curr;
    }

    public static void main(String[] args) {
        int n = 40;

        long start = System.nanoTime();
        long r1 = fibRecursive(n);
        long t1 = System.nanoTime() - start;

        start = System.nanoTime();
        long r2 = fibIterative(n);
        long t2 = System.nanoTime() - start;

        System.out.println("Recursive: " + r1 + " in " + t1 / 1_000_000 + " ms");
        System.out.println("Iterative: " + r2 + " in " + t2 / 1_000_000 + " ms");
    }
}
```

Yang diamati: hasil sama (`102334155`), tapi versi rekursif bisa memakan waktu ratusan milidetik sampai beberapa detik, sedangkan iteratif hampir instan. Alasannya, `fibRecursive(n)` menghitung ulang sub-masalah yang sama berkali-kali (kompleksitas eksponensial, sekitar O(2^n)), sedangkan iteratif linear O(n). Solusi rekursif yang efisien memakai memoization, nanti dibahas di materi algoritma.
