// Latihan 2: field static jumlahMahasiswa + method static; mengapa static tak bisa akses 'nama'
public class Mahasiswa {
    public static void main(String[] args) {
        System.out.println("Sebelum membuat object : " + Mahasiswa.getJumlahMahasiswa());
        Mahasiswa a = new Mahasiswa("2301001", "Andi", 3.75);
        Mahasiswa b = new Mahasiswa("2301002", "Budi");
        System.out.println("Setelah dua object     : " + Mahasiswa.getJumlahMahasiswa());
        Mahasiswa c = new Mahasiswa("2301003", "Citra", 3.92);
        System.out.println("Setelah tiga object    : " + Mahasiswa.getJumlahMahasiswa());

        // Counter dipakai bersama; ia milik class, bukan milik 'a', 'b', atau 'c'
        System.out.println("Dibaca lewat object a  : " + a.getJumlahMahasiswa() + "  (diperbolehkan, tetapi membingungkan; pakai NamaClass.)");
        System.out.println(a.nama() + ", " + b.nama() + ", " + c.nama());
    }

    private static int jumlahMahasiswa = 0;   // satu salinan, dibagi semua object

    private final String nim;
    private final String nama;
    private final double ipk;

    public Mahasiswa(String nim, String nama, double ipk) {
        this.nim = nim;
        this.nama = nama;
        this.ipk = ipk;
        jumlahMahasiswa++;                     // naik setiap object dibuat
    }

    public Mahasiswa(String nim, String nama) {
        this(nim, nama, 0.0);                  // jumlahMahasiswa naik sekali saja, di constructor utama
    }

    public static int getJumlahMahasiswa() {
        return jumlahMahasiswa;
        // return nama;     // ERROR: non-static variable nama cannot be referenced from a static context
    }

    public String nama() { return nama; }
}
