import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// Latihan 2: higher-order function petakan dan cariPertama
public class DemoHof {

    static <T, R> List<R> petakan(List<T> data, Function<T, R> fn) {
        List<R> hasil = new ArrayList<>(data.size());
        for (T x : data) hasil.add(fn.apply(x));
        return hasil;
    }

    static <T> T cariPertama(List<T> data, Predicate<T> syarat, T cadangan) {
        for (T x : data) {
            if (syarat.test(x)) return x;
        }
        return cadangan;
    }

    // Varian: cadangan dihitung MALAS, hanya bila memang diperlukan
    static <T> T cariPertamaMalas(List<T> data, Predicate<T> syarat, Supplier<T> cadangan) {
        for (T x : data) {
            if (syarat.test(x)) return x;
        }
        return cadangan.get();
    }

    record Produk(String nama, int harga) { }

    static int hitungMahal() {
        System.out.println("    (menghitung cadangan yang mahal...)");
        return -1;
    }

    public static void main(String[] args) {
        List<String> buah = List.of("apel", "kiwi", "mangga", "pir");
        List<Integer> angka = List.of(3, 8, 15, 22, 7);

        System.out.println("== petakan: satu fungsi, banyak perilaku ==");
        System.out.println("panjang      : " + petakan(buah, String::length));
        System.out.println("huruf besar  : " + petakan(buah, String::toUpperCase));
        System.out.println("kuadrat      : " + petakan(angka, x -> x * x));
        System.out.println("jadi Produk  : " + petakan(buah, nama -> new Produk(nama, nama.length() * 1000)));
        System.out.println("tipe berubah : " + petakan(angka, x -> x % 2 == 0 ? "genap" : "ganjil"));
        System.out.println("list kosong  : " + petakan(List.<String>of(), String::length));

        System.out.println();
        System.out.println("== cariPertama ==");
        System.out.println("genap pertama     : " + cariPertama(angka, x -> x % 2 == 0, -1));
        System.out.println("> 20 pertama      : " + cariPertama(angka, x -> x > 20, -1));
        System.out.println("> 100 (tak ada)   : " + cariPertama(angka, x -> x > 100, -1));
        System.out.println("berawalan 'm'     : " + cariPertama(buah, s -> s.startsWith("m"), "(tidak ada)"));
        System.out.println("berawalan 'z'     : " + cariPertama(buah, s -> s.startsWith("z"), "(tidak ada)"));
        System.out.println("list kosong       : " + cariPertama(List.<Integer>of(), x -> true, 0));

        System.out.println();
        System.out.println("== Cadangan eager vs malas (Supplier) ==");
        System.out.println("Eager, tetapi elemen ditemukan:");
        int e = cariPertama(angka, x -> x > 20, hitungMahal());         // hitungMahal() SELALU dipanggil lebih dulu
        System.out.println("  hasil = " + e);
        System.out.println("Malas, elemen ditemukan:");
        int m1 = cariPertamaMalas(angka, x -> x > 20, DemoHof::hitungMahal);
        System.out.println("  hasil = " + m1);
        System.out.println("Malas, elemen tidak ditemukan:");
        int m2 = cariPertamaMalas(angka, x -> x > 100, DemoHof::hitungMahal);
        System.out.println("  hasil = " + m2);
    }
}
