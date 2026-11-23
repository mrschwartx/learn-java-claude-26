import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Latihan 4: baca file baris-per-baris (BufferedReader), hitung baris dan kata terpanjang, tulis ringkasan.
// Pemakaian: java Ringkas.java <file-masuk> <file-ringkasan>
//            java Ringkas.java --besar          (membuat file 300.000 baris sementara, lalu memprosesnya)
public class Ringkas {

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--besar")) {
            Path dir = Files.createTempDirectory("ringkas");
            Path besar = dir.resolve("besar.txt");
            buatFileBesar(besar, 300_000);
            System.out.println("File besar dibuat: " + Files.size(besar) + " byte");
            Path ringkasan = dir.resolve("ringkasan.txt");
            proses(besar, ringkasan);
            tampilkan(ringkasan);
            Files.delete(besar);
            Files.delete(ringkasan);
            Files.delete(dir);
            return;
        }
        Path masuk = Path.of(args.length > 0 ? args[0] : "contoh.txt");
        Path keluar = Path.of(args.length > 1 ? args[1] : "ringkasan.txt");
        proses(masuk, keluar);
        tampilkan(keluar);
    }

    static void proses(Path masuk, Path keluar) throws IOException {
        long jumlahBaris = 0;
        long jumlahKata = 0;
        String terpanjang = "";
        long barisTerpanjang = 0;

        // Hanya SATU baris berada di memori pada satu waktu
        try (BufferedReader in = Files.newBufferedReader(masuk, StandardCharsets.UTF_8)) {
            String baris;
            while ((baris = in.readLine()) != null) {
                jumlahBaris++;
                for (String kata : baris.split("[^\\p{L}\\p{N}]+")) {    // pisah di semua karakter bukan huruf/angka
                    if (kata.isEmpty()) continue;
                    jumlahKata++;
                    if (kata.length() > terpanjang.length()) {           // '>' : bila seri, yang pertama menang
                        terpanjang = kata;
                        barisTerpanjang = jumlahBaris;
                    }
                }
            }
        }

        try (BufferedWriter out = Files.newBufferedWriter(keluar, StandardCharsets.UTF_8)) {
            out.write("jumlah baris   : " + jumlahBaris + "\n");          // "\n" eksplisit agar sama di semua OS
            out.write("jumlah kata    : " + jumlahKata + "\n");
            out.write("kata terpanjang: " + terpanjang + " (" + terpanjang.length() + " huruf, baris " + barisTerpanjang + ")\n");
        }
    }

    static void tampilkan(Path ringkasan) throws IOException {
        System.out.println("Isi ringkasan:");
        for (String b : Files.readAllLines(ringkasan, StandardCharsets.UTF_8)) {
            System.out.println("  " + b);
        }
    }

    // Isi deterministik: baris ke-i berisi 8 kata yang diturunkan dari i
    static void buatFileBesar(Path p, int baris) throws IOException {
        String[] kamus = { "java", "belajar", "koleksi", "pengecualian", "antrean", "pemrograman", "dasar", "contoh" };
        try (BufferedWriter out = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= baris; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 8; j++) {
                    sb.append(kamus[(i + j * 3) % kamus.length]).append(' ');
                }
                if (i == 250_000) sb.append("internasionalisasi");        // satu kata yang jelas terpanjang
                out.write(sb.toString().stripTrailing());
                out.write('\n');
            }
        }
    }
}
