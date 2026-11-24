package com.contoh.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void pesanMemakaiSalam() { assertEquals("Halo, Bazel!", App.pesan()); }
}
