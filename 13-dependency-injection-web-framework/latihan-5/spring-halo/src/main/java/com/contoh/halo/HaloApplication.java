package com.contoh.halo;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HaloApplication {
    public record Produk(long id, String nama) {}

    private static final Map<Long, Produk> DATA = Map.of(1L, new Produk(1, "Kopi"), 2L, new Produk(2, "Teh"));

    @GetMapping("/halo")
    public String halo() { return "Halo dari Spring Boot"; }

    @GetMapping("/produk/{id}")
    public ResponseEntity<Produk> produk(@PathVariable long id) {
        Produk p = DATA.get(id);
        return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    public static void main(String[] args) { SpringApplication.run(HaloApplication.class, args); }
}
