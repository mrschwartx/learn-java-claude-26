// Dikompilasi dengan sengaja untuk memperlihatkan pesan error (lihat error-kompilasi.txt)
public class ContohError {
    private String nama = "Andi";

    static String ambilNama() {
        return nama;     // method static tidak punya 'this', jadi tidak tahu 'nama' milik object yang mana
    }
}
