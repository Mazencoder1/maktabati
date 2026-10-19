package sa.mazen.maktabati.model;

import java.time.LocalDate;

/** A borrower's request to be notified/first-in-line once a currently-issued book is returned. */
public class HoldRequest {

    private int id;
    private Book book;
    private Borrower borrower;
    private LocalDate requestDate;

    public HoldRequest(int id, Book book, Borrower borrower, LocalDate requestDate) {
        this.id = id;
        this.book = book;
        this.borrower = borrower;
        this.requestDate = requestDate;
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

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public boolean isExpired(int expiryDays) {
        return java.time.temporal.ChronoUnit.DAYS.between(requestDate, LocalDate.now()) > expiryDays;
    }
}
