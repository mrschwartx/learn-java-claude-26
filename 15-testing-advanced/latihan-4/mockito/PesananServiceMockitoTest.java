// TIDAK DIJALANKAN (Mockito/JUnit tidak tersedia di sandbox). Versi Mockito + fake buatan sendiri.
package com.contoh.pesanan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PesananServiceMockitoTest {
    @Mock Repositori repo;
    @Mock Notifikasi notif;
    @Mock KursClient kurs;
    @InjectMocks PesananService service;

    @Test void buat_menyimpanLaluMengirimNotifikasi() {          // MOCK + verify
        service.buat("kopi");
        verify(repo).simpan("kopi");
        verify(notif).kirim("Pesanan kopi dibuat");
    }

    @Test void buat_gagalSimpan_tidakMengirimNotifikasi() {
        doThrow(new IllegalStateException("db mati")).when(repo).simpan("teh");
        assertThrows(IllegalStateException.class, () -> service.buat("teh"));
        verifyNoInteractions(notif);
    }

    @Test void hargaRupiah_memakaiKurs() {
        when(kurs.ambilKurs("USD")).thenReturn(16_000L);
        assertEquals(160_000L, service.hargaRupiah(10, "USD"));
    }

    // FAKE buatan sendiri untuk Repositori (tanpa Mockito) - dipakai di test terpisah tanpa @InjectMocks
    static class RepositoriFake implements Repositori {
        final List<String> data = new java.util.ArrayList<>();
        public void simpan(String item) { data.add(item); }
        public boolean ada(String item) { return data.contains(item); }
    }

    @Test void fake_buat_itemTersimpan() {
        RepositoriFake fake = new RepositoriFake();
        new PesananService(fake, notif, kurs).buat("kopi");
        assertEquals(List.of("kopi"), fake.data);                 // periksa STATE, bukan interaksi
    }
}
