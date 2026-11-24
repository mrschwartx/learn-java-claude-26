import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

// Latihan 2: race condition pada counter; 8 thread x 100.000 kenaikan; lima cara dibandingkan
public class Counter {
    static final int THREAD = 8;
    static final int KENAIKAN = 100_000;
    static final int RONDE = 5;

    interface Hitung { void tambah(); long baca(); }

    static class TidakAman implements Hitung {
        int n;
        public void tambah() { n++; }
        public long baca() { return n; }
    }
    static class Volatil implements Hitung {
        volatile int n;
        public void tambah() { n++; }                     // volatile TIDAK membuat n++ atomik
        public long baca() { return n; }
    }
    static class Sinkron implements Hitung {
        private int n;
        public synchronized void tambah() { n++; }
        public synchronized long baca() { return n; }
    }
    static class Atomik implements Hitung {
        private final AtomicInteger n = new AtomicInteger();
        public void tambah() { n.incrementAndGet(); }
        public long baca() { return n.get(); }
    }
    static class Kunci implements Hitung {
        private final ReentrantLock lock = new ReentrantLock();
        private int n;
        public void tambah() {
            lock.lock();
            try { n++; } finally { lock.unlock(); }       // unlock WAJIB di finally
        }
        public long baca() {
            lock.lock();
            try { return n; } finally { lock.unlock(); }
        }
    }
    static class Penambah implements Hitung {
        private final LongAdder n = new LongAdder();
        public void tambah() { n.increment(); }
        public long baca() { return n.sum(); }
    }

    /** Satu ronde: semua thread dilepas bersamaan; mengembalikan {hasil, waktu_ms}. */
    static long[] satuRonde(Hitung h) throws Exception {
        CountDownLatch gerbang = new CountDownLatch(1);
        List<Thread> semua = new ArrayList<>();
        for (int i = 0; i < THREAD; i++) {
            Thread t = new Thread(() -> {
                try { gerbang.await(); } catch (InterruptedException e) { return; }
                for (int j = 0; j < KENAIKAN; j++) h.tambah();
            });
            semua.add(t);
            t.start();
        }
        long t0 = System.nanoTime();
        gerbang.countDown();
        for (Thread t : semua) t.join();                  // join -> hasil terlihat oleh main (happens-before)
        return new long[] { h.baca(), (System.nanoTime() - t0) / 1_000_000 };
    }

    static void uji(String nama, Supplier<Hitung> pabrik, boolean seharusnyaAman) throws Exception {
        satuRonde(pabrik.get());                          // pemanasan JIT, hasil dibuang
        long[] hasil = new long[RONDE];
        long[] waktu = new long[RONDE];
        int benar = 0;
        for (int r = 0; r < RONDE; r++) {
            long[] x = satuRonde(pabrik.get());
            hasil[r] = x[0];
            waktu[r] = x[1];
            if (x[0] == (long) THREAD * KENAIKAN) benar++;
        }
        long[] urut = waktu.clone();
        Arrays.sort(urut);
        String awalan = seharusnyaAman ? "" : "[BERVARIASI] ";
        System.out.println(String.format("%s%-13s benar %d/%d ronde, hasil: %s", awalan, nama, benar, RONDE, Arrays.toString(hasil)));
        System.out.println(String.format("[BERVARIASI] %-13s median waktu: %d ms", nama, urut[RONDE / 2]));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Diharapkan: " + THREAD + " x " + KENAIKAN + " = " + (THREAD * KENAIKAN));
        System.out.println("[BERVARIASI] CPU tersedia: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        uji("TidakAman",     TidakAman::new, false);
        uji("volatile",      Volatil::new,   false);
        uji("synchronized",  Sinkron::new,   true);
        uji("AtomicInteger", Atomik::new,    true);
        uji("ReentrantLock", Kunci::new,     true);
        uji("LongAdder",     Penambah::new,  true);
    }
}
