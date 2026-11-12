# 0107 - OOP Lanjutan: Inheritance, Polymorphism, Abstract Class, Interface

## Tujuan Belajar

Setelah materi ini kamu bisa:
- Membuat hierarki class dengan `extends` dan `super`
- Membedakan overloading dan overriding
- Menjelaskan polymorphism (upcasting, dynamic dispatch) dan memakai `instanceof`
- Membuat `abstract class` dan `interface`, serta memilih di antara keduanya
- Memahami `final`, `protected`, dan class `Object`
- Memilih antara inheritance dan composition
- Menulis kode yang bergantung pada abstraksi (bukan implementasi konkret)

---

## 1. Teori

### 1.1 Inheritance (`extends`)

**Analogi:** anak mewarisi sifat orang tua, lalu menambah atau mengubah sesuatu. `Dog` **adalah** `Animal` (relasi **is-a**), jadi semua yang bisa dilakukan `Animal` juga bisa dilakukan `Dog`, plus kemampuan khusus anjing.

```java
class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void eat() {
        System.out.println(name + " is eating");
    }

    public void makeSound() {
        System.out.println(name + " makes a sound");
    }
}

class Dog extends Animal {

    public Dog(String name) {
        super(name);          // call the parent constructor
    }

    public void fetch() {
        System.out.println(name + " fetches the ball");
    }
}
```

```java
Dog dog = new Dog("Rex");
dog.eat();      // inherited from Animal
dog.fetch();    // defined in Dog
```

Istilah: `Animal` = **superclass / parent / base class**, `Dog` = **subclass / child / derived class**.

Aturan:
- Java hanya mendukung **single inheritance** untuk class: satu class hanya boleh `extends` satu parent.
- Constructor **tidak diwarisi**.
- Member `private` ada di object child tapi **tidak bisa diakses langsung** dari child (gunakan getter/`protected`).
- Semua class otomatis mewarisi `Object` (kalau tidak `extends` apa-apa).

### 1.2 `super` dan Constructor Chaining

Constructor child **selalu** memanggil constructor parent lebih dulu. Kalau tidak ditulis eksplisit, compiler menyisipkan `super();` otomatis.

```java
class Parent {
    Parent() { System.out.println("Parent()"); }
}

class Child extends Parent {
    Child() {
        // super();  <- inserted automatically
        System.out.println("Child()");
    }
}

new Child();
// Parent()
// Child()
```

Jebakan: kalau parent **tidak punya constructor tanpa parameter**, child **wajib** memanggil `super(...)` secara eksplisit, kalau tidak compile error.

```java
class Person {
    Person(String name) { }
}

class Employee extends Person {
    Employee() { }                // compile error: no Person() constructor exists
    Employee(String name) {
        super(name);              // OK
    }
}
```

`super(...)` harus **baris pertama** constructor (sama seperti `this(...)`, dan keduanya tidak bisa bersamaan).

| Pemakaian | Fungsi |
|---|---|
| `super(args)` | memanggil constructor parent |
| `super.method()` | memanggil versi parent dari method yang di-override |
| `super.field` | mengakses field parent (jarang dipakai) |

### 1.3 Method Overriding

Child **mengganti** perilaku method milik parent.

```java
class Dog extends Animal {
    public Dog(String name) { super(name); }

    @Override
    public void makeSound() {
        System.out.println(name + " says Woof!");
    }
}

class Cat extends Animal {
    public Cat(String name) { super(name); }

    @Override
    public void makeSound() {
        System.out.println(name + " says Meow!");
    }
}
```

Aturan overriding:
- Nama dan **parameter list sama persis** dengan method parent
- Return type sama atau **subtype-nya** (covariant return)
- Access modifier **sama atau lebih longgar** (tidak boleh lebih ketat). Contoh: parent `public` tidak boleh di-override jadi `protected`
- Method `final`, `static`, dan `private` **tidak bisa** di-override
- Selalu tulis `@Override` supaya compiler membantu mendeteksi salah ketik

Memanggil versi parent sambil menambah perilaku:

```java
@Override
public void eat() {
    super.eat();                           // keep the parent behavior
    System.out.println(name + " wags its tail");
}
```

**Overloading vs Overriding:**

| | Overloading | Overriding |
|---|---|---|
| Lokasi | satu class (atau antar parent-child) | parent dan child |
| Signature | **berbeda** (parameter) | **sama** |
| Ditentukan | saat **compile time** | saat **runtime** |
| Tujuan | variasi input | mengubah perilaku parent |

### 1.4 Polymorphism

**Polymorphism** = "banyak bentuk": satu reference bertipe parent bisa menunjuk ke berbagai jenis child, dan method yang dijalankan ditentukan oleh **object sebenarnya** saat runtime (**dynamic dispatch**).

```java
Animal a1 = new Dog("Rex");     // upcasting: child -> parent (implicit, safe)
Animal a2 = new Cat("Tom");

a1.makeSound();   // Rex says Woof!
a2.makeSound();   // Tom says Meow!
```

Kekuatan utamanya: kode yang menulis ke tipe parent tidak perlu tahu jenis child-nya.

```java
Animal[] zoo = { new Dog("Rex"), new Cat("Tom"), new Dog("Buddy") };
for (Animal animal : zoo) {
    animal.makeSound();   // each one behaves differently
}
```

Kalau besok ada `Bird`, loop di atas **tidak perlu diubah sama sekali**.

**Aturan akses:** yang boleh dipanggil ditentukan oleh **tipe reference** (compile time), sedangkan versi mana yang dijalankan ditentukan oleh **tipe object** (runtime).

```java
Animal a = new Dog("Rex");
a.makeSound();    // OK, runs Dog's version
// a.fetch();     // compile error: Animal has no fetch()
```

### 1.5 Downcasting dan `instanceof`

Untuk memanggil method khusus child, lakukan **downcasting** (parent → child) yang eksplisit dan berisiko.

```java
Animal a = new Cat("Tom");
Dog d = (Dog) a;      // compiles, but ClassCastException at runtime!
```

Selalu cek dengan `instanceof` terlebih dahulu. Java 16+ punya **pattern matching** yang lebih ringkas:

```java
// classic
if (a instanceof Dog) {
    Dog dog = (Dog) a;
    dog.fetch();
}

// modern (Java 16+)
if (a instanceof Dog dog) {
    dog.fetch();          // `dog` is already cast and in scope here
}
```

Kalau kamu sering butuh `instanceof` + cast, itu sering **tanda desain yang kurang baik**. Biasanya lebih bersih menaruh perilaku itu sebagai method di parent (lalu di-override).

### 1.6 Class `Object`

Semua class mewarisi `Object`, jadi semua object punya:

| Method | Fungsi |
|---|---|
| `toString()` | representasi teks (override!) |
| `equals(Object)` | perbandingan isi (override bersama `hashCode`) |
| `hashCode()` | angka hash untuk `HashMap`/`HashSet` |
| `getClass()` | informasi class saat runtime |

```java
Animal a = new Dog("Rex");
System.out.println(a.getClass().getSimpleName());   // Dog
```

### 1.7 `final` pada Inheritance

```java
final class Util { }                    // cannot be extended

class Base {
    public final void critical() { }    // cannot be overridden
}
```

### 1.8 Static Method Tidak Di-override (Hiding)

Method `static` **tidak polymorphic**. Jika child mendefinisikan static method dengan signature sama, itu disebut **hiding**, dan yang dipanggil ditentukan oleh **tipe reference**, bukan tipe object.

```java
class Parent {
    static void hello() { System.out.println("Parent static"); }
}
class Child extends Parent {
    static void hello() { System.out.println("Child static"); }
}

Parent p = new Child();
p.hello();     // Parent static  (resolved by reference type)
```

Field juga tidak polymorphic. Karena itu: **jangan** membuat field dengan nama sama di parent dan child.

### 1.9 Abstract Class

**Analogi:** `Shape` (bentuk) itu konsep umum. Tidak ada orang yang menggambar "sebuah shape" tanpa bentuk spesifik. Tapi semua shape pasti punya luas. `abstract class` merepresentasikan konsep setengah jadi seperti ini.

```java
abstract class Shape {
    private final String name;

    protected Shape(String name) {
        this.name = name;
    }

    // abstract method: no body, child MUST implement
    public abstract double area();

    // concrete method: shared behavior
    public String describe() {
        return String.format("%s with area %.2f", name, area());
    }
}

class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        super("Circle");
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        super("Rectangle");
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}
```

Aturan:
- **Tidak bisa** di-`new` langsung: `new Shape("x")` → compile error
- Boleh punya field, constructor, method biasa, **dan** method `abstract`
- Child yang konkret **wajib** meng-implementasi semua abstract method (kecuali child-nya juga `abstract`)
- Class dengan satu abstract method **harus** dideklarasikan `abstract`

Pola ini disebut **Template Method**: parent mengatur kerangka (`describe`), child mengisi bagian yang berbeda (`area`).

### 1.10 Interface

**Analogi:** interface itu **kontrak / colokan standar**. Colokan listrik tidak peduli apakah yang dicolok kulkas atau kipas, asalkan sesuai bentuk colokan. Interface mendefinisikan **apa yang bisa dilakukan**, tanpa peduli **bagaimana** dan **siapa** yang melakukannya.

```java
interface Payable {
    double calculatePay();        // implicitly public abstract
}

interface Reportable {
    String generateReport();
}
```

Class meng-implementasi interface dengan `implements` (boleh **banyak** interface):

```java
class FullTimeEmployee implements Payable, Reportable {
    private final double monthlySalary;

    public FullTimeEmployee(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double calculatePay() {
        return monthlySalary;
    }

    @Override
    public String generateReport() {
        return "Full-time, salary: " + monthlySalary;
    }
}
```

Isi interface (Java 8+):

```java
interface Notifier {
    String PREFIX = "[NOTIFY] ";                     // implicitly public static final

    void send(String message);                       // abstract

    default void sendTwice(String message) {         // default method: has a body
        send(message);
        send(message);
    }

    static String format(String message) {           // static utility
        return PREFIX + message;
    }
}
```

| Fitur | Keterangan |
|---|---|
| abstract method | tanpa body, otomatis `public abstract` |
| `default` method | punya body, bisa di-override, dipakai untuk menambah fitur tanpa memecahkan implementor lama |
| `static` method | utility milik interface, dipanggil `Notifier.format(...)` |
| `private` method (Java 9+) | helper internal untuk default/static method |
| constant | field otomatis `public static final` |
| Tidak boleh | field instance (state), constructor |

Interface juga bisa `extends` interface lain (bahkan banyak):

```java
interface Employee extends Payable, Reportable { }
```

### 1.11 Abstract Class vs Interface

| | Abstract class | Interface |
|---|---|---|
| Relasi | **is-a** (jenis dari) | **can-do** (bisa melakukan) |
| Jumlah yang bisa dipakai | satu (`extends`) | banyak (`implements`) |
| State (field instance) | Ya | Tidak (hanya constant) |
| Constructor | Ya | Tidak |
| Method punya body | Ya | `default`/`static`/`private` saja |
| Access modifier member | bebas | method publik |
| Cocok untuk | berbagi kode dan state di antara class yang berkerabat erat | mendefinisikan kemampuan/kontrak lintas class tak berkerabat |

Aturan praktis:
- Mulai dari **interface** (lebih fleksibel).
- Pakai **abstract class** kalau perlu berbagi **state atau implementasi** di antara class yang jelas satu keluarga.
- Keduanya boleh dipakai bersamaan: abstract class yang meng-implementasi interface.

### 1.12 Pemrograman ke Abstraksi (Program to an Interface)

Tulis kode yang bergantung pada **interface**, bukan class konkret. Ini fondasi dari Spring Boot (Dependency Injection).

```java
interface PaymentMethod {
    boolean pay(double amount);
}

class CreditCard implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("Paid " + amount + " with credit card");
        return true;
    }
}

class EWallet implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("Paid " + amount + " with e-wallet");
        return true;
    }
}

class Checkout {
    private final PaymentMethod paymentMethod;      // depends on the abstraction

    public Checkout(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void process(double total) {
        paymentMethod.pay(total);
    }
}

new Checkout(new CreditCard()).process(100_000);
new Checkout(new EWallet()).process(50_000);
```

`Checkout` tidak tahu dan tidak peduli jenis pembayarannya. Menambah `BankTransfer` tidak perlu mengubah `Checkout` sama sekali.

### 1.13 Inheritance vs Composition

| | Inheritance | Composition |
|---|---|---|
| Relasi | **is-a** | **has-a** |
| Keterikatan | sangat erat (child bergantung detail parent) | longgar |
| Fleksibilitas | tetap saat compile time | bisa diganti saat runtime |

Contoh keliru: `class Stack extends ArrayList` ("Stack adalah ArrayList"?). Bukan, Stack **memakai** list di dalamnya. Pakai composition.

Prinsip: **"Favor composition over inheritance."** Pakai inheritance hanya jika benar-benar relasi *is-a* dan child memang bisa dipakai di mana pun parent dipakai.

### 1.14 `sealed` (Java 17+, Pengenalan Singkat)

Membatasi class mana saja yang boleh jadi subclass.

```java
sealed interface Shape permits Circle, Square { }
final class Circle implements Shape { }
final class Square implements Shape { }
```

Berguna agar `switch` bisa dicek lengkap oleh compiler. Detailnya dibahas di materi lanjutan.

### 1.15 Urutan Eksekusi Saat `new Child()`

1. Memory dialokasikan, field diberi default value
2. Constructor child dipanggil, langsung menjalankan constructor parent (`super(...)`) **lebih dulu**
3. Field initializer dan body constructor parent dijalankan
4. Lalu field initializer dan body constructor child

Konsekuensi: **jangan memanggil method yang bisa di-override dari constructor**. Method override milik child bisa terpanggil sebelum field child terisi.

---

## 2. Praktek

> Semua contoh Praktek ditulis dalam satu file `Main.java`. Hanya `Main` yang `public`; class lain tanpa modifier supaya boleh berada di file yang sama.

### Praktek 2.1: Inheritance Dasar

```java
class Animal {
    protected String name;

    Animal(String name) {
        this.name = name;
    }

    void eat() {
        System.out.println(name + " is eating");
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);
    }

    void fetch() {
        System.out.println(name + " fetches the ball");
    }
}

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Rex");
        dog.eat();      // inherited
        dog.fetch();    // own method
    }
}
```

### Praktek 2.2: Overriding dan `super`

```java
class Animal {
    protected String name;

    Animal(String name) { this.name = name; }

    void makeSound() {
        System.out.println(name + " makes a sound");
    }

    void eat() {
        System.out.println(name + " is eating");
    }
}

class Dog extends Animal {
    Dog(String name) { super(name); }

    @Override
    void makeSound() {
        System.out.println(name + " says Woof!");
    }

    @Override
    void eat() {
        super.eat();                                   // reuse parent behavior
        System.out.println(name + " wags its tail");   // then add more
    }
}

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Rex");
        dog.makeSound();
        dog.eat();
    }
}
```

### Praktek 2.3: Polymorphism

```java
class Animal {
    protected String name;
    Animal(String name) { this.name = name; }
    void makeSound() { System.out.println(name + " makes a sound"); }
}

class Dog extends Animal {
    Dog(String name) { super(name); }
    @Override void makeSound() { System.out.println(name + " says Woof!"); }
}

class Cat extends Animal {
    Cat(String name) { super(name); }
    @Override void makeSound() { System.out.println(name + " says Meow!"); }
}

class Cow extends Animal {
    Cow(String name) { super(name); }
    @Override void makeSound() { System.out.println(name + " says Moo!"); }
}

public class Main {
    static void makeItSpeak(Animal animal) {   // accepts ANY Animal
        animal.makeSound();
    }

    public static void main(String[] args) {
        Animal[] zoo = { new Dog("Rex"), new Cat("Tom"), new Cow("Bessie"), new Animal("Generic") };

        for (Animal a : zoo) {
            makeItSpeak(a);
        }
    }
}
```

### Praktek 2.4: `instanceof` dan Downcasting

```java
class Animal { }
class Dog extends Animal { void fetch() { System.out.println("Fetching!"); } }
class Cat extends Animal { void climb() { System.out.println("Climbing!"); } }

public class Main {
    public static void main(String[] args) {
        Animal[] animals = { new Dog(), new Cat(), new Animal() };

        for (Animal a : animals) {
            if (a instanceof Dog dog) {
                dog.fetch();
            } else if (a instanceof Cat cat) {
                cat.climb();
            } else {
                System.out.println("Just an animal");
            }
        }

        // unsafe cast
        Animal a = new Cat();
        // Dog d = (Dog) a;    // ClassCastException at runtime
    }
}
```

### Praktek 2.5: Abstract Class

```java
abstract class Shape {
    private final String name;

    protected Shape(String name) { this.name = name; }

    public abstract double area();

    public String describe() {
        return String.format("%s with area %.2f", name, area());
    }
}

class Circle extends Shape {
    private final double radius;

    Circle(double radius) {
        super("Circle");
        this.radius = radius;
    }

    @Override
    public double area() { return Math.PI * radius * radius; }
}

class Rectangle extends Shape {
    private final double width;
    private final double height;

    Rectangle(double width, double height) {
        super("Rectangle");
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() { return width * height; }
}

public class Main {
    public static void main(String[] args) {
        // Shape s = new Shape("x");   // compile error: abstract

        Shape[] shapes = { new Circle(5), new Rectangle(4, 6) };
        for (Shape s : shapes) {
            System.out.println(s.describe());
        }
    }
}
```

### Praktek 2.6: Interface dan Multiple Implementation

```java
interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();

    default void dive() {
        System.out.println("Diving...");
    }
}

class Duck implements Flyable, Swimmable {
    @Override
    public void fly() { System.out.println("Duck flies"); }

    @Override
    public void swim() { System.out.println("Duck swims"); }
}

class Fish implements Swimmable {
    @Override
    public void swim() { System.out.println("Fish swims"); }
}

public class Main {
    public static void main(String[] args) {
        Duck duck = new Duck();
        duck.fly();
        duck.swim();
        duck.dive();       // default method

        Swimmable[] swimmers = { new Duck(), new Fish() };
        for (Swimmable s : swimmers) {
            s.swim();      // polymorphism through interface
        }
    }
}
```

### Praktek 2.7: Constructor Chaining

```java
class A {
    A() { System.out.println("A()"); }
}

class B extends A {
    B() {
        System.out.println("B()");
    }
}

class C extends B {
    C() {
        System.out.println("C()");
    }
}

public class Main {
    public static void main(String[] args) {
        new C();
    }
}
```

### Praktek 2.8: Program to an Interface

```java
interface PaymentMethod {
    boolean pay(double amount);
}

class CreditCard implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("Paid " + amount + " with credit card");
        return true;
    }
}

class EWallet implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("Paid " + amount + " with e-wallet");
        return true;
    }
}

class Checkout {
    private final PaymentMethod paymentMethod;

    Checkout(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    void process(double total) {
        paymentMethod.pay(total);
    }
}

public class Main {
    public static void main(String[] args) {
        new Checkout(new CreditCard()).process(100_000);
        new Checkout(new EWallet()).process(50_000);
    }
}
```

---

## 3. Latihan

**Soal 1.** Buat hierarki:
- `Vehicle` (field `brand`, `speed`; method `describe()` mengembalikan String)
- `Car extends Vehicle` (tambah `doors`)
- `Motorcycle extends Vehicle` (tambah `hasSidecar`)

Override `describe()` di tiap child dengan memanggil `super.describe()` lalu menambah info khusus. Buat array `Vehicle[]` berisi beberapa object dan cetak semua.

**Soal 2.** Prediksi output:

```java
class A {
    A() { System.out.println("A()"); }
    void hello() { System.out.println("A.hello"); }
}
class B extends A {
    B() { System.out.println("B()"); }
    @Override void hello() { System.out.println("B.hello"); }
}
class C extends B {
    C() { System.out.println("C()"); }
}

public class Quiz {
    public static void main(String[] args) {
        A obj = new C();
        obj.hello();
    }
}
```

**Soal 3.** Prediksi output dan jelaskan:

```java
class Parent {
    void greet() { System.out.println("Parent"); }
    static void hello() { System.out.println("Parent static"); }
}
class Child extends Parent {
    @Override void greet() { System.out.println("Child"); }
    static void hello() { System.out.println("Child static"); }
}

public class Quiz {
    public static void main(String[] args) {
        Parent p = new Child();
        p.greet();
        p.hello();
        Child c = new Child();
        c.hello();
    }
}
```

**Soal 4.** Payroll: buat `abstract class Employee` dengan `name` dan `abstract double calculateSalary()`. Turunkan:
- `FullTimeEmployee` (gaji bulanan tetap)
- `PartTimeEmployee` (`hourlyRate * hoursWorked`)
- `Intern` (uang saku tetap)

Buat array campuran, cetak gaji tiap orang, dan hitung **total payroll**.

**Soal 5.** Buat `abstract class Shape` dengan `area()` dan `perimeter()` abstract. Turunkan `Circle`, `Rectangle`, `Triangle` (sisi a, b, c; pakai rumus Heron). Dari array `Shape[]`, cari shape dengan **luas terbesar** dan cetak.

**Soal 6.** Buat interface `Notifier` dengan method `send(String message)`. Implementasikan `EmailNotifier`, `SmsNotifier`, `PushNotifier` (masing-masing cukup mencetak pesan dengan format berbeda). Buat class `AlertService` yang menerima `List`/array `Notifier` lewat constructor dan punya method `alertAll(String message)`. Buktikan menambah notifier baru **tidak mengubah** `AlertService`.

**Soal 7.** Kode berikut punya beberapa error compile. Temukan semuanya dan perbaiki:

```java
class Person {
    private String name;
    Person(String name) { this.name = name; }
}

class Employee extends Person {
    double salary;
    Employee(double salary) {
        this.salary = salary;
    }
    void work() { System.out.println("Working"); }
}

public class Main {
    public static void main(String[] args) {
        Person p = new Employee(5000);
        p.work();
        System.out.println(p.name);
    }
}
```

**Soal 8.** Buat interface `Discountable` dengan `double applyDiscount(double price)`, dan `default` method `String label()` yang mengembalikan `"Discount"`. Implementasikan `PercentageDiscount` (persentase) dan `FixedDiscount` (potongan tetap, hasil tidak boleh negatif). Buat method `static double checkout(double price, Discountable d)`.

**Soal 9 (Bonus).** Sistem pembayaran: buat interface `PaymentMethod` (`boolean pay(double amount)`, `String name()`), implementasi `CreditCard` (punya limit, gagal jika melebihi limit), `EWallet` (punya saldo, berkurang tiap bayar), `CashOnDelivery` (selalu berhasil). Buat `Checkout` yang menerima daftar `PaymentMethod` dan mencoba satu per satu sampai ada yang berhasil (**fallback**). Cetak metode mana yang akhirnya dipakai.

**Soal 10 (Bonus, Konseptual).** Jawab dengan kalimatmu sendiri:
- (a) Kapan memilih `abstract class`, kapan `interface`?
- (b) Kenapa `class Stack extends ArrayList` dianggap desain buruk?
- (c) Kenapa memanggil method yang bisa di-override dari constructor itu berbahaya? Beri contoh kode yang menunjukkan bug-nya.

---

## 4. Jawaban Latihan

### Jawaban Soal 1

```java
class Vehicle {
    protected final String brand;
    protected final int speed;

    Vehicle(String brand, int speed) {
        this.brand = brand;
        this.speed = speed;
    }

    public String describe() {
        return brand + " (" + speed + " km/h)";
    }
}

class Car extends Vehicle {
    private final int doors;

    Car(String brand, int speed, int doors) {
        super(brand, speed);
        this.doors = doors;
    }

    @Override
    public String describe() {
        return "Car: " + super.describe() + ", doors=" + doors;
    }
}

class Motorcycle extends Vehicle {
    private final boolean hasSidecar;

    Motorcycle(String brand, int speed, boolean hasSidecar) {
        super(brand, speed);
        this.hasSidecar = hasSidecar;
    }

    @Override
    public String describe() {
        return "Motorcycle: " + super.describe() + ", sidecar=" + hasSidecar;
    }
}

public class Main {
    public static void main(String[] args) {
        Vehicle[] vehicles = {
            new Car("Toyota", 180, 4),
            new Motorcycle("Honda", 160, false),
            new Car("Tesla", 250, 4)
        };

        for (Vehicle v : vehicles) {
            System.out.println(v.describe());
        }
    }
}
```

Loop memakai tipe `Vehicle` tetapi tiap object menjalankan `describe()` versinya sendiri. Itu polymorphism.

### Jawaban Soal 2

Output:

```
A()
B()
C()
B.hello
```

- `new C()` memicu rantai constructor dari **atas ke bawah**: `A()` → `B()` → `C()`.
- `obj` bertipe reference `A`, tapi object aslinya `C`. `C` tidak meng-override `hello()`, jadi diwarisi dari `B` (yang paling dekat), hasilnya `B.hello`. Dynamic dispatch mencari versi paling spesifik di hierarki object aslinya.

### Jawaban Soal 3

Output:

```
Child
Parent static
Child static
```

- `p.greet()`: method instance di-override, jadi ditentukan oleh **object aktual** (`Child`).
- `p.hello()`: method `static` tidak polymorphic (hanya **hiding**), ditentukan oleh **tipe reference** (`Parent`), makanya `Parent static`.
- `c.hello()`: reference bertipe `Child`, jadi `Child static`.

Pelajaran: jangan mengandalkan perilaku seperti ini. Memanggil static method lewat instance (`p.hello()`) itu membingungkan, selalu panggil lewat nama class (`Parent.hello()`).

### Jawaban Soal 4

```java
abstract class Employee {
    private final String name;

    protected Employee(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract double calculateSalary();
}

class FullTimeEmployee extends Employee {
    private final double monthlySalary;

    FullTimeEmployee(String name, double monthlySalary) {
        super(name);
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double calculateSalary() {
        return monthlySalary;
    }
}

class PartTimeEmployee extends Employee {
    private final double hourlyRate;
    private final int hoursWorked;

    PartTimeEmployee(String name, double hourlyRate, int hoursWorked) {
        super(name);
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }

    @Override
    public double calculateSalary() {
        return hourlyRate * hoursWorked;
    }
}

class Intern extends Employee {
    private final double allowance;

    Intern(String name, double allowance) {
        super(name);
        this.allowance = allowance;
    }

    @Override
    public double calculateSalary() {
        return allowance;
    }
}

public class Main {
    public static void main(String[] args) {
        Employee[] employees = {
            new FullTimeEmployee("Budi", 10_000_000),
            new PartTimeEmployee("Sari", 50_000, 80),
            new Intern("Andi", 2_000_000)
        };

        double total = 0;
        for (Employee e : employees) {
            double salary = e.calculateSalary();
            System.out.printf("%-6s : %,.2f%n", e.getName(), salary);
            total += salary;
        }
        System.out.printf("Total  : %,.2f%n", total);   // 16,000,000.00
    }
}
```

Format `%,.2f` menambahkan pemisah ribuan. Kalau ada jenis karyawan baru (misal `Contractor`), kode payroll di `main` tidak perlu diubah.

### Jawaban Soal 5

```java
abstract class Shape {
    public abstract double area();
    public abstract double perimeter();
}

class Circle extends Shape {
    private final double radius;

    Circle(double radius) { this.radius = radius; }

    @Override public double area() { return Math.PI * radius * radius; }
    @Override public double perimeter() { return 2 * Math.PI * radius; }
    @Override public String toString() { return "Circle(r=" + radius + ")"; }
}

class Rectangle extends Shape {
    private final double width;
    private final double height;

    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override public double area() { return width * height; }
    @Override public double perimeter() { return 2 * (width + height); }
    @Override public String toString() { return "Rectangle(" + width + "x" + height + ")"; }
}

class Triangle extends Shape {
    private final double a, b, c;

    Triangle(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double area() {
        double s = (a + b + c) / 2;                      // Heron's formula
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

    @Override public double perimeter() { return a + b + c; }
    @Override public String toString() { return "Triangle(" + a + "," + b + "," + c + ")"; }
}

public class Main {
    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle(3),
            new Rectangle(4, 6),
            new Triangle(3, 4, 5)
        };

        Shape largest = shapes[0];
        for (Shape s : shapes) {
            System.out.printf("%s -> area %.2f, perimeter %.2f%n", s, s.area(), s.perimeter());
            if (s.area() > largest.area()) {
                largest = s;
            }
        }
        System.out.println("Largest: " + largest);   // Circle(r=3.0)
    }
}
```

Luas: lingkaran `28.27`, persegi panjang `24.00`, segitiga `6.00`. Pemenangnya **Circle(r=3.0)**, karena `π × 9 ≈ 28.27` lebih besar dari `24`.

Perhatikan pola "cari terbesar": `largest` diinisialisasi dengan elemen pertama, lalu dibandingkan satu per satu. Itu sama dengan pola cari-max di array angka (materi `0105`), hanya saja yang dibandingkan adalah `area()` hasil polymorphism.

### Jawaban Soal 6

```java
interface Notifier {
    void send(String message);
}

class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("[EMAIL] " + message);
    }
}

class SmsNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("[SMS] " + message);
    }
}

class PushNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("[PUSH] " + message);
    }
}

class AlertService {
    private final Notifier[] notifiers;

    AlertService(Notifier... notifiers) {
        this.notifiers = notifiers;
    }

    public void alertAll(String message) {
        for (Notifier n : notifiers) {
            n.send(message);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        AlertService service = new AlertService(
            new EmailNotifier(), new SmsNotifier(), new PushNotifier());
        service.alertAll("Server is down!");

        // adding a new notifier: only a new class, AlertService is untouched
        Notifier slack = message -> System.out.println("[SLACK] " + message);
        new AlertService(slack).alertAll("Deploy finished");
    }
}
```

Interface dengan satu method abstract bisa ditulis sebagai **lambda** (`message -> ...`), dibahas di materi `0110`. `AlertService` hanya bergantung pada `Notifier`, tidak pada implementasi konkretnya. Prinsip ini disebut **Open/Closed Principle**: terbuka untuk ekstensi, tertutup untuk modifikasi.

### Jawaban Soal 7

Error yang ada:

1. `Employee(double salary)` tidak memanggil `super(...)`, padahal `Person` tidak punya constructor tanpa parameter → compile error (implicit `super()` tidak ditemukan).
2. `p.work()`: reference bertipe `Person`, `Person` tidak punya `work()`.
3. `p.name`: `name` bersifat `private` di `Person`, tidak bisa diakses dari luar class.

Perbaikan:

```java
class Person {
    private final String name;

    Person(String name) { this.name = name; }

    public String getName() { return name; }
}

class Employee extends Person {
    private final double salary;

    Employee(String name, double salary) {
        super(name);                 // fix 1
        this.salary = salary;
    }

    void work() { System.out.println(getName() + " is working"); }
}

public class Main {
    public static void main(String[] args) {
        Person p = new Employee("Budi", 5000);

        if (p instanceof Employee e) {   // fix 2: cast safely
            e.work();
        }
        System.out.println(p.getName()); // fix 3: use the getter
    }
}
```

Alternatif untuk fix 2: deklarasikan variable sebagai `Employee e = new Employee("Budi", 5000);` kalau memang butuh method `Employee`.

### Jawaban Soal 8

```java
interface Discountable {
    double applyDiscount(double price);

    default String label() {
        return "Discount";
    }
}

class PercentageDiscount implements Discountable {
    private final double percent;

    PercentageDiscount(double percent) {
        this.percent = percent;
    }

    @Override
    public double applyDiscount(double price) {
        return price - (price * percent / 100);
    }

    @Override
    public String label() {
        return percent + "% off";
    }
}

class FixedDiscount implements Discountable {
    private final double amount;

    FixedDiscount(double amount) {
        this.amount = amount;
    }

    @Override
    public double applyDiscount(double price) {
        return Math.max(price - amount, 0);   // never negative
    }
}

public class Main {
    static double checkout(double price, Discountable d) {
        double result = d.applyDiscount(price);
        System.out.printf("%s: %.2f -> %.2f%n", d.label(), price, result);
        return result;
    }

    public static void main(String[] args) {
        checkout(200_000, new PercentageDiscount(15));   // 15.0% off: 200000.00 -> 170000.00
        checkout(200_000, new FixedDiscount(50_000));    // Discount: 200000.00 -> 150000.00
        checkout(30_000, new FixedDiscount(50_000));     // Discount: 30000.00 -> 0.00
    }
}
```

`FixedDiscount` memakai `label()` default dari interface, sedangkan `PercentageDiscount` meng-override-nya. Method `checkout` menerima `Discountable` apa pun.

### Jawaban Soal 9

```java
interface PaymentMethod {
    boolean pay(double amount);
    String name();
}

class CreditCard implements PaymentMethod {
    private final double limit;

    CreditCard(double limit) { this.limit = limit; }

    @Override
    public boolean pay(double amount) {
        return amount <= limit;
    }

    @Override
    public String name() { return "Credit Card"; }
}

class EWallet implements PaymentMethod {
    private double balance;

    EWallet(double balance) { this.balance = balance; }

    @Override
    public boolean pay(double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    @Override
    public String name() { return "E-Wallet"; }
}

class CashOnDelivery implements PaymentMethod {
    @Override
    public boolean pay(double amount) { return true; }

    @Override
    public String name() { return "Cash on Delivery"; }
}

class Checkout {
    private final PaymentMethod[] methods;

    Checkout(PaymentMethod... methods) {
        this.methods = methods;
    }

    public PaymentMethod process(double amount) {
        for (PaymentMethod method : methods) {
            if (method.pay(amount)) {
                System.out.println("Paid " + amount + " using " + method.name());
                return method;
            }
            System.out.println(method.name() + " failed, trying next...");
        }
        System.out.println("All payment methods failed");
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        Checkout checkout = new Checkout(
            new CreditCard(100_000),
            new EWallet(300_000),
            new CashOnDelivery()
        );

        checkout.process(50_000);     // Credit Card
        checkout.process(250_000);    // Credit Card failed -> E-Wallet
        checkout.process(250_000);    // Credit Card failed, E-Wallet failed (balance 50,000) -> COD
    }
}
```

Perhatikan: `Checkout` sama sekali tidak tahu jenis konkret payment method. Itu persis pola yang dipakai Spring: kamu mendeklarasikan **interface**, framework yang menyuntikkan implementasinya.

### Jawaban Soal 10

**(a) Abstract class vs interface.**
Pakai **interface** untuk mendefinisikan **kemampuan** yang bisa dimiliki class apa pun (`Comparable`, `Payable`, `Notifier`), terutama kalau class-class itu tidak berkerabat. Pakai **abstract class** kalau class-class itu jelas satu keluarga (is-a) dan perlu **berbagi state atau implementasi** (field, constructor, method biasa). Kalau ragu, mulai dari interface karena class bisa mengimplementasi banyak interface tetapi hanya boleh `extends` satu class.

**(b) `class Stack extends ArrayList`.**
Relasi is-a-nya salah. Dengan `extends`, `Stack` ikut mewarisi **semua** method `ArrayList` (`add(index, x)`, `remove(index)`, `set`, ...), sehingga orang bisa menyisipkan atau menghapus elemen di tengah dan **merusak invarian LIFO** yang seharusnya dijaga stack. Selain itu `Stack` jadi terikat erat dengan detail `ArrayList`. Solusinya composition: `Stack` **memiliki** `ArrayList` privat dan hanya mengekspos `push`, `pop`, `peek`. (Catatan: `java.util.Stack` bawaan JDK justru melakukan kesalahan ini, makanya sekarang disarankan memakai `Deque`/`ArrayDeque`.)

**(c) Memanggil method override dari constructor.**
Constructor parent berjalan **sebelum** field child diinisialisasi. Kalau constructor parent memanggil method yang di-override child, versi child itu berjalan saat field child masih bernilai default (`null`/`0`).

```java
class Parent {
    Parent() {
        init();                       // calls the overridden version in Child
    }
    void init() { }
}

class Child extends Parent {
    private String name = "Budi";     // assigned AFTER Parent() finishes

    @Override
    void init() {
        System.out.println(name.length());   // NullPointerException: name is still null
    }
}

new Child();
```

Urutannya: `Parent()` jalan → `init()` (versi `Child`) jalan → `name` masih `null` → error. Aturan praktis: di constructor, hanya panggil method `private`, `final`, atau `static`.
