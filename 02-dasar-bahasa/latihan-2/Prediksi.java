// Latihan 2: prediksi dulu di kepala/kertas, baru jalankan.
public class Prediksi {
    public static void main(String[] args) {
        int x = 130;
        byte b = (byte) x;
        System.out.println("(byte) 130 = " + b);

        double d = 7 / 2;
        System.out.println("double d = 7 / 2  ->  " + d);

        // Pembanding: apa yang terjadi bila salah satu operand desimal
        double d2 = 7 / 2.0;
        System.out.println("double d2 = 7 / 2.0 -> " + d2);

        // Bukti mekanisme byte: tampilkan bit-nya
        System.out.println("130 dalam biner (32 bit): " + String.format("%32s", Integer.toBinaryString(130)).replace(' ', '0'));
        System.out.println("8 bit terbawah          : " + String.format("%8s", Integer.toBinaryString(130 & 0xFF)).replace(' ', '0'));
        System.out.println("tafsir signed 8 bit     : " + b);
    }
}
