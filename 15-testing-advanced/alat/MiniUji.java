import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * "JUnit mini" untuk sandbox ini (JUnit/Mockito tidak dapat diunduh): @Uji, @Kasus (parameterized), asersi dasar,
 * instance BARU per test (seperti JUnit). Bukan pengganti JUnit; hanya agar TDD dan mutation testing dapat dijalankan nyata.
 */
public class MiniUji {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface Uji { }
    /** Satu baris data = satu pemanggilan; argumen dipisah '|'. Mirip @CsvSource. */
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface Kasus { String[] value(); }

    public static class Asersi {
        public static void samaDengan(Object diharapkan, Object aktual) {
            if (!Objects.equals(diharapkan, aktual)) throw new AssertionError("diharapkan <" + diharapkan + "> tetapi aktual <" + aktual + ">");
        }
        public static void benar(boolean k, String pesan) { if (!k) throw new AssertionError(pesan); }
        public static void benar(boolean k) { benar(k, "diharapkan true"); }
        public static void salah(boolean k, String pesan) { if (k) throw new AssertionError(pesan); }
        public static void salah(boolean k) { salah(k, "diharapkan false"); }
        public static <T extends Throwable> T melempar(Class<T> tipe, Runnable r) {
            try { r.run(); } catch (Throwable t) {
                if (tipe.isInstance(t)) return tipe.cast(t);
                throw new AssertionError("diharapkan " + tipe.getSimpleName() + " tetapi " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
            throw new AssertionError("diharapkan " + tipe.getSimpleName() + " tetapi tidak ada exception");
        }
    }

    /** Hasil: {lulus, gagal}. Bila cetak=false tidak ada keluaran. */
    public static int[] jalankan(boolean cetak, Class<?>... kelas) throws Exception {
        int lulus = 0, gagal = 0;
        for (Class<?> k : kelas) {
            if (cetak) System.out.println(k.getSimpleName());
            Method[] ms = k.getDeclaredMethods();
            Arrays.sort(ms, Comparator.comparing(Method::getName));
            for (Method m : ms) {
                boolean uji = m.isAnnotationPresent(Uji.class), kasus = m.isAnnotationPresent(Kasus.class);
                if (!uji && !kasus) continue;
                m.setAccessible(true);
                List<Object[]> daftar = new ArrayList<>();
                List<String> nama = new ArrayList<>();
                if (kasus) for (String baris : m.getAnnotation(Kasus.class).value()) { daftar.add(ubah(m, baris)); nama.add(m.getName() + "[" + baris + "]"); }
                else { daftar.add(new Object[0]); nama.add(m.getName()); }
                for (int i = 0; i < daftar.size(); i++) {
                    Object instans = k.getDeclaredConstructor().newInstance();       // instance BARU per test
                    try { m.invoke(instans, daftar.get(i)); lulus++; if (cetak) System.out.println("  LULUS  " + nama.get(i)); }
                    catch (InvocationTargetException e) { gagal++; if (cetak) System.out.println("  GAGAL  " + nama.get(i) + " -> " + e.getCause().getMessage()); }
                }
            }
        }
        if (cetak) System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal");
        return new int[] { lulus, gagal };
    }

    static Object[] ubah(Method m, String baris) {
        String[] bagian = baris.split("\\|", -1);
        Class<?>[] tipe = m.getParameterTypes();
        Object[] a = new Object[tipe.length];
        for (int i = 0; i < tipe.length; i++) {
            String s = bagian[i].trim();
            a[i] = tipe[i] == int.class ? Integer.valueOf(s) : tipe[i] == long.class ? Long.valueOf(s) : tipe[i] == boolean.class ? Boolean.valueOf(s) : s;
        }
        return a;
    }

    /** main generik: java -cp ... MiniUji NamaKelasTest ... */
    public static void main(String[] args) throws Exception {
        List<Class<?>> k = new ArrayList<>();
        for (String n : args) k.add(Class.forName(n));
        jalankan(true, k.toArray(new Class<?>[0]));
    }
}
