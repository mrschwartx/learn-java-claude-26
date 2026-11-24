import java.util.Random;

// Mengapa java.util.Random tidak layak untuk token/salt: seed sama -> urutan IDENTIK (dapat ditebak)
public class RandomLemah {
    public static void main(String[] args) {
        Random a = new Random(42), b = new Random(42);
        System.out.println("Random(42) #1: " + a.nextInt() + ", " + a.nextInt() + ", " + a.nextInt());
        System.out.println("Random(42) #2: " + b.nextInt() + ", " + b.nextInt() + ", " + b.nextInt());
        System.out.println("Identik, jadi siapa pun yang tahu/menebak seed (mis. waktu start) dapat memprediksi 'salt'/token.");
        System.out.println();
        // Ruang seed hanya 48 bit; dari 2 keluaran, seluruh keadaan internal bisa dipulihkan (diketahui secara umum, tidak didemokan).
        byte[] x = new byte[8], y = new byte[8];
        new java.security.SecureRandom().nextBytes(x);
        new java.security.SecureRandom().nextBytes(y);
        System.out.println("SecureRandom dua instans berbeda: " + java.util.Arrays.equals(x, y) + "  (tak dapat diulang/ditebak)");
    }
}
