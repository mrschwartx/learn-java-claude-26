import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Latihan 5: exception chaining (LaporanException membungkus IOException), cause, dan batas penggunaan catch(Exception)
public class DemoLaporan {

    public static void main(String[] args) {
        System.out.println("== A. Chaining: IOException dibungkus LaporanException ==");
        try {
            Laporan.buat(Path.of("data-penjualan-tidak-ada.csv"));
        } catch (LaporanException e) {
            System.out.println("Pesan domain : " + e.getMessage());
            System.out.println("getCause()   : " + e.getCause());
            System.out.println("Rantai       : " + rantai(e));
            System.out.println("Stack trace (hanya frame milik program ini; frame JDK dan launcher dibuang agar stabil antar versi):");
            for (String baris : jejak(e)) System.out.println("  " + baris);
        }

        System.out.println();
        System.out.println("== B. Akibat menelan exception (catch kosong) ==");
        List<String> hasil = LaporanMenelan.buat(Path.of("data-penjualan-tidak-ada.csv"));
        System.out.println("Hasil laporan: " + hasil + "   <- tampak 'sukses', padahal file tidak pernah terbaca");
        System.out.println("Pemanggil mengira tidak ada penjualan; penyebab sebenarnya tidak meninggalkan jejak apa pun.");

        System.out.println();
        System.out.println("== C. Penanganan di BATAS terluar: catch (Exception) per tugas ==");
        String[] tugas = { "laporan-1", "laporan-rusak", "laporan-3" };
        int berhasil = 0;
        for (String nama : tugas) {
            try {
                proses(nama);
                berhasil++;
                System.out.println("OK    " + nama);
            } catch (Exception e) {                    // satu tugas gagal tidak boleh menghentikan yang lain
                System.out.println("GAGAL " + nama + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        System.out.println("Selesai: " + berhasil + " dari " + tugas.length + " tugas berhasil");
    }

    static void proses(String nama) {
        if (nama.contains("rusak")) throw new IllegalStateException("data " + nama + " tidak konsisten");
    }

    static String rantai(Throwable t) {
        List<String> bagian = new ArrayList<>();
        for (Throwable x = t; x != null; x = x.getCause()) {
            bagian.add(x.getClass().getSimpleName());
        }
        return String.join(" <- disebabkan oleh <- ", bagian);
    }

    // Ambil hasil printStackTrace(), buang frame JDK dan baris "... n more"
    static List<String> jejak(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        List<String> hasil = new ArrayList<>();
        for (String baris : sw.toString().split("\\R")) {
            String b = baris.strip();
            if (b.startsWith("at java.base/") || b.startsWith("at jdk.") || b.startsWith("... ")) continue;
            hasil.add(b);
        }
        return hasil;
    }
}

class LaporanException extends Exception {
    LaporanException(String pesan, Throwable penyebab) { super(pesan, penyebab); }   // penyebab diteruskan
}

class Laporan {
    static List<String> buat(Path sumber) throws LaporanException {
        try {
            return Files.readAllLines(sumber);
        } catch (IOException e) {
            throw new LaporanException("Gagal membuat laporan penjualan dari " + sumber, e);   // BUKAN: tanpa 'e'
        }
    }
}

// Versi BURUK: menelan exception
class LaporanMenelan {
    static List<String> buat(Path sumber) {
        try {
            return Files.readAllLines(sumber);
        } catch (Exception e) {
            // tidak ada apa-apa di sini
        }
        return new ArrayList<>();
    }
}
