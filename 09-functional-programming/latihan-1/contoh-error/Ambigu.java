import java.util.function.Function;

// Dikompilasi dengan sengaja: Integer::toString bisa berarti dua method
public class Ambigu {
    public static void main(String[] args) {
        Function<Integer, String> f = Integer::toString;
    }
}
