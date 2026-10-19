package sa.mazen.maktabati.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Window;

/**
 * Small, deliberate visual theme for the whole Swing UI (colors, fonts, spacing helpers)
 * so the application looks like a designed desktop app rather than default-Swing gray
 * boxes. Every screen is also forced to right-to-left orientation here, since the whole
 * UI is in Saudi-dialect Arabic.
 */
public final class Theme {

    public static final Color PRIMARY = new Color(0x0F6E64);
    public static final Color PRIMARY_DARK = new Color(0x0A4B44);
    public static final Color ACCENT = new Color(0xC9972B);
    public static final Color BACKGROUND = new Color(0xF6F1E7);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x22312B);
    public static final Color MUTED = new Color(0x6B7A74);
    public static final Color DANGER = new Color(0xB3261E);
    public static final Color SUCCESS = new Color(0x1E7A46);
    public static final Color BORDER = new Color(0xDCD3BF);

    public static final String FONT_FAMILY = "Tahoma";

    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.PLAIN, 15);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_LABEL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_TABLE = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEADER = new Font(FONT_FAMILY, Font.BOLD, 13);

    private Theme() {
    }

    public static void installGlobalDefaults() {
        try {
            UIManager.put("ToolTip.font", FONT_LABEL);
            UIManager.put("OptionPane.messageFont", FONT_LABEL);
            UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
        } catch (Exception ignored) {
            // Cosmetic only - never fatal if a UIManager key is unavailable.
        }
    }

    /** Applies RTL orientation recursively-friendly defaults to a top-level window. */
    public static void rtl(Window window) {
        window.setLocale(new java.util.Locale("ar", "SA"));
        window.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(PRIMARY_DARK);
        return label;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(PRIMARY_DARK);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT);
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, BACKGROUND.darker(), TEXT);
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY_DARK);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setFont(FONT_BUTTON);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(FONT_LABEL);
        field.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(28);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.setSelectionBackground(PRIMARY.brighter());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        header.setReorderingAllowed(false);
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}
