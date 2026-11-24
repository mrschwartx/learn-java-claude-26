import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Latihan 4: tiga CompletableFuture (harga, kurs, total) dengan thenCombine dan exceptionally
public class DemoHarga {
    static final Locale ID = Locale.of("id", "ID");
    static final long KURS_CADANGAN = 16_000;

    static void tidur(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static long ambilHarga() {                  // harga dalam USD
        tidur(100);
        return 150;
    }

    static long ambilKurs(boolean gagal) {      // kurs USD -> IDR
        tidur(100);
        if (gagal) throw new IllegalStateException("layanan kurs sedang mati");
        return 15_800;
    }

    static long ambilKursLambat() {
        tidur(2_000);
        return 15_800;
    }

    static String rp(long x) { return String.format(ID, "Rp%,d", x); }

    public static void main(String[] args) {
        // ---- 1. Jalur normal ----
        System.out.println("== 1. Semua berhasil ==");
        CompletableFuture<Long> harga = CompletableFuture.supplyAsync(DemoHarga::ambilHarga);
        CompletableFuture<Long> kurs = CompletableFuture.supplyAsync(() -> ambilKurs(false));
        long total = harga.thenCombine(kurs, (h, k) -> h * k).join();      // hitung total dari dua hasil
        System.out.println("Total = 150 USD x 15.800 = " + rp(total));

        // ---- 2. Kurs gagal, TANPA penanganan ----
        System.out.println();
        System.out.println("== 2. Kurs gagal, tanpa exceptionally ==");
        try {
            CompletableFuture.supplyAsync(DemoHarga::ambilHarga)
                    .thenCombine(CompletableFuture.supplyAsync(() -> ambilKurs(true)), (h, k) -> h * k)
                    .join();
        } catch (CompletionException e) {
            System.out.println("join() melempar : " + e.getClass().getSimpleName());
            System.out.println("penyebab asli   : " + e.getCause());
        }

        // ---- 3. exceptionally DI LANGKAH YANG GAGAL (kurs): total tetap masuk akal ----
        System.out.println();
        System.out.println("== 3. exceptionally pada langkah kurs -> pakai kurs cadangan ==");
        CompletableFuture<Long> kursAman = CompletableFuture.supplyAsync(() -> ambilKurs(true))
                .exceptionally(e -> {
                    System.out.println("  tipe exception yang diterima exceptionally: " + e.getClass().getSimpleName()
                            + " (penyebab: " + e.getCause().getMessage() + ")");
                    return KURS_CADANGAN;
                });
        long totalAman = CompletableFuture.supplyAsync(DemoHarga::ambilHarga)
                .thenCombine(kursAman, (h, k) -> h * k)
                .join();
        System.out.println("Total dengan kurs cadangan = " + rp(totalAman) + "  (150 x 16.000)");

        // ---- 4. exceptionally DI UJUNG: hanya bisa menyediakan fallback untuk TOTAL ----
        System.out.println();
        System.out.println("== 4. exceptionally di ujung rantai -> fallback untuk total ==");
        long totalUjung = CompletableFuture.supplyAsync(DemoHarga::ambilHarga)
                .thenCombine(CompletableFuture.supplyAsync(() -> ambilKurs(true)), (h, k) -> h * k)
                .exceptionally(e -> -1L)
                .join();
        System.out.println("Total = " + totalUjung + "  (-1 = 'tidak tersedia'; informasi harga ikut terbuang)");

        // ---- 5. handle(): hasil DAN error sekaligus ----
        System.out.println();
        System.out.println("== 5. handle(): satu tempat untuk sukses dan gagal ==");
        for (boolean gagal : new boolean[] { false, true }) {
            String ket = CompletableFuture.supplyAsync(() -> ambilKurs(gagal))
                    .handle((nilai, err) -> err == null ? "kurs=" + nilai : "gagal, pakai cadangan " + KURS_CADANGAN)
                    .join();
            System.out.println("gagal=" + gagal + " -> " + ket);
        }

        // ---- 6. Kurs LAMBAT (bukan gagal): orTimeout ----
        System.out.println();
        System.out.println("== 6. Kurs lambat (2 detik), batas waktu 300 ms ==");
        long t0 = System.nanoTime();
        long kursTimeout = CompletableFuture.supplyAsync(DemoHarga::ambilKursLambat)
                .orTimeout(300, TimeUnit.MILLISECONDS)
                .exceptionally(e -> {
                    Throwable sebab = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
                    System.out.println("  fallback karena: " + sebab.getClass().getSimpleName());
                    return KURS_CADANGAN;
                })
                .join();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("Kurs yang dipakai = " + kursTimeout + "; selesai jauh sebelum 2 detik? " + (ms < 1_500));
    }
}
