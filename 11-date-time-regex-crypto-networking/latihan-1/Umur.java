import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

// Latihan 1: umur lengkap, hari lahir (bahasa Indonesia), hari menuju ulang tahun berikutnya; parser STRICT
public class Umur {
    static final Locale ID = Locale.of("id", "ID");
    // STRICT mensyaratkan 'uuuu' (tahun proleptik), bukan 'yyyy' (tahun-era)
    static final DateTimeFormatter KETAT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    static final DateTimeFormatter LONGGAR = DateTimeFormatter.ofPattern("dd/MM/yyyy");   // SMART
    static final DateTimeFormatter HARI = DateTimeFormatter.ofPattern("EEEE", ID);

    /** Ulang tahun berikutnya (hari ini TIDAK dihitung "berikutnya" bila acuan tepat hari ulang tahun -> hasil 0). */
    static LocalDate ulangTahunBerikut(LocalDate lahir, LocalDate acuan) {
        LocalDate u = tahunIni(lahir, acuan.getYear());
        return u.isBefore(acuan) ? tahunIni(lahir, acuan.getYear() + 1) : u;
    }

    /** 29 Februari di tahun non-kabisat dirayakan 28 Februari (perilaku LocalDate.withYear). */
    static LocalDate tahunIni(LocalDate lahir, int tahun) { return lahir.withYear(tahun); }

    static void proses(String masukan, LocalDate acuan) {
        System.out.println("Masukan: " + masukan);
        try {
            LocalDate lahir = LocalDate.parse(masukan, KETAT);
            if (lahir.isAfter(acuan)) {
                System.out.println("  Ditolak: tanggal lahir di masa depan");
                return;
            }
            Period p = Period.between(lahir, acuan);
            System.out.println("  Umur            : " + p.getYears() + " tahun, " + p.getMonths() + " bulan, " + p.getDays() + " hari");
            System.out.println("  Hari kelahiran  : " + lahir.format(HARI));
            LocalDate next = ulangTahunBerikut(lahir, acuan);
            System.out.println("  Ulang tahun     : " + next + " (" + ChronoUnit.DAYS.between(acuan, next) + " hari lagi)");
        } catch (DateTimeParseException e) {
            System.out.println("  DITOLAK (STRICT): \"" + e.getParsedString() + "\" -> " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LocalDate acuan = LocalDate.of(2026, 11, 24);          // tetap, supaya hasil deterministik
        System.out.println("Tanggal acuan: " + acuan + " (" + acuan.format(HARI) + ")");
        System.out.println();
        for (String s : new String[] { "17/05/1998", "24/11/2000", "29/02/2000", "01/01/2026", "31/02/2026", "31/04/2020", "2026-11-01", "30/02/1999" }) {
            proses(s, acuan);
        }
        System.out.println();
        System.out.println("Pembanding resolver default SMART:");
        System.out.println("  31/02/2026 -> " + LocalDate.parse("31/02/2026", LONGGAR) + "  (dibulatkan diam-diam!)");
        System.out.println("  31/04/2020 -> " + LocalDate.parse("31/04/2020", LONGGAR));
        proses("29/02/2000", LocalDate.of(2027, 2, 27));
    }
}
