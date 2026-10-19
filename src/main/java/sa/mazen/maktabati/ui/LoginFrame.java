package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * شاشة تسجيل الدخول. First new-UI screen of the project: the original base project had
 * no GUI at all, only a console menu that called {@code Library.login()}.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = Theme.textField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = Theme.label(" ");

    public LoginFrame() {
        super("مكتبتي - تسجيل الدخول");
        Theme.rtl(this);
        buildUi();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setContentPane(root);

        JPanel header = new JPanel();
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(30, 20, 30, 20));
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel logo = new JLabel("مكتبتي");
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(0.5f);
        logo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("نظام إدارة المكتبة");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(new Color(255, 255, 255, 210));
        subtitle.setAlignmentX(0.5f);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(logo);
        header.add(javax.swing.Box.createVerticalStrut(6));
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BACKGROUND);
        form.setBorder(new EmptyBorder(30, 36, 30, 36));
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 0;
        gc.weightx = 1;

        gc.gridy = 0;
        form.add(Theme.label("اسم المستخدم"), gc);

        gc.gridy = 1;
        usernameField.setPreferredSize(new Dimension(10, 38));
        form.add(usernameField, gc);

        gc.gridy = 2;
        form.add(Theme.label("كلمة المرور"), gc);

        gc.gridy = 3;
        passwordField.setFont(Theme.FONT_LABEL);
        passwordField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        passwordField.setPreferredSize(new Dimension(10, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        form.add(passwordField, gc);

        gc.gridy = 4;
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(errorLabel, gc);

        gc.gridy = 5;
        gc.insets = new Insets(14, 0, 6, 0);
        JButton loginButton = Theme.primaryButton("تسجيل الدخول");
        loginButton.addActionListener(e -> attemptLogin());
        form.add(loginButton, gc);

        gc.gridy = 6;
        JLabel hint = Theme.subtitle("جرب: fahad.lib / lib123  (أمين) - sara.clerk / clk123 (موظف) - mohammed.b / bor123 (مستفيد)");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        hint.setFont(Theme.FONT_LABEL.deriveFont(11f));
        form.add(hint, gc);

        root.add(form, BorderLayout.CENTER);

        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    /** Pre-fills the form (used by the screenshot tool to show a realistic login screen). */
    public void prefill(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        Person person = LibraryService.getInstance().authenticate(username, password);
        if (person == null) {
            errorLabel.setText("اسم المستخدم أو كلمة المرور غير صحيحة");
            return;
        }

        errorLabel.setText(" ");
        MainDashboardFrame dashboard = new MainDashboardFrame(person);
        dashboard.setVisible(true);
        dispose();
    }
}
