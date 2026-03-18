package app.main.LibraryApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.dto.QuizRequest;
import app.main.LibraryApp.domain.dto.QuizResponse;
import app.main.LibraryApp.service.QuizService;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuizRequest request) {
        try {
            QuizResponse response = quizService.generateQuiz(request, getCurrentUserEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Book not found")) {
                return ResponseEntity.status(404).build();
            } else if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).build();
            } else if (e.getMessage().startsWith("OpenAI API key not configured")) {
                return ResponseEntity.status(503).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
