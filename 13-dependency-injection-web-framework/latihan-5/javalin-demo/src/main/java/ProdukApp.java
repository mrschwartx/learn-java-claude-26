import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import java.util.Map;

public class ProdukApp {
    public record Produk(long id, String nama) {}

    private static final Map<Long, Produk> DATA = Map.of(1L, new Produk(1, "Kopi"), 2L, new Produk(2, "Teh"));

    public static Javalin buatApp() {
        Javalin app = Javalin.create(config -> config.useVirtualThreads = true);
        app.get("/halo", ctx -> ctx.result("Halo dari Javalin"));
        app.get("/produk/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            Produk p = DATA.get(id);
            if (p == null) throw new NotFoundResponse("Produk tidak ditemukan");
            ctx.json(p);
        });
        app.exception(NumberFormatException.class, (e, ctx) -> ctx.status(HttpStatus.BAD_REQUEST).result("ID harus berupa angka"));
        return app;
    }

    public static void main(String[] args) { buatApp().start(7070); }
}
