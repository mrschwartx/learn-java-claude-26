import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Latihan 2: Mahasiswa (Modul 4) diubah menjadi record + compact constructor
public class DemoMahasiswa {
    public static void main(String[] args) {
        Mahasiswa a = new Mahasiswa("2301001", "Andi", 3.75);
        Mahasiswa b = new Mahasiswa("2301002", "Budi");        // constructor tambahan: ipk default 0.0
        Mahasiswa a2 = new Mahasiswa("2301001", "Andi", 3.75);

        // toString otomatis
        System.out.println(a);
        System.out.println(b);

        // accessor: nama komponen, tanpa awalan get
        System.out.println("a.nama() = " + a.nama() + ", a.ipk() = " + a.ipk());

        // equals / hashCode otomatis berdasarkan SEMUA komponen
        System.out.println("a.equals(a2)        : " + a.equals(a2));
        System.out.println("a == a2             : " + (a == a2));
        System.out.println("hashCode sama       : " + (a.hashCode() == a2.hashCode()));
        Set<Mahasiswa> set = new HashSet<>(Set.of(a));
        System.out.println("set.contains(a2)    : " + set.contains(a2));

        // validasi lewat compact constructor
        double[] ipkUji = { 4.0, 0.0, 4.01, -0.1, Double.NaN };
        for (double ipk : ipkUji) {
            try {
                new Mahasiswa("X", "Uji", ipk);
                System.out.println(String.format(Locale.US, "ipk %-5s -> diterima", ipk));
            } catch (IllegalArgumentException e) {
                System.out.println(String.format(Locale.US, "ipk %-5s -> ditolak: %s", ipk, e.getMessage()));
            }
        }

        // Normalisasi di compact constructor: nama dirapikan sebelum disimpan
        System.out.println(new Mahasiswa("2301009", "  Citra  ", 3.5));
    }
}

record Mahasiswa(String nim, String nama, double ipk) {

    // Compact constructor: tanpa daftar parameter dan tanpa 'this.x = x'.
    // Field diisi otomatis SETELAH blok ini selesai.
    Mahasiswa {
        if (!(ipk >= 0.0 && ipk <= 4.0)) {          // bentuk negatif ini juga menolak NaN
            throw new IllegalArgumentException("ipk harus 0..4, bukan " + ipk);
        }
        nama = nama.strip();                         // boleh mengubah parameter sebelum disimpan
    }

    // Constructor tambahan WAJIB mendelegasikan ke constructor kanonik lewat this(...)
    Mahasiswa(String nim, String nama) {
        this(nim, nama, 0.0);
    }
}
