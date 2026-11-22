import java.util.Arrays;

// Latihan 3: nilai terbesar kedua, tanpa sorting (satu putaran) dan dengan Arrays.sort
public class TerbesarKedua {

    // Satu putaran, O(n), array asli tidak diubah. "Kedua" = terbesar kedua yang BERBEDA dari terbesar.
    // Mengembalikan null bila tidak ada (array berisi kurang dari 2 nilai berbeda).
    static Integer keduaTanpaSort(int[] a) {
        Integer terbesar = null, kedua = null;
        for (int x : a) {
            if (terbesar == null || x > terbesar) {
                kedua = terbesar;          // juara lama turun jadi runner-up
                terbesar = x;
            } else if (x != terbesar && (kedua == null || x > kedua)) {
                kedua = x;
            }
        }
        return kedua;
    }

    // Dengan sort: urutkan salinan, lalu jalan dari belakang melewati nilai yang sama dengan terbesar.
    static Integer keduaDenganSort(int[] a) {
        int[] salinan = a.clone();         // jangan merusak array asli
        Arrays.sort(salinan);              // O(n log n)
        for (int i = salinan.length - 2; i >= 0; i--) {
            if (salinan[i] != salinan[salinan.length - 1]) {
                return salinan[i];
            }
        }
        return null;
    }

    public static void main(String[] args) {
        int[][] uji = {
                {42, 17, 93, 8, 56, 12, 71, 30, 64, 25},     // 10 angka biasa
                {42, 17, 93, 8, 56, 93, 71, 30, 64, 25},     // terbesar muncul dua kali
                {-5, -12, -3, -40, -3, -9, -20, -7, -15, -8},// semua negatif (jebakan inisialisasi dengan 0)
                {7, 7, 7, 7, 7, 7, 7, 7, 7, 7}               // tidak ada nilai kedua
        };
        for (int[] a : uji) {
            System.out.println("Array         : " + Arrays.toString(a));
            System.out.println("  tanpa sort  : " + keduaTanpaSort(a));
            System.out.println("  dengan sort : " + keduaDenganSort(a));
            System.out.println("  array asli tetap: " + Arrays.toString(a).equals(Arrays.toString(a.clone())));
        }
    }
}
