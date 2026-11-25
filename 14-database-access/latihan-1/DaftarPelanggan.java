import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import javax.sql.DataSource;

// Latihan 1: pelanggan + jumlah pesanan + total belanja (JOIN + GROUP BY); PreparedStatement vs konkatenasi (SQL injection)
public class DaftarPelanggan {
    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");   // HANYA untuk driver mainan sandbox; dengan org.postgresql:postgresql baris ini tidak perlu
        DataSource ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");

        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO pelanggan (nama, kota) VALUES (?, ?)")) {
            ps.setString(1, "Eka"); ps.setString(2, "Medan"); ps.executeUpdate();     // pelanggan TANPA pesanan, untuk menguji LEFT JOIN
        }

        System.out.println("== 1. Pelanggan, jumlah pesanan, total belanja ==");
        String sql = """
                SELECT p.nama, p.kota, COUNT(o.id) AS jumlah, COALESCE(SUM(o.total), 0) AS total
                FROM pelanggan p LEFT JOIN pesanan o ON o.pelanggan_id = p.id
                GROUP BY p.id, p.nama, p.kota
                ORDER BY total DESC, p.nama
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(String.format(Locale.US, "  %-8s %-8s %d pesanan  Rp%,d", rs.getString("nama"), rs.getString("kota"), rs.getLong("jumlah"), rs.getLong("total")));
            }
        }
        System.out.println("  (Eka tanpa pesanan tetap tampil karena LEFT JOIN; INNER JOIN akan menghilangkannya)");

        System.out.println();
        System.out.println("== 2. Per kota dengan parameter (PreparedStatement) ==");
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT p.nama, o.total FROM pelanggan p JOIN pesanan o ON o.pelanggan_id = p.id WHERE p.kota = ? ORDER BY o.total DESC")) {
            ps.setString(1, "Bandung");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) System.out.println(String.format(Locale.US, "  %-8s Rp%,d", rs.getString("nama"), rs.getLong("total")));
            }
        }

        System.out.println();
        System.out.println("== 3. SQL injection: input berbahaya  x' OR '1'='1 ==");
        String masukan = "x' OR '1'='1";
        // BERBAHAYA: konkatenasi -> input menjadi BAGIAN dari SQL
        String rakitan = "SELECT nama FROM pelanggan WHERE nama = '" + masukan + "'";
        System.out.println("  SQL hasil rakitan : " + rakitan);
        System.out.println("  Konkatenasi mengembalikan : " + hitung(ds, rakitan, null) + " baris (SEMUA pelanggan bocor)");
        // AMAN: nilai dikirim sebagai parameter, bukan bagian dari SQL
        System.out.println("  PreparedStatement mengembalikan : " + hitung(ds, "SELECT nama FROM pelanggan WHERE nama = ?", masukan) + " baris (dicari literal nama itu)");

        // Injeksi yang merusak: stacked query lewat Statement (simple protocol) -- diuji pada tabel cadangan agar data aman
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS korban; CREATE TABLE korban(id int); INSERT INTO korban VALUES (1)");
            String jahat = "1; DROP TABLE korban; --";
            st.executeUpdate("DELETE FROM korban WHERE id = " + jahat);                       // BURUK
        }
        System.out.println("  Konkatenasi dengan '1; DROP TABLE korban; --' -> tabel korban masih ada? " + adaTabel(ds, "korban"));
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("CREATE TABLE IF NOT EXISTS korban(id int)")) { ps.executeUpdate(); ps.close();
            try (PreparedStatement p2 = c.prepareStatement("DELETE FROM korban WHERE id = ?")) {
                try { p2.setString(1, "1; DROP TABLE korban; --"); p2.executeUpdate(); } catch (SQLException e) {
                    System.out.println("  PreparedStatement dengan nilai yang sama -> ditolak server (nilai bukan bilangan bulat) (SQLState " + e.getSQLState() + "), tidak ada yang dieksekusi");
                }
            }
            try (Statement st = c.createStatement()) { st.executeUpdate("DROP TABLE IF EXISTS korban"); }
        }
    }

    static int hitung(DataSource ds, String sql, String param) throws SQLException {
        int n = 0;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) n++; }
        }
        return n;
    }

    static boolean adaTabel(DataSource ds, String nama) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?")) {
            ps.setString(1, nama);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getLong(1) > 0; }
        }
    }
}
