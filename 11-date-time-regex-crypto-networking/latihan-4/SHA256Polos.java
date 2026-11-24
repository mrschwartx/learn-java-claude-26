import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

// Mengapa SHA-256 polos tidak layak: tanpa salt, password sama -> hash sama -> mudah dikamus / rainbow table
public class SHA256Polos {
    static String sha(String s) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        // "Basis data bocor" berisi hash SHA-256 polos dua pengguna
        Map<String, String> bocor = Map.of("andi", sha("password123"), "budi", sha("password123"), "citra", sha("Xk9#vQ2!mLp"));
        System.out.println("Hash andi == hash budi? " + bocor.get("andi").equals(bocor.get("budi")) + "  (bocor: keduanya memakai password SAMA)");

        // Penyerang cukup menghitung hash daftar password umum SEKALI untuk semua korban
        String[] kamus = { "123456", "password", "qwerty", "password123", "letmein" };
        for (String user : new java.util.TreeSet<>(bocor.keySet())) {
            String ditemukan = null;
            for (String kandidat : kamus) if (sha(kandidat).equals(bocor.get(user))) ditemukan = kandidat;
            System.out.println(String.format("%-6s -> %s", user, ditemukan == null ? "tidak ada di kamus" : "TERBONGKAR: " + ditemukan));
        }
    }
}
