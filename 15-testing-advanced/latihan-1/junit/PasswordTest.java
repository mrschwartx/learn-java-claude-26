// TIDAK DIJALANKAN (JUnit tidak tersedia di sandbox). Padanan JUnit 6 dari latihan-1/PasswordTest.java.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PasswordTest {
    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
            "Abcdef12, true", "Abcdef1, false", "abcdefg1, false", "ABCDEFG1, false", "Abcdefgh, false",
            "Pa55w0rdYangPanjang, true", "12345678, false", "'Abc 1234', true", "'', false" })
    void kuat_beragamKasus(String pw, boolean diharapkan) { assertEquals(diharapkan, Password.kuat(pw)); }

    @Test
    void kuat_null_ditolakTanpaException() { assertFalse(Password.kuat(null)); }
}
