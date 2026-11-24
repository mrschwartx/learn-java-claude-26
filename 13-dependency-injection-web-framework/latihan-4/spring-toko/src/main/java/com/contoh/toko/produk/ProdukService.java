package com.contoh.toko.produk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ProdukService {
    private final Map<Long, Produk> data = new ConcurrentHashMap<>();   // bean singleton -> harus thread-safe
    private final AtomicLong urutan = new AtomicLong();

    public List<Produk> semua() { return new ArrayList<>(data.values()); }

    public Optional<Produk> cari(long id) { return Optional.ofNullable(data.get(id)); }

    public Produk buat(ProdukRequest req) {
        long id = urutan.incrementAndGet();
        Produk baru = new Produk(id, req.nama(), req.harga());
        data.put(id, baru);
        return baru;
    }

    /** Mengganti produk bila ada; atomik lewat computeIfPresent. */
    public Optional<Produk> ganti(long id, ProdukRequest req) {
        return Optional.ofNullable(data.computeIfPresent(id, (k, lama) -> new Produk(k, req.nama(), req.harga())));
    }

    /** true bila produk ada dan terhapus. */
    public boolean hapus(long id) { return data.remove(id) != null; }
}
