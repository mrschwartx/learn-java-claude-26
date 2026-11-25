import java.util.List;

/** Latihan 4: test dengan MOCK (verify, gaya Mockito via MiniMock) vs test dengan FAKE (periksa state/hasil). */
public class PesananServiceTest {
    static final KursClient KURS_NOL = m -> 0;

    // ---------- gaya MOCK: verifikasi INTERAKSI ----------
    @MiniUji.Uji void mock_buat_menyimpanLaluMengirimNotifikasi() {
        MiniMock.Mock repo = new MiniMock.Mock(), notif = new MiniMock.Mock();
        PesananService s = new PesananService(MiniMock.buat(Repositori.class, repo), MiniMock.buat(Notifikasi.class, notif), KURS_NOL);
        s.buat("kopi");
        MiniMock.verify(repo, "simpan", "kopi");
        MiniMock.verify(notif, "kirim", "Pesanan kopi dibuat");
    }

    @MiniUji.Uji void mock_buat_gagalSimpan_tidakMengirimNotifikasi() {
        MiniMock.Mock repo = new MiniMock.Mock(), notif = new MiniMock.Mock();
        MiniMock.stubLempar(repo, "simpan", "teh", new IllegalStateException("db mati"));
        PesananService s = new PesananService(MiniMock.buat(Repositori.class, repo), MiniMock.buat(Notifikasi.class, notif), KURS_NOL);
        MiniUji.Asersi.melempar(IllegalStateException.class, () -> s.buat("teh"));
        MiniMock.tidakAda(notif);                                                   // padanan verifyNoInteractions
    }

    @MiniUji.Uji void mock_hargaRupiah_memakaiStubKurs() {
        MiniMock.Mock kurs = new MiniMock.Mock();
        MiniMock.stub(kurs, "ambilKurs", "USD", 16_000L);                           // padanan when(...).thenReturn(...)
        PesananService s = new PesananService(MiniMock.buat(Repositori.class, new MiniMock.Mock()), MiniMock.buat(Notifikasi.class, new MiniMock.Mock()), MiniMock.buat(KursClient.class, kurs));
        MiniUji.Asersi.samaDengan(160_000L, s.hargaRupiah(10, "USD"));
        MiniUji.Asersi.samaDengan(List.of("ambilKurs[USD]"), kurs.panggilan);          // padanan verify(kurs, never()).ambilKurs("EUR")
    }

    // ---------- gaya FAKE: periksa STATE / HASIL ----------
    @MiniUji.Uji void fake_buat_itemTersimpanDanNotifikasiTerkirim() {
        RepositoriFake repo = new RepositoriFake(); NotifikasiFake notif = new NotifikasiFake();
        new PesananService(repo, notif, KURS_NOL).buat("kopi");
        MiniUji.Asersi.samaDengan(List.of("kopi"), repo.data);
        MiniUji.Asersi.samaDengan(List.of("Pesanan kopi dibuat"), notif.terkirim);
    }

    @MiniUji.Uji void fake_buat_dbMati_notifikasiTidakTerkirim() {
        RepositoriFake repo = new RepositoriFake(); repo.rusak = true; NotifikasiFake notif = new NotifikasiFake();
        PesananService s = new PesananService(repo, notif, KURS_NOL);
        MiniUji.Asersi.melempar(IllegalStateException.class, () -> s.buat("teh"));
        MiniUji.Asersi.samaDengan(0, notif.terkirim.size());
    }

    @MiniUji.Uji void fake_buat_itemKosong_ditolak() {
        RepositoriFake repo = new RepositoriFake();
        PesananService s = new PesananService(repo, new NotifikasiFake(), KURS_NOL);
        MiniUji.Asersi.melempar(IllegalArgumentException.class, () -> s.buat(" "));
        MiniUji.Asersi.samaDengan(0, repo.data.size());
    }
}
