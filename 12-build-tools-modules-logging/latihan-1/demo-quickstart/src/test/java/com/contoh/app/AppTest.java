package com.contoh.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void sapaMenyebutNama() { assertEquals("Halo, Dewi!", App.sapa("Dewi")); }
}
