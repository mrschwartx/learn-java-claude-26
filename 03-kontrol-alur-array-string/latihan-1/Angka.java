import java.util.Scanner;

// Latihan 1: ganjil/genap (if-else), positif/negatif/nol (switch expression), kelipatan 5
public class Angka {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Masukkan sebuah bilangan bulat: ");
        int n = in.nextInt();
        in.close();

        // 1) if-else: ganjil / genap.
        //    Pakai n % 2 != 0 (bukan n % 2 == 1): untuk n negatif ganjil, -7 % 2 adalah -1, bukan 1.
        String paritas;
        if (n % 2 == 0) {
            paritas = "genap";
        } else {
            paritas = "ganjil";
        }

        // 2) switch expression: positif / negatif / nol, memakai Integer.signum (-1, 0, 1)
        String tanda = switch (Integer.signum(n)) {
            case 1  -> "positif";
            case -1 -> "negatif";
            default -> "nol";
        };

        // 3) kelipatan 5 (0 juga kelipatan 5, karena 0 % 5 == 0)
        boolean kelipatanLima = n % 5 == 0;

        System.out.println(n + " adalah bilangan " + paritas + ", " + tanda + ", "
                + (kelipatanLima ? "kelipatan 5" : "bukan kelipatan 5"));
    }
}
