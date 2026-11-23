import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

// Latihan 1: daftar tanpa duplikat (urutan masuk dipertahankan) dan daftar terurut tanpa duplikat
public class Unik {
    public static void main(String[] args) {
        // Masukan: satu baris bilangan dipisah spasi
        Scanner in = new Scanner(System.in);
        String[] token = in.nextLine().trim().split("\\s+");
        in.close();

        List<Integer> masukan = new ArrayList<>();
        for (String t : token) masukan.add(Integer.parseInt(t));
        System.out.println("Masukan                    : " + masukan);

        // (a) Unik + urutan masuk dipertahankan -> LinkedHashSet
        Set<Integer> unikUrutMasuk = new LinkedHashSet<>(masukan);
        System.out.println("(a) LinkedHashSet          : " + unikUrutMasuk);

        // (b) Unik + terurut naik -> TreeSet
        Set<Integer> unikTerurut = new TreeSet<>(masukan);
        System.out.println("(b) TreeSet                : " + unikTerurut);

        // Pembanding: HashSet menghapus duplikat tetapi TIDAK menjamin urutan apa pun
        Set<Integer> unikHash = new HashSet<>(masukan);
        System.out.println("(pembanding) HashSet       : " + unikHash + "   (urutan tidak dijamin)");

        // Hasil akhir sebagai List bila diperlukan
        List<Integer> hasilA = new ArrayList<>(unikUrutMasuk);
        List<Integer> hasilB = new ArrayList<>(unikTerurut);
        System.out.println("Sebagai List (a)           : " + hasilA);
        System.out.println("Sebagai List (b)           : " + hasilB);
        System.out.println("Duplikat dibuang           : " + (masukan.size() - unikUrutMasuk.size()) + " elemen");

        // Bonus: add() mengembalikan false untuk duplikat -> cara mencatat apa yang duplikat
        Set<Integer> lihat = new LinkedHashSet<>();
        List<Integer> duplikat = new ArrayList<>();
        for (int x : masukan) {
            if (!lihat.add(x)) duplikat.add(x);
        }
        System.out.println("Nilai yang berulang        : " + duplikat);
    }
}
