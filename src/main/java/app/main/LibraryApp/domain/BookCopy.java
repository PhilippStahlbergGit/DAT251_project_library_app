package app.main.LibraryApp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.Condition;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "library_id")
    @JsonIgnore
    private Library library;

    private AvailabilityStatus availabilityStatus;
    private Condition condition;
    private int rating;
    private String location;
}
