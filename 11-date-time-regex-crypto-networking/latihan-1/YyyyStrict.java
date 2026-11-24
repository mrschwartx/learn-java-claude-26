import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

// Jebakan: STRICT + 'yyyy' (tahun-era) gagal untuk tanggal yang VALID karena era tidak ditentukan
public class YyyyStrict {
    public static void main(String[] args) {
        var salah = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT);
        try {
            System.out.println(LocalDate.parse("05/03/2026", salah));
        } catch (Exception e) {
            System.out.println("yyyy + STRICT menolak tanggal VALID: " + e.getClass().getSimpleName());
            System.out.println("  " + e.getMessage());
        }
        var benar = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        System.out.println("uuuu + STRICT: " + LocalDate.parse("05/03/2026", benar));
    }
}
