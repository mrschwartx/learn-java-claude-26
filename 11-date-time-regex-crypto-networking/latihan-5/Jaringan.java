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
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;

// Latihan 5: endpoint /waktu; client paralel sendAsync + allOf; timeout; penanganan non-2xx
public class Jaringan {
    // Clock disuntikkan (Modul 11.1): server memakai jam tetap agar keluaran deterministik
    static final Clock JAM = Clock.fixed(Instant.parse("2026-11-24T00:17:00Z"), ZoneOffset.UTC);

    static void balas(HttpExchange ex, int kode, String isi) throws IOException {
        byte[] b = isi.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(kode, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    static HttpServer mulaiServer() throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        s.setExecutor(Executors.newVirtualThreadPerTaskExecutor());       // tiap request di virtual thread
        s.createContext("/halo", ex -> balas(ex, 200, "{\"pesan\":\"Halo dari server Java\"}"));
        s.createContext("/waktu", ex -> balas(ex, 200, "{\"waktu\":\"" + java.time.format.DateTimeFormatter.ISO_INSTANT.format(JAM.instant()) + "\"}"));
        s.createContext("/rusak", ex -> balas(ex, 500, "{\"galat\":\"kesalahan internal\"}"));
        s.createContext("/lambat", ex -> {
            try { Thread.sleep(3_000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            balas(ex, 200, "{\"pesan\":\"terlambat\"}");
        });
        s.start();
        return s;
    }

    /** Hasil satu panggilan: sukses (2xx) membawa isi; selain itu membawa alasan. */
    record Hasil(String path, boolean ok, String isi) {}

    static CompletableFuture<Hasil> panggil(HttpClient klien, String base, String path, Duration batas) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + path)).timeout(batas).GET().build();
        return klien.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    boolean dua = res.statusCode() / 100 == 2;
                    return new Hasil(path, dua, dua ? res.body() : "status " + res.statusCode());
                })
                .exceptionally(e -> {
                    Throwable sebab = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
                    String alasan = sebab instanceof HttpTimeoutException ? "timeout" : sebab.getClass().getSimpleName();
                    return new Hasil(path, false, alasan);
                });
    }

    static void gabung(HttpClient klien, String base, Duration batas, String... paths) {
        List<CompletableFuture<Hasil>> semua = java.util.Arrays.stream(paths).map(p -> panggil(klien, base, p, batas)).toList();
        CompletableFuture.allOf(semua.toArray(CompletableFuture[]::new)).join();   // tunggu SEMUA (tiap future sudah menangani errornya)
        for (CompletableFuture<Hasil> f : semua) {
            Hasil h = f.join();
            System.out.println(String.format("  %-8s %-6s %s", h.path(), h.ok() ? "OK" : "GAGAL", h.isi()));
        }
        long sukses = semua.stream().filter(f -> f.join().ok()).count();
        System.out.println("  -> " + sukses + " dari " + semua.size() + " panggilan sukses");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = mulaiServer();
        String base = "http://localhost:" + server.getAddress().getPort();
        HttpClient klien = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        try {
            System.out.println("== 1. /halo dan /waktu paralel ==");
            long t0 = System.nanoTime();
            gabung(klien, base, Duration.ofSeconds(5), "/halo", "/waktu");

            System.out.println();
            System.out.println("== 2. Ditambah /rusak (500) dan path tak terdaftar (404) ==");
            gabung(klien, base, Duration.ofSeconds(5), "/halo", "/waktu", "/rusak", "/tidak-ada");

            System.out.println();
            System.out.println("== 3. /lambat (server tidur 3 detik) dengan timeout 400 ms ==");
            long t1 = System.nanoTime();
            gabung(klien, base, Duration.ofMillis(400), "/halo", "/lambat", "/waktu");
            long ms = (System.nanoTime() - t1) / 1_000_000;
            System.out.println("  seluruh gabungan selesai jauh sebelum 3 detik? " + (ms < 2_000));
            System.out.println("[BERVARIASI] waktu gabungan dengan timeout: " + ms + " ms");
        } finally {
            server.stop(0);
        }
    }
}
