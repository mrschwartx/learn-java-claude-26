// Dikompilasi dengan sengaja: case umum diletakkan SEBELUM case yang lebih spesifik
public class UrutanSalah {
    static String format(Object o) {
        return switch (o) {
            case Integer i            -> "bilangan " + i;
            case Integer i when i < 0 -> "bilangan negatif " + i;      // tidak akan pernah tercapai
            default                   -> "lain";
        };
    }
}
