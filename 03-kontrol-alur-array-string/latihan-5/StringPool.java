// Latihan 5 (bagian 1): == vs equals, dan string pool
public class StringPool {
    public static void main(String[] args) {
        String literal1 = "a";
        String literal2 = "a";
        String baru = new String("a");

        System.out.println("new String(\"a\") == \"a\"      : " + (baru == literal1));
        System.out.println("new String(\"a\").equals(\"a\") : " + baru.equals(literal1));
        System.out.println("\"a\" == \"a\" (dua literal)    : " + (literal1 == literal2));
        System.out.println("baru.intern() == \"a\"        : " + (baru.intern() == literal1));

        // String yang dibentuk saat RUNTIME bukan literal, sehingga tidak otomatis masuk pool
        String depan = "ja";
        String gabung = depan + "va";             // 'depan' bukan konstanta -> dihitung saat runtime
        System.out.println();
        System.out.println("(depan + \"va\") == \"java\"     : " + (gabung == "java"));
        System.out.println("(depan + \"va\").equals(\"java\"): " + gabung.equals("java"));

        // Konstanta kompilasi digabung oleh compiler sehingga tetap dari pool
        final String depanKonstan = "ja";
        String gabung2 = depanKonstan + "va";     // 'final' + literal -> constant expression
        System.out.println("(final depan + \"va\") == \"java\": " + (gabung2 == "java"));
    }
}
