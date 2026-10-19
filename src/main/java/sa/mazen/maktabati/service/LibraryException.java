package sa.mazen.maktabati.service;

/** Signals a business-rule violation in {@link LibraryService} (e.g. book already on hold by someone else). */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
