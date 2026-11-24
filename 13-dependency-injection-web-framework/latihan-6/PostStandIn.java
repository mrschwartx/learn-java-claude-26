import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BUKAN Javalin. Stand-in JDK yang meniru kontrak POST /produk dari javalin-post dan memperlihatkan pola DI manual:
 * service di-INJEKSI lewat constructor sehingga dapat diganti fake pada uji.
 */
public class PostStandIn {
    record Produk(long id, String nama) { String json() { return "{\"id\":" + id + ",\"nama\":\"" + nama + "\"}"; } }

    interface ProdukService { Produk buat(String nama); }

    static class ServiceMemori implements ProdukService {
        final List<Produk> data = new CopyOnWriteArrayList<>();
        final AtomicLong urutan = new AtomicLong();
        public Produk buat(String nama) { Produk p = new Produk(urutan.incrementAndGet(), nama); data.add(p); return p; }
    }

    static final Pattern NAMA = Pattern.compile("\"nama\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    static class App {
        final ProdukService service;
        App(ProdukService service) { this.service = service; }                    // constructor injection manual

        HttpServer mulai() throws IOException {
            HttpServer srv = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            srv.createContext("/produk", ex -> {
                if (!ex.getRequestMethod().equals("POST")) { kirim(ex, 405, ""); return; }
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Matcher m = NAMA.matcher(body);
                String nama = m.find() ? m.group(1) : null;
                if (!body.trim().startsWith("{") || nama == null || nama.isBlank()) { kirim(ex, 400, "nama wajib diisi"); return; }
                kirim(ex, 201, service.buat(nama).json());
            });
            srv.start();
            return srv;
        }
    }

    static void kirim(HttpExchange ex, int kode, String isi) throws IOException {
        byte[] b = isi.getBytes(StandardCharsets.UTF_8);
        if (b.length == 0) ex.sendResponseHeaders(kode, -1); else ex.sendResponseHeaders(kode, b.length);
        if (b.length > 0) ex.getResponseBody().write(b);
        ex.close();
    }

    static int lulus, gagal;
    static void cek(String nama, boolean ok) { System.out.println((ok ? "  LULUS  " : "  GAGAL  ") + nama); if (ok) lulus++; else gagal++; }

    static HttpResponse<String> post(String base, String json) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(base + "/produk"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }

    public static void main(String[] args) throws Exception {
        // Uji 1: service ASLI
        ServiceMemori asli = new ServiceMemori();
        HttpServer s1 = new App(asli).mulai();
        String b1 = "http://localhost:" + s1.getAddress().getPort();
        var r = post(b1, "{\"nama\":\"Kopi\"}");
        cek("POST valid -> 201", r.statusCode() == 201);
        cek("isi respons {\"id\":1,\"nama\":\"Kopi\"}", r.body().equals("{\"id\":1,\"nama\":\"Kopi\"}"));
        cek("service benar-benar menyimpan produk", asli.data.size() == 1);
        cek("POST nama kosong -> 400", post(b1, "{\"nama\":\"\"}").statusCode() == 400);
        cek("POST nama spasi saja -> 400", post(b1, "{\"nama\":\"   \"}").statusCode() == 400);
        cek("POST tanpa field nama -> 400", post(b1, "{}").statusCode() == 400);
        cek("POST bukan JSON -> 400", post(b1, "halo").statusCode() == 400);
        cek("service TIDAK dipanggil untuk 4 permintaan tidak valid", asli.data.size() == 1);
        s1.stop(0);

        // Uji 2: DI membuat service dapat DIGANTI FAKE (inilah gunanya injeksi)
        AtomicInteger dipanggil = new AtomicInteger();
        ProdukService fake = nama -> { dipanggil.incrementAndGet(); return new Produk(777, nama); };
        HttpServer s2 = new App(fake).mulai();
        String b2 = "http://localhost:" + s2.getAddress().getPort();
        var r2 = post(b2, "{\"nama\":\"Teh\"}");
        cek("dengan fake service: id 777 dari fake", r2.body().equals("{\"id\":777,\"nama\":\"Teh\"}"));
        post(b2, "{\"nama\":\"\"}");
        cek("fake dipanggil tepat 1 kali (yang valid)", dipanggil.get() == 1);
        s2.stop(0);

        System.out.println();
        System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal");
    }
}
