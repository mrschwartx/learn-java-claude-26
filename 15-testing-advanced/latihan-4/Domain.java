import java.util.ArrayList;
import java.util.List;

interface Repositori { void simpan(String item); boolean ada(String item); }
interface Notifikasi { void kirim(String pesan); }
interface KursClient { long ambilKurs(String mata); }

/** Versi 1: persis seperti materi (simpan lalu notifikasi) + hargaRupiah (tambahan latihan). */
class PesananService {
    private final Repositori repo; private final Notifikasi notif; private final KursClient kurs;
    PesananService(Repositori repo, Notifikasi notif, KursClient kurs) { this.repo = repo; this.notif = notif; this.kurs = kurs; }

    void buat(String item) {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("item wajib diisi");
        repo.simpan(item);
        notif.kirim("Pesanan " + item + " dibuat");
    }
    long hargaRupiah(long harga, String mata) { return harga * kurs.ambilKurs(mata); }
}

/** Versi 2 (REFAKTOR, perilaku luar sama): cek dulu agar item yang sama tidak disimpan dua kali. */
class PesananServiceV2 extends PesananService {
    private final Repositori repo; private final Notifikasi notif;
    PesananServiceV2(Repositori repo, Notifikasi notif, KursClient kurs) { super(repo, notif, kurs); this.repo = repo; this.notif = notif; }
    @Override void buat(String item) {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("item wajib diisi");
        if (!repo.ada(item)) repo.simpan(item);
        notif.kirim("Pesanan " + item + " dibuat");
    }
}

/** FAKE: implementasi sungguhan yang ringan (dengan state), bukan sekadar perekam panggilan. */
class RepositoriFake implements Repositori {
    final List<String> data = new ArrayList<>();
    boolean rusak;
    public void simpan(String item) { if (rusak) throw new IllegalStateException("db mati"); data.add(item); }
    public boolean ada(String item) { return data.contains(item); }
}
class NotifikasiFake implements Notifikasi {
    final List<String> terkirim = new ArrayList<>();
    public void kirim(String pesan) { terkirim.add(pesan); }
}
