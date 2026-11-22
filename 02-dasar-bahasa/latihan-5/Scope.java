// Latihan 5: scope variabel lokal, instance, static, plus shadowing
public class Scope {
    static int jumlahObjek = 0;       // STATIC : satu salinan milik class, hidup selama class dimuat
    int nilai = 10;                   // INSTANCE (field): satu salinan per object, hidup selama object hidup

    Scope() {
        jumlahObjek++;
    }

    void tampilkan() {
        int nilai = 99;               // LOCAL: menutupi (shadow) field bernama sama
        System.out.println("nilai       (lokal, membayangi field) = " + nilai);
        System.out.println("this.nilai  (field yang asli)         = " + this.nilai);
        System.out.println("Scope.jumlahObjek (static)            = " + Scope.jumlahObjek);
    }

    // Shadowing pada parameter: pola umum di constructor/setter
    void setNilai(int nilai) {        // parameter 'nilai' menutupi field 'nilai'
        // nilai = nilai;             // SALAH: menugaskan parameter ke dirinya sendiri, field tak berubah
        this.nilai = nilai;           // BENAR: field <- parameter
    }

    public static void main(String[] args) {
        Scope a = new Scope();
        Scope b = new Scope();

        a.tampilkan();

        System.out.println();
        a.setNilai(500);
        System.out.println("a.nilai = " + a.nilai + ", b.nilai = " + b.nilai + "   (instance: terpisah per object)");
        System.out.println("jumlahObjek = " + jumlahObjek + "                  (static: dipakai bersama)");

        // Scope blok
        System.out.println();
        int x = 10;
        if (x > 5) {
            int y = x * 2;            // y hanya hidup di dalam blok if ini
            System.out.println("di dalam if : y = " + y);
        }
        // System.out.println(y);     // ERROR kompilasi: cannot find symbol
        for (int i = 0; i < 2; i++) {
            // i hanya ada di dalam for
        }
        // System.out.println(i);     // ERROR kompilasi: cannot find symbol
        System.out.println("blok selesai: y dan i sudah tidak bisa diakses");
    }
}
