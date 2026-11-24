import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Latihan 3: parse access log dengan grup bernama + demo ReDoS yang DETERMINISTIK (menghitung langkah, bukan waktu)
public class AccessLog {
    private static final Pattern LOG = Pattern.compile(
            "^(?<method>GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) (?<path>/\\S*) (?<status>[1-5]\\d{2}) (?<ms>\\d+)ms$");

    /** CharSequence pembungkus yang menghitung berapa kali engine regex membaca karakter. */
    static final class Penghitung implements CharSequence {
        private final CharSequence isi;
        long baca;
        Penghitung(CharSequence isi) { this.isi = isi; }
        public int length() { return isi.length(); }
        public char charAt(int i) { baca++; return isi.charAt(i); }
        public CharSequence subSequence(int a, int b) { return isi.subSequence(a, b); }
        public String toString() { return isi.toString(); }
    }

    static long langkah(Pattern p, String input) {
        Penghitung h = new Penghitung(input);
        p.matcher(h).matches();
        return h.baca;
    }

    public static void main(String[] args) {
        List<String> baris = List.of(
                "GET /api/produk/12 200 35ms",
                "POST /api/pesanan 201 120ms",
                "GET /api/produk/999 404 8ms",
                "GET /api/laporan 500 2300ms",
                "PUT /api/pesanan/5 503 15ms",
                "DELETE /api/pengguna/3 204 4ms",
                "GET /api/bayar 502 900ms",
                "rusak tanpa format",
                "GET api/tanpa-slash 200 1ms");

        int lima = 0, cocok = 0;
        for (String b : baris) {
            Matcher m = LOG.matcher(b);
            if (m.matches()) {
                cocok++;
                String st = m.group("status");
                boolean s5 = st.charAt(0) == '5';
                if (s5) lima++;
                System.out.printf("%-7s %-20s %s %5sms %s%n", m.group("method"), m.group("path"), st, m.group("ms"), s5 ? "<-- 5xx" : "");
            } else {
                System.out.println("TIDAK COCOK: " + b);
            }
        }
        System.out.println();
        System.out.println("Baris cocok: " + cocok + " dari " + baris.size() + "; berstatus 5xx: " + lima);

        // ----- ReDoS -----
        System.out.println();
        System.out.println("== ReDoS: jumlah pembacaan karakter oleh engine (deterministik, JDK " + Runtime.version().feature() + ") ==");
        Pattern klasik = Pattern.compile("^(a+)+$");          // contoh buku teks; JDK modern sudah memitigasinya
        Pattern jahat = Pattern.compile("^((a+)+)+$");        // pengulangan bersarang dua tingkat: masih eksponensial
        Pattern aman = Pattern.compile("^a+$");               // untuk input ini bermakna sama, tanpa bersarang
        System.out.println(String.format("%3s | %-22s | %-22s | %-18s", "n", "^((a+)+)+$ a^n+'!'", "^(a+)+$ a^n+'!'", "^a+$ a^n+'!'"));
        long sebelum = 0;
        for (int n = 8; n <= 22; n += 2) {
            String serang = "a".repeat(n) + "!";              // hampir cocok -> backtracking maksimal
            long j = langkah(jahat, serang);
            String rasio = sebelum == 0 ? "" : String.format(" (x%.1f)", (double) j / sebelum);
            System.out.println(String.format("%3d | %,13d%-9s | %-22d | %-18d", n, j, rasio, langkah(klasik, serang), langkah(aman, serang)));
            sebelum = j;
        }
        System.out.println("Pola bersarang: tiap n naik 2, langkah naik sekitar 4x (2^n) -> eksponensial.");
        System.out.println("Pola klasik ^(a+)+$ tumbuh linear di JDK ini (dimitigasi optimasi engine), tetapi mitigasi itu tidak menjangkau semua pola");
        System.out.println("(mis. yang memakai backreference atau bersarang lebih dalam), jadi jangan mengandalkannya: batasi panjang input.");
    }
}
