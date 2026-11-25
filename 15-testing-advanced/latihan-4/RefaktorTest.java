import java.util.List;

/** Perbandingan ketahanan terhadap refaktor: V2 menambah pemanggilan repo.ada(...) tanpa mengubah perilaku luar. */
public class RefaktorTest {
    static final KursClient KURS_NOL = m -> 0;
    static boolean versi2 = false;
    static PesananService buat(Repositori r, Notifikasi n) { return versi2 ? new PesananServiceV2(r, n, KURS_NOL) : new PesananService(r, n, KURS_NOL); }

    // MOCK yang terlalu terikat pada implementasi: mengharapkan DAFTAR PANGGILAN persis [simpan]
    @MiniUji.Uji void mock_terikatImplementasi_urutanPanggilanPersis() {
        MiniMock.Mock repo = new MiniMock.Mock();
        buat(MiniMock.buat(Repositori.class, repo), MiniMock.buat(Notifikasi.class, new MiniMock.Mock())).buat("kopi");
        MiniUji.Asersi.samaDengan(List.of("simpan[kopi]"), repo.panggilan);
    }

    // FAKE: hanya memeriksa hasil yang terlihat
    @MiniUji.Uji void fake_memeriksaHasil() {
        RepositoriFake repo = new RepositoriFake(); NotifikasiFake notif = new NotifikasiFake();
        buat(repo, notif).buat("kopi");
        MiniUji.Asersi.samaDengan(List.of("kopi"), repo.data);
        MiniUji.Asersi.samaDengan(List.of("Pesanan kopi dibuat"), notif.terkirim);
    }

    // Perilaku baru V2 yang NATURAL diuji dengan fake (state), merepotkan dengan mock (harus men-stub ada(...))
    @MiniUji.Uji void fake_itemSamaDuaKali_hanyaTersimpanSekali_V2() {
        if (!versi2) return;                                    // hanya bermakna pada V2
        RepositoriFake repo = new RepositoriFake();
        PesananService s = buat(repo, new NotifikasiFake());
        s.buat("kopi"); s.buat("kopi");
        MiniUji.Asersi.samaDengan(List.of("kopi"), repo.data);
    }

    public static void main(String[] a) throws Exception {
        for (boolean v2 : new boolean[] { false, true }) {
            versi2 = v2;
            System.out.println("== Implementasi " + (v2 ? "V2 (setelah refaktor)" : "V1 (awal)") + " ==");
            MiniUji.jalankan(true, RefaktorTest.class);
        }
    }
}
