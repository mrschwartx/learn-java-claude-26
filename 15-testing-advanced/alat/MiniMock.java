import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mock mini berbasis java.lang.reflect.Proxy (hanya untuk interface): stub jawaban, rekam pemanggilan, verify. Bukan Mockito. */
public class MiniMock {
    public static class Mock implements InvocationHandler {
        final List<String> panggilan = new ArrayList<>();
        final Map<String, Object> jawaban = new HashMap<>();
        final Map<String, RuntimeException> lempar = new HashMap<>();

        static String kunci(Method m, Object[] a) { return m.getName() + Arrays.deepToString(a == null ? new Object[0] : a); }

        public Object invoke(Object p, Method m, Object[] a) {
            if (m.getDeclaringClass() == Object.class) return m.getName().equals("toString") ? "mock" : m.getName().equals("hashCode") ? 1 : p == a[0];
            String k = kunci(m, a);
            panggilan.add(k);
            if (lempar.containsKey(k)) throw lempar.get(k);
            Object j = jawaban.get(k);
            if (j == null && m.getReturnType().isPrimitive()) return m.getReturnType() == boolean.class ? false : m.getReturnType() == long.class ? 0L : 0;
            return j;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T buat(Class<T> antarmuka, Mock m) { return (T) Proxy.newProxyInstance(antarmuka.getClassLoader(), new Class<?>[] { antarmuka }, m); }

    /** Pendamping untuk menyatakan stub: pakai SAAT merekam argumen, mis. Mock.jawab("ambilKurs[USD]", 16000L). */
    public static void jawab(Mock m, String metode, Object... argumen) { }

    public static void stub(Mock m, String metode, String argumen, Object nilai) { m.jawaban.put(metode + "[" + argumen + "]", nilai); }
    public static void stubLempar(Mock m, String metode, String argumen, RuntimeException e) { m.lempar.put(metode + "[" + argumen + "]", e); }
    public static void verify(Mock m, String metode, String argumen) {
        if (!m.panggilan.contains(metode + "[" + argumen + "]")) throw new AssertionError("tidak dipanggil: " + metode + "(" + argumen + "); panggilan sebenarnya: " + m.panggilan);
    }
    public static void tidakAda(Mock m) { if (!m.panggilan.isEmpty()) throw new AssertionError("seharusnya tidak ada interaksi, tetapi: " + m.panggilan); }
    public static void tidakDipanggil(Mock m, String metode) {
        for (String p : m.panggilan) if (p.startsWith(metode + "[")) throw new AssertionError("seharusnya tidak dipanggil: " + p);
    }
}
