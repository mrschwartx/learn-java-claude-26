import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Latihan 2: MiniUji dari materi + test Jumat (harga penuh) dengan Clock.fixed; kontras dengan LocalDate.now() langsung
public class MiniUjiModul {
    public static void main(String[] args) throws Exception {
        int lulus = 0, gagal = 0;
        for (Class<?> kelas : List.of(DiskonServiceTest.class, PesananServiceTest.class)) {
            System.out.println(kelas.getSimpleName());
            Method[] metode = kelas.getDeclaredMethods();
            Arrays.sort(metode, Comparator.comparing(Method::getName));
            for (Method m : metode) {
                if (!m.isAnnotationPresent(Uji.class)) continue;
                Object instans = kelas.getDeclaredConstructor().newInstance();   // instance BARU per test
                try {
                    m.invoke(instans);
                    lulus++;
                    System.out.println("  LULUS  " + m.getName());
                } catch (InvocationTargetException e) {
                    gagal++;
                    System.out.println("  GAGAL  " + m.getName() + " -> " + e.getCause().getMessage());
                }
            }
        }
        System.out.println("Ringkasan: " + lulus + " lulus, " + gagal + " gagal (1 gagal disengaja: sengajaGagal_ekspektasiSalah)");

        // ---- Mengapa Clock diinjeksi? Versi yang memanggil LocalDate.now() langsung TIDAK repeatable ----
        System.out.println();
        System.out.println("== Kontras: DiskonServiceBuruk memakai LocalDate.now() langsung ==");
        boolean akhirPekanHariIni = List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(LocalDate.now().getDayOfWeek());
        long hasil = new DiskonServiceBuruk().hitung(100_000);
        System.out.println("[BERVARIASI] hari ini: " + LocalDate.now().getDayOfWeek() + "; hitung(100000) = " + hasil);
        System.out.println("[BERVARIASI] test 'hariKerja_tanpaDiskon' versi buruk akan " + (hasil == 100_000 ? "LULUS" : "GAGAL") + " hari ini, dan hasilnya berubah bila dijalankan di akhir pekan");
        System.out.println("Versi ber-Clock memberi hasil SAMA kapan pun dijalankan (lihat DiskonServiceTest di atas).");
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Uji { }

class Asersi {
    static void samaDengan(Object diharapkan, Object aktual) {
        if (!Objects.equals(diharapkan, aktual)) throw new AssertionError("diharapkan <" + diharapkan + "> tetapi aktual <" + aktual + ">");
    }
}

class DiskonService {
    private final Clock clock;
    DiskonService(Clock clock) { this.clock = clock; }

    long hitung(long harga) {
        DayOfWeek hari = LocalDate.now(clock).getDayOfWeek();
        boolean akhirPekan = hari == DayOfWeek.SATURDAY || hari == DayOfWeek.SUNDAY;
        return akhirPekan ? harga * 90 / 100 : harga;
    }
}

/** Versi yang tidak dapat diuji secara andal: bergantung pada jam sistem. */
class DiskonServiceBuruk {
    long hitung(long harga) {
        DayOfWeek hari = LocalDate.now().getDayOfWeek();
        boolean akhirPekan = hari == DayOfWeek.SATURDAY || hari == DayOfWeek.SUNDAY;
        return akhirPekan ? harga * 90 / 100 : harga;
    }
}

class DiskonServiceTest {
    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

    @Uji void akhirPekan_diskon10Persen() {
        Clock sabtu = Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), JAKARTA);
        Asersi.samaDengan(90_000L, new DiskonService(sabtu).hitung(100_000));
    }

    @Uji void hariKerja_tanpaDiskon() {
        Clock senin = Clock.fixed(Instant.parse("2026-11-23T03:00:00Z"), JAKARTA);
        Asersi.samaDengan(100_000L, new DiskonService(senin).hitung(100_000));
    }

    // ---- Latihan 2: Jumat = harga penuh ----
    @Uji void jumat_hargaPenuh() {
        Clock jumat = Clock.fixed(Instant.parse("2026-11-20T03:00:00Z"), JAKARTA);        // Jumat 20 Nov 2026 10.00 WIB
        Asersi.samaDengan(100_000L, new DiskonService(jumat).hitung(100_000));
    }

    @Uji void jumat_pagi_dan_sore_sama() {
        Clock pagi = Clock.fixed(Instant.parse("2026-11-19T17:00:00Z"), JAKARTA);        // Jumat 00.00 WIB
        Clock malam = Clock.fixed(Instant.parse("2026-11-20T16:59:59Z"), JAKARTA);       // Jumat 23.59 WIB
        Asersi.samaDengan(100_000L, new DiskonService(pagi).hitung(100_000));
        Asersi.samaDengan(100_000L, new DiskonService(malam).hitung(100_000));
    }

    @Uji void batasZona_jumatMalamUTC_sudahSabtuDiJakarta() {
        Clock c = Clock.fixed(Instant.parse("2026-11-20T17:00:00Z"), JAKARTA);           // Jumat 17.00 UTC = Sabtu 00.00 WIB
        Asersi.samaDengan(90_000L, new DiskonService(c).hitung(100_000));
    }

    @Uji void sengajaGagal_ekspektasiSalah() {
        Clock sabtu = Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), JAKARTA);
        Asersi.samaDengan(85_000L, new DiskonService(sabtu).hitung(100_000));
    }
}

interface Notifikasi { void kirim(String pesan); }

class NotifikasiFake implements Notifikasi {
    final List<String> terkirim = new ArrayList<>();
    public void kirim(String pesan) { terkirim.add(pesan); }
}

class PesananService {
    private final Notifikasi notif;
    PesananService(Notifikasi notif) { this.notif = notif; }

    void buat(String item) {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("item wajib diisi");
        notif.kirim("Pesanan " + item + " dibuat");
    }
}

class PesananServiceTest {
    @Uji void buat_mengirimNotifikasi() {
        NotifikasiFake fake = new NotifikasiFake();
        new PesananService(fake).buat("kopi");
        Asersi.samaDengan(List.of("Pesanan kopi dibuat"), fake.terkirim);
    }

    @Uji void buat_itemKosong_melemparException() {
        NotifikasiFake fake = new NotifikasiFake();
        try {
            new PesananService(fake).buat(" ");
            throw new AssertionError("seharusnya melempar IllegalArgumentException");
        } catch (IllegalArgumentException diharapkan) {
            Asersi.samaDengan(0, fake.terkirim.size());
        }
    }
}
