import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import javax.sql.DataSource;

// Latihan 6 (bagian SQL, DIJALANKAN di PostgreSQL asli): (a) ALTER TABLE kolom email, (b) query "total pesanan > 100.000"
// Ebean sendiri TIDAK dijalankan (butuh Maven Central); ini membuktikan SQL dan skema yang akan dipetakan Ebean.
public class ColumnDanQuery {
    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");
        DataSource ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");
        try (Connection c = ds.getConnection()) {
            System.out.println("== (a) ALTER TABLE + isi email ==");
            try (Statement st = c.createStatement()) { st.executeUpdate("ALTER TABLE pelanggan ADD COLUMN email VARCHAR(150)"); }
            try (PreparedStatement ps = c.prepareStatement("UPDATE pelanggan SET email = ? WHERE nama = ?")) {
                ps.setString(1, "ani@contoh.id"); ps.setString(2, "Ani"); System.out.println("  baris diubah: " + ps.executeUpdate());
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT nama, email FROM pelanggan ORDER BY id"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) System.out.println("  " + rs.getString("nama") + " -> " + rs.getString("email"));
            }

            System.out.println();
            System.out.println("== (b) Pelanggan dengan TOTAL pesanan > 100.000 (SQL setara dengan agregasi Ebean) ==");
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT p.nama, SUM(o.total) AS total FROM pelanggan p JOIN pesanan o ON o.pelanggan_id = p.id "
                            + "GROUP BY p.nama HAVING SUM(o.total) > ? ORDER BY p.nama")) {
                ps.setLong(1, 100_000);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) System.out.println(String.format(Locale.US, "  %-8s Rp%,d", rs.getString("nama"), rs.getLong("total"))); }
            }
            System.out.println();
            System.out.println("== Pesanan tunggal > 100.000 (setara  new QPesanan().total.gt(100_000)  ) ==");
            try (PreparedStatement ps = c.prepareStatement("SELECT p.nama, o.total FROM pesanan o JOIN pelanggan p ON p.id = o.pelanggan_id WHERE o.total > ? ORDER BY p.nama")) {
                ps.setLong(1, 100_000);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) System.out.println(String.format(Locale.US, "  %-8s Rp%,d", rs.getString("nama"), rs.getLong("total"))); }
            }
        }
    }
}
