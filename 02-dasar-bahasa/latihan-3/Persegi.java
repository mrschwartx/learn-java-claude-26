import java.util.Locale;
import java.util.Scanner;

// Latihan 3: luas, keliling, dan diagonal persegi panjang
public class Persegi {
    public static void main(String[] args) {
        // Locale.US: titik sebagai pemisah desimal, agar masukan "3.5" diterima di semua mesin
        Scanner in = new Scanner(System.in).useLocale(Locale.US);

        System.out.print("Panjang: ");
        double panjang = in.nextDouble();
        System.out.print("Lebar  : ");
        double lebar = in.nextDouble();
        in.close();

        double luas = panjang * lebar;
        double keliling = 2 * (panjang + lebar);
        double diagonal = Math.sqrt(panjang * panjang + lebar * lebar);   // Pythagoras

        // Locale.US pada format agar keluaran konsisten (titik desimal); hapus argumen itu
        // bila ingin mengikuti locale sistem Anda.
        System.out.println();
        System.out.println(String.format(Locale.US, "Luas     = %.2f", luas));
        System.out.println(String.format(Locale.US, "Keliling = %.2f", keliling));
        System.out.println(String.format(Locale.US, "Diagonal = %.2f", diagonal));
    }
}
