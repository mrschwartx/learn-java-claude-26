import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Latihan 2: rapat 10.00 WIB di berbagai zona; kenapa Instant lebih aman daripada LocalDateTime
public class Rapat {
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("EEE dd MMM yyyy HH:mm 'UTC'xxx", Locale.of("id", "ID"));

    static void tampil(ZonedDateTime rapat, String... zona) {
        for (String z : zona) {
            ZonedDateTime t = rapat.withZoneSameInstant(ZoneId.of(z));
            System.out.println(String.format("  %-17s -> %s  [%s]", z, t.format(F), t.getZone().getRules().isDaylightSavings(t.toInstant()) ? "DST" : "standar"));
        }
    }

    public static void main(String[] args) {
        String[] zona = { "Asia/Jakarta", "Asia/Makassar", "Asia/Jayapura", "America/New_York" };

        ZonedDateTime rapat = ZonedDateTime.of(2026, 11, 24, 10, 0, 0, 0, ZoneId.of("Asia/Jakarta"));
        System.out.println("Rapat 24 Nov 2026, 10.00 Asia/Jakarta (Instant = " + rapat.toInstant() + ")");
        tampil(rapat, zona);

        // DST: pada tanggal berbeda, selisih ke New York BERUBAH, sedangkan zona Indonesia tidak.
        ZonedDateTime rapatJuli = ZonedDateTime.of(2026, 7, 24, 10, 0, 0, 0, ZoneId.of("Asia/Jakarta"));
        System.out.println();
        System.out.println("Rapat 24 Jul 2026, 10.00 Asia/Jakarta (Instant = " + rapatJuli.toInstant() + ")");
        tampil(rapatJuli, zona);

        // Mengapa Instant: LocalDateTime tidak membawa zona -> ambigu
        System.out.println();
        LocalDateTime tanpaZona = LocalDateTime.of(2026, 11, 24, 10, 0);
        System.out.println("LocalDateTime tersimpan: " + tanpaZona + "  <- 10.00 di mana? tidak diketahui");
        for (String z : new String[] { "Asia/Jakarta", "America/New_York" }) {
            System.out.println("  ditafsirkan di " + z + " -> Instant " + tanpaZona.atZone(ZoneId.of(z)).toInstant());
        }
        Instant disimpan = rapat.toInstant();
        System.out.println("Instant tersimpan      : " + disimpan + "  <- satu titik waktu, tak ambigu");
        System.out.println("  ditampilkan WIB      : " + disimpan.atZone(ZoneId.of("Asia/Jakarta")).toLocalTime());
        System.out.println("  ditampilkan New York : " + disimpan.atZone(ZoneId.of("America/New_York")).toLocalTime());

        // Jam yang tidak ada / ambigu saat peralihan DST (New York, 8 Mar 2026 dan 1 Nov 2026)
        System.out.println();
        System.out.println("Peralihan DST New York:");
        System.out.println("  08 Mar 2026 02:30 (tidak ada) -> " + ZonedDateTime.of(LocalDateTime.of(2026, 3, 8, 2, 30), ZoneId.of("America/New_York")));
        System.out.println("  01 Nov 2026 01:30 (muncul 2x) -> " + ZonedDateTime.of(LocalDateTime.of(2026, 11, 1, 1, 30), ZoneId.of("America/New_York")) + "  (default: offset pertama)");
    }
}
