// SALINAN RekeningBank dari materi Modul 4 (logika tidak diubah); hanya ditambah getSaldo() agar bisa diuji, dan tanpa main.
class RekeningBank {
    public static final String NAMA_BANK = "Bank Contoh";
    private static int jumlahRekening = 0;

    private final int nomor;
    private final String pemilik;
    private long saldo;

    RekeningBank(String pemilik, long saldoAwal) {
        this.nomor = ++jumlahRekening;
        this.pemilik = pemilik;
        this.saldo = saldoAwal;
    }

    RekeningBank(String pemilik) {
        this(pemilik, 0);
    }

    void setor(long jumlah) {
        if (jumlah <= 0) throw new IllegalArgumentException("Jumlah harus positif");
        saldo += jumlah;
    }

    void tarik(long jumlah) {
        if (jumlah > saldo) {
            System.out.println("Saldo tidak cukup untuk " + pemilik);
            return;
        }
        saldo -= jumlah;
    }

    long getSaldo() { return saldo; }

    static int getJumlahRekening() {
        return jumlahRekening;
    }

    @Override
    public String toString() {
        return "Rekening #" + nomor + " [" + pemilik + "] saldo=" + saldo;
    }
}
