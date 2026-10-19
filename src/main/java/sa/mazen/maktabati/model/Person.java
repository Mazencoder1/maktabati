package sa.mazen.maktabati.model;

/**
 * Base type for everyone known to the library: staff (librarian/clerk) and borrowers.
 * <p>
 * This hierarchy (Person -&gt; Staff -&gt; Librarian/Clerk, Person -&gt; Borrower) is the
 * original OOP design from the base project this repository builds on
 * (harismuneer/Library-Management-System-JAVA). It has been kept largely as-is because
 * it is a clean, well-regarded example of inheritance for this kind of domain, but the
 * fields were adapted to support a real username/password login against SQLite instead
 * of the original's numeric-ID-as-password scheme.
 */
public abstract class Person {

    private int id;
    private String username;
    private String password;
    private String name;
    private String address;
    private String phone;

    protected Person(int id, String username, String password, String name, String address, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** The Arabic, human-readable name of this person's role, used throughout the UI. */
    public abstract String getRoleArabicName();

    @Override
    public String toString() {
        return name;
    }
}
