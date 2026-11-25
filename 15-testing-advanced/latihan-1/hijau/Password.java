// Langkah HIJAU: implementasi minimal yang memenuhi test
public class Password {
    public static boolean kuat(String pw) {
        if (pw == null || pw.length() < 8) return false;
        boolean besar = false, kecil = false, angka = false;
        for (char c : pw.toCharArray()) {
            if (Character.isUpperCase(c)) besar = true;
            else if (Character.isLowerCase(c)) kecil = true;
            else if (Character.isDigit(c)) angka = true;
        }
        return besar && kecil && angka;
    }
}
