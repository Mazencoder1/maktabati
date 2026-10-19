package sa.mazen.maktabati.service;

import sa.mazen.maktabati.dao.BookDao;
import sa.mazen.maktabati.dao.HoldRequestDao;
import sa.mazen.maktabati.dao.LibraryConfigDao;
import sa.mazen.maktabati.dao.LoanDao;
import sa.mazen.maktabati.dao.PersonDao;
import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Clerk;
import sa.mazen.maktabati.model.HoldRequest;
import sa.mazen.maktabati.model.Librarian;
import sa.mazen.maktabati.model.Loan;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.model.Staff;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The library's business/domain logic, evolved from the original base project's
 * {@code Library} singleton (harismuneer/Library-Management-System-JAVA).
 * <p>
 * What changed from the original:
 * <ul>
 *   <li>No console I/O (no {@code Scanner}/{@code System.out}) anywhere in this class -
 *       every method takes its arguments and returns/throws a result, which is what makes
 *       it reusable from both a console and the new Swing UI, and directly unit-testable.</li>
 *   <li>Persistence is delegated to a proper DAO layer (SQLite) instead of the original's
 *       hand-written Derby load/dump pair ({@code populateLibrary}/{@code fillItBack}).
 *       Every mutation here is written through to the database immediately.</li>
 *   <li>Fine calculation lives on {@link Loan#computeFine} as a pure function, and this
 *       service only supplies the library's configured policy to it.</li>
 * </ul>
 * The domain model shape (Library as a singleton holding books/persons/loans, hold-request
 * queues per book) is kept from the original design because it is a reasonable, well-tested
 * shape for this problem.
 */
public class LibraryService {

    private static LibraryService instance;

    public static synchronized LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    /** Test-only hook: forces a brand-new singleton so tests can run against an isolated database. */
    public static synchronized void resetInstanceForTests() {
        instance = new LibraryService();
    }

    private final PersonDao personDao = new PersonDao();
    private final BookDao bookDao = new BookDao();
    private final LoanDao loanDao = new LoanDao();
    private final HoldRequestDao holdRequestDao = new HoldRequestDao();
    private final LibraryConfigDao configDao = new LibraryConfigDao();

    private Librarian librarian;
    private final List<Clerk> clerks = new ArrayList<>();
    private final List<Borrower> borrowers = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private LibraryConfigDao.Config config;

    private boolean loaded = false;

    private LibraryService() {
    }

    /** Loads the whole object graph from SQLite into memory. Call once at startup. */
    public synchronized void loadFromDatabase() throws SQLException {
        librarian = null;
        clerks.clear();
        borrowers.clear();
        books.clear();
        loans.clear();

        config = configDao.load();

        Map<Integer, Person> peopleById = new LinkedHashMap<>();
        for (Person p : personDao.findAll()) {
            peopleById.put(p.getId(), p);
            if (p instanceof Librarian l) {
                librarian = l;
            } else if (p instanceof Clerk c) {
                clerks.add(c);
            } else if (p instanceof Borrower b) {
                borrowers.add(b);
            }
        }

        Map<Integer, Book> booksById = new LinkedHashMap<>();
        for (Book b : bookDao.findAll()) {
            booksById.put(b.getId(), b);
            books.add(b);
        }

        for (Loan loan : loanDao.findAll(booksById, peopleById)) {
            loans.add(loan);
            if (!loan.isReturned()) {
                loan.getBorrower().addBorrowedBook(loan);
            }
        }

        for (HoldRequest hr : holdRequestDao.findAll(booksById, peopleById)) {
            hr.getBook().getHoldRequests().add(hr);
            hr.getBorrower().addHoldRequest(hr);
        }

        loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /* --------------------------- Authentication --------------------------- */

    public Person authenticate(String username, String password) {
        for (Person p : allPeople()) {
            if (p.getUsername().equalsIgnoreCase(username) && p.getPassword().equals(password)) {
                return p;
            }
        }
        return null;
    }

    private List<Person> allPeople() {
        List<Person> all = new ArrayList<>();
        if (librarian != null) {
            all.add(librarian);
        }
        all.addAll(clerks);
        all.addAll(borrowers);
        return all;
    }

    /* --------------------------- Simple getters --------------------------- */

    public Librarian getLibrarian() {
        return librarian;
    }

    public List<Clerk> getClerks() {
        return clerks;
    }

    public List<Borrower> getBorrowers() {
        return borrowers;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Loan> getAllLoans() {
        return loans;
    }

    public LibraryConfigDao.Config getConfig() {
        return config;
    }

    /* --------------------------- Search --------------------------- */

    public enum SearchField { TITLE, AUTHOR, SUBJECT }

    public List<Book> searchBooks(SearchField field, String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            return new ArrayList<>(books);
        }
        List<Book> result = new ArrayList<>();
        for (Book b : books) {
            String value = switch (field) {
                case TITLE -> b.getTitle();
                case AUTHOR -> b.getAuthor();
                case SUBJECT -> b.getSubject();
            };
            if (value != null && value.toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    /* --------------------------- Checkout / Checkin --------------------------- */

    /**
     * Checks a book out to a borrower. Mirrors the original base project's
     * {@code Book.issueBook} rule set (can't issue an already-issued book; if there's a
     * hold-request queue, only the borrower at the front of the queue may check it out)
     * but as a pure method that throws {@link LibraryException} instead of printing to
     * the console and prompting for input.
     */
    public synchronized Loan checkout(Book book, Borrower borrower, Staff staff) throws LibraryException, SQLException {
        expireOldHoldRequests(book);

        if (book.isIssued()) {
            throw new LibraryException("الكتاب \"" + book.getTitle() + "\" معار حالياً. تقدر تطلب حجزه بالدور.");
        }

        List<HoldRequest> queue = book.getHoldRequests();
        if (!queue.isEmpty()) {
            HoldRequest first = queue.get(0);
            if (first.getBorrower() != borrower) {
                throw new LibraryException("فيه طلبات حجز سابقة على هذا الكتاب. المستفيد صاحب الدور الأول هو: "
                        + first.getBorrower().getName());
            }
            // the requester is first in line: their hold is consumed by this checkout
            removeHoldRequestInternal(first);
        }

        book.setIssued(true);
        bookDao.updateIssuedStatus(book.getId(), true);

        Loan loan = new Loan(-1, book, borrower, staff, null, LocalDate.now(), null, false);
        int id = loanDao.insert(loan);
        loan.setId(id);
        loans.add(loan);
        borrower.addBorrowedBook(loan);
        return loan;
    }

    /**
     * Checks a loan's book back in. Computes the fine with the library's configured
     * policy and records whether it was paid.
     *
     * @return the fine amount computed at the moment of check-in (0 if none)
     */
    public synchronized double checkin(Loan loan, Staff staff, boolean finePaidNow) throws SQLException {
        double fine = computeFine(loan);

        loan.setReturnDate(LocalDate.now());
        loan.setReceiver(staff);
        loan.setFinePaid(fine <= 0 || finePaidNow);

        loan.getBook().setIssued(false);
        bookDao.updateIssuedStatus(loan.getBook().getId(), false);

        loan.getBorrower().removeBorrowedBook(loan);
        loanDao.updateReturn(loan);

        return fine;
    }

    /* --------------------------- Hold requests --------------------------- */

    public synchronized HoldRequest placeHoldRequest(Book book, Borrower borrower) throws LibraryException, SQLException {
        for (Loan l : borrower.getBorrowedBooks()) {
            if (l.getBook() == book) {
                throw new LibraryException("أنت مستعير هذا الكتاب حالياً.");
            }
        }
        for (HoldRequest hr : book.getHoldRequests()) {
            if (hr.getBorrower() == borrower) {
                throw new LibraryException("عندك طلب حجز قائم على هذا الكتاب.");
            }
        }

        HoldRequest hr = new HoldRequest(-1, book, borrower, LocalDate.now());
        int id = holdRequestDao.insert(hr);
        hr.setId(id);
        book.getHoldRequests().add(hr);
        borrower.addHoldRequest(hr);
        return hr;
    }

    public synchronized void cancelHoldRequest(HoldRequest hr) throws SQLException {
        removeHoldRequestInternal(hr);
    }

    private void removeHoldRequestInternal(HoldRequest hr) throws SQLException {
        hr.getBook().getHoldRequests().remove(hr);
        hr.getBorrower().removeHoldRequest(hr);
        holdRequestDao.delete(hr.getId());
    }

    private void expireOldHoldRequests(Book book) throws SQLException {
        List<HoldRequest> expired = new ArrayList<>();
        for (HoldRequest hr : book.getHoldRequests()) {
            if (hr.isExpired(config.holdRequestExpiryDays())) {
                expired.add(hr);
            }
        }
        for (HoldRequest hr : expired) {
            removeHoldRequestInternal(hr);
        }
    }

    public List<HoldRequest> getAllHoldRequests() {
        List<HoldRequest> all = new ArrayList<>();
        for (Book b : books) {
            all.addAll(b.getHoldRequests());
        }
        all.sort(Comparator.comparing(HoldRequest::getRequestDate));
        return all;
    }

    /* --------------------------- Fines --------------------------- */

    public double computeFine(Loan loan) {
        return loan.computeFine(config.returnDeadlineDays(), config.perDayFine());
    }

    /** Active (not yet returned) loans whose fine is currently greater than zero. */
    public List<Loan> getOverdueLoans() {
        List<Loan> overdue = new ArrayList<>();
        for (Loan l : loans) {
            if (!l.isReturned() && computeFine(l) > 0) {
                overdue.add(l);
            }
        }
        return overdue;
    }
}
