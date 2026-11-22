/**
 * Latihan 1.4 — Memperlihatkan efek JIT compiler (warm-up).
 *
 * Fungsi yang sama dipanggil berulang kali dan waktunya diukur per putaran.
 * Putaran awal berjalan di interpreter; setelah method "panas", JIT mengompilasinya
 * menjadi kode mesin sehingga putaran berikutnya jauh lebih cepat.
 *
 * Bandingkan:  java WarmUp.java        (JIT aktif, default)
 *              java -Xint WarmUp.java  (JIT dimatikan, interpreter murni)
 *
 * Catatan: angka berbeda antar mesin dan antar eksekusi (jumlah core CPU, beban sistem, dan
 * kapan thread kompilasi JIT selesai). Yang konsisten: putaran awal lebih lambat daripada putaran akhir.
 */
public class WarmUp {

    // Pekerjaan CPU murni: menjumlahkan hasil operasi aritmetika sederhana.
    static long kerja(int n) {
        long total = 0;
        for (int i = 0; i < n; i++) {
            total += (i % 7) * (i % 13) + (i ^ (i >>> 3));
        }
        return total;
    }

    public static void main(String[] args) {
        final int putaran = 20;
        final int beban = 5_000_000;
        long sink = 0;                                   // mencegah hasil dibuang oleh optimizer

        System.out.println("Putaran | Waktu (ms)");
        for (int r = 1; r <= putaran; r++) {
            long mulai = System.nanoTime();
            sink += kerja(beban);
            long ms = (System.nanoTime() - mulai) / 1_000_000;
            System.out.printf("%7d | %d%n", r, ms);
        }
        System.out.println("(checksum: " + sink + ")");
    }
}
