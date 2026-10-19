package sa.mazen.maktabati.dao;

import sa.mazen.maktabati.model.Borrower;
import sa.mazen.maktabati.model.Clerk;
import sa.mazen.maktabati.model.Librarian;
import sa.mazen.maktabati.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQLite persistence for every kind of {@link Person} (librarian, clerks, borrowers). */
public class PersonDao {

    public List<Person> findAll() throws SQLException {
        List<Person> people = new ArrayList<>();
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM person ORDER BY id")) {
            while (rs.next()) {
                people.add(map(rs));
            }
        }
        return people;
    }

    public Person findByUsername(String username) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM person WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(Person p, String role) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO person (username, password, name, address, phone, role, salary, desk_no, office_no) " +
                             "VALUES (?,?,?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getUsername());
            ps.setString(2, p.getPassword());
            ps.setString(3, p.getName());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getPhone());
            ps.setString(6, role);

            if (p instanceof Clerk c) {
                ps.setDouble(7, c.getSalary());
                ps.setInt(8, c.getDeskNo());
                ps.setNull(9, java.sql.Types.INTEGER);
            } else if (p instanceof Librarian l) {
                ps.setDouble(7, l.getSalary());
                ps.setNull(8, java.sql.Types.INTEGER);
                ps.setInt(9, l.getOfficeNo());
            } else {
                ps.setNull(7, java.sql.Types.REAL);
                ps.setNull(8, java.sql.Types.INTEGER);
                ps.setNull(9, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private Person map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String name = rs.getString("name");
        String address = rs.getString("address");
        String phone = rs.getString("phone");
        String role = rs.getString("role");

        return switch (role) {
            case "LIBRARIAN" -> new Librarian(id, username, password, name, address, phone,
                    rs.getDouble("salary"), rs.getInt("office_no"));
            case "CLERK" -> new Clerk(id, username, password, name, address, phone,
                    rs.getDouble("salary"), rs.getInt("desk_no"));
            default -> new Borrower(id, username, password, name, address, phone);
        };
    }
}
