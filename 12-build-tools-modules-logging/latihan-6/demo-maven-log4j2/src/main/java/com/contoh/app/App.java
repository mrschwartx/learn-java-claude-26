package com.contoh.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Aplikasi mulai");

        MDC.put("requestId", "req-001");              // konteks ikut tercatat di setiap log
        try {
            proses("pesanan-42");
            proses("pesanan-0");
        } finally {
            MDC.clear();
        }
        log.debug("Pesan DEBUG ini hanya muncul jika level DEBUG aktif");
    }

    static void proses(String id) {
        log.info("Memproses {}", id);
        try {
            if (id.endsWith("-0")) throw new IllegalArgumentException("id tidak valid: " + id);
        } catch (IllegalArgumentException e) {
            log.error("Gagal memproses {}", id, e);   // exception sebagai argumen TERAKHIR -> stack trace tercatat
        }
    }
}
