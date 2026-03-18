package app.main.LibraryApp.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class QuizResponse {
    private String bookTitle;
    private String bookAuthor;
    private List<QuizQuestion> questions;
}
