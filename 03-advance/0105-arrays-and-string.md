# 0105 - Arrays and String

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Membuat, mengisi, dan melakukan iterasi pada array 1 dimensi dan 2 dimensi
- Memakai utility class `Arrays` (`toString`, `sort`, `fill`, `copyOf`, `equals`, `binarySearch`)
- Memahami bahwa array adalah reference type dan ukurannya tetap
- Memakai method-method penting `String`
- Memahami immutability `String` dan kapan memakai `StringBuilder`
- Memproses teks: split, join, palindrome, count, dan sebagainya

---

## 1. Teori

## Bagian A: Array

### 1.1 Apa itu Array?

**Analogi:** array itu seperti **deretan loker bernomor**. Jumlah loker tetap (ditentukan saat dibuat), isinya bertipe sama, dan nomornya mulai dari **0**.

```
index:   0    1    2    3    4
value: [ 10 | 20 | 30 | 40 | 50 ]
```

### 1.2 Deklarasi dan Inisialisasi

```java
// 1) declare + allocate (elements get default values)
int[] numbers = new int[5];          // [0, 0, 0, 0, 0]

// 2) declare + initialize with values
int[] scores = {90, 85, 70, 100};

// 3) allocate, then fill later
String[] names = new String[3];
names[0] = "Budi";
names[1] = "Sari";
names[2] = "Andi";
```

Default value elemen array:

| Tipe | Default |
|---|---|
| `int`, `long`, `short`, `byte` | `0` |
| `double`, `float` | `0.0` |
| `boolean` | `false` |
| `char` | `'\u0000'` |
| reference (`String`, object) | `null` |

### 1.3 Akses Elemen dan `length`

```java
int[] a = {10, 20, 30};

System.out.println(a[0]);        // 10
System.out.println(a.length);    // 3 (property, no parentheses)
a[1] = 99;                       // modify

// the last element
System.out.println(a[a.length - 1]);   // 30
```

> `length` untuk **array** adalah property (tanpa kurung). `length()` untuk **String** adalah method (pakai kurung). Ini jebakan yang sering bikin bingung.

### 1.4 `ArrayIndexOutOfBoundsException`

Index valid: `0` sampai `length - 1`.

```java
int[] a = {1, 2, 3};
System.out.println(a[3]);   // runtime error: ArrayIndexOutOfBoundsException
```

Kesalahan umum di loop:

```java
for (int i = 0; i <= a.length; i++) { }   // BUG: should be i < a.length
```

### 1.5 Iterasi Array

```java
int[] a = {10, 20, 30};

// classic for: when you need the index
for (int i = 0; i < a.length; i++) {
    System.out.println("a[" + i + "] = " + a[i]);
}

// enhanced for: when you only need the values
for (int value : a) {
    System.out.println(value);
}
```

Enhanced `for` tidak cocok kalau kamu ingin **mengubah** elemen (variable `value` hanya salinan untuk primitive).

### 1.6 Array adalah Reference Type

```java
int[] a = {1, 2, 3};
int[] b = a;        // b points to the SAME array, no copy is made
b[0] = 99;
System.out.println(a[0]);   // 99
```

Untuk membuat **salinan** sebenarnya:

```java
int[] copy1 = Arrays.copyOf(a, a.length);
int[] copy2 = a.clone();
```

(Keduanya adalah *shallow copy*. Untuk array berisi object, isinya tetap sharing reference yang sama.)

### 1.7 Ukuran Array Tetap

Setelah dibuat, ukuran array **tidak bisa diubah**. Kalau butuh ukuran dinamis, pakai `ArrayList` (materi Collections nanti). Untuk "memperbesar", kamu harus membuat array baru lalu menyalin isinya:

```java
int[] bigger = Arrays.copyOf(a, a.length * 2);
```

### 1.8 Utility Class `Arrays`

```java
import java.util.Arrays;

int[] a = {5, 2, 9, 1, 7};

System.out.println(Arrays.toString(a));        // [5, 2, 9, 1, 7]

Arrays.sort(a);                                 // sorts in place
System.out.println(Arrays.toString(a));        // [1, 2, 5, 7, 9]

int idx = Arrays.binarySearch(a, 7);            // array MUST be sorted first
System.out.println(idx);                        // 3

int[] filled = new int[4];
Arrays.fill(filled, 8);                         // [8, 8, 8, 8]

int[] copy = Arrays.copyOf(a, 3);               // [1, 2, 5]
int[] range = Arrays.copyOfRange(a, 1, 4);      // [2, 5, 7]

System.out.println(Arrays.equals(a, copy));     // false (compares content)
System.out.println(a.equals(copy));             // false (compares reference, avoid this)
```

> `System.out.println(a)` pada array mencetak sesuatu seperti `[I@6d06d69c`, bukan isinya. Selalu pakai `Arrays.toString(a)`.

### 1.9 Array 2 Dimensi

Array 2D sebenarnya adalah **array dari array**.

```java
int[][] matrix = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

System.out.println(matrix[1][2]);          // 6 (row 1, column 2)
System.out.println(matrix.length);         // 3 (number of rows)
System.out.println(matrix[0].length);      // 3 (number of columns in row 0)

int[][] grid = new int[3][4];              // 3 rows, 4 columns, all zeros
```

Iterasi:

```java
for (int row = 0; row < matrix.length; row++) {
    for (int col = 0; col < matrix[row].length; col++) {
        System.out.print(matrix[row][col] + " ");
    }
    System.out.println();
}

// print quickly
System.out.println(Arrays.deepToString(matrix));
```

Jagged array (baris dengan panjang berbeda):

```java
int[][] jagged = new int[3][];
jagged[0] = new int[]{1};
jagged[1] = new int[]{1, 2};
jagged[2] = new int[]{1, 2, 3};
```

### 1.10 Array dan Method

```java
public static int sum(int[] numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}

public static int[] doubleAll(int[] numbers) {
    int[] result = new int[numbers.length];
    for (int i = 0; i < numbers.length; i++) {
        result[i] = numbers[i] * 2;
    }
    return result;
}
```

Ingat pass-by-value: method yang menerima array bisa mengubah **isi** array asli.

### 1.11 `main(String[] args)`

`args` adalah array `String` berisi argument command line.

```bash
java Main hello 42
```

```java
public static void main(String[] args) {
    System.out.println(args.length);   // 2
    System.out.println(args[0]);       // hello
}
```

---

## Bagian B: String

### 1.12 Recap: `String` Immutable

Setiap operasi "mengubah" `String` sebenarnya **membuat String baru**. Objek aslinya tidak berubah.

```java
String s = "Java";
s.toUpperCase();                 // result is discarded
System.out.println(s);           // Java (unchanged)

s = s.toUpperCase();             // assign the result back
System.out.println(s);           // JAVA
```

### 1.13 Method-Method Penting `String`

```java
String s = "  Hello, Java World  ";
```

| Method | Contoh | Hasil |
|---|---|---|
| `length()` | `s.length()` | `21` |
| `charAt(i)` | `"Java".charAt(1)` | `'a'` |
| `isEmpty()` | `"".isEmpty()` | `true` |
| `isBlank()` | `"   ".isBlank()` | `true` |
| `trim()` / `strip()` | `s.strip()` | `"Hello, Java World"` |
| `toUpperCase()` / `toLowerCase()` | `"Java".toLowerCase()` | `"java"` |
| `substring(start)` | `"Hello".substring(2)` | `"llo"` |
| `substring(start, end)` | `"Hello".substring(1, 4)` | `"ell"` (end tidak termasuk) |
| `indexOf(x)` | `"banana".indexOf("an")` | `1` (`-1` jika tidak ada) |
| `lastIndexOf(x)` | `"banana".lastIndexOf("an")` | `3` |
| `contains(x)` | `"Java".contains("av")` | `true` |
| `startsWith(x)` / `endsWith(x)` | `"Java".startsWith("Ja")` | `true` |
| `replace(a, b)` | `"a-b-c".replace("-", "+")` | `"a+b+c"` |
| `equals(x)` | `"a".equals("a")` | `true` |
| `equalsIgnoreCase(x)` | `"Java".equalsIgnoreCase("JAVA")` | `true` |
| `compareTo(x)` | `"a".compareTo("b")` | negatif (a < b) |
| `repeat(n)` | `"ab".repeat(3)` | `"ababab"` |
| `toCharArray()` | `"abc".toCharArray()` | `['a','b','c']` |
| `String.valueOf(x)` | `String.valueOf(42)` | `"42"` |

### 1.14 `split` dan `join`

```java
String csv = "apple,banana,cherry";
String[] fruits = csv.split(",");              // ["apple", "banana", "cherry"]

String sentence = "Java   is   fun";
String[] words = sentence.split("\\s+");       // split by one or more whitespace

String joined = String.join(" | ", fruits);    // "apple | banana | cherry"
```

Catatan: `split` memakai **regex**. Karakter spesial seperti `.` dan `|` harus di-escape:

```java
"a.b.c".split("\\.");     // OK
"a.b.c".split(".");       // WRONG: returns an empty array
```

Elemen kosong di **akhir** hasil `split` dibuang:

```java
"a,b,,".split(",").length;       // 2
"a,b,,".split(",", -1).length;   // 4
```

### 1.15 Perbandingan `String`

```java
String a = "java";
String b = "JAVA";

a.equals(b);               // false
a.equalsIgnoreCase(b);     // true
a.compareTo("kotlin");     // negative (a comes before k)
```

`==` hanya membandingkan reference. Gunakan `.equals()` untuk isi.

### 1.16 `StringBuilder`

Karena `String` immutable, menggabungkan banyak string di dalam loop itu boros (membuat banyak objek sementara). Gunakan `StringBuilder`, yang **mutable**.

```java
// inefficient for many iterations
String s = "";
for (int i = 0; i < 1000; i++) {
    s += i;
}

// efficient
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

Method yang sering dipakai:

```java
StringBuilder sb = new StringBuilder("Hello");
sb.append(" World");        // Hello World
sb.insert(0, ">> ");        // >> Hello World
sb.reverse();               // dlroW olleH >>
sb.setLength(0);            // clear
sb.length();
sb.charAt(0);
sb.deleteCharAt(0);
sb.setCharAt(0, 'X');
```

| Kelas | Mutable? | Thread-safe? | Pakai kapan |
|---|---|---|---|
| `String` | Tidak | Ya (immutable) | Teks biasa, konstanta |
| `StringBuilder` | Ya | Tidak | Membangun string di loop (paling umum) |
| `StringBuffer` | Ya | Ya (lebih lambat) | Hampir tidak pernah di kode baru |

### 1.17 `String.format` dan Text Block

```java
String msg = String.format("Name: %s, Age: %d, GPA: %.2f", "Budi", 20, 3.456);
```

Text block (Java 15+) untuk teks multi-baris:

```java
String json = """
        {
          "name": "Budi",
          "age": 20
        }
        """;
```

### 1.18 Class `Character` (Utility untuk `char`)

```java
Character.isDigit('7');           // true
Character.isLetter('a');          // true
Character.isLetterOrDigit('_');   // false
Character.isUpperCase('A');       // true
Character.isWhitespace(' ');      // true
Character.toUpperCase('a');       // 'A'
Character.getNumericValue('7');   // 7
```

Trik `char` aritmatika:

```java
char c = '7';
int digit = c - '0';        // 7
char next = (char) (c + 1); // '8'
```

### 1.19 Konversi `String` dan Angka

```java
int n = Integer.parseInt("123");
double d = Double.parseDouble("3.14");
String s1 = String.valueOf(42);
String s2 = Integer.toString(42);
String s3 = "" + 42;

Integer.parseInt("abc");   // runtime error: NumberFormatException
```

---

## 2. Praktek

### Praktek 2.1: Dasar Array

```java
import java.util.Arrays;

public class ArrayBasics {
    public static void main(String[] args) {
        int[] scores = {90, 85, 70, 100, 65};

        System.out.println("Array  : " + Arrays.toString(scores));
        System.out.println("Length : " + scores.length);
        System.out.println("First  : " + scores[0]);
        System.out.println("Last   : " + scores[scores.length - 1]);

        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        System.out.printf("Sum    : %d%n", sum);
        System.out.printf("Average: %.2f%n", (double) sum / scores.length);
    }
}
```

### Praktek 2.2: Reference vs Copy

```java
import java.util.Arrays;

public class ArrayReferenceDemo {
    public static void main(String[] args) {
        int[] original = {1, 2, 3};

        int[] alias = original;                       // same array
        int[] copy = Arrays.copyOf(original, original.length);   // new array

        alias[0] = 99;

        System.out.println("original: " + Arrays.toString(original));   // [99, 2, 3]
        System.out.println("alias   : " + Arrays.toString(alias));      // [99, 2, 3]
        System.out.println("copy    : " + Arrays.toString(copy));       // [1, 2, 3]
    }
}
```

### Praktek 2.3: Utility `Arrays`

```java
import java.util.Arrays;

public class ArraysUtilDemo {
    public static void main(String[] args) {
        int[] a = {5, 2, 9, 1, 7};

        Arrays.sort(a);
        System.out.println("Sorted      : " + Arrays.toString(a));
        System.out.println("Index of 7  : " + Arrays.binarySearch(a, 7));

        int[] filled = new int[5];
        Arrays.fill(filled, 3);
        System.out.println("Filled      : " + Arrays.toString(filled));

        int[] firstThree = Arrays.copyOf(a, 3);
        System.out.println("First three : " + Arrays.toString(firstThree));

        System.out.println("Equals      : " + Arrays.equals(a, a.clone()));
    }
}
```

### Praktek 2.4: Array 2D

```java
import java.util.Arrays;

public class Matrix2D {
    public static void main(String[] args) {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        System.out.println("Print matrix:");
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.printf("%3d", value);
            }
            System.out.println();
        }

        int diagonalSum = 0;
        for (int i = 0; i < matrix.length; i++) {
            diagonalSum += matrix[i][i];
        }
        System.out.println("Main diagonal sum: " + diagonalSum);   // 15

        System.out.println(Arrays.deepToString(matrix));
    }
}
```

### Praktek 2.5: Method `String`

```java
public class StringMethodsDemo {
    public static void main(String[] args) {
        String text = "  Learning Java is Fun  ";

        System.out.println("[" + text.strip() + "]");
        System.out.println(text.strip().length());
        System.out.println(text.toUpperCase());
        System.out.println(text.contains("Java"));
        System.out.println(text.indexOf("Java"));
        System.out.println(text.strip().substring(9, 13));
        System.out.println(text.replace("Fun", "Awesome"));

        String csv = "apple,banana,cherry";
        String[] parts = csv.split(",");
        for (String p : parts) {
            System.out.println("- " + p);
        }
        System.out.println(String.join(" + ", parts));
    }
}
```

### Praktek 2.6: Immutability dan `StringBuilder`

```java
public class StringBuilderDemo {
    public static void main(String[] args) {
        String s = "Java";
        s.concat(" 21");
        s.toUpperCase();
        System.out.println(s);        // Java (nothing changed)

        StringBuilder sb = new StringBuilder("Java");
        sb.append(" 21");
        sb.insert(0, "Learn ");
        System.out.println(sb);       // Learn Java 21

        sb.reverse();
        System.out.println(sb);       // 12 avaJ nraeL

        String a = "hello";
        String b = "hel" + "lo";      // compile-time constant, same pooled object
        String part = "hel";
        String c = part + "lo";       // computed at runtime, new object

        System.out.println(a == b);       // true
        System.out.println(a == c);       // false
        System.out.println(a.equals(c));  // true
    }
}
```

### Praktek 2.7: Memproses Karakter

```java
public class CharCounter {
    public static void main(String[] args) {
        String text = "Hello World 2026!";

        int letters = 0, digits = 0, spaces = 0, others = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
            } else if (Character.isDigit(c)) {
                digits++;
            } else if (Character.isWhitespace(c)) {
                spaces++;
            } else {
                others++;
            }
        }

        System.out.printf("Letters: %d, Digits: %d, Spaces: %d, Others: %d%n",
                letters, digits, spaces, others);
    }
}
```

---

## 3. Latihan

**Soal 1.** Diberikan `int[] data = {4, 8, 15, 16, 23, 42}`. Buat program yang mencetak jumlah, rata-rata (2 digit desimal), nilai terbesar, dan nilai terkecil, **tanpa** memakai `Arrays.sort` atau `Math.max/min`.

**Soal 2.** Buat method `reverse(int[] arr)` yang membalik isi array **di tempat** (in place, tanpa membuat array baru).

**Soal 3.** Buat method `secondLargest(int[] arr)` yang mengembalikan nilai terbesar kedua (anggap elemen minimal 2 dan ada nilai yang berbeda).

**Soal 4.** Buat method `countOccurrences(int[] arr, int target)` yang mengembalikan berapa kali `target` muncul di array.

**Soal 5.** Implementasikan **bubble sort** manual untuk mengurutkan `{5, 2, 9, 1, 7}` secara ascending. Lalu cetak hasilnya.

**Soal 6.** Buat method yang menghapus duplikat dari array dan mengembalikan array baru berisi nilai unik (urutan kemunculan pertama dipertahankan). Contoh: `{3, 1, 3, 2, 1}` menjadi `{3, 1, 2}`.

**Soal 7.** Diberikan `int[][] m = {{1, 2, 3}, {4, 5, 6}}`. Buat program yang:
- mencetak jumlah tiap baris
- mencetak jumlah tiap kolom
- mencetak **transpose** matrix (hasil 3 baris x 2 kolom)

**Soal 8.** Buat method `isPalindrome(String s)` yang **mengabaikan huruf besar/kecil, spasi, dan tanda baca**. Contoh: `"A man, a plan, a canal: Panama"` adalah palindrome.

**Soal 9.** Buat method yang menghitung jumlah **vokal** dan **konsonan** dalam sebuah kalimat.

**Soal 10.** Buat method `isAnagram(String a, String b)`. Contoh: `"Listen"` dan `"Silent"` adalah anagram (abaikan huruf besar/kecil dan spasi).

**Soal 11.** Buat method `capitalizeWords(String sentence)` yang membuat huruf pertama tiap kata menjadi kapital dan sisanya kecil. Contoh: `"jAVA is  fUN"` menjadi `"Java Is Fun"`. Perhatikan spasi ganda.

**Soal 12.** Prediksi output kode berikut:

```java
String s1 = "Java";
String s2 = s1.concat(" 21");
String s3 = s1.toUpperCase();
System.out.println(s1);
System.out.println(s2);
System.out.println(s3);

StringBuilder sb = new StringBuilder("Java");
StringBuilder sb2 = sb.append(" 21");
System.out.println(sb);
System.out.println(sb == sb2);

int[] a = {1, 2, 3};
int[] b = a;
b[0] = 100;
System.out.println(a[0]);
System.out.println("a,b,,".split(",").length);
```

**Soal 13 (Bonus).** Buat program **word frequency**: dari kalimat `"the quick brown fox jumps over the lazy dog the end"`, hitung berapa kali tiap kata muncul **tanpa memakai Map** (gunakan dua array: satu untuk kata unik, satu untuk hitungan). Setelah belajar Collections, kamu akan bisa menyelesaikan ini dengan `HashMap` jauh lebih ringkas.

**Soal 14 (Bonus).** Compress string: `"aaabbc"` menjadi `"a3b2c1"`. Gunakan `StringBuilder`.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
public class ArrayStats {
    public static void main(String[] args) {
        int[] data = {4, 8, 15, 16, 23, 42};

        int sum = 0;
        int max = data[0];
        int min = data[0];

        for (int n : data) {
            sum += n;
            if (n > max) max = n;
            if (n < min) min = n;
        }

        double average = (double) sum / data.length;

        System.out.println("Sum    : " + sum);        // 108
        System.out.printf("Average: %.2f%n", average); // 18.00
        System.out.println("Max    : " + max);        // 42
        System.out.println("Min    : " + min);        // 4
    }
}
```

Inisialisasi `max` dan `min` dengan `data[0]` (bukan `0`) supaya benar untuk array yang semuanya negatif.

### Jawaban Soal 2

```java
import java.util.Arrays;

public class ReverseArray {

    public static void reverse(int[] arr) {
        int left = 0;
        int right = arr.length - 1;

        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }

    public static void main(String[] args) {
        int[] data = {1, 2, 3, 4, 5};
        reverse(data);
        System.out.println(Arrays.toString(data));   // [5, 4, 3, 2, 1]
    }
}
```

Teknik two pointers: dua penunjuk dari ujung berlawanan yang bergerak ke tengah. Method tidak perlu `return` karena yang diubah adalah isi array asli (ingat pass-by-value untuk reference).

### Jawaban Soal 3

```java
public class SecondLargest {

    public static int secondLargest(int[] arr) {
        int first = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;

        for (int n : arr) {
            if (n > first) {
                second = first;
                first = n;
            } else if (n > second && n != first) {
                second = n;
            }
        }
        return second;
    }

    public static void main(String[] args) {
        System.out.println(secondLargest(new int[]{4, 8, 15, 16, 23, 42}));  // 23
        System.out.println(secondLargest(new int[]{10, 10, 5}));             // 5
    }
}
```

Hanya satu kali pass, tidak perlu sorting.

### Jawaban Soal 4

```java
public class CountOccurrences {

    public static int countOccurrences(int[] arr, int target) {
        int count = 0;
        for (int n : arr) {
            if (n == target) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        int[] data = {1, 3, 3, 7, 3, 9};
        System.out.println(countOccurrences(data, 3));   // 3
        System.out.println(countOccurrences(data, 5));   // 0
    }
}
```

### Jawaban Soal 5

```java
import java.util.Arrays;

public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = {5, 2, 9, 1, 7};

        for (int i = 0; i < arr.length - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }

            if (!swapped) {
                break;   // already sorted, stop early
            }
        }

        System.out.println(Arrays.toString(arr));   // [1, 2, 5, 7, 9]
    }
}
```

Setiap pass "menggelembungkan" nilai terbesar ke ujung kanan, makanya batas inner loop `arr.length - 1 - i`. Kompleksitas O(n^2), cukup untuk belajar. Di kode nyata pakai `Arrays.sort`.

### Jawaban Soal 6

```java
import java.util.Arrays;

public class RemoveDuplicates {

    public static int[] unique(int[] arr) {
        int[] temp = new int[arr.length];
        int count = 0;

        for (int n : arr) {
            boolean exists = false;
            for (int i = 0; i < count; i++) {
                if (temp[i] == n) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                temp[count++] = n;
            }
        }
        return Arrays.copyOf(temp, count);
    }

    public static void main(String[] args) {
        int[] result = unique(new int[]{3, 1, 3, 2, 1});
        System.out.println(Arrays.toString(result));   // [3, 1, 2]
    }
}
```

`temp` dibuat sebesar array asli (kemungkinan terburuk: tidak ada duplikat), lalu dipotong dengan `Arrays.copyOf` sesuai jumlah unik.

### Jawaban Soal 7

```java
import java.util.Arrays;

public class MatrixOps {
    public static void main(String[] args) {
        int[][] m = {
            {1, 2, 3},
            {4, 5, 6}
        };

        int rows = m.length;
        int cols = m[0].length;

        // sum of each row
        for (int r = 0; r < rows; r++) {
            int sum = 0;
            for (int c = 0; c < cols; c++) {
                sum += m[r][c];
            }
            System.out.println("Row " + r + " sum: " + sum);   // 6, 15
        }

        // sum of each column
        for (int c = 0; c < cols; c++) {
            int sum = 0;
            for (int r = 0; r < rows; r++) {
                sum += m[r][c];
            }
            System.out.println("Col " + c + " sum: " + sum);   // 5, 7, 9
        }

        // transpose: new matrix has swapped dimensions
        int[][] t = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                t[c][r] = m[r][c];
            }
        }
        System.out.println(Arrays.deepToString(t));   // [[1, 4], [2, 5], [3, 6]]
    }
}
```

### Jawaban Soal 8

```java
public class PalindromeCheck {

    public static boolean isPalindrome(String s) {
        String cleaned = s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        int left = 0;
        int right = cleaned.length() - 1;

        while (left < right) {
            if (cleaned.charAt(left) != cleaned.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome("A man, a plan, a canal: Panama"));  // true
        System.out.println(isPalindrome("Hello"));                            // false
        System.out.println(isPalindrome("Madam"));                            // true
    }
}
```

`replaceAll("[^a-zA-Z0-9]", "")` membuang semua karakter selain huruf dan angka (regex). Perbandingan `char` memakai `!=`, bukan `.equals`, karena `char` itu primitive.

### Jawaban Soal 9

```java
public class VowelCounter {

    public static void countVowelsAndConsonants(String sentence) {
        int vowels = 0;
        int consonants = 0;

        for (char c : sentence.toLowerCase().toCharArray()) {
            if (!Character.isLetter(c)) {
                continue;   // skip spaces, digits, punctuation
            }
            if ("aeiou".indexOf(c) >= 0) {
                vowels++;
            } else {
                consonants++;
            }
        }

        System.out.println("Vowels    : " + vowels);
        System.out.println("Consonants: " + consonants);
    }

    public static void main(String[] args) {
        countVowelsAndConsonants("Learning Java is Fun!");   // Vowels: 7, Consonants: 10
    }
}
```

Trik `"aeiou".indexOf(c) >= 0` mengecek apakah `c` ada di string vokal. Kalau `indexOf` mengembalikan `-1`, berarti bukan vokal.

Untuk `"Learning Java is Fun!"`: vokal = `e, a, i` (Learning) + `a, a` (Java) + `i` (is) + `u` (Fun) = **7**, konsonan = **10**.

### Jawaban Soal 10

```java
import java.util.Arrays;

public class AnagramCheck {

    public static boolean isAnagram(String a, String b) {
        char[] first = a.replace(" ", "").toLowerCase().toCharArray();
        char[] second = b.replace(" ", "").toLowerCase().toCharArray();

        Arrays.sort(first);
        Arrays.sort(second);

        return Arrays.equals(first, second);
    }

    public static void main(String[] args) {
        System.out.println(isAnagram("Listen", "Silent"));           // true
        System.out.println(isAnagram("Dormitory", "Dirty room"));    // true
        System.out.println(isAnagram("Hello", "World"));             // false
    }
}
```

Dua string adalah anagram kalau setelah dibersihkan dan diurutkan, isinya sama persis. Pakai `Arrays.equals` (bukan `==`) untuk membandingkan isi array.

### Jawaban Soal 11

```java
public class CapitalizeWords {

    public static String capitalizeWords(String sentence) {
        String[] words = sentence.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(capitalizeWords("jAVA is  fUN"));       // Java Is Fun
        System.out.println(capitalizeWords("  hello   world  "));  // Hello World
    }
}
```

`split("\\s+")` menangani spasi ganda. `trim()` di awal mencegah elemen kosong di depan hasil split.

### Jawaban Soal 12

```
Java
Java 21
JAVA
Java 21
true
100
2
```

- `s1.concat(...)` dan `s1.toUpperCase()` mengembalikan String baru. `s1` tetap `"Java"`.
- `sb.append(...)` mengubah `sb` itu sendiri dan mengembalikan **objek yang sama**, jadi `sb == sb2` bernilai `true`.
- `b = a` hanya menyalin reference, jadi `a[0]` ikut menjadi `100`.
- `"a,b,,".split(",")` membuang elemen kosong di akhir, hasilnya `["a", "b"]` (length `2`).

### Jawaban Soal 13

```java
public class WordFrequency {
    public static void main(String[] args) {
        String sentence = "the quick brown fox jumps over the lazy dog the end";
        String[] words = sentence.split("\\s+");

        String[] uniqueWords = new String[words.length];
        int[] counts = new int[words.length];
        int uniqueCount = 0;

        for (String word : words) {
            int foundIndex = -1;
            for (int i = 0; i < uniqueCount; i++) {
                if (uniqueWords[i].equals(word)) {
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex >= 0) {
                counts[foundIndex]++;
            } else {
                uniqueWords[uniqueCount] = word;
                counts[uniqueCount] = 1;
                uniqueCount++;
            }
        }

        for (int i = 0; i < uniqueCount; i++) {
            System.out.println(uniqueWords[i] + ": " + counts[i]);
        }
    }
}
```

Hasil: `the: 3`, sisanya `1`. Dua array paralel (`uniqueWords[i]` berpasangan dengan `counts[i]`) itu ribet dan rawan salah, dan itulah alasan kenapa `Map` ada.

### Jawaban Soal 14

```java
public class CompressString {

    public static String compress(String s) {
        if (s.isEmpty()) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        char current = s.charAt(0);
        int count = 1;

        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == current) {
                count++;
            } else {
                sb.append(current).append(count);
                current = s.charAt(i);
                count = 1;
            }
        }
        sb.append(current).append(count);   // flush the last group

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(compress("aaabbc"));   // a3b2c1
        System.out.println(compress("abc"));      // a1b1c1
        System.out.println(compress(""));         // (empty)
    }
}
```

Jebakan klasik: grup terakhir tidak ikut ter-append di dalam loop, makanya perlu `sb.append(current).append(count)` setelah loop selesai.
