package app.main.LibraryApp.domain;

import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.Condition;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_copies")
public class BookCopy extends Book {
    private AvailabilityStatus availabilityStatus;
    private Condition condition;
    private int rating;
    private String location;
}
