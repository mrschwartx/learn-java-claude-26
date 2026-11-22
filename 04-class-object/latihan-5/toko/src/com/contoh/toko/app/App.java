package com.contoh.toko.app;

import com.contoh.toko.model.Produk;

public class App {
    public static void main(String[] args) {
        Produk p = new Produk("Kopi", 25_000);
        System.out.println(p.getNama() + " = Rp" + p.getHarga());
    }
}
