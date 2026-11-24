package com.contoh.app;

import com.contoh.util.Salam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static String pesan() { return Salam.buat("Bazel"); }

    public static void main(String[] args) { log.info(pesan()); }
}
