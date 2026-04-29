package app.main.LibraryApp.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.main.LibraryApp.api.BookSearch;
import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.dto.BookSuggestion;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class OCRService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BookSearch bookSearch;

    public OCRService(BookSearch bookSearch) {
        this.bookSearch = bookSearch;
    }

    public static String normalize(String text) {
        return text.replaceAll("[^\\w\\s]", "").toLowerCase().trim();
    }

    @Value("${ocr.service.url:http://localhost:8001}")
    private String ocrServiceUrl;

    public List<Book> scanAndFindBooks(byte[] imageBytes) {
        List<Book> foundBooks = new ArrayList<>();

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            URL url = new URL(ocrServiceUrl + "/ocr");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{ \"image_base64\": \"" + base64Image + "\" }";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            StringBuilder response;
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
            }

            JsonNode root = objectMapper.readTree(response.toString());
            JsonNode texts = root.get("texts");

            if (texts != null && texts.isArray()) {
                for (JsonNode textNode : texts) {
                    String ocrText = textNode.asText();

                    List<BookSuggestion> suggestions = bookSearch.searchSuggestions(ocrText);

                    for (BookSuggestion s : suggestions) {
                        if (normalize(s.getTitle()).equalsIgnoreCase(normalize(ocrText))) {
                            Book b = new Book();
                            b.setTitle(s.getTitle());
                            b.setAuthors(s.getAuthors());
                            b.setPublicationYear(s.getYear());
                            b.setIsbn(s.getIsbn());
                            b.setPublisher(s.getPublisher());
                            foundBooks.add(b);
                            break;
                        }
                    }

                    System.out.println("Book added from OCR text: " + ocrText);
                }
            }

        } catch (IOException e) {
            System.out.println("OCR Service error: " + e.getMessage());
        }

        return foundBooks;
    }
}