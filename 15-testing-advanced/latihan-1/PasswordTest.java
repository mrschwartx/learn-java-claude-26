// Ditulis LEBIH DULU (TDD). Dijalankan oleh MiniUji; versi JUnit identik ada di latihan-1/junit/ (TIDAK DIJALANKAN).
public class PasswordTest {
    // format baris: password | diharapkan(kuat?)
    @MiniUji.Kasus({
        "Abcdef12|true",        // tepat 8 karakter, besar+kecil+angka
        "Abcdef1|false",        // 7 karakter (batas bawah - 1)
        "abcdefg1|false",       // tanpa huruf besar
        "ABCDEFG1|false",       // tanpa huruf kecil
        "Abcdefgh|false",       // tanpa angka
        "Pa55w0rdYangPanjang|true",
        "12345678|false",       // hanya angka
        "Abc 1234|true",        // spasi tidak melarang (aturan hanya 3 jenis karakter)
        "|false"                // kosong
    })
    void kuat_beragamKasus(String pw, boolean diharapkan) {
        MiniUji.Asersi.samaDengan(diharapkan, Password.kuat(pw));
    }

    @MiniUji.Uji void kuat_null_ditolakTanpaException() {
        MiniUji.Asersi.salah(Password.kuat(null), "null harus dianggap tidak kuat, bukan NullPointerException");
    }
}
