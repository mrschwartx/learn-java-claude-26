import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Latihan 3: hitung bilangan prima < 5.000.000 dengan ExecutorService + Future
public class Prima {
    static final int BATAS = 5_000_000;

    static boolean prima(int n) {
        if (n < 2) return false;
        if (n < 4) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i += 6) {            // uji 6k-1 dan 6k+1
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    /** Hitung prima pada rentang [dari, sampai). */
    static int hitung(int dari, int sampai) {
        int c = 0;
        for (int n = dari; n < sampai; n++) if (prima(n)) c++;
        return c;
    }

    /** Pecah [0, BATAS) menjadi 'potong' bagian sama lebar, jalankan di pool, jumlahkan. Mengembalikan {total, ms}. */
    static long[] paralel(int potong, int threadPool, long[] waktuPerBagian) throws Exception {
        long t0 = System.nanoTime();
        long total = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(threadPool)) {
            List<Future<Integer>> hasil = new ArrayList<>();
            int lebar = BATAS / potong;
            for (int i = 0; i < potong; i++) {
                final int dari = i * lebar;
                final int sampai = (i == potong - 1) ? BATAS : dari + lebar;
                final int idx = i;
                Callable<Integer> tugas = () -> {
                    long s = System.nanoTime();
                    int c = hitung(dari, sampai);
                    if (waktuPerBagian != null) waktuPerBagian[idx] = (System.nanoTime() - s) / 1_000_000;
                    return c;
                };
                hasil.add(pool.submit(tugas));
            }
            for (Future<Integer> f : hasil) total += f.get();
        }
        return new long[] { total, (System.nanoTime() - t0) / 1_000_000 };
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Bilangan prima di bawah " + String.format(java.util.Locale.US, "%,d", BATAS));
        System.out.println("[BERVARIASI] CPU tersedia: " + Runtime.getRuntime().availableProcessors());

        // Pemanasan JIT
        hitung(0, 500_000);

        // Satu thread
        long t0 = System.nanoTime();
        int satu = hitung(0, BATAS);
        long msSatu = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("Satu thread            : " + satu + " prima");

        // 4 bagian sama lebar, pool 4 thread
        long[] waktuBagian = new long[4];
        long[] empat = paralel(4, 4, waktuBagian);
        System.out.println("4 bagian, pool 4 thread: " + empat[0] + " prima");

        // Pembanding: banyak potongan kecil agar beban lebih merata
        long[] banyak = paralel(40, 4, null);
        System.out.println("40 potongan, pool 4    : " + banyak[0] + " prima");

        System.out.println("Ketiganya sama? " + (satu == empat[0] && empat[0] == banyak[0]));

        // Hitungan per bagian (deterministik) dan waktunya (bervariasi): bagian akhir lebih berat
        System.out.println();
        System.out.println("Jumlah prima per bagian (4 bagian sama lebar):");
        for (int i = 0; i < 4; i++) {
            int dari = i * (BATAS / 4), sampai = (i + 1) * (BATAS / 4);
            System.out.println(String.format("  [%,9d .. %,9d) : %,d prima", dari, sampai, hitung(dari, sampai)));
        }

        System.out.println();
        System.out.println("[BERVARIASI] waktu per bagian (ms): " + java.util.Arrays.toString(waktuBagian));
        System.out.println("[BERVARIASI] satu thread  : " + msSatu + " ms");
        System.out.println("[BERVARIASI] 4 bagian     : " + empat[1] + " ms  (speed-up " + String.format(java.util.Locale.US, "%.2f", (double) msSatu / empat[1]) + "x)");
        System.out.println("[BERVARIASI] 40 potongan  : " + banyak[1] + " ms  (speed-up " + String.format(java.util.Locale.US, "%.2f", (double) msSatu / banyak[1]) + "x)");
    }
}
