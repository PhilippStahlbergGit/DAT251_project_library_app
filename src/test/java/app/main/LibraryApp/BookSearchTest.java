package app.main.LibraryApp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import app.main.LibraryApp.api.BookSearch;
import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.dto.BookSuggestion;
import app.main.LibraryApp.domain.enums.Genre;
import app.main.LibraryApp.repository.UserRepository;
import app.main.LibraryApp.service.LibraryService;

@SpringBootTest
@AutoConfigureMockMvc
class BookSearchTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryService libraryService;

    @MockitoBean
    private BookSearch bookSearch;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (userRepository.findByEmail("search@test.com").isEmpty()) {
            User user = new User();
            user.setName("Search Test User");
            user.setEmail("search@test.com");
            user.setPassword("password");
            userRepository.save(user);
            libraryService.createLibrary(user);
        }
    }

    @Test
    @WithMockUser(username = "search@test.com")
    void shouldReturnSuggestionsForQuery() throws Exception {
        BookSuggestion suggestion = new BookSuggestion();
        suggestion.setTitle("Dune");
        suggestion.setAuthors(List.of("Frank Herbert"));
        suggestion.setYear(1965);
        suggestion.setIsbn("9780441013593");
        suggestion.setPublisher("Chilton Books");
        suggestion.setGenre("SCIENCE_FICTION");

        when(bookSearch.searchSuggestions("dune")).thenReturn(List.of(suggestion));

        mockMvc.perform(get("/api/books/search?q=dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].authors[0]").value("Frank Herbert"))
                .andExpect(jsonPath("$[0].year").value(1965))
                .andExpect(jsonPath("$[0].isbn").value("9780441013593"));
    }

    @Test
    @WithMockUser(username = "search@test.com")
    void shouldReturnEmptyListForBlankQuery() throws Exception {
        mockMvc.perform(get("/api/books/search?q="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "search@test.com")
    void shouldAddBookFromSuggestionWithoutCallingCompleteBookInfo() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Dune\",\"author\":\"Frank Herbert\",\"year\":1965," +
                        "\"isbn\":\"9780441013593\",\"publisher\":\"Chilton Books\",\"genre\":\"SCIENCE_FICTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.title").value("Dune"))
                .andExpect(jsonPath("$.book.isbn").value("9780441013593"))
                .andExpect(jsonPath("$.book.publicationYear").value(1965));

        verify(bookSearch, never()).completeBookInfo(any());
    }

    @Test
    @WithMockUser(username = "search@test.com")
    void shouldFallBackToOpenLibraryLookupWhenNoIsbnProvided() throws Exception {
        Book completedBook = new Book();
        completedBook.setTitle("Harry Potter and the Philosopher's Stone");
        completedBook.setAuthors(List.of("J. K. Rowling"));
        completedBook.setPublicationYear(1997);
        completedBook.setIsbn("9780747532743");
        completedBook.setPublisher("Bloomsbury");
        completedBook.setGenre(Genre.FICTION);

        when(bookSearch.completeBookInfo(any())).thenReturn(completedBook);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Harry Potter\",\"author\":\"J. K. Rowling\",\"year\":1997}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.title").value("Harry Potter and the Philosopher's Stone"));

        verify(bookSearch, times(1)).completeBookInfo(any());
    }
}
