import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Latihan 3: (1) test pada kode ASLI vs perbaikan, (2) cakupan baris dengan instrumentasi sederhana (BUKAN JaCoCo),
 * (3) mutation testing mini (BUKAN PIT): suite LEMAH vs KUAT. Dijalankan: java -cp alat-classes latihan-3/AnalisisKualitas.java
 */
public class AnalisisKualitas {
    static final String CP = System.getProperty("java.class.path");

    /** Kompilasi sumber di berkas-berkas ini ke direktori; kembalikan classloader (induk = classpath alat). */
    static ClassLoader kompilasi(Path out, Path... sumber) throws Exception {
        Files.createDirectories(out);
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        List<String> arg = new ArrayList<>(List.of("-d", out.toString(), "-cp", CP, "-proc:none"));
        for (Path p : sumber) arg.add(p.toString());
        int kode = jc.run(null, null, null, arg.toArray(new String[0]));
        if (kode != 0) throw new IllegalStateException("kompilasi gagal: " + List.of(sumber));
        return new URLClassLoader(new URL[] { out.toUri().toURL() }, AnalisisKualitas.class.getClassLoader());
    }

    static int[] jalankan(ClassLoader cl, boolean cetak, String... kelas) throws Exception {
        Class<?> uji = cl.loadClass("MiniUji");
        List<Class<?>> ks = new ArrayList<>();
        for (String k : kelas) ks.add(cl.loadClass(k));
        return (int[]) uji.getMethod("jalankan", boolean.class, Class[].class).invoke(null, cetak, (Object) ks.toArray(new Class<?>[0]));
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("m15");
        Path asli = Path.of("latihan-3/asli/RekeningBank.java"), baik = Path.of("latihan-3/perbaikan/RekeningBank.java");
        Path kuat = Path.of("latihan-3/RekeningBankTest.java"), lemah = Path.of("latihan-3/RekeningBankLemahTest.java");

        System.out.println("== 1. Suite KUAT pada kode ASLI materi Modul 4 ==");
        jalankan(kompilasi(tmp.resolve("asli"), asli, kuat), true, "RekeningBankTest");
        System.out.println("  -> temuan: tarik(-50) dan tarik(0) tidak ditolak; tarik(-50) bahkan MENAMBAH saldo (bug pada kode materi)");

        System.out.println();
        System.out.println("== 2. Suite KUAT pada kode PERBAIKAN (tarik menolak jumlah <= 0) ==");
        jalankan(kompilasi(tmp.resolve("baik"), baik, kuat), true, "RekeningBankTest");

        System.out.println();
        System.out.println("== 3. Cakupan baris (instrumentasi sederhana pada kode perbaikan) ==");
        List<String> baris = Files.readAllLines(baik);
        List<Integer> target = new ArrayList<>();
        StringBuilder inst = new StringBuilder();
        for (int i = 0; i < baris.size(); i++) {
            String b = baris.get(i), t = b.strip();
            boolean statement = b.startsWith("        ") && t.endsWith(";") && !t.startsWith("this(") && !t.startsWith("super(");
            if (statement) { target.add(i + 1); inst.append("Cov.hit(").append(i + 1).append("); "); }
            inst.append(b).append('\n');
        }
        Path instDir = tmp.resolve("inst-src"); Files.createDirectories(instDir);
        Path instFile = instDir.resolve("RekeningBank.java"); Files.writeString(instFile, inst.toString());
        for (Path suite : List.of(lemah, kuat)) {
            ClassLoader cl = kompilasi(tmp.resolve("inst-" + suite.getFileName()), instFile, suite);
            String nama = suite.getFileName().toString().replace(".java", "");
            jalankan(cl, false, nama);
            @SuppressWarnings("unchecked") java.util.Set<Integer> hit = (java.util.Set<Integer>) cl.loadClass("Cov").getField("HIT").get(null);
            TreeSet<Integer> kena = new TreeSet<>(hit); kena.retainAll(target);
            System.out.println(String.format("  %-24s : %d dari %d baris pernyataan dieksekusi (%d%%)", nama, kena.size(), target.size(), 100 * kena.size() / target.size()));
            cl.loadClass("Cov").getField("HIT").get(null).getClass();
            ((java.util.Set<?>) cl.loadClass("Cov").getField("HIT").get(null)).clear();
        }
        System.out.println("  Catatan: baris `if (...) throw ...;` dihitung 'kena' walau throw tidak pernah dieksekusi: cakupan baris bukan cakupan cabang.");

        System.out.println();
        System.out.println("== 4. Mutation testing mini (mutan dibuat dari kode perbaikan; baseline suite KUAT harus hijau) ==");
        String dasar = Files.readString(baik);
        Map<String, String[]> mutan = new LinkedHashMap<>();           // nama -> {cari, ganti}
        mutan.put("M1 setor: `jumlah <= 0` -> `jumlah < 0` (batas)", new String[] { "if (jumlah <= 0) throw new IllegalArgumentException(\"Jumlah harus positif\");\n        saldo += jumlah;", "if (jumlah < 0) throw new IllegalArgumentException(\"Jumlah harus positif\");\n        saldo += jumlah;" });
        mutan.put("M2 setor: `saldo += jumlah` -> `saldo -= jumlah`", new String[] { "saldo += jumlah;", "saldo -= jumlah;" });
        mutan.put("M3 tarik: `jumlah > saldo` -> `jumlah >= saldo` (batas)", new String[] { "if (jumlah > saldo) {", "if (jumlah >= saldo) {" });
        mutan.put("M4 tarik: `saldo -= jumlah` -> `saldo += jumlah`", new String[] { "saldo -= jumlah;", "saldo += jumlah;" });
        mutan.put("M5 tarik: hapus `return;` setelah pesan", new String[] { "            return;\n", "" });
        mutan.put("M6 tarik: `jumlah <= 0` -> `jumlah < 0` (batas)", new String[] { "void tarik(long jumlah) {\n        if (jumlah <= 0)", "void tarik(long jumlah) {\n        if (jumlah < 0)" });
        mutan.put("M7 tarik: hapus pesan saldo tidak cukup", new String[] { "System.out.println(\"Saldo tidak cukup untuk \" + pemilik);", "" });
        System.out.println(String.format("  %-62s %-8s %-8s", "Mutan", "LEMAH", "KUAT"));
        int mati1 = 0, mati2 = 0, n = 0;
        for (var e : mutan.entrySet()) {
            String sumber = dasar.replace(e.getValue()[0], e.getValue()[1]);
            if (sumber.equals(dasar)) throw new IllegalStateException("mutan tidak berlaku: " + e.getKey());
            Path d = tmp.resolve("mut" + (++n)); Files.createDirectories(d);
            Path f = d.resolve("RekeningBank.java"); Files.writeString(f, sumber);
            String hasil1, hasil2;
            ClassLoader c1 = kompilasi(tmp.resolve("mutL" + n), f, lemah), c2 = kompilasi(tmp.resolve("mutK" + n), f, kuat);
            java.io.PrintStream asliOut = System.out;
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));         // bungkam pesan "Saldo tidak cukup"
            int[] a, b;
            try { a = jalankan(c1, false, "RekeningBankLemahTest"); b = jalankan(c2, false, "RekeningBankTest"); } finally { System.setOut(asliOut); }
            hasil1 = a[1] > 0 ? "MATI" : "HIDUP"; hasil2 = b[1] > 0 ? "MATI" : "HIDUP";
            if (a[1] > 0) mati1++; if (b[1] > 0) mati2++;
            System.out.println(String.format("  %-62s %-8s %-8s", e.getKey(), hasil1, hasil2));
        }
        System.out.println(String.format("  Skor mutasi (mutan MATI / total): LEMAH %d/%d, KUAT %d/%d", mati1, n, mati2, n));
        System.out.println("  MATI = test gagal saat kode diubah (test menangkap perubahan); HIDUP = perubahan lolos tak terdeteksi.");
        // bersihkan
        try (var s = Files.walk(tmp)) { s.sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); }
    }
}
