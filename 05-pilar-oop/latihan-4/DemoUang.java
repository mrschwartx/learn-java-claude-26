import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Latihan 4: equals + hashCode + toString yang benar, dan dampak bila hashCode dihapus
public class DemoUang {
    public static void main(String[] args) {
        // --- Versi benar ---
        Uang a = new Uang(50_000, "IDR");
        Uang b = new Uang(50_000, "IDR");     // object BERBEDA, isi SAMA
        Uang c = new Uang(50_000, "USD");     // mata uang beda

        System.out.println("== Uang (equals + hashCode) ==");
        System.out.println("a == b           : " + (a == b));
        System.out.println("a.equals(b)      : " + a.equals(b));
        System.out.println("a.equals(c)      : " + a.equals(c));
        System.out.println("a.hashCode()==b.hashCode() : " + (a.hashCode() == b.hashCode()));
        System.out.println("a.equals(null)   : " + a.equals(null));
        System.out.println("toString         : " + a);

        Set<Uang> set = new HashSet<>();
        set.add(a);
        set.add(b);
        set.add(c);
        System.out.println("HashSet berisi " + set.size() + " elemen (a dan b dianggap satu, c elemen terpisah)");
        System.out.println("set.contains(new Uang(50_000, \"IDR\")) : " + set.contains(new Uang(50_000, "IDR")));

        // --- Versi hashCode dihapus ---
        UangRusak ra = new UangRusak(50_000, "IDR");
        UangRusak rb = new UangRusak(50_000, "IDR");
        System.out.println();
        System.out.println("== UangRusak (equals saja, hashCode dihapus) ==");
        System.out.println("ra.equals(rb)    : " + ra.equals(rb) + "   <- equals bilang SAMA");
        System.out.println("hashCode sama?   : " + (ra.hashCode() == rb.hashCode()) + "  <- tapi hashCode berbeda (berbasis identitas)");

        Set<UangRusak> rusak = new HashSet<>();
        rusak.add(ra);
        rusak.add(rb);
        System.out.println("HashSet berisi " + rusak.size() + " elemen  <- seharusnya 1");
        System.out.println("rusak.contains(new UangRusak(50_000, \"IDR\")) : "
                + rusak.contains(new UangRusak(50_000, "IDR")) + "  <- seharusnya true");
    }
}

final class Uang {
    private final long jumlah;
    private final String mata;

    Uang(long jumlah, String mata) {
        this.jumlah = jumlah;
        this.mata = mata;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Uang u)) return false;          // juga menangani null
        return jumlah == u.jumlah && Objects.equals(mata, u.mata);
    }

    @Override public int hashCode() {
        return Objects.hash(jumlah, mata);                 // field yang SAMA dengan equals
    }

    @Override public String toString() {
        return mata + " " + jumlah;
    }
}

final class UangRusak {
    private final long jumlah;
    private final String mata;

    UangRusak(long jumlah, String mata) {
        this.jumlah = jumlah;
        this.mata = mata;
    }

    @Override public boolean equals(Object o) {
        return o instanceof UangRusak u && jumlah == u.jumlah && Objects.equals(mata, u.mata);
    }
    // hashCode() sengaja TIDAK di-override
}
