import java.util.concurrent.atomic.AtomicBoolean;

// Latihan 5b: happens-before dan flag berhenti. Tiap varian diberi batas waktu agar tidak menggantung.
public class Volatile {
    static boolean flagBiasa = true;
    static volatile boolean flagVolatile = true;
    static final AtomicBoolean flagAtomik = new AtomicBoolean(true);

    static long hitungan;

    /** Jalankan worker daemon; tunggu maks 1 detik setelah flag dimatikan; laporkan apakah worker berhenti. */
    static boolean uji(String nama, Runnable worker, Runnable matikan) throws Exception {
        Thread t = new Thread(worker, nama);
        t.setDaemon(true);
        t.start();
        Thread.sleep(200);
        matikan.run();
        t.join(1_000);
        boolean berhenti = !t.isAlive();
        System.out.println(String.format("%-18s -> worker berhenti dalam 1 detik? %s", nama, berhenti));
        return berhenti;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[BERVARIASI] hasil varian 'biasa' bergantung JIT/CPU; di JVM HotSpot loop kosong umumnya TIDAK berhenti");
        boolean biasa = uji("flag biasa", () -> { while (flagBiasa) { hitungan++; } }, () -> flagBiasa = false);
        uji("flag volatile", () -> { while (flagVolatile) { hitungan++; } }, () -> flagVolatile = false);
        uji("AtomicBoolean", () -> { while (flagAtomik.get()) { hitungan++; } }, () -> flagAtomik.set(false));
        System.out.println();
        System.out.println("[BERVARIASI] varian biasa berhenti? " + biasa);

        // Happens-before: tulis data lalu flag volatile; pembaca yang melihat flag PASTI melihat data
        class Paket { int data; volatile boolean siap; }
        Paket p = new Paket();
        int[] dilihat = { -1 };
        Thread pembaca = new Thread(() -> {
            while (!p.siap) Thread.onSpinWait();
            dilihat[0] = p.data;                            // dijamin 42 oleh happens-before
        });
        pembaca.start();
        p.data = 42;                                        // (1) tulis data biasa
        p.siap = true;                                      // (2) tulis volatile -> mempublikasikan (1)
        pembaca.join();
        System.out.println("Pembaca melihat data = " + dilihat[0] + " (dijamin 42 oleh happens-before)");
    }
}
