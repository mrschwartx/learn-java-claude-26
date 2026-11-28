// Latihan 5: constructor 6 parameter vs Builder
public class DemoMobil {
    public static void main(String[] args) {
        // --- Cara lama: 6 argumen berurutan ---
        MobilLama lama = new MobilLama("Toyota", "Avanza", "Putih", 2022, "Manual", 4);
        System.out.println("Benar    : " + lama);

        // Tertukar warna dan tipe? Compiler TIDAK mengeluh: semuanya String.
        MobilLama keliru = new MobilLama("Toyota", "Putih", "Avanza", 2022, "Manual", 4);
        System.out.println("Tertukar : " + keliru + "   <- lolos kompilasi, datanya salah");

        // --- Dengan Builder ---
        Mobil baru = new Mobil.Builder("Toyota", "Avanza")     // dua field wajib
                .warna("Putih")
                .tahun(2022)
                .transmisi("Manual")
                .jumlahPintu(4)
                .build();
        System.out.println("Builder  : " + baru);

        // Hanya yang wajib: sisanya memakai default
        System.out.println("Minimal  : " + new Mobil.Builder("Honda", "Brio").build());

        // Validasi tersentralisasi di build()
        try {
            new Mobil.Builder("Honda", "Brio").jumlahPintu(0).build();
        } catch (IllegalStateException e) {
            System.out.println("Ditolak  : " + e.getMessage());
        }
    }
}

// Pendekatan lama: constructor telescoping dengan 6 parameter
class MobilLama {
    private final String merek, tipe, warna, transmisi;
    private final int tahun, jumlahPintu;

    MobilLama(String merek, String tipe, String warna, int tahun, String transmisi, int jumlahPintu) {
        this.merek = merek;
        this.tipe = tipe;
        this.warna = warna;
        this.tahun = tahun;
        this.transmisi = transmisi;
        this.jumlahPintu = jumlahPintu;
    }

    @Override public String toString() {
        return "MobilLama{" + merek + " " + tipe + ", warna=" + warna + ", " + tahun + ", " + transmisi + ", " + jumlahPintu + " pintu}";
    }
}

// Dengan Builder: immutable, constructor private
class Mobil {
    private final String merek, tipe, warna, transmisi;
    private final int tahun, jumlahPintu;

    private Mobil(Builder b) {
        this.merek = b.merek;
        this.tipe = b.tipe;
        this.warna = b.warna;
        this.tahun = b.tahun;
        this.transmisi = b.transmisi;
        this.jumlahPintu = b.jumlahPintu;
    }

    @Override public String toString() {
        return "Mobil{" + merek + " " + tipe + ", warna=" + warna + ", " + tahun + ", " + transmisi + ", " + jumlahPintu + " pintu}";
    }

    static class Builder {
        private final String merek, tipe;               // wajib
        private String warna = "Putih", transmisi = "Manual";   // opsional dengan default
        private int tahun = 2024, jumlahPintu = 4;

        Builder(String merek, String tipe) {
            this.merek = merek;
            this.tipe = tipe;
        }
        Builder warna(String w)      { this.warna = w; return this; }
        Builder tahun(int t)         { this.tahun = t; return this; }
        Builder transmisi(String t)  { this.transmisi = t; return this; }
        Builder jumlahPintu(int j)   { this.jumlahPintu = j; return this; }

        Mobil build() {
            if (jumlahPintu < 2 || jumlahPintu > 5) {
                throw new IllegalStateException("jumlahPintu harus 2..5, bukan " + jumlahPintu);
            }
            return new Mobil(this);
        }
    }
}
