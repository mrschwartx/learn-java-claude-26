import java.util.NoSuchElementException;
import java.util.Scanner;

// Latihan 1: ambilUmur(String) + loop yang meminta ulang sampai valid
public class Umur {

    static int ambilUmur(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("umur tidak boleh kosong");
        }
        String teks = input.strip();
        int umur;
        try {
            umur = Integer.parseInt(teks);
        } catch (NumberFormatException e) {
            // Ganti pesan teknis ("For input string: ...") dengan pesan yang jelas bagi pengguna,
            // tetapi simpan penyebab aslinya (chaining)
            throw new IllegalArgumentException("umur harus berupa bilangan bulat, bukan \"" + teks + "\"", e);
        }
        if (umur < 0 || umur > 150) {
            throw new IllegalArgumentException("umur harus di antara 0 dan 150, bukan " + umur);
        }
        return umur;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("Masukkan umur:");
            String baris;
            try {
                baris = in.nextLine();
            } catch (NoSuchElementException e) {          // masukan habis sebelum ada yang valid
                System.out.println("Masukan berakhir sebelum ada umur yang valid.");
                return;
            }
            try {
                int umur = ambilUmur(baris);
                System.out.println("Umur valid: " + umur);
                break;                                    // keluar dari loop dengan break, bukan dengan exception
            } catch (IllegalArgumentException e) {
                System.out.println("  Ditolak -> " + e.getMessage());
            }
        }
    }
}
