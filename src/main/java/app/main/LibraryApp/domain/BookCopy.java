package app.main.LibraryApp.domain;

import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.Condition;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookCopy extends Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private AvailabilityStatus availabilityStatus;
    private Condition condition;
    private int rating;
    private String location;
}
