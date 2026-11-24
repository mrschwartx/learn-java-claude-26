import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * BUKAN Spring Boot. Ini "kembaran" tanpa framework yang meniru KONTRAK HTTP dari spring-toko
 * (GET/POST/PUT/DELETE /api/produk, status code, aturan validasi) agar perilakunya dapat DIUJI di sandbox ini.
 * Yang diuji di sini adalah logika dan kontrak, bukan anotasi Spring.
 */
public class TokoStandIn {
    record Produk(long id, String nama, long harga) {
        String json() { return "{\"id\":" + id + ",\"nama\":\"" + nama.replace("\"", "\\\"") + "\",\"harga\":" + harga + "}"; }
    }

    // ---- service (sama dengan ProdukService di spring-toko) ----
    static class Service {
        final Map<Long, Produk> data = new ConcurrentHashMap<>();
        final AtomicLong urutan = new AtomicLong();
        Produk buat(String n, long h) { long id = urutan.incrementAndGet(); Produk p = new Produk(id, n, h); data.put(id, p); return p; }
        Optional<Produk> cari(long id) { return Optional.ofNullable(data.get(id)); }
        Optional<Produk> ganti(long id, String n, long h) { return Optional.ofNullable(data.computeIfPresent(id, (k, l) -> new Produk(k, n, h))); }
        boolean hapus(long id) { return data.remove(id) != null; }
    }

    // ---- validasi: sama dengan anotasi pada ProdukRequest ----
    static final Pattern NAMA = Pattern.compile("\"nama\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    static final Pattern HARGA = Pattern.compile("\"harga\"\\s*:\\s*(-?\\d+)");

    /** Mengembalikan daftar galat (kosong = valid). */
    static List<String> validasi(String nama, Long harga) {
        List<String> g = new ArrayList<>();
        if (nama == null || nama.isBlank()) g.add("nama: nama wajib diisi");
        else if (nama.length() > 50) g.add("nama: nama maksimal 50 karakter");
        if (harga == null || harga <= 0) g.add("harga: harga harus positif");
        return g;
    }

    static void kirim(HttpExchange ex, int kode, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        if (b.length == 0) ex.sendResponseHeaders(kode, -1); else ex.sendResponseHeaders(kode, b.length);
        if (b.length > 0) try (OutputStream os = ex.getResponseBody()) { os.write(b); }
        ex.close();
    }

    static String problem(int status, List<String> galat) {
        return "{\"status\":" + status + ",\"detail\":\"Data tidak valid\",\"galat\":["
                + galat.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]}";
    }

    static HttpServer mulai(Service s) throws IOException {
        HttpServer srv = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        srv.createContext("/api/produk", ex -> {
            String path = ex.getRequestURI().getPath();
            String sisa = path.substring("/api/produk".length());           // "" atau "/{id}"
            String m = ex.getRequestMethod();
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (sisa.isEmpty() || sisa.equals("/")) {
                if (m.equals("GET")) { kirim(ex, 200, "[" + s.data.values().stream().sorted(java.util.Comparator.comparingLong(Produk::id)).map(Produk::json).collect(Collectors.joining(",")) + "]"); return; }
                if (m.equals("POST")) {
                    Matcher mn = NAMA.matcher(body), mh = HARGA.matcher(body);
                    String nama = mn.find() ? mn.group(1) : null; Long harga = mh.find() ? Long.valueOf(mh.group(1)) : null;
                    List<String> g = validasi(nama, harga);
                    if (!g.isEmpty()) { kirim(ex, 400, problem(400, g)); return; }
                    Produk p = s.buat(nama, harga);
                    ex.getResponseHeaders().add("Location", "/api/produk/" + p.id());
                    kirim(ex, 201, p.json()); return;
                }
                kirim(ex, 405, ""); return;
            }
            long id;
            try { id = Long.parseLong(sisa.substring(1)); } catch (NumberFormatException e) { kirim(ex, 400, ""); return; }
            switch (m) {
                case "GET" -> { var p = s.cari(id); if (p.isPresent()) kirim(ex, 200, p.get().json()); else kirim(ex, 404, ""); }
                case "PUT" -> {
                    Matcher mn = NAMA.matcher(body), mh = HARGA.matcher(body);
                    String nama = mn.find() ? mn.group(1) : null; Long harga = mh.find() ? Long.valueOf(mh.group(1)) : null;
                    List<String> g = validasi(nama, harga);
                    if (!g.isEmpty()) { kirim(ex, 400, problem(400, g)); return; }        // validasi dulu (seperti @Valid), lalu 404
                    var p = s.ganti(id, nama, harga);
                    if (p.isPresent()) kirim(ex, 200, p.get().json()); else kirim(ex, 404, "");
                }
                case "DELETE" -> kirim(ex, s.hapus(id) ? 204 : 404, "");
                default -> kirim(ex, 405, "");
            }
        });
        srv.start();
        return srv;
    }

    // ---------------- uji ----------------
    static int lulus, gagal;
    static HttpClient klien = HttpClient.newHttpClient();
    static String base;

    static HttpResponse<String> panggil(String metode, String path, String json) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(base + path)).header("Content-Type", "application/json");
        b.method(metode, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return klien.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    static void harap(String nama, HttpResponse<String> r, int kode, String isi) {
        boolean ok = r.statusCode() == kode && (isi == null || r.body().equals(isi));
        System.out.println((ok ? "  LULUS  " : "  GAGAL  ") + nama + " -> " + r.statusCode() + (r.body().isEmpty() ? "" : " " + r.body()));
        if (ok) lulus++; else { gagal++; System.out.println("         diharapkan " + kode + (isi == null ? "" : " " + isi)); }
    }

    public static void main(String[] args) throws Exception {
        Service s = new Service();
        HttpServer srv = mulai(s);
        base = "http://localhost:" + srv.getAddress().getPort();
        try {
            harap("GET daftar kosong", panggil("GET", "/api/produk", null), 200, "[]");
            var c = panggil("POST", "/api/produk", "{\"nama\":\"Kopi Gayo\",\"harga\":25000}");
            harap("POST valid", c, 201, "{\"id\":1,\"nama\":\"Kopi Gayo\",\"harga\":25000}");
            System.out.println("  header Location = " + c.headers().firstValue("Location").orElse("(tidak ada)"));
            harap("GET /1", panggil("GET", "/api/produk/1", null), 200, "{\"id\":1,\"nama\":\"Kopi Gayo\",\"harga\":25000}");
            harap("GET /99", panggil("GET", "/api/produk/99", null), 404, "");
            harap("POST nama kosong & harga negatif", panggil("POST", "/api/produk", "{\"nama\":\"\",\"harga\":-5}"), 400,
                    "{\"status\":400,\"detail\":\"Data tidak valid\",\"galat\":[\"nama: nama wajib diisi\",\"harga: harga harus positif\"]}");
            harap("POST nama 51 karakter (aturan baru)", panggil("POST", "/api/produk", "{\"nama\":\"" + "x".repeat(51) + "\",\"harga\":100}"), 400,
                    "{\"status\":400,\"detail\":\"Data tidak valid\",\"galat\":[\"nama: nama maksimal 50 karakter\"]}");
            harap("POST nama tepat 50 karakter", panggil("POST", "/api/produk", "{\"nama\":\"" + "x".repeat(50) + "\",\"harga\":100}"), 201, null);
            harap("POST tanpa field harga", panggil("POST", "/api/produk", "{\"nama\":\"Teh\"}"), 400, null);
            harap("PUT /1 valid", panggil("PUT", "/api/produk/1", "{\"nama\":\"Kopi Toraja\",\"harga\":30000}"), 200, "{\"id\":1,\"nama\":\"Kopi Toraja\",\"harga\":30000}");
            harap("GET /1 setelah PUT", panggil("GET", "/api/produk/1", null), 200, "{\"id\":1,\"nama\":\"Kopi Toraja\",\"harga\":30000}");
            harap("PUT /99 (tidak ada)", panggil("PUT", "/api/produk/99", "{\"nama\":\"A\",\"harga\":1}"), 404, "");
            harap("PUT /1 tidak valid", panggil("PUT", "/api/produk/1", "{\"nama\":\" \",\"harga\":0}"), 400, null);
            harap("DELETE /1", panggil("DELETE", "/api/produk/1", null), 204, "");
            harap("DELETE /1 lagi", panggil("DELETE", "/api/produk/1", null), 404, "");
            harap("GET /1 setelah DELETE", panggil("GET", "/api/produk/1", null), 404, "");
            harap("GET /abc (bukan angka)", panggil("GET", "/api/produk/abc", null), 400, "");
        } finally {
            srv.stop(0);
        }
        System.out.println();
        System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal");
    }
}
