import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Latihan 2: dependensi melingkar A -> B -> A. Container lama: StackOverflowError. Container baru: pesan jelas.
public class DemoMelingkar {
    static class A { A(B b) {} }
    static class B { B(A a) {} }
    static class C { C(D d) {} }
    static class D { D(E e) {} }
    static class E { E(C c) {} }
    static class Mandiri { }
    static class BergantungMandiri { BergantungMandiri(Mandiri m) {} }

    // ----- LAMA: rekursi tanpa pengaman -----
    static class Lama {
        private final List<Class<?>> kelas;
        Lama(Class<?>... k) { kelas = List.of(k); }
        Object ambil(Class<?> t) throws Exception {
            Class<?> impl = kelas.stream().filter(t::isAssignableFrom).findFirst().orElseThrow();
            Constructor<?> c = impl.getDeclaredConstructors()[0];
            Object[] arg = new Object[c.getParameterCount()];
            for (int i = 0; i < arg.length; i++) arg[i] = ambil(c.getParameterTypes()[i]);
            c.setAccessible(true);
            return c.newInstance(arg);
        }
    }

    // ----- BARU: mencatat bean yang SEDANG dibuat -----
    static class Baru {
        private final List<Class<?>> kelas;
        private final Map<Class<?>, Object> singleton = new HashMap<>();
        private final Set<Class<?>> sedangDibuat = new LinkedHashSet<>();    // urutan penyusunan dipertahankan
        Baru(Class<?>... k) { kelas = List.of(k); }

        Object ambil(Class<?> t) throws Exception {
            Class<?> impl = kelas.stream().filter(t::isAssignableFrom).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Tidak ada bean untuk " + t.getSimpleName()));
            Object ada = singleton.get(impl);
            if (ada != null) return ada;
            if (!sedangDibuat.add(impl)) {                                   // sudah ada di jalur pembuatan -> siklus
                List<String> jalur = new ArrayList<>(sedangDibuat.stream().map(Class::getSimpleName).toList());
                jalur = jalur.subList(jalur.indexOf(impl.getSimpleName()), jalur.size());
                jalur.add(impl.getSimpleName());
                throw new IllegalStateException("Dependensi melingkar: " + String.join(" -> ", jalur));
            }
            try {
                Constructor<?> c = impl.getDeclaredConstructors()[0];
                Object[] arg = new Object[c.getParameterCount()];
                for (int i = 0; i < arg.length; i++) arg[i] = ambil(c.getParameterTypes()[i]);
                c.setAccessible(true);
                Object baru = c.newInstance(arg);
                singleton.put(impl, baru);
                return baru;
            } finally {
                sedangDibuat.remove(impl);                                   // bersihkan, juga saat gagal
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== Container LAMA, A <-> B ==");
        try {
            new Lama(A.class, B.class).ambil(A.class);
        } catch (StackOverflowError e) {
            System.out.println("  StackOverflowError (tanpa petunjuk bean mana yang bermasalah)");
        }

        System.out.println();
        System.out.println("== Container BARU ==");
        for (Class<?>[] set : new Class<?>[][] { { A.class, B.class }, { C.class, D.class, E.class } }) {
            try {
                new Baru(set).ambil(set[0]);
            } catch (IllegalStateException e) {
                System.out.println("  " + e.getMessage());
            }
        }
        // Bukan siklus: bean yang dipakai dua kali (berlian) TIDAK boleh dianggap siklus
        Baru normal = new Baru(BergantungMandiri.class, Mandiri.class);
        normal.ambil(BergantungMandiri.class);
        normal.ambil(BergantungMandiri.class);
        System.out.println("  Tanpa siklus tetap normal; pengambilan ulang tidak salah dianggap siklus: OK");

        // Setelah gagal, container masih dapat dipakai (sedangDibuat dibersihkan)
        Baru pulih = new Baru(A.class, B.class, Mandiri.class);
        try { pulih.ambil(A.class); } catch (IllegalStateException e) { /* diharapkan */ }
        System.out.println("  Container pulih setelah gagal, bean lain tetap bisa dibuat: " + (pulih.ambil(Mandiri.class) != null));
    }
}
