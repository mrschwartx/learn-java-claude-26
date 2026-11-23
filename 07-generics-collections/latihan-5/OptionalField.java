import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

// Latihan 5 (bagian 2): mengapa Optional tidak cocok dipakai sebagai FIELD class
public class OptionalField {
    public static void main(String[] args) throws Exception {
        // 1) Field Optional itu sendiri masih bisa null -> tujuan Optional (menghapus null) gagal
        ProfilBurukOptional p = new ProfilBurukOptional("Ani", null);     // compiler tidak melarang
        try {
            System.out.println(p.email().isPresent());
        } catch (NullPointerException e) {
            System.out.println("1) Optional bernilai null -> NullPointerException saat dipakai");
        }

        // 2) Optional tidak Serializable -> class yang menyimpannya tidak bisa diserialisasi
        try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
            out.writeObject(new ProfilBurukOptional("Budi", Optional.of("budi@contoh.com")));
        } catch (java.io.NotSerializableException e) {
            System.out.println("2) Serialisasi gagal: NotSerializableException: " + e.getMessage());
        }

        // 3) Alternatif yang disarankan: simpan nilai/nullable di field, bungkus Optional di GETTER
        ProfilBaik b = new ProfilBaik("Citra", null);
        System.out.println("3) getter Optional      : " + b.email());
        System.out.println("   dengan nilai          : " + new ProfilBaik("Dewi", "dewi@contoh.com").email());
        try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
            out.writeObject(b);
            System.out.println("   serialisasi ProfilBaik: berhasil");
        } catch (IOException e) {
            System.out.println("   serialisasi gagal: " + e);
        }
    }
}

// Pendekatan yang DIHINDARI: Optional sebagai field
class ProfilBurukOptional implements Serializable {
    private final String nama;
    private final Optional<String> email;

    ProfilBurukOptional(String nama, Optional<String> email) {
        this.nama = nama;
        this.email = email;
    }
    Optional<String> email() { return email; }
}

// Pendekatan yang DISARANKAN: field biasa (boleh null, didokumentasikan), Optional hanya di tipe kembalian
class ProfilBaik implements Serializable {
    private final String nama;
    private final String email;        // boleh null

    ProfilBaik(String nama, String email) {
        this.nama = nama;
        this.email = email;
    }
    Optional<String> email() { return Optional.ofNullable(email); }
}
