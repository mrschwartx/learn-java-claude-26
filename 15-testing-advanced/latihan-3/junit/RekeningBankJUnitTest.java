// TIDAK DIJALANKAN (JUnit tidak tersedia di sandbox). Padanan JUnit 6 dari RekeningBankTest.java.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RekeningBankJUnitTest {
    @Test void setor_menambahSaldo() {
        RekeningBank r = new RekeningBank("Ani", 100);
        r.setor(50);
        assertEquals(150L, r.getSaldo());
    }

    @ParameterizedTest @ValueSource(longs = { -1, 0, -1000 })
    void setor_nolAtauNegatif_melempar(long jumlah) {
        RekeningBank r = new RekeningBank("Ani", 100);
        assertThrows(IllegalArgumentException.class, () -> r.setor(jumlah));
        assertEquals(100L, r.getSaldo());
    }

    @Test void tarik_tepatSebesarSaldo_diizinkan() {
        RekeningBank r = new RekeningBank("Ani", 100);
        r.tarik(100);
        assertEquals(0L, r.getSaldo());
    }

    @ParameterizedTest @ValueSource(longs = { -50, 0 })
    void tarik_nolAtauNegatif_melempar(long jumlah) {
        RekeningBank r = new RekeningBank("Ani", 100);
        assertThrows(IllegalArgumentException.class, () -> r.tarik(jumlah));
    }
}
