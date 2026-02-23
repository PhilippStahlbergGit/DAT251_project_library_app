package app.main.LibraryApp.domain;

import java.util.List;

import app.main.LibraryApp.domain.enums.Genre;
import lombok.Data;

@Data
public class Book {
    private String isbn;
    private String title;
    private List<String> authors;
    private String publisher;
    private Integer publicationYear;
    private Genre genre;
}
