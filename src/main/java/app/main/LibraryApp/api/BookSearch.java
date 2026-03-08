package app.main.LibraryApp.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.enums.Genre;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class BookSearch {

    public Book completeBookInfo(Book book) {
        // Implement logic to fetch book from API and populate all fields

        // Search by given fields (e.g., title, author) to find the best match in the
        // API
        String title = book.getTitle();
        List<String> authors = book.getAuthors();
        int publicationYear = book.getPublicationYear();

        String searchQuery = "title_suggest:\"" + title + "\" " +
                "author:\"" + authors.get(0) + "\" " +
                "first_publish_year:" + publicationYear;

        try {
            HttpClient client = HttpClient.newHttpClient();

            String query = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            // 'eng' is the Open Library language code for English
            String url = "https://openlibrary.org/search.json"
                    + "?q=" + query
                    + "&language=eng"
                    + "&sort" // The default is to sort by relevance
                    + "&fields=title,author_name,language,isbn,first_publish_year,publisher,subject";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "MyLibraryApp")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode docs = root.get("docs");

            // Get first result (most relevant) and populate the book object with the
            // retrieved information
            JsonNode Jsonbook = docs.path(0);
            // Populate the book object with the retrieved information
            book.setTitle(Jsonbook.path("title").asText("N/A"));
            book.setAuthors(List.of(Jsonbook.path("author_name").path(0).asText("N/A")));
            book.setPublicationYear(Jsonbook.path("first_publish_year").asInt(0));
            book.setIsbn(Jsonbook.path("isbn").path(0).asText("N/A"));

            // TODO: Currently only taking the first publisher, should ideally only show the
            // most relevant one, but this is a start
            book.setPublisher(Jsonbook.path("publisher").path(0).asText("N/A"));

            JsonNode subjectNode = Jsonbook.path("subject");
            List<String> subjects = new ArrayList<>();
            if (subjectNode.isArray()) {
                for (JsonNode n : subjectNode) {
                    subjects.add(n.asText(""));
                }
            }
            // TODO: Currently only taking one subject/genre, should ideally show all
            // relevant ones
            book.setGenre(mapSubjectToGenre(subjects));

            System.out.println(
                    book + " by " +
                            book.getAuthors().get(0) +
                            " (" + book.getPublicationYear() + ")" +
                            " ISBN: " + book.getIsbn());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch book information from API", e);
        }
        return book;

    }

    private Genre mapSubjectToGenre(List<String> subjects) {

        if (subjects == null || subjects.isEmpty()) {
            return Genre.UNKNOWN;
        }

        for (String subject : subjects) {
            String lowerSubject = subject.toLowerCase();

            if (lowerSubject.contains("non-fiction") || lowerSubject.contains("nonfiction")) {
                return Genre.NON_FICTION;
            } else if (lowerSubject.contains("science fiction") || lowerSubject.contains("sci-fi")) {
                return Genre.SCIENCE_FICTION;
            } else if (lowerSubject.contains("fantasy")) {
                return Genre.FANTASY;
            } else if (lowerSubject.contains("mystery")) {
                return Genre.MYSTERY;
            } else if (lowerSubject.contains("biography")) {
                return Genre.BIOGRAPHY;
            } else if (lowerSubject.contains("history")) {
                return Genre.HISTORY;
            } else if (lowerSubject.contains("romance")) {
                return Genre.ROMANCE;
            } else if (lowerSubject.contains("fiction")) {
                return Genre.FICTION;
            }
        }
        return Genre.UNKNOWN;
    }
}
