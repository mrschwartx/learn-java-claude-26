// Dikompilasi dengan sengaja: 'Menunggu' ditambahkan ke permits, tetapi switch TIDAK diubah
public class HasilBertambah {
    static String tampilkan(Hasil h) {
        return switch (h) {
            case Sukses s -> "OK: " + s.data();
            case Gagal g  -> "GAGAL: " + g.pesan();
        };
    }
}

sealed interface Hasil permits Sukses, Gagal, Menunggu { }
record Sukses(String data) implements Hasil { }
record Gagal(String pesan) implements Hasil { }
record Menunggu(int antrian) implements Hasil { }
