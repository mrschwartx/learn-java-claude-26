import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Latihan 1: interface Pembayaran dengan DUA implementasi. Container lama diam-diam memilih yang pertama;
// container baru menolak ambiguitas kecuali ada @Utama (mirip @Primary) atau @Nama (mirip @Qualifier).
public class DemoPembayaran {

    // ---------------- anotasi ----------------
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface Komponen { String nama() default ""; }
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface Utama { }          // ~ @Primary
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) @interface Nama { String value(); }  // ~ @Qualifier

    // ---------------- kontrak & implementasi ----------------
    interface Repositori { void simpan(String item); }
    interface Notifikasi { void kirim(String pesan); }
    interface Pembayaran { String bayar(String item); }

    @Komponen(nama = "kartu") static class PembayaranKartu implements Pembayaran {
        public String bayar(String item) { return "kartu kredit untuk " + item; }
    }
    @Komponen(nama = "transfer") static class PembayaranTransfer implements Pembayaran {
        public String bayar(String item) { return "transfer bank untuk " + item; }
    }
    @Komponen static class RepositoriMemori implements Repositori {
        public void simpan(String item) { System.out.println("  [repo] disimpan: " + item); }
    }
    @Komponen static class NotifikasiEmail implements Notifikasi {
        public void kirim(String pesan) { System.out.println("  [email] " + pesan); }
    }

    @Komponen static class PesananService {
        private final Repositori repo; private final Notifikasi notif; private final Pembayaran bayar;
        PesananService(Repositori repo, Notifikasi notif, Pembayaran bayar) { this.repo = repo; this.notif = notif; this.bayar = bayar; }
        void buat(String item) { repo.simpan(item); notif.kirim("Pesanan " + item + " dibayar via " + bayar.bayar(item)); }
    }

    /** Varian yang meminta implementasi tertentu lewat @Nama (qualifier). */
    @Komponen static class PesananServiceTransfer {
        private final Pembayaran bayar;
        PesananServiceTransfer(@Nama("transfer") Pembayaran bayar) { this.bayar = bayar; }
        String info() { return bayar.bayar("laptop"); }
    }

    /** Varian yang ditandai @Utama pada salah satu implementasi. */
    @Komponen(nama = "dompet") @Utama static class PembayaranDompet implements Pembayaran {
        public String bayar(String item) { return "dompet digital untuk " + item; }
    }

    // ---------------- container LAMA (dari modul): findFirst ----------------
    static class ContainerLama {
        private final List<Class<?>> terdaftar = new ArrayList<>();
        private final Map<Class<?>, Object> singleton = new HashMap<>();
        ContainerLama(Class<?>... kelas) { terdaftar.addAll(List.of(kelas)); }

        @SuppressWarnings("unchecked") <T> T ambil(Class<T> tipe) {
            Class<?> impl = terdaftar.stream().filter(tipe::isAssignableFrom).findFirst()          // <- diam-diam: yang PERTAMA
                    .orElseThrow(() -> new IllegalStateException("Tidak ada bean untuk " + tipe.getSimpleName()));
            Object ada = singleton.get(impl);
            if (ada != null) return (T) ada;
            try {
                Constructor<?> c = impl.getDeclaredConstructors()[0];
                Object[] arg = new Object[c.getParameterCount()];
                for (int i = 0; i < arg.length; i++) arg[i] = ambil(c.getParameterTypes()[i]);
                c.setAccessible(true);
                Object baru = c.newInstance(arg);
                singleton.put(impl, baru);
                return (T) baru;
            } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
    }

    // ---------------- container BARU: ambiguitas ditolak, @Utama dan @Nama dikenali ----------------
    static class ContainerBaru {
        private final List<Class<?>> terdaftar = new ArrayList<>();
        private final Map<Class<?>, Object> singleton = new HashMap<>();
        ContainerBaru(Class<?>... kelas) { terdaftar.addAll(List.of(kelas)); }

        <T> T ambil(Class<T> tipe) { return ambil(tipe, null); }

        @SuppressWarnings("unchecked") private <T> T ambil(Class<T> tipe, String nama) {
            List<Class<?>> kandidat = terdaftar.stream().filter(tipe::isAssignableFrom).collect(Collectors.toList());
            if (nama != null) kandidat = kandidat.stream().filter(k -> nama.equals(k.getAnnotation(Komponen.class).nama())).collect(Collectors.toList());
            if (kandidat.isEmpty()) throw new IllegalStateException("Tidak ada bean untuk " + tipe.getSimpleName() + (nama == null ? "" : " bernama '" + nama + "'"));
            if (kandidat.size() > 1) {                                                              // ambiguitas
                List<Class<?>> utama = kandidat.stream().filter(k -> k.isAnnotationPresent(Utama.class)).toList();
                if (utama.size() != 1) throw new IllegalStateException("Ambigu: " + kandidat.size() + " bean untuk " + tipe.getSimpleName()
                        + " " + kandidat.stream().map(Class::getSimpleName).toList() + "; beri @Utama pada satu, atau pakai @Nama");
                kandidat = utama;
            }
            Class<?> impl = kandidat.get(0);
            Object ada = singleton.get(impl);
            if (ada != null) return (T) ada;
            try {
                Constructor<?> c = impl.getDeclaredConstructors()[0];
                Parameter[] p = c.getParameters();
                Object[] arg = new Object[p.length];
                for (int i = 0; i < p.length; i++) {
                    Nama q = p[i].getAnnotation(Nama.class);
                    arg[i] = ambil(p[i].getType(), q == null ? null : q.value());
                }
                c.setAccessible(true);
                Object baru = c.newInstance(arg);
                singleton.put(impl, baru);
                return (T) baru;
            } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
    }

    public static void main(String[] args) {
        System.out.println("== 1. Container LAMA: dua implementasi didaftarkan, urutan menentukan pemenang (diam-diam) ==");
        new ContainerLama(PesananService.class, RepositoriMemori.class, NotifikasiEmail.class, PembayaranKartu.class, PembayaranTransfer.class)
                .ambil(PesananService.class).buat("buku");
        new ContainerLama(PesananService.class, RepositoriMemori.class, NotifikasiEmail.class, PembayaranTransfer.class, PembayaranKartu.class)
                .ambil(PesananService.class).buat("buku");

        System.out.println();
        System.out.println("== 2. Container BARU: ambiguitas ditolak dengan pesan jelas ==");
        try {
            new ContainerBaru(PesananService.class, RepositoriMemori.class, NotifikasiEmail.class, PembayaranKartu.class, PembayaranTransfer.class)
                    .ambil(PesananService.class);
        } catch (IllegalStateException e) { System.out.println("  Error: " + e.getMessage()); }

        System.out.println();
        System.out.println("== 3. Perbaikan A (~@Qualifier): parameter meminta @Nama(\"transfer\") ==");
        System.out.println("  " + new ContainerBaru(PesananServiceTransfer.class, PembayaranKartu.class, PembayaranTransfer.class)
                .ambil(PesananServiceTransfer.class).info());

        System.out.println();
        System.out.println("== 4. Perbaikan B (~@Primary): satu implementasi ditandai @Utama ==");
        new ContainerBaru(PesananService.class, RepositoriMemori.class, NotifikasiEmail.class, PembayaranKartu.class, PembayaranTransfer.class, PembayaranDompet.class)
                .ambil(PesananService.class).buat("buku");

        System.out.println();
        System.out.println("== 5. Nama tak ada ==");
        try {
            new ContainerBaru(PesananServiceTransfer.class, PembayaranKartu.class).ambil(PesananServiceTransfer.class);
        } catch (IllegalStateException e) { System.out.println("  Error: " + e.getMessage()); }
    }
}
