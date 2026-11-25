package com.contoh.toko.pelanggan;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelanggan")
public class Pelanggan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version                                         // optimistic locking: UPDATE ... WHERE id=? AND version=?
    private long version;

    @Column(nullable = false)
    private String nama;

    private String kota;

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL, orphanRemoval = true)   // LAZY secara default
    private List<Pesanan> pesanan = new ArrayList<>();

    protected Pelanggan() { }

    public Pelanggan(String nama, String kota) { this.nama = nama; this.kota = kota; }

    public void tambahPesanan(Pesanan p) { pesanan.add(p); p.setPelanggan(this); }

    public Long getId() { return id; }
    public long getVersion() { return version; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKota() { return kota; }
    public List<Pesanan> getPesanan() { return pesanan; }
}
