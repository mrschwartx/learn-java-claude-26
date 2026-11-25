package com.contoh.toko.pelanggan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "pesanan")
public class Pesanan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pelanggan_id")
    private Pelanggan pelanggan;

    private long total;
    private LocalDateTime dibuat = LocalDateTime.now();

    protected Pesanan() { }
    public Pesanan(long total) { this.total = total; }

    void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }
    public long getTotal() { return total; }
}
