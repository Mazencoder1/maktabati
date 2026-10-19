package sa.mazen.maktabati.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Persists the library's lending policy (return deadline, fine rate, hold expiry). */
public class LibraryConfigDao {

    public record Config(int returnDeadlineDays, double perDayFine, int holdRequestExpiryDays) {
    }

    public Config load() throws SQLException {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM library_config WHERE id = 1")) {
            if (rs.next()) {
                return new Config(rs.getInt("return_deadline_days"), rs.getDouble("per_day_fine"),
                        rs.getInt("hold_request_expiry"));
            }
        }
        // sensible defaults if never seeded
        return new Config(14, 2.0, 7);
    }

    public void save(Config config) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO library_config (id, return_deadline_days, per_day_fine, hold_request_expiry) " +
                             "VALUES (1, ?, ?, ?) " +
                             "ON CONFLICT(id) DO UPDATE SET return_deadline_days = excluded.return_deadline_days, " +
                             "per_day_fine = excluded.per_day_fine, hold_request_expiry = excluded.hold_request_expiry")) {
            ps.setInt(1, config.returnDeadlineDays());
            ps.setDouble(2, config.perDayFine());
            ps.setInt(3, config.holdRequestExpiryDays());
            ps.executeUpdate();
        }
    }
}
