package sa.mazen.maktabati.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the fine-calculation formula on {@link Loan}. These are pure tests
 * (no database, no service, no singleton) since {@link Loan#computeFine} takes the
 * library's policy as plain parameters.
 */
class LoanTest {

    private static final int DEADLINE_DAYS = 14;
    private static final double PER_DAY_FINE = 2.0;

    private Loan loanIssuedDaysAgo(long daysAgo) {
        Book book = new Book(1, "كتاب تجريبي", "مؤلف", "تصنيف", true);
        Borrower borrower = new Borrower(1, "u1", "p1", "مستفيد تجريبي", "عنوان", "0500000000");
        Staff staff = new Clerk(2, "u2", "p2", "موظف تجريبي", "عنوان", "0500000001", 4000, 1);
        LocalDate issued = LocalDate.now().minusDays(daysAgo);
        return new Loan(1, book, borrower, staff, null, issued, null, false);
    }

    @Test
    void noFineWhenWellWithinDeadline() {
        Loan loan = loanIssuedDaysAgo(3);
        assertEquals(0.0, loan.computeFine(DEADLINE_DAYS, PER_DAY_FINE));
    }

    @Test
    void noFineExactlyAtDeadline() {
        Loan loan = loanIssuedDaysAgo(DEADLINE_DAYS);
        assertEquals(0.0, loan.computeFine(DEADLINE_DAYS, PER_DAY_FINE));
    }

    @Test
    void finesAccrueOneDayPastDeadline() {
        Loan loan = loanIssuedDaysAgo(DEADLINE_DAYS + 1);
        assertEquals(PER_DAY_FINE, loan.computeFine(DEADLINE_DAYS, PER_DAY_FINE), 0.0001);
    }

    @Test
    void finesScaleLinearlyWithOverdueDays() {
        Loan loan = loanIssuedDaysAgo(DEADLINE_DAYS + 6);
        // 6 days overdue * 2.0 SAR/day = 12.0
        assertEquals(12.0, loan.computeFine(DEADLINE_DAYS, PER_DAY_FINE), 0.0001);
    }

    @Test
    void noFineIfAlreadyMarkedPaidRegardlessOfOverdueDays() {
        Book book = new Book(1, "كتاب", "مؤلف", "تصنيف", false);
        Borrower borrower = new Borrower(1, "u1", "p1", "مستفيد", "عنوان", "0500000000");
        Staff staff = new Clerk(2, "u2", "p2", "موظف", "عنوان", "0500000001", 4000, 1);
        Loan loan = new Loan(1, book, borrower, staff, staff,
                LocalDate.now().minusDays(40), LocalDate.now().minusDays(1), true);
        assertEquals(0.0, loan.computeFine(DEADLINE_DAYS, PER_DAY_FINE));
    }

    @Test
    void daysOutCountsFromIssueToReturnDateWhenReturned() {
        Book book = new Book(1, "كتاب", "مؤلف", "تصنيف", false);
        Borrower borrower = new Borrower(1, "u1", "p1", "مستفيد", "عنوان", "0500000000");
        Staff staff = new Clerk(2, "u2", "p2", "موظف", "عنوان", "0500000001", 4000, 1);
        LocalDate issued = LocalDate.now().minusDays(10);
        LocalDate returned = LocalDate.now().minusDays(2);
        Loan loan = new Loan(1, book, borrower, staff, staff, issued, returned, false);
        assertEquals(8, loan.daysOut(LocalDate.now()));
        assertTrue(loan.isReturned());
    }
}
