package app.main.LibraryApp.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.dto.QuizQuestion;
import app.main.LibraryApp.domain.dto.QuizRequest;
import app.main.LibraryApp.domain.dto.QuizResponse;
import app.main.LibraryApp.repository.BookCopyRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class QuizService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_MODEL = "gpt-4o-mini";

    private final BookCopyRepository bookCopyRepository;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    public QuizService(BookCopyRepository bookCopyRepository) {
        this.bookCopyRepository = bookCopyRepository;
    }

    public QuizResponse generateQuiz(QuizRequest request, String userEmail) {
        BookCopy copy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!copy.getLibrary().getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        Book book = copy.getBook();
        int numQuestions = request.getNumQuestions() > 0 ? Math.min(request.getNumQuestions(), 10) : 5;

        List<QuizQuestion> questions = callOpenAiApi(book, numQuestions);

        QuizResponse response = new QuizResponse();
        response.setBookTitle(book.getTitle());
        response.setBookAuthor(book.getAuthors() != null && !book.getAuthors().isEmpty()
                ? book.getAuthors().get(0) : "Unknown");
        response.setQuestions(questions);
        return response;
    }

    private List<QuizQuestion> callOpenAiApi(Book book, int numQuestions) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key not configured");
        }

        String authorStr = (book.getAuthors() != null && !book.getAuthors().isEmpty())
                ? String.join(", ", book.getAuthors()) : "Unknown";
        String genreStr = book.getGenre() != null ? book.getGenre().name() : "UNKNOWN";
        int year = book.getPublicationYear() != null ? book.getPublicationYear() : 0;
        String yearStr = year > 0 ? String.valueOf(year) : "year unknown";

        String promptText = String.format(
                "You are a literary quiz master. Generate %d multiple-choice quiz questions about the book \"%s\" by %s (%s) in the genre %s.\n\n" +
                "Each question should test knowledge about the book's plot, themes, characters, or historical/literary context.\n\n" +
                "Respond with ONLY a valid JSON array (no markdown, no explanation) with this exact structure:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"Question text here?\",\n" +
                "    \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                "    \"correctIndex\": 0,\n" +
                "    \"explanation\": \"Brief explanation of why this answer is correct.\"\n" +
                "  }\n" +
                "]\n\n" +
                "Make sure correctIndex is the 0-based index of the correct option in the options array.",
                numQuestions, book.getTitle(), authorStr, yearStr, genreStr
        );

        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode messageContent = mapper.createObjectNode();
            messageContent.put("role", "user");
            messageContent.put("content", promptText);

            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", OPENAI_MODEL);
            requestBody.put("max_tokens", 2048);
            ArrayNode messages = mapper.createArrayNode();
            messages.add(messageContent);
            requestBody.set("messages", messages);

            String requestJson = mapper.writeValueAsString(requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException("OpenAI API error: " + httpResponse.statusCode() + " " + httpResponse.body());
            }

            JsonNode responseRoot = mapper.readTree(httpResponse.body());
            String content = responseRoot.path("choices").path(0).path("message").path("content").textValue();

            // Strip markdown code fences if present
            String jsonText = content.trim();
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
            }

            JsonNode questionsArray = mapper.readTree(jsonText);
            List<QuizQuestion> questions = new ArrayList<>();
            for (JsonNode q : questionsArray) {
                QuizQuestion question = new QuizQuestion();
                question.setQuestion(q.path("question").textValue());
                question.setCorrectIndex(q.path("correctIndex").intValue());
                question.setExplanation(q.path("explanation").textValue());

                List<String> options = new ArrayList<>();
                for (JsonNode opt : q.path("options")) {
                    options.add(opt.textValue());
                }
                question.setOptions(options);
                questions.add(question);
            }
            return questions;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage(), e);
        }
    }
}
