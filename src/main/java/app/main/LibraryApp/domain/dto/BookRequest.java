package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class BookRequest {

    private String title;
    private String author;
    private String year;
}
