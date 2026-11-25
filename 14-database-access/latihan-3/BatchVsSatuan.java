import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;

// Latihan 3: 1.000 INSERT: (a) executeUpdate satu-satu dalam autocommit, (b) satu-satu dalam SATU transaksi, (c) addBatch/executeBatch
public class BatchVsSatuan {
    static final int N = 1000;
    static DataSource ds;

    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");
        ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");
        String sql = "INSERT INTO pelanggan (nama, kota) VALUES (?, ?)";

        // pemanasan (JIT + koneksi) pada baris yang kemudian dihapus
        satuSatu(sql, 50, true, "pemanasan"); hapus("pemanasan");

        long a = satuSatu(sql, N, true, "autocommit");
        System.out.println("(a) 1.000 executeUpdate, autocommit : " + hitung("autocommit") + " baris tersimpan");
        long b = satuSatu(sql, N, false, "satutxn");
        System.out.println("(b) 1.000 executeUpdate, satu transaksi: " + hitung("satutxn") + " baris tersimpan");
        long c = batch(sql, N, "batch");
        System.out.println("(c) addBatch + executeBatch (1 transaksi): " + hitung("batch") + " baris tersimpan");

        System.out.println();
        System.out.println(String.format(Locale.US, "[BERVARIASI] (a) autocommit satu-satu : %6d ms", a));
        System.out.println(String.format(Locale.US, "[BERVARIASI] (b) satu transaksi      : %6d ms  (%.1fx lebih cepat dari a)", b, (double) a / b));
        System.out.println(String.format(Locale.US, "[BERVARIASI] (c) batch               : %6d ms  (%.1fx lebih cepat dari a)", c, (double) a / c));
        System.out.println("[BERVARIASI] a lebih lambat dari b dan c? " + (a > b && a > c));
    }

    static long satuSatu(String sql, int n, boolean autocommit, String kota) throws SQLException {
        long t0 = System.nanoTime();
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(autocommit);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < n; i++) { ps.setString(1, "P" + i); ps.setString(2, kota); ps.executeUpdate(); }
            }
            if (!autocommit) c.commit();
        }
        return (System.nanoTime() - t0) / 1_000_000;
    }

    static long batch(String sql, int n, String kota) throws SQLException {
        long t0 = System.nanoTime();
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < n; i++) { ps.setString(1, "P" + i); ps.setString(2, kota); ps.addBatch(); }
                int[] hasil = ps.executeBatch();
                if (hasil.length != n) throw new IllegalStateException("jumlah hasil batch " + hasil.length);
            }
            c.commit();
        }
        return (System.nanoTime() - t0) / 1_000_000;
    }

    static long hitung(String kota) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM pelanggan WHERE kota = ?")) {
            ps.setString(1, kota);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getLong(1); }
        }
    }

    static void hapus(String kota) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM pelanggan WHERE kota = ?")) { ps.setString(1, kota); ps.executeUpdate(); }
    }
}
