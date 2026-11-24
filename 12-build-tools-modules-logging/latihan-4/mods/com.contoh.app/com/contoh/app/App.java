package com.contoh.app;

import com.contoh.util.Sapa;

public class App {
    public static void main(String[] args) {
        System.out.println(Sapa.sapa("Java"));
        // Info modul saat runtime
        Module m = App.class.getModule();
        System.out.println("Modul App   : " + m.getName() + " (named=" + m.isNamed() + ")");
        Module u = Sapa.class.getModule();
        System.out.println("Modul Sapa  : " + u.getName());
        System.out.println("util meng-export com.contoh.util ke app?          " + u.isExported("com.contoh.util", m));
        System.out.println("util meng-export com.contoh.util.internal ke app? " + u.isExported("com.contoh.util.internal", m));
        // Reflection ke package yang tidak di-export (class public!) -> ditolak saat runtime
        try {
            Class<?> c = Class.forName("com.contoh.util.internal.Rahasia");
            c.getMethod("cap").invoke(null);
            System.out.println("Reflection berhasil (tidak seharusnya)");
        } catch (ReflectiveOperationException e) {
            System.out.println("Reflection ke internal ditolak: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
