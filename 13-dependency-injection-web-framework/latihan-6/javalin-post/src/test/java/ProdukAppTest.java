import static org.junit.jupiter.api.Assertions.assertEquals;

import io.javalin.testtools.JavalinTest;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

class ProdukAppTest {
    static RequestBody json(String s) { return RequestBody.create(s, MediaType.parse("application/json")); }

    @Test void postValid201() {
        JavalinTest.test(new ProdukApp(new ProdukApp.ProdukService()).buatApp(), (server, client) -> {
            var r = client.post("/produk", json("{\"nama\":\"Kopi\"}"));
            assertEquals(201, r.code());
            assertEquals("{\"id\":1,\"nama\":\"Kopi\"}", r.body().string());
        });
    }

    @Test void postNamaKosong400() {
        JavalinTest.test(new ProdukApp(new ProdukApp.ProdukService()).buatApp(), (server, client) ->
                assertEquals(400, client.post("/produk", json("{\"nama\":\"\"}")).code()));
    }
}
