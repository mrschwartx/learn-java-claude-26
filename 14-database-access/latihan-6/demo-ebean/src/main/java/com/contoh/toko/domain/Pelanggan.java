package com.contoh.toko.domain;

import io.ebean.Model;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelanggan")
public class Pelanggan extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nama;
    private String kota;
    private String email;                                    // (a) kolom baru: ALTER TABLE pelanggan ADD COLUMN email VARCHAR(150)

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL)
    private List<Pesanan> pesanan = new ArrayList<>();

    public Long getId() { return id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Pesanan> getPesanan() { return pesanan; }
}
