// Suite LEMAH: menjalankan hampir semua baris tetapi TIDAK memverifikasi apa pun (hanya "tidak crash").
public class RekeningBankLemahTest {
    @MiniUji.Uji void jalankanSemuaJalur() {
        RekeningBank r = new RekeningBank("A", 100);
        r.setor(50);
        r.tarik(30);
        r.tarik(10_000);                                                 // jalur saldo tidak cukup
        try { r.setor(-1); } catch (IllegalArgumentException e) { }     // jalur negatif (tanpa asersi)
        try { r.tarik(-5); } catch (IllegalArgumentException e) { }
        r.toString();
        new RekeningBank("B");
        RekeningBank.getJumlahRekening();
    }
}
