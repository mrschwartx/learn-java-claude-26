import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

/** Load test mini (PENGGANTI JMeter): N thread x M request POST /api/produk; ukur p50/p95/p99, throughput, error%. Angka BERVARIASI. */
public class BebanTest {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        int[] threads = args.length > 0 ? Arrays.stream(args[0].split(",")).mapToInt(Integer::parseInt).toArray() : new int[] { 1, 5, 10, 20 };
        int perThread = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        DataSource ds = Dukungan.ds(); Dukungan.resetSkema(ds);
        ProdukApp app = new ProdukApp(ds).mulai();
        try {
            for (int i = 0; i < 50; i++) Dukungan.post(app.url() + "/api/produk", "{\"nama\":\"pemanasan\",\"harga\":1}");   // warm-up (JIT, koneksi)
            System.out.println("| Thread | Request | Throughput (req/s) | rata-rata (ms) | p50 (ms) | p95 (ms) | p99 (ms) | maks (ms) | Error % |");
            System.out.println("|---:|---:|---:|---:|---:|---:|---:|---:|---:|");
            for (int t : threads) {
                AtomicInteger galat = new AtomicInteger();
                ExecutorService pool = Executors.newFixedThreadPool(t);
                List<Future<long[]>> fs = new ArrayList<>();
                long mulai = System.nanoTime();
                for (int i = 0; i < t; i++) {
                    final int id = i;
                    fs.add(pool.submit(() -> {
                        long[] lat = new long[perThread];
                        for (int k = 0; k < perThread; k++) {
                            long s = System.nanoTime();
                            try {
                                var r = Dukungan.post(app.url() + "/api/produk", "{\"nama\":\"beban-" + id + "-" + k + "\",\"harga\":" + (1000 + k) + "}");
                                if (r.statusCode() != 201) galat.incrementAndGet();
                            } catch (Exception e) { galat.incrementAndGet(); }
                            lat[k] = System.nanoTime() - s;
                        }
                        return lat;
                    }));
                }
                long[] semua = new long[t * perThread]; int n = 0;
                for (var f : fs) for (long v : f.get()) semua[n++] = v;
                double detik = (System.nanoTime() - mulai) / 1e9; pool.shutdown();
                Arrays.sort(semua);
                double rata = Arrays.stream(semua).average().orElse(0) / 1e6;
                System.out.println(String.format("| %d | %d | %.0f | %.1f | %.1f | %.1f | %.1f | %.1f | %.1f |", t, semua.length, semua.length / detik, rata, pct(semua, 50), pct(semua, 95), pct(semua, 99), semua[semua.length - 1] / 1e6, 100.0 * galat.get() / semua.length));
            }
        } finally { app.henti(); }
    }
    static double pct(long[] terurut, int p) { int i = (int) Math.ceil(p / 100.0 * terurut.length) - 1; return terurut[Math.max(0, i)] / 1e6; }
}
