package sa.mazen.maktabati.dao;

import sa.mazen.maktabati.model.Book;
import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.HoldRequest;
import sa.mazen.maktabati.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** SQLite persistence for {@link HoldRequest} rows. */
public class HoldRequestDao {

    public List<HoldRequest> findAll(Map<Integer, Book> booksById, Map<Integer, Person> peopleById) throws SQLException {
        List<HoldRequest> requests = new ArrayList<>();
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM hold_request ORDER BY id")) {
            while (rs.next()) {
                Book book = booksById.get(rs.getInt("book_id"));
                Borrower borrower = (Borrower) peopleById.get(rs.getInt("borrower_id"));
                LocalDate requestDate = LocalDate.parse(rs.getString("request_date"));
                requests.add(new HoldRequest(rs.getInt("id"), book, borrower, requestDate));
            }
        }
        return requests;
    }

    public int insert(HoldRequest hr) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO hold_request (book_id, borrower_id, request_date) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, hr.getBook().getId());
            ps.setInt(2, hr.getBorrower().getId());
            ps.setString(3, hr.getRequestDate().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM hold_request WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
