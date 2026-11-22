// Latihan 1: satu variabel untuk setiap 8 tipe primitif + batas tipe numeriknya
public class Primitif {
    public static void main(String[] args) {
        byte    b = 100;
        short   s = 30_000;
        int     i = 2_000_000_000;
        long    l = 9_000_000_000L;     // akhiran L wajib: 9 miliar melebihi rentang int
        float   f = 3.14159f;           // akhiran f wajib: literal desimal default adalah double
        double  d = 2.718281828459045;
        char    c = 'J';
        boolean z = true;

        System.out.println("== Nilai variabel ==");
        System.out.println("byte    b = " + b);
        System.out.println("short   s = " + s);
        System.out.println("int     i = " + i);
        System.out.println("long    l = " + l);
        System.out.println("float   f = " + f);
        System.out.println("double  d = " + d);
        System.out.println("char    c = " + c + "  (kode Unicode " + (int) c + ")");
        System.out.println("boolean z = " + z);

        System.out.println();
        System.out.println("== Rentang tipe numerik (konstanta wrapper) ==");
        System.out.println("byte   : " + Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE);
        System.out.println("short  : " + Short.MIN_VALUE + " .. " + Short.MAX_VALUE);
        System.out.println("int    : " + Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE);
        System.out.println("long   : " + Long.MIN_VALUE + " .. " + Long.MAX_VALUE);
        System.out.println("float  : " + (-Float.MAX_VALUE) + " .. " + Float.MAX_VALUE
                + "  (nilai positif terkecil " + Float.MIN_VALUE + ")");
        System.out.println("double : " + (-Double.MAX_VALUE) + " .. " + Double.MAX_VALUE
                + "  (nilai positif terkecil " + Double.MIN_VALUE + ")");
        System.out.println("char   : " + (int) Character.MIN_VALUE + " .. " + (int) Character.MAX_VALUE
                + "  (tanpa tanda, 16 bit)");
        System.out.println("boolean: tidak punya rentang numerik, hanya true / false");

        System.out.println();
        System.out.println("== Ukuran dalam bit (konstanta SIZE) ==");
        System.out.println("byte=" + Byte.SIZE + " short=" + Short.SIZE + " int=" + Integer.SIZE
                + " long=" + Long.SIZE + " float=" + Float.SIZE + " double=" + Double.SIZE
                + " char=" + Character.SIZE);
    }
}
