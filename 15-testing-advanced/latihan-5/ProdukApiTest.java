import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

/** Integration test: HTTP sungguhan -> aplikasi -> JDBC -> PostgreSQL sungguhan. (Padanan REST Assured + Testcontainers; lihat spring/.) */
public class ProdukApiTest {
    static ProdukApp app; static DataSource ds;

    @MiniUji.Uji void post_valid_201_location_dan_tersimpanDiDatabase() throws Exception {
        HttpResponse<String> r = Dukungan.post(app.url() + "/api/produk", "{\"nama\":\"Kopi Gayo\",\"harga\":25000}");
        MiniUji.Asersi.samaDengan(201, r.statusCode());
        String loc = r.headers().firstValue("Location").orElse("(tidak ada)");
        MiniUji.Asersi.benar(loc.startsWith("/api/produk/"), "Location: " + loc);
        MiniUji.Asersi.benar(r.body().contains("\"nama\":\"Kopi Gayo\"") && r.body().contains("\"harga\":25000"), "body: " + r.body());
        long id = Long.parseLong(loc.substring("/api/produk/".length()));
        try (Connection c = ds.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT nama, harga FROM produk WHERE id = " + id)) {
            MiniUji.Asersi.benar(rs.next(), "baris dengan id " + id + " harus ada di database");
            MiniUji.Asersi.samaDengan("Kopi Gayo", rs.getString(1));
            MiniUji.Asersi.samaDengan(25000L, rs.getLong(2));
        }
    }

    @MiniUji.Kasus({ "{\"nama\":\"\",\"harga\":-5}|2", "{\"nama\":\"\",\"harga\":100}|1", "{\"nama\":\"Teh\",\"harga\":0}|1", "{\"harga\":100}|1" })
    void post_tidakValid_400_dan_tidakAdaBarisBaru(String json, int jumlahGalat) throws Exception {
        long sebelum = hitung();
        HttpResponse<String> r = Dukungan.post(app.url() + "/api/produk", json);
        MiniUji.Asersi.samaDengan(400, r.statusCode());
        int n = r.body().split("\"[a-z]+: ", -1).length - 1;
        MiniUji.Asersi.samaDengan(jumlahGalat, n);
        MiniUji.Asersi.samaDengan(sebelum, hitung());
    }

    @MiniUji.Uji void get_tidakDidukung_405() throws Exception {
        var r = Dukungan.KLIEN.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(app.url() + "/api/produk")).GET().build(), HttpResponse.BodyHandlers.ofString());
        MiniUji.Asersi.samaDengan(405, r.statusCode());
    }

    @MiniUji.Uji void skema_menolakHargaNegatif_langsungDiDatabase() throws Exception {      // CHECK constraint sebagai jaring pengaman kedua
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            try { s.execute("INSERT INTO produk(nama, harga) VALUES ('x', -1)"); }
            catch (java.sql.SQLException diharapkan) { return; }
            throw new AssertionError("database seharusnya menolak harga negatif (CHECK constraint)");
        }
    }

    static long hitung() throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT count(*) FROM produk")) { rs.next(); return rs.getLong(1); }
    }

    public static void main(String[] a) throws Exception {
        ds = Dukungan.ds(); Dukungan.resetSkema(ds); app = new ProdukApp(ds).mulai();
        try { int[] h = MiniUji.jalankan(true, ProdukApiTest.class); if (h[1] > 0) System.exit(1); }
        finally { app.henti(); }
    }
}
