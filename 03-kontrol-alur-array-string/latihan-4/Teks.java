// Latihan 4: hitungVokal, balik (tanpa StringBuilder.reverse), dan biaya penyambungan dengan +
public class Teks {

    static int hitungVokal(String teks) {
        int jumlah = 0;
        for (int i = 0; i < teks.length(); i++) {
            char c = Character.toLowerCase(teks.charAt(i));
            if (c == 'a' || c == 'i' || c == 'u' || c == 'e' || c == 'o') {
                jumlah++;
            }
        }
        return jumlah;
    }

    // Loop dari belakang dengan charAt, hasil ditaruh di char[] (bukan String, agar tidak membuat
    // objek String baru di tiap putaran)
    static String balik(String teks) {
        char[] hasil = new char[teks.length()];
        for (int i = 0; i < teks.length(); i++) {
            hasil[i] = teks.charAt(teks.length() - 1 - i);
        }
        return new String(hasil);
    }

    public static void main(String[] args) {
        System.out.println("hitungVokal(\"Belajar Java\")           = " + hitungVokal("Belajar Java"));
        System.out.println("hitungVokal(\"Programming\")            = " + hitungVokal("Programming"));
        System.out.println("hitungVokal(\"AIUEO aiueo\")            = " + hitungVokal("AIUEO aiueo"));
        System.out.println("hitungVokal(\"\")                       = " + hitungVokal(""));
        System.out.println("balik(\"Belajar Java\")                 = " + balik("Belajar Java"));
        System.out.println("balik(\"\")                             = \"" + balik("") + "\"");
        System.out.println("balik(\"a\")                            = " + balik("a"));
        System.out.println("balik(balik(\"Halo\")).equals(\"Halo\")  = " + balik(balik("Halo")).equals("Halo"));

        // --- Mengapa + di dalam loop panjang tidak efisien ---
        // Bagian deterministik: berapa karakter yang disalin?
        int n = 20_000;
        long salinanPlus = 0;               // tiap "s = s + x" menyalin seluruh isi s ke String baru
        for (int i = 1; i <= n; i++) salinanPlus += i;   // panjang s pada putaran ke-i adalah i
        System.out.println();
        System.out.println("Menyambung 1 karakter sebanyak " + n + " kali:");
        System.out.println("  dengan +             : sekitar " + String.format(java.util.Locale.US, "%,d", salinanPlus) + " karakter disalin  (~ n^2 / 2)");
        System.out.println("  dengan StringBuilder : sekitar " + String.format(java.util.Locale.US, "%,d", n) + " karakter ditulis  (~ n)");

        // Bagian pengukuran: waktu BERVARIASI antar mesin dan antar eksekusi
        long t0 = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) s += "x";
        long msPlus = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append("x");
        long msSb = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("  panjang hasil sama   : " + (s.length() == sb.length()));
        System.out.println("  [WAKTU, bervariasi]  : + = " + msPlus + " ms, StringBuilder = " + msSb + " ms");
    }
}
