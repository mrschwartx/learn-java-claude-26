// TIDAK DIJALANKAN: butuh Spring Boot, REST Assured, Testcontainers, dan Docker (tidak tersedia di sandbox).
// Letakkan di src/test/java/com/contoh/toko/produk/ pada proyek Modul 13/14. Nama *IT dijalankan oleh Maven Failsafe (mvn verify).
package com.contoh.toko.produk;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProdukApiIT {

    @Container
    @ServiceConnection                                   // Spring Boot otomatis menyetel spring.datasource.* dari container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @LocalServerPort int port;

    @BeforeEach void siapkan() { RestAssured.port = port; }

    @Test void postProduk_valid_201() {
        given().contentType(ContentType.JSON).body("{\"nama\":\"Kopi Gayo\",\"harga\":25000}")
        .when().post("/api/produk")
        .then().statusCode(201)
               .header("Location", containsString("/api/produk/"))
               .body("nama", equalTo("Kopi Gayo"))
               .body("id", greaterThan(0));
    }

    @Test void postProduk_tidakValid_400() {
        given().contentType(ContentType.JSON).body("{\"nama\":\"\",\"harga\":-5}")
        .when().post("/api/produk")
        .then().statusCode(400);
    }
}
