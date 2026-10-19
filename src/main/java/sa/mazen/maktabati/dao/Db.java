package sa.mazen.maktabati.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens the SQLite database file used by the whole application and makes sure the schema
 * exists. This is the direct replacement for the original base project's
 * {@code Library.makeConnection()} / embedded Apache Derby setup - a real DAO layer now
 * sits between the domain model and persistence instead of the old project's single
 * "Library" class loading/dumping everything by hand over a Derby JDBC connection.
 */
public final class Db {

    private static final String SCHEMA_FILE = "schema.sql";

    private Db() {
    }

    /** Overridable via -Dmaktabati.db.file=... so tests can point at a throwaway database. */
    private static String dbFile() {
        return System.getProperty("maktabati.db.file", "maktabati.db");
    }

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile());
        try (Statement s = con.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    /** Creates all tables (if they don't already exist) by running schema.sql. */
    public static void initSchema() throws SQLException, IOException {
        String sql = Files.readString(Path.of(SCHEMA_FILE));
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            for (String stmt : sql.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        }
    }

    /** True if the database file already has at least one book (i.e. has been seeded). */
    public static boolean isSeeded() {
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            var rs = st.executeQuery("SELECT COUNT(*) AS c FROM book");
            return rs.next() && rs.getInt("c") > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
