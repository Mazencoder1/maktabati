package sa.mazen.maktabati.ui;

import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.HoldRequest;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * شاشة قائمة طلبات الحجز. أمين المكتبة والموظفون يشاهدون كل الطلبات على كل الكتب،
 * والمستفيد يشاهد طلباته هو فقط ويقدر يلغيها.
 */
public class HoldRequestsPanel extends JPanel {

    private final LibraryService service = LibraryService.getInstance();
    private final Person currentUser;
    private final DefaultTableModel model;
    private final JTable table;

    public HoldRequestsPanel(Person currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setBorder(Theme.pad(16, 16, 16, 16));

        add(Theme.heading(currentUser instanceof Borrower ? "طلبات حجزي" : "قائمة طلبات الحجز"), BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"الترتيب", "الكتاب", "المستفيد", "تاريخ الطلب"}, 0) {
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

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.setBackground(Theme.BACKGROUND);
        bottom.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JButton cancelButton = Theme.dangerButton("إلغاء الطلب المحدد");
        cancelButton.addActionListener(e -> cancelSelected());
        bottom.add(cancelButton);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<HoldRequest> all = service.getAllHoldRequests();

        int position = 0;
        String lastBookTitle = null;
        for (HoldRequest hr : all) {
            if (currentUser instanceof Borrower borrower && hr.getBorrower() != borrower) {
                continue;
            }
            if (!hr.getBook().getTitle().equals(lastBookTitle)) {
                position = 1;
                lastBookTitle = hr.getBook().getTitle();
            } else {
                position++;
            }
            model.addRow(new Object[]{position, hr.getBook().getTitle(), hr.getBorrower().getName(),
                    hr.getRequestDate().format(fmt)});
        }
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "اختر طلب من الجدول أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bookTitle = (String) model.getValueAt(row, 1);
        String borrowerName = (String) model.getValueAt(row, 2);

        HoldRequest target = null;
        for (HoldRequest hr : service.getAllHoldRequests()) {
            if (hr.getBook().getTitle().equals(bookTitle) && hr.getBorrower().getName().equals(borrowerName)) {
                target = hr;
                break;
            }
        }
        if (target == null) {
            return;
        }
        try {
            service.cancelHoldRequest(target);
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "خطأ في قاعدة البيانات: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
