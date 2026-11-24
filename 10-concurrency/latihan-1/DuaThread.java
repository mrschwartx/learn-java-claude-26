import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Latihan 1: dua thread mencetak 1..5 (jeda 200 ms); main mencetak "selesai" setelah join; start() vs run()
public class DuaThread {

    static Runnable tugas(String label, Queue<String> catatan) {
        return () -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("  [live] " + label + " (thread " + Thread.currentThread().getName() + ") : " + i);
                catatan.add(label + ":" + i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();       // jangan menelan interupsi
                    return;
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        // ============ A. start() + join() ============
        System.out.println("== A. start() lalu join() ==");
        Queue<String> catatan = new ConcurrentLinkedQueue<>();
        Thread a = new Thread(tugas("A", catatan), "thread-A");
        Thread b = new Thread(tugas("B", catatan), "thread-B");

        long t0 = System.nanoTime();
        a.start();
        b.start();
        System.out.println("Tepat setelah start(): A hidup? " + a.isAlive() + ", B hidup? " + b.isAlive()
                + "   (main TIDAK menunggu, ia langsung lanjut)");
        a.join();
        b.join();
        long msStart = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("selesai");                        // dicetak SETELAH keduanya berhenti
        System.out.println("Status: A=" + a.getState() + ", B=" + b.getState());

        // Urutan per thread selalu 1..5; yang bervariasi hanya selang-seling antar thread
        System.out.println("Urutan milik A: " + urutan(catatan, "A"));
        System.out.println("Urutan milik B: " + urutan(catatan, "B"));
        System.out.println("Jumlah catatan: " + catatan.size());

        // ============ B. run() alih-alih start() ============
        System.out.println();
        System.out.println("== B. run() alih-alih start() ==");
        Queue<String> catatan2 = new ConcurrentLinkedQueue<>();
        Thread c = new Thread(tugas("A", catatan2), "thread-A");
        Thread d = new Thread(tugas("B", catatan2), "thread-B");
        long t1 = System.nanoTime();
        c.run();                                              // BUKAN thread baru: dijalankan oleh main
        d.run();
        long msRun = (System.nanoTime() - t1) / 1_000_000;
        System.out.println("selesai");
        System.out.println("Status c: " + c.getState() + " (tidak pernah di-start)");
        System.out.println("Urutan seluruhnya: " + catatan2);

        System.out.println();
        System.out.println("[BERVARIASI] waktu start()+join(): " + msStart + " ms   | waktu run(): " + msRun + " ms");
        System.out.println("run() sekitar dua kali lebih lama (berurutan, bukan bersamaan)? " + (msRun > msStart * 1.5));
    }

    static List<String> urutan(Queue<String> catatan, String label) {
        return catatan.stream().filter(s -> s.startsWith(label + ":")).map(s -> s.substring(2)).toList();
    }
}
