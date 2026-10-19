package sa.mazen.maktabati.app;

import sa.mazen.maktabati.dao.Db;
import sa.mazen.maktabati.dao.Seeder;
import sa.mazen.maktabati.service.LibraryService;
import sa.mazen.maktabati.ui.LoginFrame;
import sa.mazen.maktabati.ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Application entry point: prepares the SQLite database (schema + demo seed data if
 * empty), loads the domain model into {@link LibraryService}, then shows the Swing
 * login screen. The original base project's {@code Main.java} instead ran a blocking
 * console menu loop directly against the singleton.
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        Db.initSchema();
        Seeder.seedIfEmpty();
        LibraryService.getInstance().loadFromDatabase();

        Theme.installGlobalDefaults();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
