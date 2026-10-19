package sa.mazen.maktabati.service;

import sa.mazen.maktabati.dao.BookDao;
import sa.mazen.maktabati.dao.Db;
import sa.mazen.maktabati.dao.LibraryConfigDao;
import sa.mazen.maktabati.dao.PersonDao;
import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Clerk;
import sa.mazen.maktabati.model.Librarian;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link LibraryService}'s core business rules (checkout,
 * checkin, hold-request queueing), run against a real, throwaway SQLite database file
 * so the DAO layer is exercised too, not just mocked.
 */
class LibraryServiceTest {

    private static int counter = 0;
    private String dbFileName;

    private LibraryService service;
    private Librarian librarian;
    private Clerk clerk;
    private Borrower b1;
    private Borrower b2;
    private Borrower b3;
    private Book bookA;

    @BeforeEach
    void setUp() throws Exception {
        dbFileName = "test-maktabati-" + (counter++) + ".db";
        new File(dbFileName).delete();
        System.setProperty("maktabati.db.file", dbFileName);

        Db.initSchema();

        new LibraryConfigDao().save(new LibraryConfigDao.Config(14, 2.0, 7));

        PersonDao personDao = new PersonDao();
        Librarian lib = new Librarian(-1, "lib.test", "pw", "أمين تجريبي", "عنوان", "0500000000", 9000, 1);
        int libId = personDao.insert(lib, "LIBRARIAN");

        Clerk clk = new Clerk(-1, "clk.test", "pw", "موظف تجريبي", "عنوان", "0500000001", 5000, 1);
        int clkId = personDao.insert(clk, "CLERK");

        Borrower bo1 = new Borrower(-1, "b1.test", "pw", "مستفيد واحد", "عنوان", "0500000002");
        int b1Id = personDao.insert(bo1, "BORROWER");
        Borrower bo2 = new Borrower(-1, "b2.test", "pw", "مستفيد اثنين", "عنوان", "0500000003");
        int b2Id = personDao.insert(bo2, "BORROWER");
        Borrower bo3 = new Borrower(-1, "b3.test", "pw", "مستفيد ثلاثة", "عنوان", "0500000004");
        int b3Id = personDao.insert(bo3, "BORROWER");

        BookDao bookDao = new BookDao();
        Book book = new Book(-1, "كتاب الاختبار", "مؤلف الاختبار", "تصنيف", false);
        int bookId = bookDao.insert(book);

        LibraryService.resetInstanceForTests();
        service = LibraryService.getInstance();
        service.loadFromDatabase();

        librarian = service.getLibrarian();
        clerk = service.getClerks().get(0);
        List<Borrower> borrowers = service.getBorrowers();
        b1 = findById(borrowers, b1Id);
        b2 = findById(borrowers, b2Id);
        b3 = findById(borrowers, b3Id);
        bookA = service.getBooks().stream().filter(b -> b.getId() == bookId).findFirst().orElseThrow();
    }

    private Borrower findById(List<Borrower> borrowers, int id) {
        return borrowers.stream().filter(b -> b.getId() == id).findFirst().orElseThrow();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("maktabati.db.file");
        new File(dbFileName).delete();
    }

    @Test
    void checkoutIssuesBookAndCreatesActiveLoan() throws Exception {
        var loan = service.checkout(bookA, b1, clerk);

        assertTrue(bookA.isIssued());
        assertEquals(b1, loan.getBorrower());
        assertEquals(clerk, loan.getIssuer());
        assertFalse(loan.isReturned());
        assertTrue(service.getAllLoans().contains(loan));
        assertTrue(b1.getBorrowedBooks().contains(loan));
    }

    @Test
    void checkoutFailsWhenBookAlreadyIssued() throws Exception {
        service.checkout(bookA, b1, clerk);

        LibraryException ex = assertThrows(LibraryException.class, () -> service.checkout(bookA, b2, clerk));
        assertTrue(ex.getMessage().contains("معار"));
    }

    @Test
    void checkinReturnsBookAndClearsBorrowerActiveLoan() throws Exception {
        var loan = service.checkout(bookA, b1, clerk);

        double fine = service.checkin(loan, librarian, false);

        assertEquals(0.0, fine, 0.0001); // checked out today, nowhere near overdue
        assertFalse(bookA.isIssued());
        assertTrue(loan.isReturned());
        assertFalse(b1.getBorrowedBooks().contains(loan));
        assertEquals(librarian, loan.getReceiver());
    }

    @Test
    void holdRequestQueueMustBeServicedInFifoOrder() throws Exception {
        service.checkout(bookA, b1, clerk);               // b1 has the book
        service.placeHoldRequest(bookA, b2);               // b2 first in line
        service.placeHoldRequest(bookA, b3);               // b3 second in line

        service.checkin(service.getAllLoans().get(0), clerk, true); // b1 returns it

        // b3 tries to jump the queue -> rejected
        LibraryException ex = assertThrows(LibraryException.class, () -> service.checkout(bookA, b3, clerk));
        assertTrue(ex.getMessage().contains("الدور"));

        // b2 is first in line -> allowed, and their hold request is consumed
        var loan = service.checkout(bookA, b2, clerk);
        assertEquals(b2, loan.getBorrower());
        assertEquals(1, bookA.getHoldRequests().size());
        assertEquals(b3, bookA.getHoldRequests().get(0).getBorrower());
    }

    @Test
    void cannotPlaceHoldRequestOnABookYouAlreadyHaveBorrowed() throws Exception {
        service.checkout(bookA, b1, clerk);

        assertThrows(LibraryException.class, () -> service.placeHoldRequest(bookA, b1));
    }

    @Test
    void cannotPlaceDuplicateHoldRequestForSameBorrower() throws Exception {
        service.checkout(bookA, b1, clerk);
        service.placeHoldRequest(bookA, b2);

        assertThrows(LibraryException.class, () -> service.placeHoldRequest(bookA, b2));
    }

    @Test
    void authenticateAcceptsCorrectCredentialsAndRejectsWrongPassword() {
        assertEquals(librarian, service.authenticate("lib.test", "pw"));
        assertEquals(null, service.authenticate("lib.test", "wrong-password"));
        assertEquals(null, service.authenticate("no-such-user", "pw"));
    }
}
