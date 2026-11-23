import java.util.HashMap;
import java.util.Map;

// Latihan 2: StokHabisException sebagai checked, lalu sebagai unchecked
public class DemoGudang {
    public static void main(String[] args) {
        System.out.println("== Versi CHECKED ==");
        Gudang g = new Gudang();
        try {
            g.ambil("beras", 4);
            g.ambil("beras", 4);
            g.ambil("beras", 4);          // stok tinggal 2 -> StokHabisException
        } catch (StokHabisException e) {  // compiler MEMAKSA kita menangani ini
            System.out.println("Ditangani: " + e.getMessage() + " (kurang " + e.getKekurangan() + ")");
        }

        try {
            g.ambil("emas", 1);           // barang tidak dikenal: kesalahan pemanggil, bukan stok habis
        } catch (IllegalArgumentException e) {
            System.out.println("Argumen salah: " + e.getMessage());
        } catch (StokHabisException e) {
            System.out.println("tidak mungkin sampai sini");
        }

        System.out.println();
        System.out.println("== Versi UNCHECKED ==");
        GudangRuntime gr = new GudangRuntime();
        gr.ambil("gula", 2);              // tanpa try/catch: compiler tidak memaksa
        System.out.println("gula diambil 2, sisa " + gr.sisa("gula"));
        try {
            gr.ambil("gula", 5);
        } catch (StokHabisRuntimeException e) {                  // boleh ditangani, tidak wajib
            System.out.println("Ditangani (opsional): " + e.getMessage());
        }
        gr.ambil("gula", 99);             // TIDAK ditangani: program berhenti dengan exception di bawah ini
        System.out.println("baris ini tidak pernah tercapai");
    }
}

// ---------- Versi 1: checked ----------
class StokHabisException extends Exception {
    private final int kekurangan;
    StokHabisException(String pesan, int kekurangan) {
        super(pesan);
        this.kekurangan = kekurangan;
    }
    int getKekurangan() { return kekurangan; }
}

class Gudang {
    private final Map<String, Integer> stok = new HashMap<>(Map.of("beras", 10, "gula", 3));

    void ambil(String barang, int jumlah) throws StokHabisException {
        Integer ada = stok.get(barang);
        if (ada == null) {
            throw new IllegalArgumentException("barang tidak dikenal: " + barang);
        }
        if (jumlah > ada) {
            throw new StokHabisException("stok " + barang + " hanya " + ada + ", diminta " + jumlah, jumlah - ada);
        }
        stok.put(barang, ada - jumlah);
        System.out.println("Ambil " + jumlah + " " + barang + ", sisa " + (ada - jumlah));
    }
}

// ---------- Versi 2: unchecked ----------
class StokHabisRuntimeException extends RuntimeException {
    StokHabisRuntimeException(String pesan) { super(pesan); }
}

class GudangRuntime {
    private final Map<String, Integer> stok = new HashMap<>(Map.of("beras", 10, "gula", 3));

    int sisa(String barang) { return stok.get(barang); }

    void ambil(String barang, int jumlah) {                      // tanpa 'throws'
        int ada = stok.get(barang);
        if (jumlah > ada) {
            throw new StokHabisRuntimeException("stok " + barang + " hanya " + ada + ", diminta " + jumlah);
        }
        stok.put(barang, ada - jumlah);
    }
}
