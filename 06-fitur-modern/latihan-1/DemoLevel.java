import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Latihan 1: enum Level dengan field dan method, plus EnumMap untuk menghitung kemunculan
public class DemoLevel {
    public static void main(String[] args) {
        // --- Perilaku enum ---
        System.out.println("WARN.lebihPentingDari(INFO)  : " + Level.WARN.lebihPentingDari(Level.INFO));
        System.out.println("DEBUG.lebihPentingDari(INFO) : " + Level.DEBUG.lebihPentingDari(Level.INFO));
        System.out.println("INFO.lebihPentingDari(INFO)  : " + Level.INFO.lebihPentingDari(Level.INFO));
        for (Level l : Level.values()) {
            System.out.println("  " + l + " ordinal=" + l.ordinal() + " bobot=" + l.bobot());
        }

        // --- List "acak" yang bisa diulang: Random dengan seed tetap ---
        Random acak = new Random(42);
        Level[] semua = Level.values();
        List<Level> log = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            log.add(semua[acak.nextInt(semua.length)]);
        }
        System.out.println();
        System.out.println("Log (20 entri): " + log);

        // --- EnumMap: hitung kemunculan ---
        Map<Level, Integer> hitung = new EnumMap<>(Level.class);
        for (Level l : log) {
            hitung.merge(l, 1, Integer::sum);       // tambah 1; mulai dari 1 bila belum ada
        }
        System.out.println("Hasil hitung  : " + hitung + "   (urut sesuai deklarasi enum)");

        // Level yang tidak muncul tidak ada di map; untuk menampilkan semua level, beri nilai awal 0
        Map<Level, Integer> lengkap = new EnumMap<>(Level.class);
        for (Level l : Level.values()) lengkap.put(l, 0);
        for (Level l : log) lengkap.merge(l, 1, Integer::sum);
        System.out.println("Dengan nol    : " + lengkap);

        int total = lengkap.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Jumlah        : " + total);

        // Bila sebagian level tidak muncul: tanpa nilai awal, level itu tidak ada di map
        List<Level> lima = log.subList(0, 5);
        Map<Level, Integer> sebagian = new EnumMap<>(Level.class);
        for (Level l : lima) sebagian.merge(l, 1, Integer::sum);
        System.out.println();
        System.out.println("5 entri pertama: " + lima);
        System.out.println("  tanpa nilai awal : " + sebagian + "   (ERROR tidak muncul)");
        System.out.println("  getOrDefault     : ERROR=" + sebagian.getOrDefault(Level.ERROR, 0));

        // Hanya log yang penting (>= WARN)
        long penting = log.stream().filter(l -> l.lebihPentingDari(Level.INFO)).count();
        System.out.println("Entri lebih penting dari INFO: " + penting);
    }
}

enum Level {
    DEBUG(10), INFO(20), WARN(30), ERROR(40);

    private final int bobot;
    Level(int bobot) { this.bobot = bobot; }
    int bobot() { return bobot; }

    // Lebih penting = bobot lebih besar. Perbandingan sama dengan lain = false.
    boolean lebihPentingDari(Level lain) {
        return this.bobot > lain.bobot;
    }
}
