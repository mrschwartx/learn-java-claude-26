import java.util.concurrent.CompletableFuture;

public class LambdaBerubah {
    public static void main(String[] args) {
        int total = 0;
        CompletableFuture.runAsync(() -> total++).join();   // variabel lokal di lambda harus effectively final
    }
}
