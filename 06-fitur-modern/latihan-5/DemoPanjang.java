import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Latihan 5: @Panjang(min, max) + Validator yang diperluas, dan efek @Retention
public class DemoPanjang {
    public static void main(String[] args) throws Exception {
        System.out.println("valid        -> " + Validator.validasi(new Pengguna("Andi", "andi@contoh.com", "Rahasia123")));
        System.out.println("nama pendek  -> " + Validator.validasi(new Pengguna("A", "a@contoh.com", "Rahasia123")));
        System.out.println("nama panjang -> " + Validator.validasi(new Pengguna("N".repeat(30), "n@contoh.com", "Rahasia123")));
        System.out.println("sandi pendek -> " + Validator.validasi(new Pengguna("Budi", "b@contoh.com", "abc")));
        System.out.println("nama null    -> " + Validator.validasi(new Pengguna(null, "x@contoh.com", "Rahasia123")));
        System.out.println("semua salah  -> " + Validator.validasi(new Pengguna("", null, "x")));

        // --- Pengaruh @Retention ---
        System.out.println();
        System.out.println("== Apa yang terbaca lewat reflection? ==");
        Field f = Percobaan.class.getDeclaredField("nilai");
        System.out.println("@UjiRuntime : " + f.getAnnotation(UjiRuntime.class));
        System.out.println("@UjiClass   : " + f.getAnnotation(UjiClass.class));
        System.out.println("@UjiSource  : " + f.getAnnotation(UjiSource.class));
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Wajib {
    String pesan() default "wajib diisi";
}

@Retention(RetentionPolicy.RUNTIME)      // WAJIB RUNTIME agar Validator bisa membacanya
@Target(ElementType.FIELD)
@interface Panjang {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}

class Pengguna {
    @Wajib @Panjang(min = 2, max = 20) String nama;
    @Wajib(pesan = "email harus ada") String email;
    @Panjang(min = 8, max = 64) String sandi;

    Pengguna(String nama, String email, String sandi) {
        this.nama = nama; this.email = email; this.sandi = sandi;
    }
}

class Validator {
    static List<String> validasi(Object objek) throws IllegalAccessException {
        List<String> galat = new ArrayList<>();
        for (Field f : objek.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Object nilai = f.get(objek);

            Wajib w = f.getAnnotation(Wajib.class);
            if (w != null && (nilai == null || nilai.toString().isBlank())) {
                galat.add(f.getName() + ": " + w.pesan());
            }

            Panjang p = f.getAnnotation(Panjang.class);
            // null dibiarkan: kewajiban terisi adalah urusan @Wajib, bukan @Panjang
            if (p != null && nilai instanceof String s) {
                if (s.length() < p.min() || s.length() > p.max()) {
                    galat.add(f.getName() + ": panjang harus " + p.min() + ".." + p.max() + ", bukan " + s.length());
                }
            }
        }
        return galat;
    }
}

// --- Tiga annotation yang identik, hanya beda retention ---
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface UjiRuntime { }
@Retention(RetentionPolicy.CLASS)   @Target(ElementType.FIELD) @interface UjiClass   { }
@Retention(RetentionPolicy.SOURCE)  @Target(ElementType.FIELD) @interface UjiSource  { }

class Percobaan {
    @UjiRuntime @UjiClass @UjiSource int nilai;
}
