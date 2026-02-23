package app.main.LibraryApp.domain;

import java.util.List;

import app.main.LibraryApp.domain.enums.Genre;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isbn;
    private String title;
    private List<String> authors;
    private String publisher;
    private Integer publicationYear;
    private Genre genre;

}
