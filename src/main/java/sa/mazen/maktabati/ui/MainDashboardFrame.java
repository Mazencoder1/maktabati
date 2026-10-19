package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.model.Staff;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * لوحة التحكم الرئيسية، بتبويبات مختلفة حسب دور المستخدم (أمين مكتبة / موظف / مستفيد).
 * This is the new role-based dashboard the base project never had (it only offered a
 * flat console menu).
 */
public class MainDashboardFrame extends JFrame {

    private final Person currentUser;
    private CatalogPanel catalogPanel;
    private CheckoutPanel checkoutPanel;
    private HoldRequestsPanel holdRequestsPanel;
    private FinesPanel finesPanel;
    private JTabbedPane tabbedPane;

    public MainDashboardFrame(Person currentUser) {
        super("مكتبتي - لوحة التحكم");
        this.currentUser = currentUser;
        Theme.rtl(this);
        buildUi();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel welcome = new JLabel("مرحباً، " + currentUser.getName() + "  -  " + currentUser.getRoleArabicName());
        welcome.setFont(Theme.FONT_HEADING);
        welcome.setForeground(Color.WHITE);
        header.add(welcome, BorderLayout.EAST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        right.setOpaque(false);
        javax.swing.JButton logout = Theme.secondaryButton("تسجيل الخروج");
        logout.setBackground(Theme.PRIMARY);
        logout.setForeground(Color.WHITE);
        logout.setBorder(javax.swing.BorderFactory.createLineBorder(Color.WHITE, 1, true));
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        right.add(logout);
        header.add(right, BorderLayout.WEST);

        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_LABEL_BOLD);
        tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tabs.setBackground(Theme.BACKGROUND);

        catalogPanel = new CatalogPanel(currentUser);
        tabs.addTab("الفهرس", catalogPanel);

        if (currentUser instanceof Staff staff) {
            checkoutPanel = new CheckoutPanel(staff, this::refreshAll);
            tabs.addTab("الاستعارة والاسترجاع", checkoutPanel);
        }

        holdRequestsPanel = new HoldRequestsPanel(currentUser);
        tabs.addTab(currentUser instanceof Staff ? "طلبات الحجز" : "حجوزاتي", holdRequestsPanel);

        finesPanel = new FinesPanel(currentUser);
        tabs.addTab(currentUser instanceof Staff ? "الغرامات" : "غراماتي", finesPanel);

        tabs.addChangeListener(e -> refreshAll());
        this.tabbedPane = tabs;
        return tabs;
    }

    /** Used by the screenshot-capture tool to show a specific tab before rendering. */
    public void selectTabByTitle(String title) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(title)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }
    }

    private void refreshAll() {
        if (catalogPanel != null) catalogPanel.refresh();
        if (checkoutPanel != null) checkoutPanel.refresh();
        if (holdRequestsPanel != null) holdRequestsPanel.refresh();
        if (finesPanel != null) finesPanel.refresh();
    }
}
