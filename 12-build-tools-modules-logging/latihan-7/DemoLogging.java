import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

// Demo JUL dari modul (satu perubahan: tidak ada) -- dijalankan untuk membandingkan dengan TinyLog
public class DemoLogging {
    private static final Logger LOG = Logger.getLogger(DemoLogging.class.getName());

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$-7s %5$s%n");

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOG.setUseParentHandlers(false);
        LOG.addHandler(handler);
        LOG.setLevel(Level.FINE);

        LOG.info("Aplikasi mulai");
        LOG.fine("Detail diagnostik");
        LOG.finest("Terlalu rinci");
        LOG.log(Level.WARNING, "Pengguna {0} mencoba login gagal {1} kali", new Object[] {"andi", 3});
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Gagal parse", e);
        }
    }
}
