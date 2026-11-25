import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

// Latihan 2: transfer + riwayat dalam SATU transaksi; kegagalan disimulasikan setelah UPDATE pertama
public class TransferAman {
    static DataSource ds;

    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");
        ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");

        tampil("Awal");

        System.out.println();
        System.out.println("== A. Transfer sukses: Andi -> Budi 150.000 (saldo + riwayat dalam satu transaksi) ==");
        transfer(1, 2, 150_000, false, true);
        tampil("Sesudah A");

        System.out.println();
        System.out.println("== B. Saldo tidak cukup: Budi -> Andi 9.999.999 ==");
        try { transfer(2, 1, 9_999_999, false, true); } catch (IllegalStateException e) { System.out.println("  Dibatalkan: " + e.getMessage()); }
        tampil("Sesudah B (tidak berubah)");

        System.out.println();
        System.out.println("== C. Kegagalan SETELAH UPDATE pertama, DENGAN transaksi -> rollback ==");
        try { transfer(1, 2, 50_000, true, true); } catch (RuntimeException e) { System.out.println("  Dibatalkan: " + e.getMessage()); }
        tampil("Sesudah C (tidak berubah: UPDATE pertama dibatalkan)");

        System.out.println();
        System.out.println("== D. Kegagalan yang sama TANPA transaksi (autocommit) -> uang hilang ==");
        try { transfer(1, 2, 50_000, true, false); } catch (RuntimeException e) { System.out.println("  Gagal: " + e.getMessage()); }
        tampil("Sesudah D (Andi berkurang 50.000, Budi tidak bertambah: data RUSAK)");
    }

    /** gagalSetelahUpdatePertama: lempar exception setelah UPDATE pertama; denganTransaksi=false -> autocommit. */
    static void transfer(long dari, long ke, long jumlah, boolean gagalSetelahUpdatePertama, boolean denganTransaksi) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(!denganTransaksi);
            try {
                try (PreparedStatement kurangi = c.prepareStatement("UPDATE rekening SET saldo = saldo - ? WHERE id = ? AND saldo >= ?")) {
                    kurangi.setLong(1, jumlah); kurangi.setLong(2, dari); kurangi.setLong(3, jumlah);
                    if (kurangi.executeUpdate() == 0) throw new IllegalStateException("saldo rekening " + dari + " tidak cukup");
                }
                if (gagalSetelahUpdatePertama) throw new RuntimeException("kegagalan simulasi setelah UPDATE pertama");
                try (PreparedStatement tambah = c.prepareStatement("UPDATE rekening SET saldo = saldo + ? WHERE id = ?")) {
                    tambah.setLong(1, jumlah); tambah.setLong(2, ke); tambah.executeUpdate();
                }
                try (PreparedStatement riwayat = c.prepareStatement("INSERT INTO riwayat_transfer (dari, ke, jumlah) VALUES (?, ?, ?)")) {
                    riwayat.setLong(1, dari); riwayat.setLong(2, ke); riwayat.setLong(3, jumlah); riwayat.executeUpdate();
                }
                if (denganTransaksi) c.commit();
            } catch (SQLException | RuntimeException e) {
                if (denganTransaksi) c.rollback();
                throw e;
            }
        }
    }

    static void tampil(String judul) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT pemilik, saldo FROM rekening ORDER BY id");
             PreparedStatement pr = c.prepareStatement("SELECT COUNT(*) FROM riwayat_transfer")) {
            StringBuilder sb = new StringBuilder("  [" + judul + "] ");
            long total = 0;
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { sb.append(rs.getString("pemilik")).append('=').append(rs.getLong("saldo")).append("  "); total += rs.getLong("saldo"); } }
            try (ResultSet rs = pr.executeQuery()) { rs.next(); sb.append("| total=").append(total).append(" | baris riwayat=").append(rs.getLong(1)); }
            System.out.println(sb);
        }
    }
}
