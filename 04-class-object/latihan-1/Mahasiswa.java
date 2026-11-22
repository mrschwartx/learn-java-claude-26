// Latihan 1: class Mahasiswa dengan field private, dua constructor (chaining dengan this(...)), toString
public class Mahasiswa {
    public static void main(String[] args) {
        Mahasiswa a = new Mahasiswa("2301001", "Andi", 3.75);
        Mahasiswa b = new Mahasiswa("2301002", "Budi");              // ipk default 0.0
        Mahasiswa c = new Mahasiswa("2301003", "Citra", 3.92);

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }

    private final String nim;
    private final String nama;
    private final double ipk;

    // Constructor lengkap
    public Mahasiswa(String nim, String nama, double ipk) {
        this.nim = nim;
        this.nama = nama;
        this.ipk = ipk;
    }

    // Constructor singkat: meneruskan ke constructor lengkap lewat this(...)
    public Mahasiswa(String nim, String nama) {
        this(nim, nama, 0.0);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "Mahasiswa{nim=%s, nama=%s, ipk=%.2f}", nim, nama, ipk);
    }
}
