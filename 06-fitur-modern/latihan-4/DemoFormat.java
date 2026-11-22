import java.util.List;

// Latihan 4: switch pattern: null, Integer negatif (guard), Integer lain, List<?>, default
public class DemoFormat {
    static String format(Object o) {
        return switch (o) {
            case null                  -> "kosong (null)";
            case Integer i when i < 0  -> "bilangan negatif " + i;     // lebih spesifik: HARUS di atas
            case Integer i             -> "bilangan " + i;             // lebih umum: di bawah
            case List<?> daftar        -> "list dengan " + daftar.size() + " elemen";
            default                    -> "objek lain: " + o;
        };
    }

    public static void main(String[] args) {
        Object[] uji = { null, -5, 0, 42, List.of(1, 2, 3), List.of(), "teks", 3.14 };
        for (Object o : uji) {
            System.out.println(String.format("%-16s -> %s", o == null ? "null" : o.toString(), format(o)));
        }
    }
}
