package sa.mazen.maktabati.tools;

import sa.mazen.maktabati.dao.Db;
import sa.mazen.maktabati.dao.Seeder;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.service.LibraryService;
import sa.mazen.maktabati.ui.LoginFrame;
import sa.mazen.maktabati.ui.MainDashboardFrame;
import sa.mazen.maktabati.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Developer-only utility (not part of the running application) used to generate the
 * README screenshots from the real Swing UI with real seeded data, instead of mockups.
 * It renders each JFrame to an off-screen image via {@code Component.printAll}, which
 * works reliably regardless of the session's display state.
 */
public final class ScreenshotTool {

    public static void main(String[] args) throws Exception {
        Db.initSchema();
        Seeder.seedIfEmpty();
        LibraryService.getInstance().loadFromDatabase();
        Theme.installGlobalDefaults();

        new File("screenshots").mkdirs();

        // 1) Login screen
        LoginFrame login = new LoginFrame();
        login.prefill("fahad.lib", "lib123");
        capture(login, "screenshots/02-login.png");
        login.dispose();

        Person librarian = LibraryService.getInstance().getLibrarian();

        // 2) Main dashboard / catalog -> hero screenshot at project root
        MainDashboardFrame dashboard = new MainDashboardFrame(librarian);
        dashboard.selectTabByTitle("الفهرس");
        capture(dashboard, "screenshot.png");

        // 3) Checkout / checkin screen with real data
        dashboard.selectTabByTitle("الاستعارة والاسترجاع");
        capture(dashboard, "screenshots/03-checkout.png");

        // 4) Fines / overdue screen
        dashboard.selectTabByTitle("الغرامات");
        capture(dashboard, "screenshots/04-fines.png");
        dashboard.dispose();

        System.out.println("Screenshots captured successfully.");
        System.exit(0);
    }

    private static void capture(JFrame frame, String path) throws Exception {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // let layout/paint settle before capturing
        Thread.sleep(400);

        BufferedImage image = new BufferedImage(
                Math.max(frame.getWidth(), 100), Math.max(frame.getHeight(), 100), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        frame.printAll(g);
        g.dispose();

        ImageIO.write(image, "png", new File(path));
        System.out.println("Saved " + path + " (" + image.getWidth() + "x" + image.getHeight() + ")");
    }
}
