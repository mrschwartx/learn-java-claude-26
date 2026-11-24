package com.contoh;

import org.tinylog.Logger;

public class DemoTinyLog {
    public static void main(String[] args) {
        Logger.info("Aplikasi mulai");
        Logger.debug("Detail diagnostik untuk {}", "pengembang");
        Logger.trace("Terlalu rinci");                                   // tidak tampil (level minimum debug)
        Logger.warn("Pengguna {} mencoba login gagal {} kali", "andi", 3);
        Logger.debug("Hasil perhitungan berat: {}", () -> hitungBerat()); // lambda: hanya dievaluasi jika DEBUG aktif
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            Logger.error(e, "Gagal parse {}", "abc");                    // exception DI DEPAN
        }
    }

    private static int hitungBerat() { return 42; }
}
