package sa.mazen.maktabati.dao;

import sa.mazen.maktabati.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQLite persistence for {@link Book} rows. */
public class BookDao {

    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM book ORDER BY id")) {
            while (rs.next()) {
                books.add(map(rs));
            }
        }
        return books;
    }

    public int insert(Book b) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO book (title, author, subject, is_issued) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getSubject());
            ps.setInt(4, b.isIssued() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void updateIssuedStatus(int bookId, boolean issued) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE book SET is_issued = ? WHERE id = ?")) {
            ps.setInt(1, issued ? 1 : 0);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                rs.getString("subject"), rs.getInt("is_issued") == 1);
    }
}
