import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Stack;

// Latihan 3: pengecekan kurung seimbang dengan ArrayDeque sebagai stack
public class Kurung {

    static boolean seimbang(String teks) {
        Map<Character, Character> pasangan = Map.of(')', '(', ']', '[', '}', '{');
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : teks.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);                                   // kurung buka: simpan
            } else if (pasangan.containsKey(c)) {                // kurung tutup
                if (stack.isEmpty() || stack.pop() != pasangan.get(c)) {
                    return false;                                // tak ada pasangan, atau salah jenis
                }
            }                                                    // karakter lain diabaikan
        }
        return stack.isEmpty();                                  // sisa kurung buka = tidak seimbang
    }

    public static void main(String[] args) {
        String[] uji = { "{[()]}", "{[(])}", "((", "))", "", "a(b)[c]{d}", "([)]", "(()", "{[]}()" };
        for (String s : uji) {
            System.out.println(String.format("%-12s -> %s", "\"" + s + "\"", seimbang(s) ? "SEIMBANG" : "tidak seimbang"));
        }

        // --- Mengapa ArrayDeque, bukan Stack? ---
        System.out.println();
        System.out.println("== Perbandingan Stack (legacy) vs ArrayDeque ==");

        Stack<Integer> lama = new Stack<>();
        Deque<Integer> baru = new ArrayDeque<>();
        for (int i = 1; i <= 3; i++) { lama.push(i); baru.push(i); }

        // 1) Urutan iterasi berbeda
        System.out.println("Stack      setelah push 1,2,3 : " + lama + "   (iterasi dari DASAR ke puncak)");
        System.out.println("ArrayDeque setelah push 1,2,3 : " + baru + "   (iterasi dari PUNCAK ke dasar)");

        // 2) Stack mewarisi Vector: bisa disalahgunakan melanggar LIFO
        lama.add(0, 99);                                         // menyisip di dasar stack
        System.out.println("Stack setelah add(0, 99)      : " + lama + "   <- operasi non-stack lolos");
        System.out.println("Stack.get(1)                  : " + lama.get(1) + "   <- akses acak lolos");

        // 3) ArrayDeque tidak punya akses acak seperti itu; yang ada hanya operasi di ujung
        System.out.println("ArrayDeque: peek()=" + baru.peek() + ", pop()=" + baru.pop() + ", sisa=" + baru);

        // 4) Perilaku pada stack kosong
        Deque<Integer> kosong = new ArrayDeque<>();
        System.out.println("kosong.poll() / peek()        : " + kosong.poll() + " / " + kosong.peek() + "   (null, tanpa exception)");
        try {
            kosong.pop();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("kosong.pop()                  : NoSuchElementException");
        }
        try {
            kosong.push(null);
        } catch (NullPointerException e) {
            System.out.println("kosong.push(null)             : NullPointerException (ArrayDeque menolak null)");
        }
    }
}
