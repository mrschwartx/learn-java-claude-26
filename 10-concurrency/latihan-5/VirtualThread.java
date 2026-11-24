import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// Latihan 5a: 10.000 tugas yang masing-masing tidur 1 detik (meniru I/O): pool 200 thread vs virtual thread
public class VirtualThread {
    static final int TUGAS = 10_000;

    static long jalankan(ExecutorService pool, AtomicInteger selesai) {
        long t0 = System.nanoTime();
        try (pool) {                                        // close() menunggu semua tugas selesai
            for (int i = 0; i < TUGAS; i++) {
                pool.submit(() -> {
                    try { Thread.sleep(1_000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    selesai.incrementAndGet();
                });
            }
        }
        return (System.nanoTime() - t0) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger a = new AtomicInteger(), b = new AtomicInteger();
        long msPool = jalankan(Executors.newFixedThreadPool(200), a);
        long msVirtual = jalankan(Executors.newVirtualThreadPerTaskExecutor(), b);

        System.out.println("Tugas selesai (pool 200 thread)   : " + a.get() + " dari " + TUGAS);
        System.out.println("Tugas selesai (virtual thread)    : " + b.get() + " dari " + TUGAS);
        System.out.println("Perkiraan teori pool : 10.000 / 200 = 50 gelombang x 1 detik = sekitar 50 detik");
        System.out.println("Perkiraan teori virtual: semua tidur bersamaan = sekitar 1 detik");
        System.out.println();
        System.out.println("[BERVARIASI] pool 200 thread : " + msPool + " ms");
        System.out.println("[BERVARIASI] virtual thread  : " + msVirtual + " ms");
        System.out.println("[BERVARIASI] rasio           : " + String.format(Locale.US, "%.1f", (double) msPool / msVirtual) + "x");
        System.out.println("Pool jauh lebih lambat (>20x)? " + (msPool > msVirtual * 20));

        // Fakta dasar virtual thread
        Thread v = Thread.ofVirtual().name("v-1").start(() -> {});
        v.join();
        System.out.println();
        System.out.println("isVirtual()=" + v.isVirtual() + ", isDaemon()=" + v.isDaemon() + " (virtual thread selalu daemon)");
    }
}
