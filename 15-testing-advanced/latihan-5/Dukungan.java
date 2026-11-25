import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;

/** Pembantu: koneksi ke PostgreSQL sementara (pg-up.sh Modul 14), reset skema, dan klien HTTP. */
public class Dukungan {
    static final String URL = System.getProperty("pg.url", "jdbc:postgresql://127.0.0.1:54329/toko");

    static DataSource ds() throws Exception {
        Class.forName("MiniPg");
        return MiniPg.dataSource(URL, "belajar", "");
    }
    static void resetSkema(DataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            for (String perintah : Files.readString(Path.of("latihan-5/schema-produk.sql")).split(";")) if (!perintah.isBlank()) s.execute(perintah);
        }
    }
    static final HttpClient KLIEN = HttpClient.newHttpClient();
    static HttpResponse<String> post(String url, String json) throws Exception {
        return KLIEN.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
