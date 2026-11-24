import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Latihan 4: analisis Pegawai dengan stream
public class DemoPegawai {
    static final Locale ID = Locale.of("id", "ID");

    record Pegawai(String nama, String divisi, double gaji) { }

    static String rp(double x) { return String.format(ID, "Rp%,.2f", x); }

    public static void main(String[] args) {
        List<Pegawai> data = List.of(
                new Pegawai("Andi",   "Engineering", 12_000_000),
                new Pegawai("Budi",   "Engineering", 15_000_000),
                new Pegawai("Citra",  "Engineering", 18_000_000),
                new Pegawai("Dewi",   "Keuangan",     9_000_000),
                new Pegawai("Eko",    "Keuangan",    11_000_000),
                new Pegawai("Fajar",  "Pemasaran",    8_000_000),
                new Pegawai("Gita",   "Pemasaran",   10_000_000),
                new Pegawai("Hadi",   "Pemasaran",    9_500_000));

        // (a) gaji rata-rata per divisi
        Map<String, Double> rata = data.stream().collect(
                Collectors.groupingBy(Pegawai::divisi, TreeMap::new, Collectors.averagingDouble(Pegawai::gaji)));
        System.out.println("(a) Gaji rata-rata per divisi:");
        rata.forEach((d, g) -> System.out.println("    " + String.format("%-12s", d) + rp(g)));

        // (b) pegawai bergaji tertinggi di tiap divisi
        //     maxBy menghasilkan Optional (divisi kosong tidak mungkin ada di groupingBy, tetapi tipenya tetap Optional)
        Map<String, Pegawai> tertinggi = data.stream().collect(
                Collectors.groupingBy(Pegawai::divisi, TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Pegawai::gaji)),
                                opt -> opt.orElseThrow())));
        System.out.println("(b) Gaji tertinggi per divisi:");
        tertinggi.forEach((d, p) -> System.out.println("    " + String.format("%-12s", d) + p.nama() + " (" + rp(p.gaji()) + ")"));

        // (b) cara alternatif dengan toMap + BinaryOperator.maxBy: hasil harus sama
        Map<String, Pegawai> tertinggi2 = data.stream().collect(
                Collectors.toMap(Pegawai::divisi, p -> p,
                        java.util.function.BinaryOperator.maxBy(Comparator.comparingDouble(Pegawai::gaji)),
                        TreeMap::new));
        System.out.println("    cara toMap sama dengan groupingBy? " + tertinggi.equals(tertinggi2));

        // (c) nama pegawai bergaji di atas rata-rata KESELURUHAN, satu String dipisah koma
        //     Rata-rata harus diketahui lebih dulu -> dua langkah (dua pipeline); tidak bisa satu pass
        double rataSemua = data.stream().mapToDouble(Pegawai::gaji).average().orElse(0);
        String diAtas = data.stream()
                .filter(p -> p.gaji() > rataSemua)
                .map(Pegawai::nama)
                .collect(Collectors.joining(", "));
        System.out.println("(c) Rata-rata keseluruhan: " + rp(rataSemua));
        System.out.println("    Di atas rata-rata    : " + diAtas);

        // Verifikasi silang dengan loop biasa
        double total = 0;
        for (Pegawai p : data) total += p.gaji();
        StringBuilder sb = new StringBuilder();
        for (Pegawai p : data) {
            if (p.gaji() > total / data.size()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(p.nama());
            }
        }
        System.out.println("    cocok dengan versi loop? " + sb.toString().equals(diAtas));

        // Kasus tepi: tidak ada yang lolos -> joining menghasilkan "" (bukan null)
        String kosong = data.stream().filter(p -> p.gaji() > 1e12).map(Pegawai::nama).collect(Collectors.joining(", "));
        System.out.println("    tanpa pemenang -> \"" + kosong + "\" (string kosong)");
    }
}
