import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Latihan 3: pipeline normalisasi nama (andThen/compose) dan validator dari tiga Predicate (and)
public class DemoPipeline {
    public static void main(String[] args) {
        // Langkah-langkah kecil, masing-masing satu tanggung jawab
        Function<String, String> trim = String::trim;
        Function<String, String> kecil = s -> s.toLowerCase(Locale.ROOT);       // ROOT: tidak bergantung locale mesin
        Function<String, String> kapitalTiapKata = s -> Arrays.stream(s.split("\\s+"))
                .map(k -> k.isEmpty() ? k : k.substring(0, 1).toUpperCase(Locale.ROOT) + k.substring(1))
                .collect(Collectors.joining(" "));

        // Cara 1: andThen  -> dibaca KIRI ke KANAN
        Function<String, String> normalisasi = trim.andThen(kecil).andThen(kapitalTiapKata);

        // Cara 2: compose  -> dibaca KANAN ke KIRI, hasilnya sama
        Function<String, String> normalisasi2 = kapitalTiapKata.compose(kecil).compose(trim);

        String[] uji = { "  bUdI   sAnToSo ", "ANI", "  rina  kartika  dewi", "x", "" };
        for (String s : uji) {
            String a = normalisasi.apply(s);
            boolean sama = a.equals(normalisasi2.apply(s));
            System.out.println(String.format("%-26s -> \"%s\"   (andThen == compose: %s)", "\"" + s + "\"", a, sama));
        }

        // Urutan penting: kapital SEBELUM lowercase menghancurkan hasilnya
        Function<String, String> urutanSalah = trim.andThen(kapitalTiapKata).andThen(kecil);
        System.out.println("Urutan salah (kapital lalu lowercase): \"" + urutanSalah.apply("  bUdI sAnToSo ") + "\"");

        System.out.println();
        System.out.println("== Validator: tiga Predicate digabung dengan and ==");
        Predicate<String> tidakKosong = s -> s != null && !s.isBlank();
        Predicate<String> tanpaAngka  = s -> s.chars().noneMatch(Character::isDigit);
        Predicate<String> minimal3    = s -> s.strip().length() >= 3;

        Predicate<String> namaValid = tidakKosong.and(tanpaAngka).and(minimal3);

        List<String> kandidat = Arrays.asList("Budi", "Jo", "", "   ", "Ani2", "x1y2", "Siti Aminah", null);
        for (String k : kandidat) {
            // Hasil per aturan, untuk melihat aturan mana yang menggagalkan
            String rincian = k == null ? "null -> and berhenti di tidakKosong (short-circuit); aturan lain tidak dipanggil"
                    : String.format("kosong?%-5s angka?%-5s pendek?%-5s", !tidakKosong.test(k), !tanpaAngka.test(k), !minimal3.test(k));
            System.out.println(String.format("%-14s valid=%-5s  %s", k == null ? "null" : "\"" + k + "\"", namaValid.test(k), rincian));
        }

        // Pengait lain: or dan negate
        Predicate<String> bermasalah = namaValid.negate();
        Predicate<String> pendekAtauAngka = minimal3.negate().or(tanpaAngka.negate());
        System.out.println();
        System.out.println("negate  -> \"Jo\" bermasalah? " + bermasalah.test("Jo"));
        System.out.println("or      -> \"Ani2\" pendek atau berangka? " + pendekAtauAngka.test("Ani2"));
    }
}
