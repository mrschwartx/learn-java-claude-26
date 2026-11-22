// Latihan 4: bukti bahwa Java selalu pass-by-value
public class PassByValue {

    // GAGAL menukar: a dan b adalah SALINAN nilai int
    static void tukar(int a, int b) {
        int sementara = a;
        a = b;
        b = sementara;
        System.out.println("  di dalam tukar: a=" + a + ", b=" + b);
    }

    // BERHASIL mengubah isi: parameter menyalin REFERENSI, tetapi merujuk object yang sama
    static void tambahkan(StringBuilder sb) {
        sb.append(" Dunia");
    }

    // GAGAL mengganti object milik pemanggil: hanya mengganti salinan referensi
    static void ganti(StringBuilder sb) {
        sb = new StringBuilder("Baru");
        System.out.println("  di dalam ganti: sb=" + sb);
    }

    // GAGAL menukar dua referensi (swap versi object)
    static void tukarReferensi(StringBuilder x, StringBuilder y) {
        StringBuilder sementara = x;
        x = y;
        y = sementara;
    }

    public static void main(String[] args) {
        int p = 1, q = 2;
        System.out.println("Sebelum tukar: p=" + p + ", q=" + q);
        tukar(p, q);
        System.out.println("Setelah tukar: p=" + p + ", q=" + q + "   <- tidak tertukar");

        System.out.println();
        StringBuilder sb = new StringBuilder("Halo");
        tambahkan(sb);
        System.out.println("Setelah tambahkan: " + sb + "   <- isi object berubah");

        ganti(sb);
        System.out.println("Setelah ganti    : " + sb + "   <- tidak berubah");

        System.out.println();
        StringBuilder x = new StringBuilder("X");
        StringBuilder y = new StringBuilder("Y");
        tukarReferensi(x, y);
        System.out.println("Setelah tukarReferensi: x=" + x + ", y=" + y + "   <- tidak tertukar");

        // Bukti bahwa referensi 'sb' dan parameter menunjuk object yang sama (hashCode identik)
        System.out.println();
        StringBuilder asal = new StringBuilder("A");
        cetakIdentitas(asal, System.identityHashCode(asal));
    }

    static void cetakIdentitas(StringBuilder param, int identitasAsal) {
        System.out.println("Object sama dengan milik pemanggil? " + (System.identityHashCode(param) == identitasAsal));
    }
}
