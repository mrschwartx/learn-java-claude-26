// Versi setelah 'Menunggu' ditambahkan DAN switch diperbarui: kembali terkompilasi
public class DemoHasilMenunggu {
    static String tampilkan(Hasil h) {
        return switch (h) {
            case Sukses s   -> "OK: " + s.data();
            case Gagal g    -> "GAGAL: " + g.pesan();
            case Menunggu m -> "MENUNGGU: antrian ke-" + m.antrian();
        };
    }

    public static void main(String[] args) {
        System.out.println(tampilkan(new Sukses("selesai")));
        System.out.println(tampilkan(new Gagal("timeout")));
        System.out.println(tampilkan(new Menunggu(3)));
    }
}

sealed interface Hasil permits Sukses, Gagal, Menunggu { }
record Sukses(String data) implements Hasil { }
record Gagal(String pesan) implements Hasil { }
record Menunggu(int antrian) implements Hasil { }
