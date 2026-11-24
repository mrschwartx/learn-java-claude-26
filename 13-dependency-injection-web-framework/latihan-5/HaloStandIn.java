import com.sun.net.httpserver.HttpServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HaloStandIn {
    record Produk(long id, String nama) { String json() { return "{\"id\":" + id + ",\"nama\":\"" + nama + "\"}"; } }
    static final Map<Long, Produk> DATA = Map.of(1L, new Produk(1, "Kopi"), 2L, new Produk(2, "Teh"));

    static void kirim(com.sun.net.httpserver.HttpExchange ex, int kode, String isi) throws java.io.IOException {
        byte[] b = isi.getBytes(StandardCharsets.UTF_8);
        if (b.length == 0) ex.sendResponseHeaders(kode, -1); else ex.sendResponseHeaders(kode, b.length);
        if (b.length > 0) ex.getResponseBody().write(b);
        ex.close();
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        HttpServer srv = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        srv.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        srv.createContext("/halo", ex -> kirim(ex, 200, "Halo dari JDK HttpServer"));
        srv.createContext("/produk/", ex -> {
            try {
                Produk p = DATA.get(Long.parseLong(ex.getRequestURI().getPath().substring("/produk/".length())));
                if (p == null) kirim(ex, 404, ""); else kirim(ex, 200, p.json());
            } catch (NumberFormatException e) { kirim(ex, 400, "ID harus berupa angka"); }
        });
        srv.start();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        String base = "http://localhost:" + srv.getAddress().getPort();
        HttpClient c = HttpClient.newHttpClient();
        for (String p : new String[] { "/halo", "/produk/1", "/produk/99", "/produk/abc" }) {
            HttpResponse<String> r = c.send(HttpRequest.newBuilder(URI.create(base + p)).build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("GET " + p + " -> " + r.statusCode() + (r.body().isEmpty() ? "" : " " + r.body()));
        }
        System.out.println("[BERVARIASI] waktu membuat+menyalakan server di dalam JVM yang sudah hidup: " + ms + " ms");
        srv.stop(0);
    }
}
