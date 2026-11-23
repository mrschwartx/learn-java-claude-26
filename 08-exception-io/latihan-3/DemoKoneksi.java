import java.util.Arrays;

// Latihan 3: urutan penutupan, suppressed exception, dan perbandingan dengan finally manual
public class DemoKoneksi {

    public static void main(String[] args) {
        skenario("1. Normal (tidak ada yang gagal)", false, false, false);
        skenario("2. Badan try gagal, close() B juga gagal", false, true, true);
        skenario("3. Badan try sukses, tetapi close() B gagal", false, true, false);
        skenario("4. Badan try gagal, close() A dan B sama-sama gagal", true, true, true);

        System.out.println();
        System.out.println("== 5. Constructor B gagal saat dibuka: A tetap ditutup ==");
        try (Koneksi a = new Koneksi("A", false, false);
             Koneksi b = new Koneksi("B", true, false)) {
            System.out.println("-- badan try (tidak tercapai) --");
        } catch (Exception e) {
            System.out.println("Ditangkap : " + e.getMessage());
        }

        System.out.println();
        System.out.println("== 6. Pembanding: finally manual MENIMPA exception utama ==");
        Koneksi k = null;
        try {
            try {
                k = new Koneksi("X", false, true);
                throw new IllegalStateException("kegagalan utama");
            } finally {
                k.close();                          // melempar exception, menggantikan 'kegagalan utama'
            }
        } catch (Exception e) {
            System.out.println("Ditangkap : " + e.getMessage() + "   <- penyebab sebenarnya HILANG");
            System.out.println("Suppressed: " + Arrays.toString(e.getSuppressed()));
        }
    }

    static void skenario(String judul, boolean aGagalTutup, boolean bGagalTutup, boolean badanGagal) {
        System.out.println();
        System.out.println("== " + judul + " ==");
        try (Koneksi a = new Koneksi("A", false, aGagalTutup);
             Koneksi b = new Koneksi("B", false, bGagalTutup)) {
            System.out.println("-- badan try --");
            if (badanGagal) throw new IllegalStateException("kegagalan utama");
        } catch (Exception e) {
            System.out.println("Ditangkap : " + e.getMessage());
            for (Throwable t : e.getSuppressed()) {
                System.out.println("Suppressed: " + t.getMessage());
            }
        }
    }
}

class Koneksi implements AutoCloseable {
    private final String nama;
    private final boolean gagalTutup;

    Koneksi(String nama, boolean gagalBuka, boolean gagalTutup) {
        if (gagalBuka) {
            System.out.println("buka " + nama + " GAGAL");
            throw new IllegalStateException("gagal membuka " + nama);
        }
        this.nama = nama;
        this.gagalTutup = gagalTutup;
        System.out.println("buka " + nama);
    }

    @Override
    public void close() {
        System.out.println("tutup " + nama);
        if (gagalTutup) throw new IllegalStateException("gagal menutup " + nama);
    }
}
