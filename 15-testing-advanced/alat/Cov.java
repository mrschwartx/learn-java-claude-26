import java.util.Set;
import java.util.TreeSet;

/** Pencatat baris yang dieksekusi untuk instrumentasi sederhana (BUKAN JaCoCo). */
public class Cov {
    public static final Set<Integer> HIT = new TreeSet<>();
    public static void hit(int baris) { HIT.add(baris); }
}
