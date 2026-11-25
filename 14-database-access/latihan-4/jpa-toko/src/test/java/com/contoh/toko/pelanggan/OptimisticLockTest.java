package com.contoh.toko.pelanggan;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

// TIDAK DIJALANKAN. Dua "transaksi" disimulasikan dengan dua instance yang dimuat lalu di-detach (tanpa transaksi pembungkus).
@SpringBootTest
class OptimisticLockTest {
    @Autowired PelangganRepository repo;

    @Test
    void updateDariVersiKedaluwarsaDitolak() {
        Pelanggan asli = repo.save(new Pelanggan("Ani", "Bandung"));
        Pelanggan salinan1 = repo.findById(asli.getId()).orElseThrow();   // tanpa @Transactional -> langsung detached
        Pelanggan salinan2 = repo.findById(asli.getId()).orElseThrow();

        salinan1.setNama("Ani Lestari");
        repo.save(salinan1);                                              // sukses, version 0 -> 1

        salinan2.setNama("Ani Wijaya");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> repo.save(salinan2));   // version 0 sudah kedaluwarsa
    }
}
