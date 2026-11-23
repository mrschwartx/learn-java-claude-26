// Dikompilasi dengan sengaja: bounded type menolak tipe yang tidak Comparable
public class MinSalah {
    static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static void main(String[] args) {
        Object o = min(new Object(), new Object());     // Object bukan Comparable
        Object campur = min(1, "satu");                  // Integer dan String tidak bisa disatukan
    }
}
