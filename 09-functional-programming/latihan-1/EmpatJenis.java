import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// Latihan 1: empat bentuk method reference, masing-masing dibandingkan dengan lambda setaranya
public class EmpatJenis {
    public static void main(String[] args) {

        // 1) Method STATIC: Kelas::metodeStatic        setara: s -> Integer.parseInt(s)
        Function<String, Integer> refStatic    = Integer::parseInt;
        Function<String, Integer> lambdaStatic = s -> Integer.parseInt(s);
        System.out.println("1) static           : " + refStatic.apply("42") + " | " + lambdaStatic.apply("42"));

        // 2) Method instance dari OBJECT TERTENTU: objek::metode     setara: s -> sapaan.concat(s)
        String sapaan = "Halo, ";
        UnaryOperator<String> refObjek    = sapaan::concat;
        UnaryOperator<String> lambdaObjek = s -> sapaan.concat(s);
        System.out.println("2) object tertentu  : " + refObjek.apply("Dunia") + " | " + lambdaObjek.apply("Dunia"));

        // 3) Method instance dari TIPE: Kelas::metodeInstance   setara: s -> s.toUpperCase()
        //    parameter PERTAMA menjadi receiver (objek yang dipanggil)
        Function<String, String> refTipe    = String::toUpperCase;
        Function<String, String> lambdaTipe = s -> s.toUpperCase();
        System.out.println("3) tipe (receiver)  : " + refTipe.apply("java") + " | " + lambdaTipe.apply("java"));

        // 4) CONSTRUCTOR: Kelas::new                    setara: () -> new ArrayList<>()
        Supplier<List<String>> refCtor    = ArrayList::new;
        Supplier<List<String>> lambdaCtor = () -> new ArrayList<>();
        List<String> a = refCtor.get(), b = lambdaCtor.get();
        System.out.println("4) constructor      : " + a + " | " + b + "  (object berbeda? " + (a != b) + ")");

        // Constructor reference yang SAMA memilih constructor berbeda sesuai tipe fungsionalnya
        Supplier<StringBuilder>           tanpaArg  = StringBuilder::new;         // new StringBuilder()
        Function<String, StringBuilder>   denganArg = StringBuilder::new;         // new StringBuilder(String)
        System.out.println("   StringBuilder::new: \"" + tanpaArg.get() + "\" vs \"" + denganArg.apply("isi") + "\"");

        // Object pada bentuk (2) ditentukan SAAT method reference dibuat, bukan saat dipanggil
        StringBuilder sb = new StringBuilder("A");
        Supplier<String> ref = sb::toString;          // receiver = objek sb yang sekarang
        sb.append("B");                               // mengubah ISI objek yang sama
        System.out.println("   receiver terikat  : " + ref.get() + "  (isi objek ikut berubah; objeknya tetap sama)");
    }
}
