package app.main.LibraryApp.domain;

import java.util.List;

import app.main.LibraryApp.domain.enums.Genre;

public class Book {
    String isbn;
    String title;
    List<String> authors;
    String publisher;
    Integer publicationYear;
    Genre genre;
}
