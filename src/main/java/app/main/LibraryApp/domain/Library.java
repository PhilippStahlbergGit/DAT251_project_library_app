package app.main.LibraryApp.domain;

import java.util.List;
import java.util.UUID;

public class Library {
    private UUID id;
    private User owner;
    private List<BookCopy> bookCopies;
}
