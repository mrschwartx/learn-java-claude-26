// Latihan 2: method static bernama sama di induk dan anak = method HIDING, bukan overriding
public class MethodHiding {
    public static void main(String[] args) {
        Hewan h = new Anjing();            // tipe variabel Hewan, tipe object Anjing

        System.out.println("-- method static (hiding) --");
        h.info();                          // static: dipilih dari tipe VARIABEL -> Hewan
        ((Anjing) h).info();               // setelah cast ke Anjing -> versi Anjing
        Hewan.info();
        Anjing.info();

        System.out.println();
        System.out.println("-- method instance (overriding), sebagai pembanding --");
        h.bersuara();                      // instance: dipilih dari tipe OBJECT -> Anjing
    }
}

class Hewan {
    static void info()   { System.out.println("info() milik Hewan"); }
    void bersuara()      { System.out.println("Hewan bersuara"); }
}

class Anjing extends Hewan {
    static void info()   { System.out.println("info() milik Anjing"); }      // menyembunyikan (hide) Hewan.info()
    @Override void bersuara() { System.out.println("Anjing: guk guk"); }    // meng-override
}
