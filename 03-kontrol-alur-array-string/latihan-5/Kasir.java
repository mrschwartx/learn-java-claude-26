import java.util.Locale;

// Latihan 5 (bagian 2): struk kasir dengan printf, rata kiri untuk teks, rata kanan untuk angka
public class Kasir {
    // Locale Indonesia agar pemisah ribuan selalu titik (25.000), di mesin mana pun
    static final Locale ID = Locale.of("id", "ID");

    public static void main(String[] args) {
        String[] nama  = {"Kopi Susu", "Roti Bakar", "Air Mineral"};
        int[]    qty   = {2, 1, 3};
        int[]    harga = {25_000, 18_500, 5_000};

        String garis = "-".repeat(44);
        System.out.println(garis);
        System.out.println(String.format(ID, "%-14s %3s %11s %12s", "Item", "Qty", "Harga", "Subtotal"));
        System.out.println(garis);

        long total = 0;
        for (int i = 0; i < nama.length; i++) {
            long subtotal = (long) qty[i] * harga[i];
            total += subtotal;
            System.out.println(String.format(ID, "%-14s %3d %,11d %,12d", nama[i], qty[i], harga[i], subtotal));
        }

        long ppn = Math.round(total * 0.11);   // PPN 11%, pembulatan ke rupiah terdekat
        System.out.println(garis);
        System.out.println(String.format(ID, "%-30s %,13d", "Total", total));
        System.out.println(String.format(ID, "%-30s %,13d", "PPN 11%", ppn));
        System.out.println(String.format(ID, "%-30s %,13d", "Total Bayar", total + ppn));
        System.out.println(garis);
    }
}
