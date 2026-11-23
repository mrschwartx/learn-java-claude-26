// Latihan 2: generic class Pasangan<A, B> dengan tukar(), dan generic method min dengan bounded type
public class DemoPasangan {

    static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static void main(String[] args) {
        Pasangan<String, Integer> p = new Pasangan<>("umur", 25);
        Pasangan<Integer, String> t = p.tukar();        // tipe berubah: <String,Integer> -> <Integer,String>
        System.out.println("p        = " + p);
        System.out.println("p.tukar()= " + t);
        System.out.println("tukar dua kali sama dengan asal? " + p.tukar().tukar().equals(p));

        int angka = t.pertama();                         // tanpa cast: compiler tahu bertipe Integer
        String teks = t.kedua();
        System.out.println("angka=" + angka + ", teks=" + teks);

        System.out.println();
        System.out.println("min(7, 3)            = " + min(7, 3));
        System.out.println("min(\"kiwi\", \"apel\")  = " + min("kiwi", "apel"));
        System.out.println("min(2.5, 2.4)        = " + min(2.5, 2.4));
        System.out.println("min(\"a\", \"a\")        = " + min("a", "a"));
    }
}

class Pasangan<A, B> {
    private final A pertama;
    private final B kedua;

    Pasangan(A pertama, B kedua) {
        this.pertama = pertama;
        this.kedua = kedua;
    }

    A pertama() { return pertama; }
    B kedua()   { return kedua; }

    Pasangan<B, A> tukar() {
        return new Pasangan<>(kedua, pertama);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Pasangan<?, ?> p
                && java.util.Objects.equals(pertama, p.pertama)
                && java.util.Objects.equals(kedua, p.kedua);
    }

    @Override public int hashCode() { return java.util.Objects.hash(pertama, kedua); }

    @Override public String toString() { return "(" + pertama + ", " + kedua + ")"; }
}
