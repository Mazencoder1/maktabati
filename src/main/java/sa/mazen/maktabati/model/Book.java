package sa.mazen.maktabati.model;

import java.util.ArrayList;
import java.util.List;

/** A book title held by the library, with its hold-request queue. */
public class Book {

    private int id;
    private String title;
    private String author;
    private String subject;
    private boolean issued;
    private final List<HoldRequest> holdRequests = new ArrayList<>();

    public Book(int id, String title, String author, String subject, boolean issued) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.subject = subject;
        this.issued = issued;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isIssued() {
        return issued;
    }

    public void setIssued(boolean issued) {
        this.issued = issued;
    }

    public List<HoldRequest> getHoldRequests() {
        return holdRequests;
    }

    @Override
    public String toString() {
        return title + " - " + author;
    }
}
