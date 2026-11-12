# 0106 - OOP Basics: Class, Object, Constructor, Encapsulation

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Menjelaskan perbedaan class dan object
- Membuat class dengan field, method, dan constructor
- Memakai keyword `this`, `static`, dan `final` dengan benar
- Menerapkan encapsulation (private field + getter/setter + validasi)
- Meng-override `toString`, `equals`, dan `hashCode`
- Memahami reference, `null`, dan `NullPointerException`
- Membuat immutable class dan memakai composition (has-a)
- Mengorganisir kode dengan `package` dan `import`

---

## 1. Teori

### 1.1 Class dan Object

**Analogi:** class itu **cetakan kue** (blueprint), object itu **kue** yang dihasilkan. Satu cetakan bisa menghasilkan banyak kue, tiap kue punya "isi" sendiri-sendiri (rasa, topping), tapi bentuk dasarnya sama.

- **Class** = definisi: data apa yang dimiliki (field) dan apa yang bisa dilakukan (method).
- **Object** (instance) = wujud nyata hasil `new` dari class, tinggal di memory (heap).

```java
public class Student {
    // fields (state)
    String name;
    int age;

    // method (behavior)
    void introduce() {
        System.out.println("Hi, I'm " + name + ", " + age + " years old.");
    }
}
```

```java
Student s1 = new Student();   // create an object
s1.name = "Budi";
s1.age = 20;
s1.introduce();

Student s2 = new Student();   // another, independent object
s2.name = "Sari";
s2.age = 22;
s2.introduce();
```

### 1.2 Field (Instance Variable)

Variable yang dideklarasikan di dalam class, di luar method. Setiap object punya **salinan sendiri**.

Field punya **default value** (berbeda dengan local variable yang wajib diinisialisasi):

| Tipe | Default |
|---|---|
| `int`, `long`, `short`, `byte` | `0` |
| `double`, `float` | `0.0` |
| `boolean` | `false` |
| `char` | `'\u0000'` |
| reference (`String`, object) | `null` |

### 1.3 Constructor

Method khusus yang dipanggil saat `new`, tugasnya **menginisialisasi object**.

Aturan:
- Nama sama persis dengan nama class
- **Tidak punya return type** (bahkan bukan `void`)
- Boleh di-overload (beberapa constructor dengan parameter berbeda)

```java
public class Student {
    String name;
    int age;

    // parameterized constructor
    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

Student s = new Student("Budi", 20);
```

**Default constructor:** kalau kamu **tidak menulis constructor sama sekali**, compiler otomatis menyediakan constructor tanpa parameter. Begitu kamu menulis satu constructor saja, default constructor **hilang**.

```java
Student s = new Student();   // compile error if only Student(String, int) is defined
```

### 1.4 Keyword `this`

| Pemakaian | Fungsi |
|---|---|
| `this.name` | merujuk ke field milik object saat ini (membedakan dari parameter yang namanya sama) |
| `this.method()` | memanggil method lain di object yang sama |
| `this(...)` | memanggil constructor lain di class yang sama (**harus baris pertama**) |

```java
public class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;     // this.name = field, name = parameter
        this.age = age;
    }

    public Student(String name) {
        this(name, 18);       // delegate to the other constructor
    }

    public Student() {
        this("Unknown");      // delegate again
    }
}
```

Tanpa `this`, `name = name;` hanya mengisi parameter dengan dirinya sendiri dan field tetap tidak berubah (bug klasik yang tidak memunculkan error).

### 1.5 Access Modifier

| Modifier | Class itu sendiri | Package yang sama | Subclass (beda package) | Dimana saja |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(default / package-private)* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

Aturan praktis:
- **Field: `private`** (hampir selalu)
- **Method yang jadi API: `public`**
- Method helper internal: `private`

### 1.6 Encapsulation

**Analogi:** mesin ATM. Kamu tidak boleh membuka brankasnya dan mengambil uang langsung (`balance` itu `private`). Kamu hanya boleh lewat tombol resmi (`withdraw`, `deposit`) yang punya aturan (cek PIN, cek saldo).

Encapsulation = **menyembunyikan data (private) dan mengontrol akses lewat method (public)**, sehingga object selalu berada dalam keadaan valid.

```java
public class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        setAge(age);              // reuse the validation
    }

    // getter
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    // setter with validation
    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
    }
}
```

> `throw new IllegalArgumentException(...)` melempar error kalau data tidak valid. Exception dibahas lengkap di materi `0108`. Untuk sekarang, anggap itu cara menghentikan program saat ada input yang salah.

Kenapa tidak `public` saja fieldnya?

```java
student.age = -5;   // possible if age is public: invalid state, no one stops it
```

Dengan setter, aturan validasi ada di **satu tempat**. Kalau aturan berubah, cukup ubah di satu tempat.

Konvensi getter/setter:
- `getXxx()` / `setXxx(value)`
- Untuk `boolean`: `isXxx()` (misal `isActive()`)
- Tidak semua field butuh setter. Field yang tidak boleh berubah (seperti `id`) cukup punya getter saja.

### 1.7 `static` vs Instance

| | Instance (non-static) | `static` |
|---|---|---|
| Milik | **object** | **class** |
| Jumlah salinan | satu per object | satu untuk semua |
| Diakses lewat | `object.member` | `ClassName.member` |
| Bisa akses instance member? | Ya | **Tidak langsung** |

```java
public class Product {
    private static int count = 0;        // shared by all Product objects
    private final int id;                // each object has its own id
    private String name;

    public Product(String name) {
        this.name = name;
        count++;
        this.id = count;
    }

    public int getId() {
        return id;
    }

    public static int getCount() {       // static method: no `this`
        return count;
    }
}

new Product("Pen");
new Product("Book");
System.out.println(Product.getCount());   // 2
```

Kenapa `main` itu `static`? Karena JVM harus memanggilnya **sebelum ada object apa pun**.

Static method tidak punya `this`, jadi tidak bisa langsung mengakses field/method instance:

```java
public class Demo {
    int value = 10;

    public static void main(String[] args) {
        // System.out.println(value);        // compile error
        Demo d = new Demo();
        System.out.println(d.value);         // OK: go through an object
    }
}
```

Gunakan `static` untuk: utility (`Math.max`), konstanta (`static final`), counter bersama, factory method. Jangan menjadikan sesuatu `static` hanya supaya "gampang dipanggil dari main".

### 1.8 `final`

| Dipasang pada | Artinya |
|---|---|
| variable / field | tidak bisa di-assign ulang setelah diisi |
| method | tidak bisa di-override |
| class | tidak bisa di-extend (contoh: `String`) |

```java
public class Circle {
    public static final double PI = 3.14159;   // constant
    private final String id;                   // must be assigned once (in constructor)

    public Circle(String id) {
        this.id = id;
    }
}
```

> `final` pada reference berarti **reference-nya** tidak bisa diarahkan ke object lain, bukan berarti isi object tidak bisa berubah.
>
> ```java
> final int[] arr = {1, 2, 3};
> arr[0] = 99;          // OK
> // arr = new int[5];  // compile error
> ```

### 1.9 Reference, `null`, dan `NullPointerException`

Variable bertipe class **tidak menyimpan object**, melainkan **reference** (alamat) ke object di heap.

```java
Student a = new Student("Budi", 20);
Student b = a;           // b and a point to the SAME object
b.setAge(30);
System.out.println(a.getAge());   // 30
```

`null` artinya "tidak menunjuk ke object apa pun". Memanggil method pada `null` menghasilkan **`NullPointerException`** (error runtime paling terkenal di Java).

```java
Student s = null;
s.getName();   // NullPointerException
```

Pencegahan:
- Inisialisasi sebelum dipakai
- Cek `if (s != null)` bila memang bisa `null`
- Untuk membandingkan String dengan literal: `"abc".equals(x)` (aman walau `x` `null`)

Garbage Collector (GC) otomatis membersihkan object yang sudah tidak dirujuk siapa pun. Kamu tidak perlu `free()` seperti di C.

### 1.10 `toString()`

Semua class di Java otomatis mewarisi `Object`, yang punya `toString()`. Defaultnya mencetak sesuatu seperti `Student@1b6d3586`, tidak berguna. Override agar informatif:

```java
@Override
public String toString() {
    return "Student{name='" + name + "', age=" + age + "}";
}

System.out.println(student);   // Student{name='Budi', age=20}
```

`@Override` adalah annotation yang meminta compiler memastikan kamu benar-benar meng-override method parent (kalau salah ketik nama, akan error).

### 1.11 `equals()` dan `hashCode()`

Default `equals()` memakai `==` (bandingkan **reference**). Kalau dua object berbeda tapi isinya sama dianggap setara, override `equals` **dan** `hashCode`.

```java
import java.util.Objects;

public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point other = (Point) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
```

**Kontrak:** kalau `a.equals(b)` bernilai `true`, maka `a.hashCode() == b.hashCode()` **wajib**. Melanggar ini akan merusak `HashMap` dan `HashSet` (materi Collections nanti). Aturan praktis: override keduanya sekaligus, dengan field yang sama.

### 1.12 Immutable Class

Object yang **tidak bisa berubah** setelah dibuat. Sangat aman untuk dibagikan antar bagian program dan antar thread (contoh: `String`, `Integer`).

Resep:
1. Class `final` (atau constructor private)
2. Semua field `private final`
3. Tidak ada setter
4. Operasi "mengubah" mengembalikan **object baru**

```java
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);   // new object, original untouched
    }
}
```

### 1.13 Composition (Has-A)

Object bisa **memiliki** object lain sebagai field.

```java
public class Address {
    private final String city;
    private final String street;

    public Address(String city, String street) {
        this.city = city;
        this.street = street;
    }

    public String getCity() { return city; }
    public String getStreet() { return street; }
}

public class Student {
    private String name;
    private Address address;   // Student HAS-A Address

    public Student(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public String getCity() {
        return address.getCity();
    }
}
```

Hubungan "has-a" lebih fleksibel dibanding inheritance "is-a" (dibahas di `0107`).

### 1.14 Urutan Inisialisasi Object

Saat `new Student(...)`:
1. Field diberi default value (`0`, `null`, `false`)
2. Field initializer dijalankan (`private int age = 18;`)
3. Body constructor dijalankan

### 1.15 Package dan Import

Package mengelompokkan class (seperti folder) dan mencegah bentrok nama.

```java
package com.example.school;       // first line of the file

import java.util.Scanner;         // import a class from another package
import java.util.*;               // import everything in the package (use sparingly)

public class Student { }
```

Struktur folder harus sesuai package:

```
src/
└── com/
    └── example/
        └── school/
            ├── Student.java
            └── Main.java
```

Konvensi: nama package **lowercase**, biasanya dari domain terbalik (`com.company.project.module`). Class di `java.lang` (seperti `String`, `Math`, `System`) otomatis ter-import.

### 1.16 POJO / JavaBean

Class sederhana yang berisi field `private`, constructor, getter/setter, dan (biasanya) `toString`/`equals`/`hashCode`. Ini bentuk dasar **entity** dan **DTO** yang akan kamu buat ribuan kali di Spring Boot/JPA. (Java modern punya `record` untuk versi ringkasnya, dibahas di materi `0110`.)

---

## 2. Praktek

> Taruh class `public` di file bernama sama. Di Praktek ini, class pendukung ditulis tanpa `public` di file yang sama supaya mudah dijalankan. Di project nyata, satu class per file.

### Praktek 2.1: Class dan Object Pertama

```java
class Car {
    String brand;
    int speed;

    void accelerate(int amount) {
        speed += amount;
    }

    void printStatus() {
        System.out.println(brand + " is running at " + speed + " km/h");
    }
}

public class Main {
    public static void main(String[] args) {
        Car car1 = new Car();
        car1.brand = "Toyota";
        car1.accelerate(60);

        Car car2 = new Car();
        car2.brand = "Honda";
        car2.accelerate(80);
        car2.accelerate(20);

        car1.printStatus();   // Toyota is running at 60 km/h
        car2.printStatus();   // Honda is running at 100 km/h
    }
}
```

### Praktek 2.2: Constructor dan `this`

```java
class Student {
    private String name;
    private int age;

    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    Student(String name) {
        this(name, 18);
    }

    Student() {
        this("Unknown");
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + "}";
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(new Student("Budi", 20));
        System.out.println(new Student("Sari"));
        System.out.println(new Student());
    }
}
```

### Praktek 2.3: Encapsulation dan Validasi

```java
class BankAccount {
    private final String accountNumber;
    private double balance;

    BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = Math.max(initialBalance, 0);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount("123-456", 1000);

        System.out.println(acc.deposit(500));     // true
        System.out.println(acc.withdraw(2000));   // false (insufficient balance)
        System.out.println(acc.withdraw(300));    // true
        System.out.println(acc.getBalance());     // 1200.0

        // acc.balance = 1_000_000;   // compile error: balance is private
    }
}
```

### Praktek 2.4: `static` Counter

```java
class Product {
    private static int count = 0;
    private final int id;
    private final String name;

    Product(String name) {
        this.name = name;
        this.id = ++count;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public static int getCount() { return count; }
}

public class Main {
    public static void main(String[] args) {
        Product p1 = new Product("Pen");
        Product p2 = new Product("Book");
        Product p3 = new Product("Bag");

        System.out.println(p1.getId() + " " + p1.getName());   // 1 Pen
        System.out.println(p3.getId() + " " + p3.getName());   // 3 Bag
        System.out.println("Total: " + Product.getCount());    // Total: 3
    }
}
```

### Praktek 2.5: `equals` dan `hashCode`

```java
import java.util.Objects;

class Point {
    private final int x;
    private final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point other = (Point) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

public class Main {
    public static void main(String[] args) {
        Point a = new Point(1, 2);
        Point b = new Point(1, 2);
        Point c = a;

        System.out.println(a == b);                       // false (different objects)
        System.out.println(a.equals(b));                  // true  (same content)
        System.out.println(a == c);                       // true  (same reference)
        System.out.println(a.hashCode() == b.hashCode()); // true
        System.out.println(a);                            // (1, 2)
    }
}
```

### Praktek 2.6: Reference dan `NullPointerException`

```java
class Person {
    private String name;

    Person(String name) { this.name = name; }

    String getName() { return name; }
    void setName(String name) { this.name = name; }
}

public class Main {
    public static void main(String[] args) {
        Person p1 = new Person("Budi");
        Person p2 = p1;            // same object
        p2.setName("Sari");
        System.out.println(p1.getName());   // Sari

        Person p3 = null;
        if (p3 != null) {
            System.out.println(p3.getName());
        } else {
            System.out.println("p3 is null, skipped safely");
        }

        // System.out.println(p3.getName());   // NullPointerException
    }
}
```

### Praktek 2.7: Immutable Class dan Composition

```java
final class Address {
    private final String city;
    private final String street;

    Address(String city, String street) {
        this.city = city;
        this.street = street;
    }

    public String getCity() { return city; }

    @Override
    public String toString() { return street + ", " + city; }
}

class Student {
    private final String name;
    private final Address address;

    Student(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        return name + " lives at " + address;
    }
}

public class Main {
    public static void main(String[] args) {
        Address address = new Address("Jakarta", "Jl. Sudirman 1");
        Student student = new Student("Budi", address);
        System.out.println(student);   // Budi lives at Jl. Sudirman 1, Jakarta
    }
}
```

---

## 3. Latihan

**Soal 1.** Buat class `Book` dengan field `private` `title`, `author`, `price`, dan `pages`. Lengkapi dengan constructor, getter, `toString`, dan method `applyDiscount(double percent)` yang mengurangi `price`. Buat 3 object dan cetak.

**Soal 2.** Buat class `Rectangle` dengan `width` dan `height`. Tambahkan method `area()`, `perimeter()`, dan `isSquare()`. Setter harus menolak nilai `<= 0` (boleh mengabaikan perubahan atau melempar `IllegalArgumentException("... must be positive")`).

**Soal 3.** Buat class `BankAccount` dengan field `accountNumber`, `owner`, `balance`. Method:
- `deposit(double amount)` → `boolean`
- `withdraw(double amount)` → `boolean`
- `transferTo(BankAccount target, double amount)` → `boolean` (kurangi saldo sendiri, tambah saldo target, gagal kalau saldo tidak cukup)

Uji skenario sukses dan gagal.

**Soal 4.** Buat class `Employee` dengan **auto-generated ID** memakai field `static`. ID dimulai dari `1001` dan bertambah satu setiap object baru dibuat. Tambahkan `static int getTotalEmployees()`.

**Soal 5.** Prediksi output dan tentukan baris mana yang melempar error:

```java
class Person {
    private String name;
    Person(String name) { this.name = name; }
    String getName() { return name; }
    void setName(String name) { this.name = name; }
}

public class Quiz {
    public static void main(String[] args) {
        Person p1 = new Person("Budi");
        Person p2 = p1;
        p2.setName("Sari");
        System.out.println(p1.getName());

        Person p3 = new Person("Sari");
        System.out.println(p1 == p3);
        System.out.println(p1.equals(p3));

        p1 = null;
        System.out.println(p2.getName());
        System.out.println(p1.getName());
    }
}
```

**Soal 6.** Buat class `Money` yang **immutable** dengan `amount` (`long`, dalam satuan sen) dan `currency` (`String`). Buat method `add(Money other)` dan `subtract(Money other)` yang mengembalikan `Money` baru. Override `equals`, `hashCode`, dan `toString`. Apa yang terjadi jika mata uangnya berbeda? (Silakan lempar `IllegalArgumentException`.)

**Soal 7.** Buat class `Counter` dengan:
- field `private int value`
- `increment()`, `decrement()`, `reset()`, `getValue()`
- `decrement()` tidak boleh membuat `value` di bawah `0`

Buat dua `Counter` dan buktikan keduanya independen.

**Soal 8.** Class berikut punya beberapa masalah desain dan satu bug tersembunyi. Temukan dan perbaiki semuanya:

```java
public class User {
    public String username;
    public String password;
    public int age;

    public User(String username, String password, int age) {
        username = username;
        password = password;
        age = age;
    }
}
```

**Soal 9.** Composition: buat class `Course` (`code`, `title`) dan `Student` (`name`, array `Course[] courses` berukuran maksimal 3). Method `enroll(Course c)` mengembalikan `false` jika sudah penuh atau course sudah ada. Method `printCourses()` mencetak semua course yang diambil.

**Soal 10 (Bonus).** Buat mini sistem perpustakaan: class `Book` (`isbn`, `title`, `available`) dan `Library` yang menyimpan `Book[]`. Method: `addBook`, `findByIsbn(String)` (return `Book` atau `null`), `borrow(String isbn)`, `giveBack(String isbn)`, `printAvailableBooks()`. Pastikan `Book.equals` memakai `isbn`.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
class Book {
    private final String title;
    private final String author;
    private double price;
    private final int pages;

    public Book(String title, String author, double price, int pages) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.pages = pages;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }
    public int getPages() { return pages; }

    public void applyDiscount(double percent) {
        if (percent <= 0 || percent > 100) {
            return;   // ignore invalid discount
        }
        price = price - (price * percent / 100);
    }

    @Override
    public String toString() {
        return String.format("Book{title='%s', author='%s', price=%.2f, pages=%d}",
                title, author, price, pages);
    }
}

public class Main {
    public static void main(String[] args) {
        Book b1 = new Book("Effective Java", "Joshua Bloch", 500_000, 416);
        Book b2 = new Book("Clean Code", "Robert Martin", 450_000, 464);
        Book b3 = new Book("Java Basics", "Budi", 100_000, 200);

        b1.applyDiscount(10);

        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);
    }
}
```

`b1` setelah diskon 10%: `450000.00`. Field yang tidak berubah (`title`, `author`, `pages`) dijadikan `final`, dan hanya `price` yang punya perilaku mengubah nilai.

### Jawaban Soal 2

```java
class Rectangle {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive");
        }
        this.width = width;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        this.height = height;
    }

    public double area() {
        return width * height;
    }

    public double perimeter() {
        return 2 * (width + height);
    }

    public boolean isSquare() {
        return width == height;
    }
}

public class Main {
    public static void main(String[] args) {
        Rectangle r = new Rectangle(4, 5);
        System.out.println(r.area());        // 20.0
        System.out.println(r.perimeter());   // 18.0
        System.out.println(r.isSquare());    // false

        r.setWidth(5);
        System.out.println(r.isSquare());    // true
    }
}
```

Constructor memanggil setter supaya validasi **tidak terduplikasi**. Satu aturan, satu tempat.

### Jawaban Soal 3

```java
class BankAccount {
    private final String accountNumber;
    private final String owner;
    private double balance;

    public BankAccount(String accountNumber, String owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = Math.max(initialBalance, 0);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public double getBalance() { return balance; }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public boolean transferTo(BankAccount target, double amount) {
        if (target == null || target == this) {
            return false;
        }
        if (!withdraw(amount)) {
            return false;       // withdraw already validates amount and balance
        }
        target.deposit(amount);
        return true;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount a = new BankAccount("111", "Budi", 1000);
        BankAccount b = new BankAccount("222", "Sari", 500);

        System.out.println(a.transferTo(b, 300));    // true
        System.out.println(a.getBalance());          // 700.0
        System.out.println(b.getBalance());          // 800.0

        System.out.println(a.transferTo(b, 5000));   // false (insufficient)
        System.out.println(a.transferTo(a, 100));    // false (same account)
        System.out.println(a.transferTo(null, 100)); // false
    }
}
```

`transferTo` memakai ulang `withdraw` dan `deposit`, jadi aturan bisnis tidak ditulis dua kali. Perhatikan juga pengecekan `target == null`: tanpa itu, `target.deposit(...)` akan melempar `NullPointerException`.

### Jawaban Soal 4

```java
class Employee {
    private static int nextId = 1001;
    private static int totalEmployees = 0;

    private final int id;
    private final String name;

    public Employee(String name) {
        this.name = name;
        this.id = nextId++;
        totalEmployees++;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public static int getTotalEmployees() {
        return totalEmployees;
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "'}";
    }
}

public class Main {
    public static void main(String[] args) {
        Employee e1 = new Employee("Budi");
        Employee e2 = new Employee("Sari");
        Employee e3 = new Employee("Andi");

        System.out.println(e1);   // Employee{id=1001, name='Budi'}
        System.out.println(e2);   // Employee{id=1002, name='Sari'}
        System.out.println(e3);   // Employee{id=1003, name='Andi'}
        System.out.println(Employee.getTotalEmployees());   // 3
    }
}
```

`nextId++` memakai nilai saat ini, lalu menambahkannya. `id` bersifat `final` (sekali diisi, tidak berubah), sedangkan `nextId` bersifat `static` (satu untuk semua object). Di aplikasi nyata, ID biasanya dibuat oleh database, bukan counter static di memory (ini hilang setiap aplikasi restart dan tidak aman untuk multi-thread).

### Jawaban Soal 5

Output:

```
Sari
false
false
Sari
```

Lalu baris terakhir `System.out.println(p1.getName());` melempar **`NullPointerException`**, karena `p1` sudah di-set `null`.

Penjelasan:
- `p2 = p1`: keduanya menunjuk object yang sama, jadi `setName("Sari")` terlihat lewat `p1`.
- `p1 == p3`: dua object berbeda di memory, jadi `false`.
- `p1.equals(p3)`: `Person` tidak meng-override `equals`, jadi memakai versi `Object` (sama dengan `==`), hasilnya `false` walaupun nama sama.
- `p1 = null`: hanya memutus `p1`. Object-nya masih hidup karena `p2` masih menunjuk ke sana, makanya `p2.getName()` tetap `Sari`.

### Jawaban Soal 6

```java
import java.util.Objects;

final class Money {
    private final long amount;       // in cents
    private final String currency;

    public Money(long amount, String currency) {
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount + other.amount, currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount - other.amount, currency);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %d.%02d", currency, amount / 100, Math.abs(amount % 100));
    }
}

public class Main {
    public static void main(String[] args) {
        Money a = new Money(150_00, "IDR");
        Money b = new Money(50_00, "IDR");

        System.out.println(a.add(b));        // IDR 200.00
        System.out.println(a.subtract(b));   // IDR 100.00
        System.out.println(a);               // IDR 150.00  (unchanged)
        System.out.println(a.equals(new Money(150_00, "IDR")));   // true

        // a.add(new Money(1_00, "USD"));    // IllegalArgumentException: Currency mismatch
    }
}
```

Poin penting:
- Simpan uang sebagai **`long` dalam satuan sen**, bukan `double` (ingat jebakan `0.1 + 0.2`).
- `add` dan `subtract` **tidak mengubah** `this`. Keduanya membuat object baru.
- Menambah dua mata uang berbeda adalah kesalahan logika, jadi dilempar exception, bukan dibiarkan diam-diam.

### Jawaban Soal 7

```java
class Counter {
    private int value;

    public void increment() {
        value++;
    }

    public void decrement() {
        if (value > 0) {
            value--;
        }
    }

    public void reset() {
        value = 0;
    }

    public int getValue() {
        return value;
    }
}

public class Main {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        Counter c2 = new Counter();

        c1.increment();
        c1.increment();
        c1.increment();
        c2.increment();
        c2.decrement();
        c2.decrement();   // stays at 0

        System.out.println(c1.getValue());   // 3
        System.out.println(c2.getValue());   // 0

        c1.reset();
        System.out.println(c1.getValue());   // 0
    }
}
```

Karena `value` adalah field **instance** (bukan `static`), tiap `Counter` punya salinan sendiri, sehingga independen. Kalau `value` dijadikan `static`, `c1` dan `c2` akan berbagi satu nilai.

### Jawaban Soal 8

Masalah:

1. **Bug tersembunyi:** `username = username;` hanya menugaskan parameter ke dirinya sendiri. Field tidak pernah terisi (semuanya `null`/`0`) dan **tidak ada error**. Harus `this.username = username;`.
2. **Field `public`:** melanggar encapsulation. Siapa pun bisa mengubah `age = -50` atau membaca `password`.
3. **`password` disimpan dan dibuka sebagai plain text:** di aplikasi nyata password tidak boleh disimpan mentah (harus di-hash dengan BCrypt, dibahas di materi Spring Security).
4. Tidak ada validasi (`age` negatif, `username` kosong).
5. Tidak ada `toString`, dan kalau ditambahkan **jangan** menyertakan `password`.

Perbaikan:

```java
public class User {
    private final String username;
    private String password;
    private int age;

    public User(String username, String password, int age) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        this.username = username;
        this.password = password;
        setAge(age);
    }

    public String getUsername() { return username; }
    public int getAge() { return age; }

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
    }

    public boolean checkPassword(String input) {
        return password != null && password.equals(input);
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', age=" + age + "}";   // password omitted on purpose
    }
}
```

Perhatikan: tidak ada `getPassword()`. Pola `checkPassword` menjaga password tidak keluar dari object.

### Jawaban Soal 9

```java
class Course {
    private final String code;
    private final String title;

    public Course(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}

class Student {
    private static final int MAX_COURSES = 3;

    private final String name;
    private final Course[] courses = new Course[MAX_COURSES];
    private int courseCount = 0;

    public Student(String name) {
        this.name = name;
    }

    public boolean enroll(Course course) {
        if (course == null || courseCount >= MAX_COURSES) {
            return false;
        }
        for (int i = 0; i < courseCount; i++) {
            if (courses[i].getCode().equals(course.getCode())) {
                return false;   // already enrolled
            }
        }
        courses[courseCount++] = course;
        return true;
    }

    public void printCourses() {
        System.out.println(name + "'s courses:");
        if (courseCount == 0) {
            System.out.println("  (none)");
            return;
        }
        for (int i = 0; i < courseCount; i++) {
            System.out.println("  " + courses[i]);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Student s = new Student("Budi");
        Course java = new Course("CS101", "Java Basics");
        Course db = new Course("CS102", "Database");
        Course web = new Course("CS103", "Web Dev");
        Course ai = new Course("CS104", "AI");

        System.out.println(s.enroll(java));   // true
        System.out.println(s.enroll(java));   // false (duplicate)
        System.out.println(s.enroll(db));     // true
        System.out.println(s.enroll(web));    // true
        System.out.println(s.enroll(ai));     // false (full)

        s.printCourses();
    }
}
```

Variable `courseCount` melacak berapa slot yang terisi. Loop hanya sampai `courseCount`, bukan `courses.length` (sisanya masih `null`, dan memanggil `.getCode()` di sana akan `NullPointerException`).

### Jawaban Soal 10

```java
import java.util.Objects;

class Book {
    private final String isbn;
    private final String title;
    private boolean available = true;

    public Book(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isbn.equals(((Book) o).isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return title + " [" + isbn + "] " + (available ? "(available)" : "(borrowed)");
    }
}

class Library {
    private final Book[] books;
    private int count = 0;

    public Library(int capacity) {
        this.books = new Book[capacity];
    }

    public boolean addBook(Book book) {
        if (book == null || count >= books.length || findByIsbn(book.getIsbn()) != null) {
            return false;
        }
        books[count++] = book;
        return true;
    }

    public Book findByIsbn(String isbn) {
        for (int i = 0; i < count; i++) {
            if (books[i].getIsbn().equals(isbn)) {
                return books[i];
            }
        }
        return null;
    }

    public boolean borrow(String isbn) {
        Book book = findByIsbn(isbn);
        if (book == null || !book.isAvailable()) {
            return false;
        }
        book.setAvailable(false);
        return true;
    }

    public boolean giveBack(String isbn) {
        Book book = findByIsbn(isbn);
        if (book == null || book.isAvailable()) {
            return false;
        }
        book.setAvailable(true);
        return true;
    }

    public void printAvailableBooks() {
        System.out.println("Available books:");
        boolean any = false;
        for (int i = 0; i < count; i++) {
            if (books[i].isAvailable()) {
                System.out.println("  " + books[i]);
                any = true;
            }
        }
        if (!any) {
            System.out.println("  (none)");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Library library = new Library(10);
        library.addBook(new Book("111", "Effective Java"));
        library.addBook(new Book("222", "Clean Code"));
        System.out.println(library.addBook(new Book("111", "Duplicate")));   // false

        System.out.println(library.borrow("111"));    // true
        System.out.println(library.borrow("111"));    // false (already borrowed)
        System.out.println(library.borrow("999"));    // false (not found)

        library.printAvailableBooks();   // only Clean Code

        System.out.println(library.giveBack("111"));  // true
        library.printAvailableBooks();   // both
    }
}
```

Kunci: `findByIsbn` mengembalikan `null` kalau tidak ketemu, jadi **semua pemanggilnya wajib cek `null`**. Ini persis alasan kenapa Java modern punya `Optional` (dibahas nanti). Di Praktek ini, logika pencarian yang berulang sudah dipusatkan di satu method, dan itu adalah kebiasaan yang bagus.
