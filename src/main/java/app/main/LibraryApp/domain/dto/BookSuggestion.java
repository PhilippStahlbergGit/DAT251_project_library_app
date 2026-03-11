package app.main.LibraryApp.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class BookSuggestion {

    private String title;
    private List<String> authors;
    private int year;
    private String isbn;
    private String publisher;
    private String genre;
}
