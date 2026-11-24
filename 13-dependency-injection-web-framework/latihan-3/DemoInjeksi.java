import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Latihan 3: tiga alasan constructor injection > field injection, dan uji PesananService TANPA container
public class DemoInjeksi {
    interface Repositori { void simpan(String item); }
    interface Notifikasi { void kirim(String pesan); }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface Suntik { }          // ~ @Autowired pada field

    // ---------- Gaya BURUK: field injection ----------
    static class ServiceField {
        @Suntik private Repositori repo;              // tidak bisa final; dependency tersembunyi
        @Suntik private Notifikasi notif;
        void buat(String item) { repo.simpan(item); notif.kirim("Pesanan " + item); }
    }

    // ---------- Gaya BAIK: constructor injection ----------
    static class ServiceCtor {
        private final Repositori repo;                // final -> tidak bisa tertukar setelah dibuat
        private final Notifikasi notif;
        ServiceCtor(Repositori repo, Notifikasi notif) {
            this.repo = java.util.Objects.requireNonNull(repo);
            this.notif = java.util.Objects.requireNonNull(notif);
        }
        void buat(String item) { repo.simpan(item); notif.kirim("Pesanan " + item); }
    }

    // ---------- Fake buatan tangan (tanpa Mockito, tanpa container) ----------
    static class RepoFake implements Repositori {
        final List<String> tersimpan = new ArrayList<>();
        public void simpan(String item) { tersimpan.add(item); }
    }
    static class NotifFake implements Notifikasi {
        final List<String> terkirim = new ArrayList<>();
        public void kirim(String pesan) { terkirim.add(pesan); }
    }

    static int lulus, gagal;
    static void cek(String nama, boolean kondisi) {
        System.out.println((kondisi ? "  LULUS  " : "  GAGAL  ") + nama);
        if (kondisi) lulus++; else gagal++;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== Alasan 1: dependency TERSEMBUNYI. new ServiceField() tampak valid tetapi rusak ==");
        ServiceField rusak = new ServiceField();              // kompilasi lolos; tidak ada petunjuk butuh apa pun
        try {
            rusak.buat("kopi");
        } catch (NullPointerException e) {
            System.out.println("  NullPointerException saat dipakai (bukan saat dibuat): objek 'setengah jadi' beredar");
        }
        try {
            new ServiceCtor(null, null);
        } catch (NullPointerException e) {
            System.out.println("  Constructor: gagal LANGSUNG saat dibuat jika dependency tidak diberikan");
        }
        // new ServiceCtor() tanpa argumen bahkan tidak dapat dikompilasi -> dependency terlihat di tanda tangan

        System.out.println();
        System.out.println("== Alasan 2: tanpa container, field injection hanya bisa diisi lewat REFLECTION ==");
        ServiceField lewatRefleksi = new ServiceField();
        RepoFake r0 = new RepoFake(); NotifFake n0 = new NotifFake();
        for (Field f : ServiceField.class.getDeclaredFields()) {
            f.setAccessible(true);                            // membobol private
            f.set(lewatRefleksi, f.getType() == Repositori.class ? r0 : n0);
        }
        lewatRefleksi.buat("teh");
        System.out.println("  Berhasil, tetapi harus membobol field private; dan field tidak bisa 'final': " + java.lang.reflect.Modifier.isFinal(ServiceField.class.getDeclaredFields()[0].getModifiers()));
        System.out.println("  Pada ServiceCtor field 'final': " + java.lang.reflect.Modifier.isFinal(ServiceCtor.class.getDeclaredFields()[0].getModifiers()));

        System.out.println();
        System.out.println("== Alasan 3: object selalu valid + mudah diuji; di bawah: uji ServiceCtor TANPA container ==");
        RepoFake repo = new RepoFake();
        NotifFake notif = new NotifFake();
        ServiceCtor service = new ServiceCtor(repo, notif);   // cukup 'new' dengan fake
        service.buat("kopi");
        service.buat("susu");
        cek("repositori menerima 2 item", repo.tersimpan.size() == 2);
        cek("urutan item benar", repo.tersimpan.equals(List.of("kopi", "susu")));
        cek("notifikasi dikirim untuk tiap pesanan", notif.terkirim.equals(List.of("Pesanan kopi", "Pesanan susu")));

        boolean menolakNull = false;
        try { new ServiceCtor(null, notif); } catch (NullPointerException e) { menolakNull = true; }
        cek("menolak dependency null saat dibuat", menolakNull);

        // Kegagalan dependency tidak menelan data: repo yang melempar
        Repositori rusakRepo = item -> { throw new IllegalStateException("DB mati"); };
        NotifFake n2 = new NotifFake();
        boolean melempar = false;
        try { new ServiceCtor(rusakRepo, n2).buat("kopi"); } catch (IllegalStateException e) { melempar = true; }
        cek("kegagalan repo diteruskan", melempar);
        cek("notifikasi TIDAK terkirim bila simpan gagal", n2.terkirim.isEmpty());

        System.out.println();
        System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal");
    }
}
