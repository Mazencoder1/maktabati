package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Loan;
import sa.mazen.maktabati.model.Staff;
import sa.mazen.maktabati.service.LibraryException;
import sa.mazen.maktabati.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/** شاشة الاستعارة والاسترجاع - لموظفي المكتبة (أمين المكتبة والموظفين) فقط. */
public class CheckoutPanel extends JPanel {

    private final LibraryService service = LibraryService.getInstance();
    private final Staff currentStaff;
    private final Runnable onDataChanged;

    private final JComboBox<Book> bookCombo = new JComboBox<>();
    private final JComboBox<Borrower> borrowerCombo = new JComboBox<>();

    private DefaultTableModel loanModel;
    private JTable loanTable;
    private final JLabel fineLabel = Theme.label("الغرامة: 0.00 ريال");
    private final JCheckBox finePaidCheck = new JCheckBox("تم دفع الغرامة");

    public CheckoutPanel(Staff currentStaff, Runnable onDataChanged) {
        this.currentStaff = currentStaff;
        this.onDataChanged = onDataChanged;

        setLayout(new GridLayout(1, 2, 16, 0));
        setBackground(Theme.BACKGROUND);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setBorder(Theme.pad(16, 16, 16, 16));

        add(buildIssueCard());
        add(buildReturnCard());

        refresh();
    }

    private JPanel buildIssueCard() {
        JPanel card = Theme.card();
        card.setLayout(new java.awt.GridBagLayout());
        var gc = new java.awt.GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gc.insets = new java.awt.Insets(6, 0, 6, 0);
        gc.gridx = 0;
        gc.weightx = 1;

        gc.gridy = 0;
        card.add(Theme.heading("تسليم كتاب"), gc);

        gc.gridy = 1;
        card.add(Theme.label("الكتاب"), gc);
        gc.gridy = 2;
        bookCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        card.add(bookCombo, gc);

        gc.gridy = 3;
        card.add(Theme.label("المستفيد"), gc);
        gc.gridy = 4;
        borrowerCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        card.add(borrowerCombo, gc);

        gc.gridy = 5;
        gc.insets = new java.awt.Insets(16, 0, 6, 0);
        JButton issueButton = Theme.primaryButton("تسليم الكتاب للمستفيد");
        issueButton.addActionListener(e -> doIssue());
        card.add(issueButton, gc);

        return card;
    }

    private JPanel buildReturnCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = Theme.heading("استرجاع كتاب");
        card.add(heading, BorderLayout.NORTH);

        DefaultTableModel activeLoans = new DefaultTableModel(
                new Object[]{"الكتاب", "المستفيد", "تاريخ الاستعارة"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(activeLoans);
        Theme.styleTable(table);
        this.loanModel = activeLoans;
        this.loanTable = table;
        table.getSelectionModel().addListSelectionListener(e -> updateFinePreview(table));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 4));
        bottom.setBackground(Theme.CARD);
        bottom.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fineLabel.setFont(Theme.FONT_LABEL_BOLD);
        bottom.add(fineLabel);
        bottom.add(finePaidCheck);
        JButton returnButton = Theme.accentButton("استرجاع الكتاب");
        returnButton.addActionListener(e -> doReturn(table));
        bottom.add(returnButton);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    public void refresh() {
        bookCombo.removeAllItems();
        for (Book b : service.getBooks()) {
            if (!b.isIssued()) {
                bookCombo.addItem(b);
            }
        }
        borrowerCombo.removeAllItems();
        for (Borrower b : service.getBorrowers()) {
            borrowerCombo.addItem(b);
        }

        loanModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Loan l : service.getAllLoans()) {
            if (!l.isReturned()) {
                loanModel.addRow(new Object[]{l.getBook().getTitle(), l.getBorrower().getName(),
                        l.getIssuedDate().format(fmt)});
            }
        }
        fineLabel.setText("الغرامة: 0.00 ريال");
        finePaidCheck.setSelected(false);
    }

    private void updateFinePreview(JTable table) {
        Loan loan = selectedLoan(table);
        if (loan == null) {
            fineLabel.setText("الغرامة: 0.00 ريال");
            return;
        }
        double fine = service.computeFine(loan);
        fineLabel.setText(String.format("الغرامة: %.2f ريال", fine));
        finePaidCheck.setSelected(fine <= 0);
    }

    private Loan selectedLoan(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String title = (String) loanModel.getValueAt(row, 0);
        String borrowerName = (String) loanModel.getValueAt(row, 1);
        for (Loan l : service.getAllLoans()) {
            if (!l.isReturned() && l.getBook().getTitle().equals(title) && l.getBorrower().getName().equals(borrowerName)) {
                return l;
            }
        }
        return null;
    }

    private void doIssue() {
        Book book = (Book) bookCombo.getSelectedItem();
        Borrower borrower = (Borrower) borrowerCombo.getSelectedItem();
        if (book == null || borrower == null) {
            JOptionPane.showMessageDialog(this, "اختر كتاب ومستفيد.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            service.checkout(book, borrower, currentStaff);
            JOptionPane.showMessageDialog(this,
                    "تم تسليم \"" + book.getTitle() + "\" إلى " + borrower.getName() + " بنجاح.",
                    "تم", JOptionPane.INFORMATION_MESSAGE);
            refresh();
            if (onDataChanged != null) onDataChanged.run();
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "خطأ في قاعدة البيانات: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doReturn(JTable table) {
        Loan loan = selectedLoan(table);
        if (loan == null) {
            JOptionPane.showMessageDialog(this, "اختر كتاب من الجدول للاسترجاع.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double fine = service.checkin(loan, currentStaff, finePaidCheck.isSelected());
            String msg = fine > 0
                    ? String.format("تم استرجاع الكتاب. الغرامة: %.2f ريال (%s).", fine,
                        finePaidCheck.isSelected() ? "تم الدفع" : "لم تُدفع بعد")
                    : "تم استرجاع الكتاب بدون غرامة.";
            JOptionPane.showMessageDialog(this, msg, "تم", JOptionPane.INFORMATION_MESSAGE);
            refresh();
            if (onDataChanged != null) onDataChanged.run();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "خطأ في قاعدة البيانات: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
