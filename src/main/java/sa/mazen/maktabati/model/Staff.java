package sa.mazen.maktabati.model;

/** A library employee (Librarian or Clerk). Part of the original base project's hierarchy. */
public abstract class Staff extends Person {

    private double salary;

    protected Staff(int id, String username, String password, String name, String address, String phone, double salary) {
        super(id, username, password, name, address, phone);
        this.salary = salary;
    }

    public double getSalary() {
        return salary;
    }
}
