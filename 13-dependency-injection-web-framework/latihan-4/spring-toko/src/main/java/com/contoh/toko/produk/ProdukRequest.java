package com.contoh.toko.produk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProdukRequest(
        @NotBlank(message = "nama wajib diisi")
        @Size(max = 50, message = "nama maksimal 50 karakter")     // aturan validasi BARU (latihan 4)
        String nama,
        @Positive(message = "harga harus positif") long harga) { }
