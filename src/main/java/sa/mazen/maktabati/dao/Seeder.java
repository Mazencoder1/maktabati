package sa.mazen.maktabati.dao;

import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Clerk;
import sa.mazen.maktabati.model.Librarian;
import sa.mazen.maktabati.model.Loan;
import sa.mazen.maktabati.model.HoldRequest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Populates a freshly created database with realistic Arabic/Saudi demo data: one
 * librarian, a couple of clerks, several borrowers, a shelf of well-known Arabic book
 * titles, some active and overdue loans (so the fines screen has something real to show)
 * and a hold-request queue. This replaces the original base project's approach of
 * manually typing data in through the console every run.
 */
public final class Seeder {

    private Seeder() {
    }

    public static void seedIfEmpty() throws SQLException {
        if (Db.isSeeded()) {
            return;
        }

        PersonDao personDao = new PersonDao();
        BookDao bookDao = new BookDao();
        LoanDao loanDao = new LoanDao();
        HoldRequestDao holdRequestDao = new HoldRequestDao();
        LibraryConfigDao configDao = new LibraryConfigDao();

        // -------- Library policy: 14 days before fines start, 2 SAR/day, holds expire after 7 days --------
        configDao.save(new LibraryConfigDao.Config(14, 2.0, 7));

        // -------- Staff --------
        Librarian librarian = new Librarian(-1, "fahad.lib", "lib123", "فهد القحطاني", "الرياض - حي النزهة", "0501112222", 9000, 1);
        int librarianId = personDao.insert(librarian, "LIBRARIAN");
        librarian.setId(librarianId);

        Clerk clerk1 = new Clerk(-1, "sara.clerk", "clk123", "سارة العتيبي", "الرياض - حي الملقا", "0503334444", 5200, 1);
        int clerk1Id = personDao.insert(clerk1, "CLERK");
        clerk1.setId(clerk1Id);

        Clerk clerk2 = new Clerk(-1, "nasser.clerk", "clk123", "ناصر الزهراني", "جدة - حي الروضة", "0505556666", 5000, 2);
        int clerk2Id = personDao.insert(clerk2, "CLERK");
        clerk2.setId(clerk2Id);

        // -------- Borrowers --------
        String[][] borrowerSeed = {
                {"mohammed.b", "bor123", "محمد الحربي", "الرياض - حي العليا", "0551112233"},
                {"noura.b", "bor123", "نورة الدوسري", "الدمام - حي الشاطئ", "0552223344"},
                {"abdullah.b", "bor123", "عبدالله الشهري", "مكة المكرمة - العزيزية", "0553334455"},
                {"reem.b", "bor123", "ريم المطيري", "الرياض - حي الياسمين", "0554445566"},
                {"khalid.b", "bor123", "خالد الغامدي", "الطائف - حي شهار", "0555556677"},
                {"lama.b", "bor123", "لمى السبيعي", "الرياض - حي الربوة", "0556667788"},
        };

        Borrower[] borrowers = new Borrower[borrowerSeed.length];
        for (int i = 0; i < borrowerSeed.length; i++) {
            String[] d = borrowerSeed[i];
            Borrower b = new Borrower(-1, d[0], d[1], d[2], d[3], d[4]);
            int id = personDao.insert(b, "BORROWER");
            b.setId(id);
            borrowers[i] = b;
        }

        // -------- Books: well-known Arabic titles, classic and modern --------
        String[][] bookSeed = {
                {"مدن الملح", "عبدالرحمن منيف", "رواية"},
                {"موسم الهجرة إلى الشمال", "الطيب صالح", "رواية"},
                {"ثلاثية غرناطة", "رضوى عاشور", "رواية"},
                {"رجال في الشمس", "غسان كنفاني", "رواية"},
                {"الأسود يليق بك", "أحلام مستغانمي", "رواية"},
                {"عزازيل", "يوسف زيدان", "رواية"},
                {"ساق البامبو", "سعود السنعوسي", "رواية"},
                {"البخلاء", "الجاحظ", "تراث"},
                {"كليلة ودمنة", "عبدالله بن المقفع", "تراث"},
                {"ألف ليلة وليلة", "مجهول", "تراث"},
                {"مقدمة ابن خلدون", "ابن خلدون", "تاريخ"},
                {"طوق الحمامة", "ابن حزم", "تراث"},
                {"العادات السبع للناس الأكثر فعالية", "ستيفن كوفي", "تطوير ذاتي"},
                {"فن اللامبالاة", "مارك مانسون", "تطوير ذاتي"},
                {"تاريخ الطبري", "الطبري", "تاريخ"},
        };

        Book[] books = new Book[bookSeed.length];
        for (int i = 0; i < bookSeed.length; i++) {
            String[] d = bookSeed[i];
            Book b = new Book(-1, d[0], d[1], d[2], false);
            int id = bookDao.insert(b);
            b.setId(id);
            books[i] = b;
        }

        // -------- Loans: mix of overdue, on-time active, and already-returned --------
        LocalDate today = LocalDate.now();

        // Overdue loan (issued 30 days ago, deadline is 14 days -> fine accrues)
        issue(bookDao, loanDao, books[0], borrowers[0], librarian, today.minusDays(30));

        // Another overdue loan under a clerk
        issue(bookDao, loanDao, books[4], borrowers[2], clerk1, today.minusDays(21));

        // Active, still within the deadline (no fine yet)
        issue(bookDao, loanDao, books[6], borrowers[1], clerk2, today.minusDays(4));
        issue(bookDao, loanDao, books[9], borrowers[3], librarian, today.minusDays(2));

        // A returned loan, fine already paid
        Loan returned = new Loan(-1, books[10], borrowers[4], clerk1, librarian,
                today.minusDays(40), today.minusDays(33), true);
        int returnedId = loanDao.insert(returned);
        returned.setId(returnedId);

        // -------- Hold requests: a queue of two borrowers waiting on an issued book --------
        HoldRequest hr1 = new HoldRequest(-1, books[0], borrowers[5], today.minusDays(2));
        int hr1Id = holdRequestDao.insert(hr1);
        hr1.setId(hr1Id);

        HoldRequest hr2 = new HoldRequest(-1, books[0], borrowers[3], today.minusDays(1));
        int hr2Id = holdRequestDao.insert(hr2);
        hr2.setId(hr2Id);
    }

    private static void issue(BookDao bookDao, LoanDao loanDao, Book book, Borrower borrower,
                               sa.mazen.maktabati.model.Staff issuer, LocalDate issuedDate) throws SQLException {
        book.setIssued(true);
        bookDao.updateIssuedStatus(book.getId(), true);
        Loan loan = new Loan(-1, book, borrower, issuer, null, issuedDate, null, false);
        int id = loanDao.insert(loan);
        loan.setId(id);
        borrower.addBorrowedBook(loan);
    }
}
