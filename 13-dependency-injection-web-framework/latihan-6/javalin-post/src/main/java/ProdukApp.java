import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

// TIDAK DIJALANKAN (butuh Maven Central). Javalin tidak punya DI: ProdukService diberikan lewat constructor oleh main().
public class ProdukApp {
    public record Produk(long id, String nama) {}
    public record ProdukRequest(String nama) {}

    public static class ProdukService {
        private final List<Produk> data = new CopyOnWriteArrayList<>();
        private final AtomicLong urutan = new AtomicLong();
        public Produk buat(String nama) { Produk p = new Produk(urutan.incrementAndGet(), nama); data.add(p); return p; }
        public List<Produk> semua() { return List.copyOf(data); }
    }

    private final ProdukService service;

    public ProdukApp(ProdukService service) { this.service = service; }      // constructor injection MANUAL

    public Javalin buatApp() {
        Javalin app = Javalin.create(config -> config.useVirtualThreads = true);
        app.post("/produk", ctx -> {
            ProdukRequest req = ctx.bodyAsClass(ProdukRequest.class);            // gagal parse JSON -> 400 otomatis oleh Javalin
            if (req.nama() == null || req.nama().isBlank()) throw new BadRequestResponse("nama wajib diisi");
            Produk baru = service.buat(req.nama());
            ctx.status(HttpStatus.CREATED).json(baru);                           // 201 + JSON
        });
        app.get("/produk", ctx -> ctx.json(service.semua()));
        return app;
    }

    public static void main(String[] args) {
        new ProdukApp(new ProdukService()).buatApp().start(7070);               // "container" kita = main()
    }
}
