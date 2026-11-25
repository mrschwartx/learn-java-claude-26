import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

// Latihan 4 (bagian yang dapat dijalankan): MEKANISME di balik @Version dan pessimistic locking, langsung di JDBC.
// Ini bukan JPA/Hibernate; ini SQL yang secara konsep dihasilkan @Version (UPDATE ... WHERE version = ?).
public class LockingDemo {
    static DataSource ds;

    static class KonflikVersi extends RuntimeException { KonflikVersi(String m) { super(m); } }

    record Baca(long id, String nama, int versi) {}

    static Baca baca(Connection c, long id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id, nama, version FROM pelanggan_v WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return new Baca(rs.getLong("id"), rs.getString("nama"), rs.getInt("version")); }
        }
    }

    /** Optimistic locking manual: UPDATE hanya jika versi masih sama; 0 baris terubah = ada yang mendahului. */
    static void ubahNama(Connection c, Baca dibaca, String namaBaru) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE pelanggan_v SET nama = ?, version = version + 1 WHERE id = ? AND version = ?")) {
            ps.setString(1, namaBaru); ps.setLong(2, dibaca.id()); ps.setInt(3, dibaca.versi());
            if (ps.executeUpdate() == 0) throw new KonflikVersi("data sudah diubah transaksi lain (versi dibaca=" + dibaca.versi() + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");
        ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
                "CREATE TABLE pelanggan_v (id BIGINT PRIMARY KEY, nama VARCHAR(100) NOT NULL, version INT NOT NULL DEFAULT 0)")) { ps.executeUpdate(); }
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO pelanggan_v (id, nama) VALUES (1, 'Ani')")) { ps.executeUpdate(); }

        System.out.println("== A. Optimistic locking: dua transaksi membaca pelanggan yang sama (versi 0) ==");
        try (Connection t1 = ds.getConnection(); Connection t2 = ds.getConnection()) {
            t1.setAutoCommit(false); t2.setAutoCommit(false);
            Baca b1 = baca(t1, 1), b2 = baca(t2, 1);
            System.out.println("  T1 dan T2 sama-sama membaca: " + b1);

            ubahNama(t1, b1, "Ani Lestari"); t1.commit();
            System.out.println("  T1 mengubah nama -> commit sukses (versi menjadi 1)");

            try { ubahNama(t2, b2, "Ani Wijaya"); t2.commit(); System.out.println("  T2 sukses (TIDAK diharapkan)"); }
            catch (KonflikVersi e) { t2.rollback(); System.out.println("  T2 GAGAL: " + e.getMessage() + "  <- analog OptimisticLockException"); }
        }
        try (Connection c = ds.getConnection()) { System.out.println("  Hasil akhir: " + baca(c, 1) + "  (perubahan T1 tidak tertimpa)"); }

        System.out.println();
        System.out.println("== B. Tanpa versi (last-write-wins): update buta menimpa diam-diam ==");
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE pelanggan_v SET nama = ? WHERE id = 1")) {
            ps.setString(1, "Ani Wijaya"); ps.executeUpdate();
        }
        try (Connection c = ds.getConnection()) { System.out.println("  Nama sekarang: " + baca(c, 1).nama() + "  (perubahan T1 'Ani Lestari' hilang tanpa error)"); }

        System.out.println();
        System.out.println("== C. Pessimistic locking: SELECT ... FOR UPDATE menahan baris ==");
        try (Connection t1 = ds.getConnection(); Connection t2 = ds.getConnection()) {
            t1.setAutoCommit(false); t2.setAutoCommit(false);
            try (PreparedStatement ps = t1.prepareStatement("SELECT id FROM pelanggan_v WHERE id = ? FOR UPDATE")) { ps.setLong(1, 1); ps.executeQuery(); }
            System.out.println("  T1 mengunci baris id=1 (FOR UPDATE)");
            try (PreparedStatement ps = t2.prepareStatement("SELECT id FROM pelanggan_v WHERE id = ? FOR UPDATE NOWAIT")) {
                ps.setLong(1, 1); ps.executeQuery();
                System.out.println("  T2 berhasil mengunci (TIDAK diharapkan)");
            } catch (SQLException e) {
                t2.rollback();
                System.out.println("  T2 (NOWAIT) langsung ditolak: SQLState " + e.getSQLState() + " - " + e.getMessage());
            }
            t1.commit();
            try (PreparedStatement ps = t2.prepareStatement("SELECT id FROM pelanggan_v WHERE id = ? FOR UPDATE NOWAIT")) { ps.setLong(1, 1); ps.executeQuery(); }
            t2.commit();
            System.out.println("  Setelah T1 commit (kunci dilepas), T2 berhasil mengunci");
        }
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DROP TABLE pelanggan_v")) { ps.executeUpdate(); }
    }
}
