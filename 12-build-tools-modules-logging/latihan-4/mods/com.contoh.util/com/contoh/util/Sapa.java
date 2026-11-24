package com.contoh.util;

import com.contoh.util.internal.Rahasia;

public class Sapa {
    public static String sapa(String nama) {
        return "Halo, " + nama + "! " + Rahasia.cap();
    }
}
