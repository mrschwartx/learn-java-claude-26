// Latihan 2 (versi A): FizzBuzz 1..100 dengan switch expression, satu hasil per baris
public class FizzBuzzSwitch {
    public static void main(String[] args) {
        for (int i = 1; i <= 100; i++) {
            String hasil = switch (i % 15) {
                case 0 -> "FizzBuzz";
                case 3, 6, 9, 12 -> "Fizz";
                case 5, 10 -> "Buzz";
                default -> String.valueOf(i);
            };
            System.out.println(hasil);
        }
    }
}
