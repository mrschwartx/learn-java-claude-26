import static org.junit.jupiter.api.Assertions.assertEquals;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

class ProdukAppTest {
    @Test void halo() {
        JavalinTest.test(ProdukApp.buatApp(), (server, client) -> assertEquals("Halo dari Javalin", client.get("/halo").body().string()));
    }
    @Test void produkDitemukan() {
        JavalinTest.test(ProdukApp.buatApp(), (server, client) -> {
            var r = client.get("/produk/1");
            assertEquals(200, r.code());
            assertEquals("{\"id\":1,\"nama\":\"Kopi\"}", r.body().string());
        });
    }
    @Test void produkTidakAda() {
        JavalinTest.test(ProdukApp.buatApp(), (server, client) -> assertEquals(404, client.get("/produk/99").code()));
    }
    @Test void idBukanAngka() {
        JavalinTest.test(ProdukApp.buatApp(), (server, client) -> assertEquals(400, client.get("/produk/abc").code()));
    }
}
