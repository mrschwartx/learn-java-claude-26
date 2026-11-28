// Latihan 3: dua interface dengan default method bernama sama; konflik diselesaikan dengan X.super.metode()
public class DemoBebek {
    public static void main(String[] args) {
        Bebek b = new Bebek();
        b.bergerak();

        System.out.println();
        // Lewat tipe interface, tetap method milik Bebek yang jalan (dynamic binding)
        Terbang t = b;
        t.bergerak();
        Berenang r = b;
        r.bergerak();
    }
}

interface Terbang {
    default void bergerak() { System.out.println("[Terbang] mengepakkan sayap"); }
}

interface Berenang {
    default void bergerak() { System.out.println("[Berenang] mengayuh kaki"); }
}

class Bebek implements Terbang, Berenang {
    // Tanpa method ini compiler menolak: dua default method bernama sama = ambigu (diamond problem)
    @Override
    public void bergerak() {
        System.out.println("Bebek bergerak:");
        Terbang.super.bergerak();      // pilih versi Terbang
        Berenang.super.bergerak();     // pilih versi Berenang
    }
}
