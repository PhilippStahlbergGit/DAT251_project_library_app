package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class QuizRequest {
    private Long bookCopyId;
    private int numQuestions = 5;
}
