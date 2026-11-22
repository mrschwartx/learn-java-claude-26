package com.contoh.toko.model;

public class Produk {
    private final String nama;
    private final int harga;

    public Produk(String nama, int harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public String getNama() { return nama; }
    public int getHarga() { return harga; }
}
