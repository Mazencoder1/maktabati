package sa.mazen.maktabati.model;

import java.util.ArrayList;
import java.util.List;

/** A library member who can borrow books and place hold requests. */
public class Borrower extends Person {

    private final List<Loan> borrowedBooks = new ArrayList<>();
    private final List<HoldRequest> onHoldBooks = new ArrayList<>();

    public Borrower(int id, String username, String password, String name, String address, String phone) {
        super(id, username, password, name, address, phone);
    }

    public List<Loan> getBorrowedBooks() {
        return borrowedBooks;
    }

    public List<HoldRequest> getOnHoldBooks() {
        return onHoldBooks;
    }

    public void addBorrowedBook(Loan loan) {
        borrowedBooks.add(loan);
    }

    public void removeBorrowedBook(Loan loan) {
        borrowedBooks.remove(loan);
    }

    public void addHoldRequest(HoldRequest hr) {
        onHoldBooks.add(hr);
    }

    public void removeHoldRequest(HoldRequest hr) {
        onHoldBooks.remove(hr);
    }

    @Override
    public String getRoleArabicName() {
        return "مستفيد";
    }
}
