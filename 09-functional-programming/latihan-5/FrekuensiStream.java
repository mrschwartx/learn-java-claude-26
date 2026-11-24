import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Latihan 5: FrekuensiKata (Modul 7) menjadi pipeline stream; lalu lazy, sekali-pakai, dan parallel
public class FrekuensiStream {
    public static void main(String[] args) {
        String teks = "java itu menyenangkan dan java itu kuat, belajar java dan belajar terus";

        // --- Versi Modul 7 (loop + merge) ---
        Map<String, Integer> lama = new HashMap<>();
        for (String k : teks.replace(",", "").split(" ")) lama.merge(k, 1, Integer::sum);

        // --- Versi stream: groupingBy + counting ---
        Map<String, Long> baru = Arrays.stream(teks.replace(",", "").split(" "))
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));

        System.out.println("== Frekuensi (jumlah menurun, lalu abjad) ==");
        baru.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));

        boolean sama = lama.size() == baru.size()
                && lama.entrySet().stream().allMatch(e -> baru.get(e.getKey()) == e.getValue().longValue());
        System.out.println("Hasil sama dengan versi loop Modul 7? " + sama);
        System.out.println("TreeMap: " + baru);

        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("== LAZY: tidak ada yang jalan sebelum operasi terminal ==");
        Stream<Integer> s = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .peek(x -> System.out.println("  sumber   " + x))
                .filter(x -> { System.out.println("  filter   " + x); return x % 2 == 0; })
                .map(x -> { System.out.println("  map      " + x); return x * 10; });
        System.out.println("Pipeline dibangun; belum ada apa pun tercetak di atas.");
        System.out.println("Terminal: limit(2).toList() ->");
        List<Integer> hasil = s.limit(2).toList();
        System.out.println("Hasil: " + hasil + "   (elemen 5..10 tidak pernah disentuh: short-circuit)");

        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("== SEKALI PAKAI ==");
        Stream<String> sekali = Stream.of("a", "b");
        System.out.println("pemakaian pertama: " + sekali.toList());
        try {
            sekali.toList();
        } catch (IllegalStateException e) {
            System.out.println("pemakaian kedua  : IllegalStateException: " + e.getMessage());
        }
        // Solusi: buat stream BARU dari sumber (atau pakai Supplier<Stream>)
        List<String> sumber = List.of("a", "b");
        System.out.println("stream baru dari sumber yang sama: " + sumber.stream().count() + " lalu " + sumber.stream().toList());

        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("== PARALLEL: yang pasti (deterministik) ==");
        List<Integer> urutSeq = IntStream.range(0, 20).boxed().toList();
        List<Integer> urutPar = IntStream.range(0, 20).parallel().boxed().toList();
        System.out.println("toList() pada stream paralel mempertahankan urutan? " + urutSeq.equals(urutPar));
        System.out.println("jumlah 1..1000 sequential/parallel sama? "
                + (IntStream.rangeClosed(1, 1000).sum() == IntStream.rangeClosed(1, 1000).parallel().sum()));

        System.out.println();
        System.out.println("== PARALLEL: yang merugikan (hasil di bawah BERVARIASI antar eksekusi/mesin) ==");
        // 1) State bersama yang tidak thread-safe: ulangi 50 kali agar race condition sempat muncul
        int salah = 0, gagal = 0;
        for (int percobaan = 0; percobaan < 50; percobaan++) {
            List<Integer> bersama = new ArrayList<>();
            try {
                IntStream.range(0, 100_000).parallel().forEach(bersama::add);      // SALAH: ArrayList bukan thread-safe
                if (bersama.size() != 100_000) salah++;
            } catch (RuntimeException e) {
                gagal++;
            }
        }
        System.out.println("[BERVARIASI] CPU tersedia = " + Runtime.getRuntime().availableProcessors());
        System.out.println("[BERVARIASI] forEach(list::add) paralel, 50 percobaan: ukuran salah " + salah
                + "x, melempar exception " + gagal + "x, benar " + (50 - salah - gagal) + "x");

        // 2) Operasi reduce yang tidak asosiatif
        int seq = IntStream.rangeClosed(1, 100).reduce(0, (a, b) -> a - b);
        int par = IntStream.rangeClosed(1, 100).parallel().reduce(0, (a, b) -> a - b);
        System.out.println("[BERVARIASI] reduce pengurangan  sequential=" + seq + ", parallel=" + par + "  (sama? " + (seq == par) + ")");
    }
}
