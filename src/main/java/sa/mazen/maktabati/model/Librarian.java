package sa.mazen.maktabati.model;

/** The library's single Librarian. From the original base project's design (one per library). */
public class Librarian extends Staff {

    private int officeNo;

    public Librarian(int id, String username, String password, String name, String address, String phone,
                      double salary, int officeNo) {
        super(id, username, password, name, address, phone, salary);
        this.officeNo = officeNo;
    }

    public int getOfficeNo() {
        return officeNo;
    }

    @Override
    public String getRoleArabicName() {
        return "أمين المكتبة";
    }
}
