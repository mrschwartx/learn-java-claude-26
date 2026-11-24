import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Latihan 4: PBKDF2 + salt acak, format "iterasi:salt:hash" (Base64)
public class PasswordHasher {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERASI = 310_000;
    private static final int SALT_BYTE = 16;
    private static final int HASH_BIT = 256;
    private static final int ITERASI_MAKS = 5_000_000;       // cegah DoS lewat nilai iterasi di data tersimpan
    private static final SecureRandom RNG = new SecureRandom();

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTE];
        RNG.nextBytes(salt);                                  // salt ACAK per pengguna
        return format(ITERASI, salt, turunkan(password, salt, ITERASI));
    }

    public static boolean verifikasi(String password, String tersimpan) {
        try {
            String[] bagian = tersimpan.split(":", -1);
            if (bagian.length != 3) return false;
            int iterasi = Integer.parseInt(bagian[0]);
            if (iterasi < 1 || iterasi > ITERASI_MAKS) return false;
            byte[] salt = Base64.getDecoder().decode(bagian[1]);
            byte[] harapan = Base64.getDecoder().decode(bagian[2]);
            byte[] hitung = turunkan(password, salt, iterasi, harapan.length * 8);
            return MessageDigest.isEqual(harapan, hitung);    // perbandingan waktu-konstan, bukan Arrays.equals
        } catch (IllegalArgumentException e) {                // format rusak / Base64 salah
            return false;
        }
    }

    private static byte[] turunkan(String password, byte[] salt, int iterasi) { return turunkan(password, salt, iterasi, HASH_BIT); }

    private static byte[] turunkan(String password, byte[] salt, int iterasi, int bit) {
        char[] pw = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(pw, salt, iterasi, bit);
        try {
            return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
            Arrays.fill(pw, '\0');                            // hapus password dari memori sedini mungkin
        }
    }

    private static String format(int iterasi, byte[] salt, byte[] hash) {
        Base64.Encoder b = Base64.getEncoder();
        return iterasi + ":" + b.encodeToString(salt) + ":" + b.encodeToString(hash);
    }

    public static void main(String[] args) throws Exception {
        String pw = "rahasia123";
        String a = hash(pw), b = hash(pw);

        String[] bagian = a.split(":");
        System.out.println("Format           : iterasi:salt:hash  (" + bagian.length + " bagian)");
        System.out.println("Iterasi          : " + bagian[0]);
        System.out.println("Panjang salt     : " + Base64.getDecoder().decode(bagian[1]).length + " byte");
        System.out.println("Panjang hash     : " + Base64.getDecoder().decode(bagian[2]).length + " byte");
        System.out.println();
        System.out.println("verifikasi(benar)                  : " + verifikasi(pw, a));
        System.out.println("verifikasi(salah)                  : " + verifikasi("rahasia124", a));
        System.out.println("verifikasi(kosong)                 : " + verifikasi("", a));
        System.out.println("verifikasi(format rusak)           : " + verifikasi(pw, "bukan-format-valid"));
        System.out.println("verifikasi(Base64 rusak)           : " + verifikasi(pw, "310000:@@@:@@@"));
        System.out.println("verifikasi(iterasi absurd)         : " + verifikasi(pw, "999999999:" + bagian[1] + ":" + bagian[2]));
        System.out.println();
        System.out.println("Password sama, dua hash tersimpan SAMA? " + a.equals(b) + "  (salt berbeda)");
        System.out.println("Keduanya tetap lolos verifikasi?       " + (verifikasi(pw, a) && verifikasi(pw, b)));
        System.out.println("hash lama (iterasi rendah) tetap bisa diverifikasi (iterasi ikut tersimpan): "
                + verifikasi(pw, format(1_000, Base64.getDecoder().decode(bagian[1]), turunkan(pw, Base64.getDecoder().decode(bagian[1]), 1_000))));

        // Biaya: PBKDF2 sengaja lambat; SHA-256 polos ribuan kali lebih cepat bagi penyerang
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] data = pw.getBytes(StandardCharsets.UTF_8);
        int n = 20;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) turunkan(pw, new byte[16], ITERASI);
        double msPbkdf = (System.nanoTime() - t0) / 1e6 / n;
        int m = 200_000;
        t0 = System.nanoTime();
        for (int i = 0; i < m; i++) sha.digest(data);
        double msSha = (System.nanoTime() - t0) / 1e6 / m;
        System.out.println();
        System.out.println(String.format(java.util.Locale.US, "[BERVARIASI] PBKDF2 310.000 iterasi: %.1f ms/tebakan | SHA-256 polos: %.5f ms/tebakan | rasio sekitar %,.0fx",
                msPbkdf, msSha, msPbkdf / msSha));
    }
}
