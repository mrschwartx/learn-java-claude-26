// Dikompilasi dengan sengaja: default diletakkan sebelum case bertipe
public class DefaultDiAtas {
    static String format(Object o) {
        return switch (o) {
            default       -> "lain";
            case Integer i -> "bilangan " + i;
        };
    }
}
