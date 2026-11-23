import java.util.LinkedHashMap;
import java.util.Map;

// Latihan 5 (bagian 1): cache berkapasitas 2, dua mode LinkedHashMap
public class DemoLru {
    public static void main(String[] args) {
        // Skenario: put(a), put(b), get(a), put(c)
        // PREDIKSI mode akses (LRU): get(a) menjadikan a "paling baru", sehingga b yang tertua -> b dibuang.
        //                            isi akhir: [a, c]
        // PREDIKSI mode penyisipan : get(a) tidak mengubah urutan; a tertua -> a dibuang.
        //                            isi akhir: [b, c]

        Cache<String, String> lru = new Cache<>(2, true);
        System.out.println("== Mode URUTAN-AKSES (LRU) ==");
        jalankan(lru);

        Cache<String, String> fifo = new Cache<>(2, false);
        System.out.println();
        System.out.println("== Mode URUTAN-PENYISIPAN (FIFO) ==");
        jalankan(fifo);
    }

    static void jalankan(Cache<String, String> c) {
        c.put("a", "1");        System.out.println("put(a)  -> " + c.keySet());
        c.put("b", "2");        System.out.println("put(b)  -> " + c.keySet());
        c.get("a");             System.out.println("get(a)  -> " + c.keySet());
        c.put("c", "3");        System.out.println("put(c)  -> " + c.keySet() + "   isi akhir: " + c);
        System.out.println("contains a? " + c.containsKey("a") + ", contains b? " + c.containsKey("b"));
    }
}

class Cache<K, V> extends LinkedHashMap<K, V> {
    private final int kapasitas;

    Cache(int kapasitas, boolean urutkanBerdasarkanAkses) {
        super(16, 0.75f, urutkanBerdasarkanAkses);       // parameter ketiga = accessOrder
        this.kapasitas = kapasitas;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > kapasitas;
    }
}
