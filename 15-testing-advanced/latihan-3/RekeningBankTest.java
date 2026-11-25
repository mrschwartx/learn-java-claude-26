import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// Suite KUAT: asersi bermakna untuk setor, tarik, saldo tidak cukup, jumlah negatif/nol, batas
public class RekeningBankTest {
    @MiniUji.Uji void setor_menambahSaldo() {
        RekeningBank r = new RekeningBank("Ani", 100);
        r.setor(50);
        MiniUji.Asersi.samaDengan(150L, r.getSaldo());
    }

    @MiniUji.Kasus({ "-1", "0", "-1000" })
    void setor_nolAtauNegatif_melempar(long jumlah) {
        RekeningBank r = new RekeningBank("Ani", 100);
        MiniUji.Asersi.melempar(IllegalArgumentException.class, () -> r.setor(jumlah));
        MiniUji.Asersi.samaDengan(100L, r.getSaldo());                  // saldo tidak berubah
    }

    @MiniUji.Uji void tarik_mengurangiSaldo() {
        RekeningBank r = new RekeningBank("Ani", 100);
        r.tarik(30);
        MiniUji.Asersi.samaDengan(70L, r.getSaldo());
    }

    @MiniUji.Uji void tarik_tepatSebesarSaldo_diizinkan() {              // batas: jumlah == saldo
        RekeningBank r = new RekeningBank("Ani", 100);
        r.tarik(100);
        MiniUji.Asersi.samaDengan(0L, r.getSaldo());
    }

    @MiniUji.Uji void tarik_saldoTidakCukup_saldoTetapDanPesanDicetak() {
        RekeningBank r = new RekeningBank("Ani", 100);
        PrintStream asli = System.out;
        ByteArrayOutputStream tangkap = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tangkap));
        try { r.tarik(101); } finally { System.setOut(asli); }
        MiniUji.Asersi.samaDengan(100L, r.getSaldo());
        MiniUji.Asersi.benar(tangkap.toString().contains("Saldo tidak cukup untuk Ani"), "pesan saldo tidak cukup harus dicetak");
    }

    @MiniUji.Kasus({ "-50", "0" })
    void tarik_nolAtauNegatif_melempar(long jumlah) {                   // pada kode ASLI materi: GAGAL (tarik(-50) menambah saldo)
        RekeningBank r = new RekeningBank("Ani", 100);
        MiniUji.Asersi.melempar(IllegalArgumentException.class, () -> r.tarik(jumlah));
        MiniUji.Asersi.samaDengan(100L, r.getSaldo());
    }

    @MiniUji.Uji void toString_memuatNomorPemilikDanSaldo() {
        RekeningBank r = new RekeningBank("Budi", 7);
        String s = r.toString();
        MiniUji.Asersi.benar(s.contains("[Budi]") && s.endsWith("saldo=7"), "toString: " + s);
    }

    @MiniUji.Uji void konstruktorTanpaSaldoAwal_mulaiDariNol() {
        MiniUji.Asersi.samaDengan(0L, new RekeningBank("Cici").getSaldo());
    }

    @MiniUji.Uji void jumlahRekening_bertambahSetiapDibuat() {
        int sebelum = RekeningBank.getJumlahRekening();
        new RekeningBank("X");
        MiniUji.Asersi.samaDengan(sebelum + 1, RekeningBank.getJumlahRekening());
    }
}
