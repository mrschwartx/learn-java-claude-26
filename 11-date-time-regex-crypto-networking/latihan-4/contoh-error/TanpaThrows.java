import java.security.MessageDigest;

public class TanpaThrows {
    public static void main(String[] args) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");   // NoSuchAlgorithmException = checked
    }
}
