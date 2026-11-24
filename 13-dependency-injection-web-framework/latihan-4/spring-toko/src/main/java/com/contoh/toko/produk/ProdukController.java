package com.contoh.toko.produk;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produk")
public class ProdukController {
    private final ProdukService service;

    public ProdukController(ProdukService service) { this.service = service; }   // constructor injection

    @GetMapping
    public List<Produk> semua() { return service.semua(); }

    @GetMapping("/{id}")
    public ResponseEntity<Produk> detail(@PathVariable long id) {
        return service.cari(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produk> buat(@Valid @RequestBody ProdukRequest req) {
        Produk baru = service.buat(req);
        return ResponseEntity.created(URI.create("/api/produk/" + baru.id())).body(baru);       // 201 + Location
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produk> ganti(@PathVariable long id, @Valid @RequestBody ProdukRequest req) {
        return service.ganti(id, req).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());   // 200 / 404
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hapus(@PathVariable long id) {
        return service.hapus(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); // 204 / 404
    }
}
