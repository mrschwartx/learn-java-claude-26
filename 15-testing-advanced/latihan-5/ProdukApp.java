import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 * PENGGANTI aplikasi Spring Boot Modul 13 (Spring tidak tersedia di sandbox): endpoint POST /api/produk dengan kontrak yang sama
 * (201 + Location + JSON; 400 bila nama kosong / harga tidak positif) dan database PostgreSQL SUNGGUHAN lewat JDBC.
 */
public class ProdukApp {
    private final HttpServer server;
    private final DataSource ds;

    public ProdukApp(DataSource ds) throws IOException {
        this.ds = ds;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.createContext("/api/produk", this::tangani);
    }
    public ProdukApp mulai() { server.start(); return this; }
    public void henti() { server.stop(0); ((java.util.concurrent.ExecutorService) server.getExecutor()).shutdownNow(); }
    public String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    private static final Pattern NAMA = Pattern.compile("\"nama\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern HARGA = Pattern.compile("\"harga\"\\s*:\\s*(-?\\d+)");

    private void tangani(HttpExchange ex) throws IOException {
        try {
            if (!ex.getRequestMethod().equals("POST")) { kirim(ex, 405, "{\"galat\":\"metode tidak didukung\"}", null); return; }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Matcher mn = NAMA.matcher(body), mh = HARGA.matcher(body);
            String nama = mn.find() ? mn.group(1) : "";
            long harga = mh.find() ? Long.parseLong(mh.group(1)) : 0;
            StringBuilder galat = new StringBuilder();
            if (nama.isBlank()) galat.append("\"nama: nama wajib diisi\"");
            if (harga <= 0) galat.append(galat.length() > 0 ? "," : "").append("\"harga: harga harus positif\"");
            if (galat.length() > 0) { kirim(ex, 400, "{\"detail\":\"Data tidak valid\",\"galat\":[" + galat + "]}", null); return; }
            try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO produk(nama, harga) VALUES (?, ?) RETURNING id")) {
                ps.setString(1, nama); ps.setLong(2, harga);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next(); long id = rs.getLong(1);
                    kirim(ex, 201, "{\"id\":" + id + ",\"nama\":\"" + nama + "\",\"harga\":" + harga + "}", "/api/produk/" + id);
                }
            }
        } catch (SQLException | RuntimeException e) {
            kirim(ex, 500, "{\"galat\":\"" + e.getClass().getSimpleName() + "\"}", null);
        }
    }

    private static void kirim(HttpExchange ex, int kode, String json, String lokasi) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        if (lokasi != null) ex.getResponseHeaders().set("Location", lokasi);
        ex.sendResponseHeaders(kode, b.length);
        try (var o = ex.getResponseBody()) { o.write(b); }
    }
}
