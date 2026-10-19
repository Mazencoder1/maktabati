package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Loan;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** شاشة الكتب المتأخرة والغرامات المستحقة. */
public class FinesPanel extends JPanel {

    private final LibraryService service = LibraryService.getInstance();
    private final Person currentUser;
    private final DefaultTableModel model;
    private final JLabel totalLabel = Theme.label("إجمالي الغرامات المستحقة: 0.00 ريال");

    public FinesPanel(Person currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setBorder(Theme.pad(16, 16, 16, 16));

        add(Theme.heading(currentUser instanceof Borrower ? "غراماتي وكتبي المتأخرة" : "الكتب المتأخرة والغرامات"),
                BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"الكتاب", "المستفيد", "تاريخ الاستعارة", "أيام التأخير", "الغرامة (ريال)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(scroll, BorderLayout.CENTER);

        totalLabel.setFont(Theme.FONT_LABEL_BOLD);
        totalLabel.setForeground(Theme.DANGER);
        add(totalLabel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        double total = 0;
        int deadline = service.getConfig().returnDeadlineDays();

        for (Loan l : service.getOverdueLoans()) {
            if (currentUser instanceof Borrower borrower && l.getBorrower() != borrower) {
                continue;
            }
            double fine = service.computeFine(l);
            long daysOut = l.daysOut(LocalDate.now());
            long overdueDays = daysOut - deadline;
            model.addRow(new Object[]{l.getBook().getTitle(), l.getBorrower().getName(),
                    l.getIssuedDate().format(fmt), overdueDays, String.format("%.2f", fine)});
            total += fine;
        }
        totalLabel.setText(String.format("إجمالي الغرامات المستحقة: %.2f ريال", total));
    }
}
