import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

// Latihan 5: 20 pelanggan x 3 pesanan; hitung jumlah query untuk pola N+1 vs satu JOIN, memakai penghitung statement driver.
// Ini mereplikasi POLA yang dihasilkan lazy loading Hibernate, bukan Hibernate itu sendiri.
public class NPlusSatu {
    static DataSource ds;
    record Pel(long id, String nama) {}

    public static void main(String[] args) throws Exception {
        Class.forName("MiniPg");
        ds = MiniPg.dataSource("jdbc:postgresql://127.0.0.1:54329/toko", "belajar", "rahasia");
        siapkan();

        MiniPg.STATEMEN.set(0);
        long totalN = totalNPlusSatu("Surabaya");
        int queryN = MiniPg.STATEMEN.get();

        MiniPg.STATEMEN.set(0);
        long totalJoin = totalJoin("Surabaya");
        int queryJ = MiniPg.STATEMEN.get();

        MiniPg.STATEMEN.set(0);
        long totalBatch = totalBatchFetch("Surabaya", 20);
        int queryB = MiniPg.STATEMEN.get();

        System.out.println("Data: 20 pelanggan di Surabaya, masing-masing 3 pesanan (60 pesanan)");
        System.out.println();
        System.out.println("N+1 (1 query pelanggan + 1 query pesanan per pelanggan) : total=" + totalN + ", query=" + queryN);
        System.out.println("JOIN FETCH / EntityGraph (1 query join)                 : total=" + totalJoin + ", query=" + queryJ);
        System.out.println("Batch fetching (pesanan WHERE pelanggan_id = ANY(...), 20 id per query): total=" + totalBatch + ", query=" + queryB);
        System.out.println("Ketiganya menghasilkan total sama? " + (totalN == totalJoin && totalJoin == totalBatch));
        System.out.println();
        System.out.println("Dengan 1.000 pelanggan N+1 menjadi 1.001 query; JOIN tetap 1; batch size 20 menjadi 1 + 50 = 51 query.");
    }

    static void siapkan() throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("INSERT INTO pelanggan (nama, kota) VALUES (?, 'Surabaya') RETURNING id")) {
                for (int i = 1; i <= 20; i++) {
                    p.setString(1, "Pel" + String.format("%02d", i));
                    long id;
                    try (ResultSet rs = p.executeQuery()) { rs.next(); id = rs.getLong(1); }
                    try (PreparedStatement o = c.prepareStatement("INSERT INTO pesanan (pelanggan_id, total) VALUES (?, ?)")) {
                        for (int k = 1; k <= 3; k++) { o.setLong(1, id); o.setLong(2, 10_000L * k); o.executeUpdate(); }
                    }
                }
            }
            c.commit();
        }
    }

    static long totalNPlusSatu(String kota) throws SQLException {
        long total = 0;
        try (Connection c = ds.getConnection()) {
            List<Pel> pel = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, nama FROM pelanggan WHERE kota = ? ORDER BY nama")) {   // query #1
                ps.setString(1, kota);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) pel.add(new Pel(rs.getLong("id"), rs.getString("nama"))); }
            }
            for (Pel p : pel) {                                                                                                // +N query
                try (PreparedStatement ps = c.prepareStatement("SELECT total FROM pesanan WHERE pelanggan_id = ?")) {
                    ps.setLong(1, p.id());
                    try (ResultSet rs = ps.executeQuery()) { while (rs.next()) total += rs.getLong("total"); }
                }
            }
        }
        return total;
    }

    static long totalJoin(String kota) throws SQLException {
        long total = 0;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT p.id, o.total FROM pelanggan p LEFT JOIN pesanan o ON o.pelanggan_id = p.id WHERE p.kota = ?")) {
            ps.setString(1, kota);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) total += rs.getLong("total"); }
        }
        return total;
    }

    static long totalBatchFetch(String kota, int ukuran) throws SQLException {
        long total = 0;
        try (Connection c = ds.getConnection()) {
            List<Long> ids = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM pelanggan WHERE kota = ?")) {
                ps.setString(1, kota);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getLong("id")); }
            }
            for (int i = 0; i < ids.size(); i += ukuran) {
                List<Long> potong = ids.subList(i, Math.min(i + ukuran, ids.size()));
                String in = String.join(",", potong.stream().map(x -> "?").toList());
                try (PreparedStatement ps = c.prepareStatement("SELECT total FROM pesanan WHERE pelanggan_id IN (" + in + ")")) {
                    for (int k = 0; k < potong.size(); k++) ps.setLong(k + 1, potong.get(k));
                    try (ResultSet rs = ps.executeQuery()) { while (rs.next()) total += rs.getLong("total"); }
                }
            }
        }
        return total;
    }
}
