package app.main.LibraryApp;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class OpenLibraryTest {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String query = URLEncoder.encode("harry potte", StandardCharsets.UTF_8);

        // 'nor' is the Open Library language code for Norwegian
        String url = "https://openlibrary.org/search.json"
                + "?q=" + query
                + "&language=eng"
                + "&fields=title,author_name,language,isbn,first_publish_year";

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

        for (JsonNode book : docs) {
            String title = book.path("title").asText();
            if (title.isEmpty())
                title = "N/A";
            String author = book.path("author_name").path(0).asText();
            if (author.isEmpty())
                author = "N/A";
            int year = book.path("first_publish_year").asInt();
            System.out.println(title + " by " + author + " (" + year + ")");
        }
    }
}