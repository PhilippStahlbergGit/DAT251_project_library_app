package app.main.LibraryApp.domain;

import java.util.List;
import java.util.UUID;

public class Library {
    UUID id;
    User owner;
    List<BookCopy> bookCopies;
}
