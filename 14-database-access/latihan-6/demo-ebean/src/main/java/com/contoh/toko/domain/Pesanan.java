package com.contoh.toko.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pesanan")
public class Pesanan extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pelanggan_id")
    private Pelanggan pelanggan;

    private long total;

    @WhenCreated
    private LocalDateTime dibuat;

    public Long getId() { return id; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public LocalDateTime getDibuat() { return dibuat; }
}
