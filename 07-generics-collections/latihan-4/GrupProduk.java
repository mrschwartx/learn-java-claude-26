import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

// Latihan 4: mengelompokkan Produk per kategori tanpa stream (computeIfAbsent), urut harga menurun
public class GrupProduk {
    static final Locale ID = Locale.of("id", "ID");

    record Produk(String nama, String kategori, int harga) { }

    public static void main(String[] args) {
        List<Produk> semua = List.of(
                new Produk("Kopi Arabika 250g", "Minuman", 85_000),
                new Produk("Teh Hijau", "Minuman", 32_000),
                new Produk("Roti Tawar", "Makanan", 18_500),
                new Produk("Mie Instan", "Makanan", 3_500),
                new Produk("Keripik Singkong", "Makanan", 15_000),
                new Produk("Sabun Mandi", "Kebersihan", 7_500),
                new Produk("Sampo 170ml", "Kebersihan", 24_000),
                new Produk("Air Mineral 1,5L", "Minuman", 6_000)
        );

        // TreeMap: kategori tampil terurut A-Z (HashMap tidak menjamin urutan)
        Map<String, List<Produk>> perKategori = new TreeMap<>();
        for (Produk p : semua) {
            // computeIfAbsent: buat list kosong bila kategori belum ada, lalu kembalikan list-nya
            perKategori.computeIfAbsent(p.kategori(), k -> new ArrayList<>()).add(p);
        }

        // Urutkan tiap grup menurut harga MENURUN
        Comparator<Produk> hargaMenurun = Comparator.comparingInt(Produk::harga).reversed();
        for (List<Produk> grup : perKategori.values()) {
            grup.sort(hargaMenurun);
        }

        for (Map.Entry<String, List<Produk>> e : perKategori.entrySet()) {
            System.out.println(e.getKey() + " (" + e.getValue().size() + " produk)");
            for (Produk p : e.getValue()) {
                System.out.println(String.format(ID, "  %-20s Rp%,8d", p.nama(), p.harga()));
            }
        }

        // Verifikasi: tidak ada produk yang hilang atau ganda
        int total = perKategori.values().stream().mapToInt(List::size).sum();
        System.out.println();
        System.out.println("Total di semua grup = " + total + " (dari " + semua.size() + " produk)");

        // Pola lama tanpa computeIfAbsent, sebagai pembanding keterbacaan
        Map<String, List<Produk>> cara2 = new TreeMap<>();
        for (Produk p : semua) {
            List<Produk> grup = cara2.get(p.kategori());
            if (grup == null) {
                grup = new ArrayList<>();
                cara2.put(p.kategori(), grup);
            }
            grup.add(p);
        }
        System.out.println("Kunci cara computeIfAbsent = " + perKategori.keySet());
        System.out.println("Kunci cara get/null/put    = " + cara2.keySet());
    }
}
