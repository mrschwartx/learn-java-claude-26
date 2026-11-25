package com.contoh.toko.pelanggan;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PelangganRepository extends JpaRepository<Pelanggan, Long> {

    List<Pelanggan> findByKotaOrderByNamaAsc(String kota);

    // Cara 1: JOIN FETCH
    @Query("select distinct p from Pelanggan p left join fetch p.pesanan where p.kota = :kota")
    List<Pelanggan> cariDenganPesanan(@Param("kota") String kota);

    // Cara 2: @EntityGraph (deklaratif, tanpa menulis JPQL join fetch)
    @EntityGraph(attributePaths = "pesanan")
    List<Pelanggan> findWithPesananByKota(String kota);

    // Cara 3: batch fetching tidak butuh method khusus; cukup spring.jpa.properties.hibernate.default_batch_fetch_size (lihat application.yml)
}
