import java.math.BigDecimal;

// Latihan 4: mengapa double tidak aman untuk uang, dan perbaikannya
public class Uang {
    public static void main(String[] args) {
        // --- double: jumlahkan 0.1 sebanyak 10 kali ---
        double total = 0.0;
        for (int i = 0; i < 10; i++) {
            total += 0.1;
        }
        System.out.println("[double] 0.1 x 10      = " + total);
        System.out.println("[double] total == 1.0 ? " + (total == 1.0));

        // --- BigDecimal (konstruktor String) ---
        BigDecimal totalBd = BigDecimal.ZERO;
        BigDecimal sepuluhSen = new BigDecimal("0.1");
        for (int i = 0; i < 10; i++) {
            totalBd = totalBd.add(sepuluhSen);
        }
        System.out.println("[BigDecimal] 0.1 x 10  = " + totalBd);
        System.out.println("[BigDecimal] sama dengan 1.0 ? " + (totalBd.compareTo(new BigDecimal("1.0")) == 0));

        // --- Jebakan konstruktor double ---
        System.out.println();
        System.out.println("new BigDecimal(0.1)   = " + new BigDecimal(0.1));
        System.out.println("new BigDecimal(\"0.1\") = " + new BigDecimal("0.1"));
        System.out.println("BigDecimal.valueOf(0.1) = " + BigDecimal.valueOf(0.1) + "   (alternatif aman bila sumbernya double)");

        // --- Contoh nyata: 1000 transaksi Rp 0,10 ---
        double saldoDouble = 0;
        BigDecimal saldoBd = BigDecimal.ZERO;
        for (int i = 0; i < 1000; i++) {
            saldoDouble += 0.1;
            saldoBd = saldoBd.add(new BigDecimal("0.1"));
        }
        System.out.println();
        System.out.println("1000 x 0.1 pakai double     = " + saldoDouble);
        System.out.println("1000 x 0.1 pakai BigDecimal = " + saldoBd);

        // equals vs compareTo pada BigDecimal: skala ikut diperhitungkan
        System.out.println();
        System.out.println("new BigDecimal(\"1.0\").equals(new BigDecimal(\"1.00\"))    = "
                + new BigDecimal("1.0").equals(new BigDecimal("1.00")));
        System.out.println("new BigDecimal(\"1.0\").compareTo(new BigDecimal(\"1.00\")) = "
                + new BigDecimal("1.0").compareTo(new BigDecimal("1.00")));
    }
}
