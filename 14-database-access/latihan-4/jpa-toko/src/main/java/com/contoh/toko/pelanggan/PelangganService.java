package com.contoh.toko.pelanggan;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PelangganService {
    private final PelangganRepository repo;

    public PelangganService(PelangganRepository repo) { this.repo = repo; }

    @Transactional
    public Pelanggan daftar(String nama, String kota, long... totalPesanan) {
        Pelanggan p = new Pelanggan(nama, kota);
        for (long t : totalPesanan) p.tambahPesanan(new Pesanan(t));
        return repo.save(p);
    }

    @Transactional
    public void ubahNama(Long id, String namaBaru) {
        repo.findById(id).orElseThrow().setNama(namaBaru);          // dirty checking -> UPDATE ... WHERE version = ?
    }

    @Transactional(readOnly = true)
    public long totalSemuaNPlusSatu(String kota) {                  // N+1: 1 query pelanggan + 1 per pelanggan
        long total = 0;
        for (Pelanggan p : repo.findByKotaOrderByNamaAsc(kota)) for (Pesanan o : p.getPesanan()) total += o.getTotal();
        return total;
    }

    @Transactional(readOnly = true)
    public long totalSemua(String kota) {                           // 1 query
        List<Pelanggan> data = repo.cariDenganPesanan(kota);
        return data.stream().flatMap(p -> p.getPesanan().stream()).mapToLong(Pesanan::getTotal).sum();
    }
}
