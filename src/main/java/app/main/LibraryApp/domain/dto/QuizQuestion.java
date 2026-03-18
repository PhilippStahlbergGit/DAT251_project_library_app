package app.main.LibraryApp.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class QuizQuestion {
    private String question;
    private List<String> options;
    private int correctIndex;
    private String explanation;
}
