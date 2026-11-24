package demo.lain;   // di luar com.contoh -> backend demo memakai level INFO (DEBUG nonaktif)

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Latihan 5: tiga contoh log BURUK dan perbaikannya, diukur (bukan hanya diklaim)
public class LogBuruk {
    private static final Logger log = LoggerFactory.getLogger(LogBuruk.class);

    /** Objek yang menghitung berapa kali toString() dipanggil (biaya merakit string). */
    static class Mahal {
        static final AtomicInteger PANGGILAN = new AtomicInteger();
        public String toString() { PANGGILAN.incrementAndGet(); return "objek-mahal"; }
    }

    // ---------- 1. Konkatenasi ----------
    static void contoh1() {
        System.out.println("== 1. Konkatenasi vs parameterized (level DEBUG NONAKTIF) ==");
        Mahal m = new Mahal();
        Mahal.PANGGILAN.set(0);
        for (int i = 0; i < 1000; i++) log.debug("Data: " + m);           // BURUK: string dirakit walau tak dicetak
        System.out.println("  konkatenasi : toString() dipanggil " + Mahal.PANGGILAN.get() + " kali dari 1000 log");
        Mahal.PANGGILAN.set(0);
        for (int i = 0; i < 1000; i++) log.debug("Data: {}", m);          // BAIK: dirakit hanya jika level aktif
        System.out.println("  parameterized: toString() dipanggil " + Mahal.PANGGILAN.get() + " kali dari 1000 log");
        Mahal.PANGGILAN.set(0);
        for (int i = 0; i < 1000; i++) if (log.isDebugEnabled()) log.debug("Data: {}", hitungMahal());
        System.out.println("  argumen hasil perhitungan mahal + guard isDebugEnabled(): dihitung " + Mahal.PANGGILAN.get() + " kali");
    }
    static Mahal hitungMahal() { Mahal.PANGGILAN.incrementAndGet(); return new Mahal(); }

    // ---------- 2. Data sensitif ----------
    static String samarkan(String s) { return s == null || s.length() < 4 ? "****" : "****" + s.substring(s.length() - 4); }

    static void contoh2() {
        System.out.println();
        System.out.println("== 2. Data sensitif ==");
        String user = "andi", password = "rahasia123", kartu = "1234567812345678";
        log.info("BURUK login user={} password={} kartu={}", user, password, kartu);
        log.info("BAIK  login user={} kartu={}", user, samarkan(kartu));
    }

    // ---------- 3. Log lalu lempar ulang ----------
    static class Repo { void simpan() { throw new IllegalStateException("DB mati"); } }

    static void burukRepo(Repo r)    { try { r.simpan(); } catch (IllegalStateException e) { log.error("repo gagal", e); throw e; } }
    static void burukService(Repo r) { try { burukRepo(r); } catch (IllegalStateException e) { log.error("service gagal", e); throw e; } }
    static void burukController(Repo r) { try { burukService(r); } catch (IllegalStateException e) { log.error("controller gagal", e); } }

    static void baikRepo(Repo r)     { r.simpan(); }                                    // biarkan naik
    static void baikService(Repo r)  { try { baikRepo(r); } catch (IllegalStateException e) {
        throw new IllegalStateException("pesanan gagal disimpan", e); } }               // bungkus, bawa penyebab (cause)
    static void baikController(Repo r) { try { baikService(r); } catch (IllegalStateException e) {
        log.error("Permintaan gagal", e); } }                                           // log SEKALI di batas

    static void contoh3() {
        System.out.println();
        System.out.println("== 3. Log-dan-lempar-ulang di setiap lapisan ==");
        System.out.println("-- BURUK (1 kegagalan -> 3 entri ERROR):");
        burukController(new Repo());
        System.out.println("-- BAIK (1 kegagalan -> 1 entri, penyebab asli tetap ada lewat cause):");
        baikController(new Repo());
    }

    public static void main(String[] args) {
        contoh1();
        contoh2();
        contoh3();
    }
}
