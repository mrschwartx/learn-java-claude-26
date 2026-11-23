// Dikompilasi dengan sengaja: checked exception tidak ditangani dan tidak dideklarasikan
class StokHabisException extends Exception {
    StokHabisException(String pesan) { super(pesan); }
}

class Gudang {
    void ambil(String barang, int jumlah) throws StokHabisException {
        throw new StokHabisException("habis");
    }
}

public class TidakDitangani {
    public static void main(String[] args) {
        new Gudang().ambil("beras", 1);          // tidak ada try/catch dan main tidak punya throws
    }
}
