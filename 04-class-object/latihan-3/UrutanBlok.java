// Latihan 3: urutan static block, instance block, constructor pada tiga object
public class UrutanBlok {
    public static void main(String[] args) {
        System.out.println("-- main dimulai (class Contoh belum dimuat) --");
        for (int i = 1; i <= 3; i++) {
            System.out.println("-- membuat object ke-" + i + " --");
            new Contoh();
        }
    }
}

class Contoh {
    static int s = init("static field");
    int i = init("instance field");

    static { System.out.println("static block"); }
    { System.out.println("instance block"); }

    Contoh() { System.out.println("constructor"); }

    static int init(String nama) {
        System.out.println("inisialisasi " + nama);
        return 1;
    }
}
