package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.service.LibraryException;
import sa.mazen.maktabati.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/** شاشة فهرس الكتب: بحث وعرض كل الكتب، وللمستفيد إمكانية طلب حجز كتاب معار. */
public class CatalogPanel extends JPanel {

    private final LibraryService service = LibraryService.getInstance();
    private final Person currentUser;
    private final DefaultTableModel model;
    private final JTable table;
    private final javax.swing.JTextField searchField = Theme.textField();
    private final JComboBox<String> fieldCombo = new JComboBox<>(new String[]{"العنوان", "المؤلف", "التصنيف"});

    public CatalogPanel(Person currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setBorder(Theme.pad(16, 16, 16, 16));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        searchBar.setBackground(Theme.BACKGROUND);
        searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchField.setColumns(22);
        JButton searchButton = Theme.primaryButton("بحث");
        JButton resetButton = Theme.secondaryButton("عرض الكل");
        searchBar.add(Theme.label("ابحث في:"));
        searchBar.add(fieldCombo);
        searchBar.add(searchField);
        searchBar.add(searchButton);
        searchBar.add(resetButton);

        if (currentUser instanceof Borrower) {
            JButton holdButton = Theme.accentButton("اطلب حجز الكتاب المحدد");
            holdButton.addActionListener(e -> placeHold());
            searchBar.add(holdButton);
        }

        add(searchBar, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"العنوان", "المؤلف", "التصنيف", "الحالة"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(scroll, BorderLayout.CENTER);

        searchButton.addActionListener(e -> doSearch());
        resetButton.addActionListener(e -> refresh());

        refresh();
    }

    private void doSearch() {
        LibraryService.SearchField field = switch (fieldCombo.getSelectedIndex()) {
            case 1 -> LibraryService.SearchField.AUTHOR;
            case 2 -> LibraryService.SearchField.SUBJECT;
            default -> LibraryService.SearchField.TITLE;
        };
        render(service.searchBooks(field, searchField.getText()));
    }

    public void refresh() {
        render(service.getBooks());
    }

    private void render(List<Book> books) {
        model.setRowCount(0);
        for (Book b : books) {
            model.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getSubject(),
                    b.isIssued() ? "معار" : "متوفر"});
        }
    }

    private void placeHold() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "اختر كتاب من الجدول أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Book> current = service.searchBooks(LibraryService.SearchField.TITLE, "");
        String title = (String) model.getValueAt(row, 0);
        Book book = current.stream().filter(b -> b.getTitle().equals(title)).findFirst().orElse(null);
        if (book == null) {
            return;
        }
        try {
            service.placeHoldRequest(book, (Borrower) currentUser);
            JOptionPane.showMessageDialog(this, "تم تسجيل طلب الحجز بنجاح.", "تم", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "خطأ في قاعدة البيانات: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
