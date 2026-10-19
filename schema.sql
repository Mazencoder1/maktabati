-- مكتبتي - SQLite schema
-- Replaces the original base project's embedded Apache Derby database.
-- One flat "person" table carries the common Person fields plus the few
-- staff-only / role-only columns (nullable), which keeps the DAO layer simple
-- while the Java side still models Person/Staff/Librarian/Clerk/Borrower as a
-- real inheritance hierarchy.

CREATE TABLE IF NOT EXISTS person (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    username  TEXT NOT NULL UNIQUE,
    password  TEXT NOT NULL,
    name      TEXT NOT NULL,
    address   TEXT,
    phone     TEXT,
    role      TEXT NOT NULL CHECK (role IN ('LIBRARIAN', 'CLERK', 'BORROWER')),
    salary    REAL,          -- staff only
    desk_no   INTEGER,       -- clerk only
    office_no INTEGER        -- librarian only
);

CREATE TABLE IF NOT EXISTS book (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    title    TEXT NOT NULL,
    author   TEXT NOT NULL,
    subject  TEXT NOT NULL,
    is_issued INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS loan (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id      INTEGER NOT NULL REFERENCES book(id),
    borrower_id  INTEGER NOT NULL REFERENCES person(id),
    issuer_id    INTEGER NOT NULL REFERENCES person(id),
    receiver_id  INTEGER REFERENCES person(id),
    issued_date  TEXT NOT NULL,   -- ISO-8601 (yyyy-MM-dd)
    return_date  TEXT,
    fine_paid    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS hold_request (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id      INTEGER NOT NULL REFERENCES book(id),
    borrower_id  INTEGER NOT NULL REFERENCES person(id),
    request_date TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS library_config (
    id                   INTEGER PRIMARY KEY CHECK (id = 1),
    return_deadline_days INTEGER NOT NULL,
    per_day_fine         REAL NOT NULL,
    hold_request_expiry  INTEGER NOT NULL
);
