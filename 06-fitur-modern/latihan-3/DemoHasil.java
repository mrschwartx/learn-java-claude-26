// Latihan 3: sealed interface Hasil + switch expression tanpa default
public class DemoHasil {
    static String tampilkan(Hasil h) {
        return switch (h) {
            case Sukses s -> "OK: " + s.data();
            case Gagal g  -> "GAGAL: " + g.pesan();
        };                                       // tanpa default: compiler memastikan semua subtipe tercakup
    }

    public static void main(String[] args) {
        System.out.println(tampilkan(new Sukses("12 baris dimuat")));
        System.out.println(tampilkan(new Gagal("koneksi terputus")));
    }
}

sealed interface Hasil permits Sukses, Gagal { }
record Sukses(String data) implements Hasil { }
record Gagal(String pesan) implements Hasil { }
