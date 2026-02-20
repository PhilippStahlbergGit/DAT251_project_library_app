package app.main.LibraryApp.domain;

import java.util.UUID;

import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.Condition;

public class BookCopy {
    private UUID id;
    private Book book;
    private AvailabilityStatus availabilityStatus;
    private Condition condition;
    private int rating;
    private String location;
}
