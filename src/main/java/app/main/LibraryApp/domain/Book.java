package app.main.LibraryApp.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.main.LibraryApp.domain.enums.Genre;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "books")
@Inheritance(strategy = InheritanceType.JOINED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isbn;
    private String title;
    @ElementCollection
    private List<String> authors;
    private String publisher;
    private Integer publicationYear;
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "library_id")
    @JsonIgnore
    private Library library;
}
