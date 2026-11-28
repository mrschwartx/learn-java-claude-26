import java.util.List;
import java.util.Locale;

// Latihan 1: hierarki Karyawan (abstract) + polymorphism lewat List<Karyawan>
public class DemoKaryawan {
    static final Locale ID = Locale.of("id", "ID");   // pemisah ribuan titik, di mesin mana pun

    public static void main(String[] args) {
        List<Karyawan> daftar = List.of(
                new KaryawanTetap("Andi", 8_000_000, 1_500_000),
                new KaryawanKontrak("Budi", 75_000, 120),
                new KaryawanTetap("Citra", 9_500_000, 2_000_000),
                new KaryawanKontrak("Dewi", 90_000, 80)
        );

        long total = 0;
        for (Karyawan k : daftar) {
            long gaji = k.hitungGaji();      // <-- POLYMORPHISM: variabel bertipe Karyawan,
                                             //     yang dijalankan adalah hitungGaji() milik tipe objek sebenarnya
            total += gaji;
            System.out.println(String.format(ID, "%-6s %-16s Rp%,12d", k.nama(), k.getClass().getSimpleName(), gaji));
        }
        System.out.println(String.format(ID, "%-23s Rp%,12d", "Total gaji", total));
    }
}

abstract class Karyawan {
    private final String nama;
    Karyawan(String nama) { this.nama = nama; }
    String nama() { return nama; }
    abstract long hitungGaji();               // kontrak: tiap jenis karyawan punya rumus sendiri
}

class KaryawanTetap extends Karyawan {
    private final long gajiPokok, tunjangan;
    KaryawanTetap(String nama, long gajiPokok, long tunjangan) {
        super(nama);
        this.gajiPokok = gajiPokok;
        this.tunjangan = tunjangan;
    }
    @Override long hitungGaji() { return gajiPokok + tunjangan; }
}

class KaryawanKontrak extends Karyawan {
    private final long tarifPerJam;
    private final int jamKerja;
    KaryawanKontrak(String nama, long tarifPerJam, int jamKerja) {
        super(nama);
        this.tarifPerJam = tarifPerJam;
        this.jamKerja = jamKerja;
    }
    @Override long hitungGaji() { return tarifPerJam * jamKerja; }
}
