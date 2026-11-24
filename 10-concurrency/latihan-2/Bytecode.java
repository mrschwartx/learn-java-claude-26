// Dikompilasi lalu dibongkar dengan javap -c untuk memperlihatkan bahwa n++ adalah tiga langkah
public class Bytecode {
    static int n;
    static volatile int v;

    static void tambah()         { n++; }
    static void tambahVolatile() { v++; }
}
