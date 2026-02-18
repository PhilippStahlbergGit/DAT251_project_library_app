package app.main.LibraryApp.domain;

import java.util.UUID;

import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.Condition;

public class BookCopy {
    UUID id;
    Book book;
    AvailabilityStatus availabilityStatus;
    Condition condition;
    int rating;
    String location;
}
