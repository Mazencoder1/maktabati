package sa.mazen.maktabati.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A single checkout record of a {@link Book} by a {@link Borrower}.
 * <p>
 * Fine calculation ({@link #computeFine(int, double)}) is kept here as a pure function
 * of the loan's dates and the library's policy (deadline days / per-day fine), with no
 * console I/O or singleton access, so it can be unit-tested directly and reused by every
 * screen in the Swing UI.
 */
public class Loan {

    private int id;
    private Book book;
    private Borrower borrower;
    private Staff issuer;
    private Staff receiver;
    private LocalDate issuedDate;
    private LocalDate returnDate;
    private boolean finePaid;

    public Loan(int id, Book book, Borrower borrower, Staff issuer, Staff receiver,
                LocalDate issuedDate, LocalDate returnDate, boolean finePaid) {
        this.id = id;
        this.book = book;
        this.borrower = borrower;
        this.issuer = issuer;
        this.receiver = receiver;
        this.issuedDate = issuedDate;
        this.returnDate = returnDate;
        this.finePaid = finePaid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public Staff getIssuer() {
        return issuer;
    }

    public Staff getReceiver() {
        return receiver;
    }

    public void setReceiver(Staff receiver) {
        this.receiver = receiver;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isFinePaid() {
        return finePaid;
    }

    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    /**
     * Number of days this loan has been out, counted from the issued date to either the
     * return date (if already returned) or today (if still active).
     */
    public long daysOut(LocalDate asOf) {
        LocalDate end = returnDate != null ? returnDate : asOf;
        return ChronoUnit.DAYS.between(issuedDate, end);
    }

    /**
     * Computes the fine owed on this loan given the library's policy, as of today.
     * Pure function: no I/O, no singleton lookups - easy to unit test.
     *
     * @param returnDeadlineDays number of days a borrower may keep the book before fines start
     * @param perDayFine         fine amount (SAR) charged per day overdue
     */
    public double computeFine(int returnDeadlineDays, double perDayFine) {
        if (finePaid) {
            return 0.0;
        }
        long daysOut = daysOut(LocalDate.now());
        long overdueDays = daysOut - returnDeadlineDays;
        return overdueDays > 0 ? overdueDays * perDayFine : 0.0;
    }
}
