import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * DRIVER JDBC MAINAN untuk PostgreSQL, ditulis HANYA untuk sandbox ini (tidak ada driver resmi/akses Maven Central).
 * Bukan pengganti org.postgresql:postgresql. Fitur: autentikasi trust, protokol query diperluas (Parse/Bind/Execute,
 * parameter dikirim terpisah dari SQL = sama dengan PreparedStatement sungguhan), transaksi, batch (pipelined),
 * ResultSet teks dasar. Menerima URL jdbc:postgresql://host:port/db agar kode aplikasi TIDAK perlu diubah.
 */
public final class MiniPg implements Driver {
    /** Jumlah statement yang dieksekusi di server (untuk memperlihatkan N+1). */
    public static final AtomicInteger STATEMEN = new AtomicInteger();

    static { try { DriverManager.registerDriver(new MiniPg()); } catch (SQLException e) { throw new ExceptionInInitializerError(e); } }
    /** Memaksa class dimuat (registrasi driver). */
    public static void daftar() { }

    public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:postgresql://"); }
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) return null;
        String s = url.substring("jdbc:postgresql://".length());
        String hostPort = s.substring(0, s.indexOf('/')); String db = s.substring(s.indexOf('/') + 1);
        String host = hostPort.contains(":") ? hostPort.substring(0, hostPort.indexOf(':')) : hostPort;
        int port = hostPort.contains(":") ? Integer.parseInt(hostPort.substring(hostPort.indexOf(':') + 1)) : 5432;
        return new Konek(host, port, db, info.getProperty("user", "belajar")).proxy();
    }
    public int getMajorVersion() { return 0; } public int getMinorVersion() { return 1; } public boolean jdbcCompliant() { return false; }
    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
    public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }

    /** DataSource sederhana (BUKAN pool): membuka koneksi baru tiap getConnection(). */
    public static DataSource dataSource(String url, String user, String password) {
        return (DataSource) Proxy.newProxyInstance(MiniPg.class.getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
            if (m.getName().equals("getConnection")) { Properties pr = new Properties(); pr.setProperty("user", user); return DriverManager.getConnection(url, pr); }
            if (m.getName().equals("toString")) return "MiniPg.DataSource";
            if (m.getName().equals("hashCode")) return 1; if (m.getName().equals("equals")) return p == a[0];
            throw new SQLFeatureNotSupportedException(m.getName());
        });
    }

    // ================= koneksi =================
    static final class Hasil { List<String> kolom = new ArrayList<>(); List<String[]> baris = new ArrayList<>(); int diubah = -1; }

    static final class Konek implements InvocationHandler {
        final Socket soket; final DataInputStream in; final DataOutputStream out;
        boolean autoCommit = true, dalamTxn, tutup; int isolasi = Connection.TRANSACTION_READ_COMMITTED;

        Konek(String host, int port, String db, String user) throws SQLException {
            try {
                soket = new Socket(host, port);
                in = new DataInputStream(new BufferedInputStream(soket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(soket.getOutputStream()));
                ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream d = new DataOutputStream(b);
                d.writeInt(196608); cstr(d, "user"); cstr(d, user); cstr(d, "database"); cstr(d, db); d.writeByte(0);
                out.writeInt(b.size() + 4); out.write(b.toByteArray()); out.flush();
                while (true) {
                    char t = (char) in.readByte(); int len = in.readInt() - 4; byte[] p = in.readNBytes(len);
                    if (t == 'R') { int kode = new DataInputStream(new java.io.ByteArrayInputStream(p)).readInt(); if (kode != 0) throw new SQLException("autentikasi tipe " + kode + " tidak didukung driver mainan (hanya trust)"); }
                    else if (t == 'E') throw galat(p);
                    else if (t == 'Z') break;
                }
            } catch (IOException e) { throw new SQLException("gagal terhubung: " + e.getMessage(), "08001", e); }
        }

        static void cstr(DataOutputStream d, String s) throws IOException { d.write(s.getBytes(StandardCharsets.UTF_8)); d.writeByte(0); }
        static SQLException galat(byte[] p) {
            String m = "", c = ""; int i = 0;
            while (i < p.length && p[i] != 0) { char f = (char) p[i++]; int j = i; while (p[j] != 0) j++; String v = new String(p, i, j - i, StandardCharsets.UTF_8); i = j + 1; if (f == 'M') m = v; if (f == 'C') c = v; }
            return new SQLException(m, c);
        }
        void kirim(char tipe, byte[] isi) throws IOException { out.writeByte(tipe); out.writeInt(isi.length + 4); out.write(isi); }
        void kirimTeks(char tipe, String s) throws IOException { ByteArrayOutputStream b = new ByteArrayOutputStream(); b.write(s.getBytes(StandardCharsets.UTF_8)); b.write(0); kirim(tipe, b.toByteArray()); }

        /** Baca respons sampai ReadyForQuery. Mengembalikan daftar hasil; melempar error pertama setelah sinkron. */
        List<Hasil> baca() throws SQLException, IOException {
            List<Hasil> hasil = new ArrayList<>(); Hasil cur = null; SQLException err = null;
            while (true) {
                char t = (char) in.readByte(); int len = in.readInt() - 4; byte[] p = in.readNBytes(len);
                DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(p));
                switch (t) {
                    case 'T' -> { cur = new Hasil(); int n = d.readShort(); for (int i = 0; i < n; i++) { cur.kolom.add(leaCstr(d)); d.skipBytes(18); } }
                    case 'D' -> { int n = d.readShort(); String[] r = new String[n]; for (int i = 0; i < n; i++) { int l = d.readInt(); r[i] = l < 0 ? null : new String(d.readNBytes(l), StandardCharsets.UTF_8); } if (cur == null) cur = new Hasil(); cur.baris.add(r); }
                    case 'C' -> { String tag = leaCstr(d); if (cur == null) cur = new Hasil(); String[] w = tag.split(" ");
                        cur.diubah = (w[0].equals("SELECT") || w[0].equals("INSERT") || w[0].equals("UPDATE") || w[0].equals("DELETE")) ? Integer.parseInt(w[w.length - 1]) : 0;
                        hasil.add(cur); cur = null; }
                    case 'n' -> { cur = new Hasil(); }
                    case 'E' -> { if (err == null) err = galat(p); cur = null; }
                    case 'Z' -> { if (err != null) throw err; return hasil; }
                    default -> { }
                }
            }
        }
        static String leaCstr(DataInputStream d) throws IOException { ByteArrayOutputStream b = new ByteArrayOutputStream(); int x; while ((x = d.read()) > 0) b.write(x); return b.toString(StandardCharsets.UTF_8); }

        void simple(String sql) throws SQLException { try { kirimTeks('Q', sql); out.flush(); baca(); } catch (IOException e) { throw new SQLException(e); } }
        void mulaiTxn() throws SQLException {
            if (!autoCommit && !dalamTxn) {
                String lv = switch (isolasi) { case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ UNCOMMITTED"; case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE READ"; case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE"; default -> "READ COMMITTED"; };
                simple("BEGIN ISOLATION LEVEL " + lv); dalamTxn = true;
            }
        }
        static String keDollar(String sql) { StringBuilder sb = new StringBuilder(); int n = 0; boolean kutip = false;
            for (char c : sql.toCharArray()) { if (c == '\'') kutip = !kutip; if (c == '?' && !kutip) sb.append('$').append(++n); else sb.append(c); } return sb.toString(); }

        /** Satu eksekusi extended (Parse+Bind+[Describe]+Execute), TANPA Sync (dipakai juga oleh batch). */
        void kirimEksekusi(String sql, List<String> param, boolean describe) throws IOException {
            ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream d = new DataOutputStream(b);
            cstr(d, ""); cstr(d, keDollar(sql)); d.writeShort(0); kirim('P', b.toByteArray());
            b.reset(); cstr(d, ""); cstr(d, ""); d.writeShort(0); d.writeShort(param.size());
            for (String v : param) { if (v == null) d.writeInt(-1); else { byte[] x = v.getBytes(StandardCharsets.UTF_8); d.writeInt(x.length); d.write(x); } }
            d.writeShort(0); kirim('B', b.toByteArray());
            if (describe) { b.reset(); d.writeByte('P'); cstr(d, ""); kirim('D', b.toByteArray()); }
            b.reset(); cstr(d, ""); d.writeInt(0); kirim('E', b.toByteArray());
            STATEMEN.incrementAndGet();
        }
        Hasil jalankan(String sql, List<String> param) throws SQLException {
            try { mulaiTxn(); kirimEksekusi(sql, param, true); kirim('S', new byte[0]); out.flush(); List<Hasil> h = baca(); return h.isEmpty() ? new Hasil() : h.get(0); }
            catch (SQLException e) { throw e; } catch (IOException e) { throw new SQLException(e); }
        }
        int[] jalankanBatch(String sql, List<List<String>> semua) throws SQLException {
            try { mulaiTxn(); for (List<String> p : semua) kirimEksekusi(sql, p, false); kirim('S', new byte[0]); out.flush();
                List<Hasil> h = baca(); int[] r = new int[h.size()]; for (int i = 0; i < r.length; i++) r[i] = h.get(i).diubah; return r; }
            catch (IOException e) { throw new SQLException(e); }
        }

        Connection proxy() { return (Connection) Proxy.newProxyInstance(MiniPg.class.getClassLoader(), new Class<?>[] { Connection.class }, this); }

        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            {
                switch (m.getName()) {
                    case "prepareStatement" -> { return new Stmt(this, (String) a[0]).proxy(); }
                    case "createStatement" -> { return new Stmt(this, null).proxy(); }
                    case "setAutoCommit" -> { if ((Boolean) a[0] && dalamTxn) { simple("COMMIT"); dalamTxn = false; } autoCommit = (Boolean) a[0]; return null; }
                    case "getAutoCommit" -> { return autoCommit; }
                    case "commit" -> { if (dalamTxn) { simple("COMMIT"); dalamTxn = false; } return null; }
                    case "rollback" -> { if (dalamTxn) { simple("ROLLBACK"); dalamTxn = false; } return null; }
                    case "setTransactionIsolation" -> { isolasi = (Integer) a[0]; return null; }
                    case "getTransactionIsolation" -> { return isolasi; }
                    case "isClosed" -> { return tutup; }
                    case "isValid" -> { return !tutup; }
                    case "close" -> { if (!tutup) { tutup = true; try { kirim('X', new byte[0]); out.flush(); soket.close(); } catch (IOException e) { } } return null; }
                    case "toString" -> { return "MiniPg.Connection"; } case "hashCode" -> { return System.identityHashCode(p); } case "equals" -> { return p == a[0]; }
                    default -> throw new SQLFeatureNotSupportedException("MiniPg: Connection." + m.getName());
                }
            }
        }
    }

    // ================= statement =================
    static final class Stmt implements InvocationHandler {
        final Konek k; final String sql; final Map<Integer, String> param = new HashMap<>(); final List<List<String>> batch = new ArrayList<>(); boolean tutup;
        Stmt(Konek k, String sql) { this.k = k; this.sql = sql; }
        Object proxy() { return Proxy.newProxyInstance(MiniPg.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, this); }
        List<String> daftarParam() { int n = param.keySet().stream().mapToInt(i -> i).max().orElse(0); List<String> l = new ArrayList<>(); for (int i = 1; i <= n; i++) l.add(param.get(i)); return l; }

        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            String n = m.getName();
            switch (n) {
                case "setString", "setLong", "setInt", "setObject", "setBoolean", "setDouble" -> { param.put((Integer) a[0], a[1] == null ? null : String.valueOf(a[1])); return null; }
                case "setNull" -> { param.put((Integer) a[0], null); return null; }
                case "clearParameters" -> { param.clear(); return null; }
                case "executeQuery" -> { Hasil h = k.jalankan(sql != null ? sql : (String) a[0], sql != null ? daftarParam() : List.of()); return new Rs(h).proxy(); }
                case "executeUpdate" -> { if (sql != null) return k.jalankan(sql, daftarParam()).diubah;
                    k.mulaiTxn(); k.simple((String) a[0]); return 0; }
                case "execute" -> { if (sql != null) { k.jalankan(sql, daftarParam()); return true; } k.mulaiTxn(); k.simple((String) a[0]); return false; }
                case "addBatch" -> { batch.add(daftarParam()); param.clear(); return null; }
                case "executeBatch" -> { int[] r = k.jalankanBatch(sql, new ArrayList<>(batch)); batch.clear(); return r; }
                case "close" -> { tutup = true; return null; }
                case "isClosed" -> { return tutup; }
                case "toString" -> { return "MiniPg.Statement"; } case "hashCode" -> { return System.identityHashCode(p); } case "equals" -> { return p == a[0]; }
                default -> throw new SQLFeatureNotSupportedException("MiniPg: Statement." + n);
            }
        }
    }

    // ================= result set =================
    static final class Rs implements InvocationHandler {
        static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]");
        final Hasil h; int pos = -1; boolean nullTerakhir;
        Rs(Hasil h) { this.h = h; }
        Object proxy() { return Proxy.newProxyInstance(MiniPg.class.getClassLoader(), new Class<?>[] { ResultSet.class }, this); }
        String nilai(Object k) throws SQLException {
            int i = k instanceof Integer x ? x - 1 : h.kolom.indexOf(((String) k).toLowerCase());
            if (i < 0) throw new SQLException("kolom tidak ada: " + k);
            String v = h.baris.get(pos)[i]; nullTerakhir = v == null; return v;
        }
        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            switch (m.getName()) {
                case "next" -> { return ++pos < h.baris.size(); }
                case "getString" -> { return nilai(a[0]); }
                case "getLong" -> { String v = nilai(a[0]); return v == null ? 0L : Long.parseLong(v); }
                case "getInt" -> { String v = nilai(a[0]); return v == null ? 0 : Integer.parseInt(v); }
                case "getBoolean" -> { String v = nilai(a[0]); return "t".equals(v); }
                case "getObject" -> { String v = nilai(a[0]); if (v == null) return null; Class<?> c = a.length > 1 ? (Class<?>) a[1] : String.class;
                    if (c == LocalDateTime.class) return LocalDateTime.parse(v, TS); if (c == Long.class) return Long.valueOf(v); if (c == Integer.class) return Integer.valueOf(v); return v; }
                case "wasNull" -> { return nullTerakhir; }
                case "close" -> { return null; }
                case "toString" -> { return "MiniPg.ResultSet"; } case "hashCode" -> { return System.identityHashCode(p); } case "equals" -> { return p == a[0]; }
                default -> throw new SQLFeatureNotSupportedException("MiniPg: ResultSet." + m.getName());
            }
        }
    }
}
