package sa.mazen.maktabati.model;

/** A front-desk Clerk who can check books in/out on behalf of the library. */
public class Clerk extends Staff {

    private int deskNo;

    public Clerk(int id, String username, String password, String name, String address, String phone,
                 double salary, int deskNo) {
        super(id, username, password, name, address, phone, salary);
        this.deskNo = deskNo;
    }

    public int getDeskNo() {
        return deskNo;
    }

    @Override
    public String getRoleArabicName() {
        return "موظف الاستقبال";
    }
}
