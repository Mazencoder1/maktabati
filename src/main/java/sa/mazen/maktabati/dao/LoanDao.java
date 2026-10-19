package sa.mazen.maktabati.dao;

import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Loan;
import sa.mazen.maktabati.model.Person;
import sa.mazen.maktabati.model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** SQLite persistence for {@link Loan} rows (checkout / checkin history). */
public class LoanDao {

    /**
     * Loads every loan row, re-linking each one to the already-loaded Book/Person objects
     * passed in (keyed by id) so the whole application works against one shared, in-memory
     * object graph rather than re-creating duplicate model objects per query.
     */
    public List<Loan> findAll(Map<Integer, Book> booksById, Map<Integer, Person> peopleById) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM loan ORDER BY id")) {
            while (rs.next()) {
                Book book = booksById.get(rs.getInt("book_id"));
                Borrower borrower = (Borrower) peopleById.get(rs.getInt("borrower_id"));
                Staff issuer = (Staff) peopleById.get(rs.getInt("issuer_id"));
                Integer receiverId = rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null;
                Staff receiver = receiverId != null ? (Staff) peopleById.get(receiverId) : null;
                LocalDate issuedDate = LocalDate.parse(rs.getString("issued_date"));
                String returnDateStr = rs.getString("return_date");
                LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : null;
                boolean finePaid = rs.getInt("fine_paid") == 1;

                loans.add(new Loan(rs.getInt("id"), book, borrower, issuer, receiver, issuedDate, returnDate, finePaid));
            }
        }
        return loans;
    }

    public int insert(Loan loan) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO loan (book_id, borrower_id, issuer_id, receiver_id, issued_date, return_date, fine_paid) " +
                             "VALUES (?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBook().getId());
            ps.setInt(2, loan.getBorrower().getId());
            ps.setInt(3, loan.getIssuer().getId());
            if (loan.getReceiver() != null) {
                ps.setInt(4, loan.getReceiver().getId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, loan.getIssuedDate().toString());
            ps.setString(6, loan.getReturnDate() != null ? loan.getReturnDate().toString() : null);
            ps.setInt(7, loan.isFinePaid() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Persists the check-in: receiver, return date and fine-paid status. */
    public void updateReturn(Loan loan) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE loan SET receiver_id = ?, return_date = ?, fine_paid = ? WHERE id = ?")) {
            ps.setInt(1, loan.getReceiver().getId());
            ps.setString(2, loan.getReturnDate().toString());
            ps.setInt(3, loan.isFinePaid() ? 1 : 0);
            ps.setInt(4, loan.getId());
            ps.executeUpdate();
        }
    }
}
