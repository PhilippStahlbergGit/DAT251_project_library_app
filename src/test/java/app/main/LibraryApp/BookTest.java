package app.main.LibraryApp;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.repository.BookRepository;
import app.main.LibraryApp.repository.UserRepository;
import app.main.LibraryApp.service.BookService;
import app.main.LibraryApp.service.LibraryService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BookTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        // create the user and library that @WithMockUser will simulate
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) // apply spring security to MockMvc
                .build();

        if (userRepository.findByEmail("test@test.com").isEmpty()) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("test@test.com");
            user.setPassword("password");
            userRepository.save(user);
            libraryService.createLibrary(user);
        }
    }

    @Test
    @WithMockUser(username = "test@test.com") // simulates a logged in user
    void shouldAddBook() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"1984\",\"author\":\"George Orwell\",\"year\":1949}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("1984"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldDeleteBook() throws Exception {
        // first add a book
        String response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"1984\",\"author\":\"George Orwell\",\"year\":1949}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // extract the id from the response
        Long bookId = new ObjectMapper().readTree(response).get("id").asLong();

        // then delete it
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldNotDeleteBookBelongingToAnotherUser() throws Exception {
        // create a second user with their own book
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        userRepository.save(otherUser);
        libraryService.createLibrary(otherUser);

        String response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Other Book\",\"author\":\"Other Author\",\"year\":2000}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = new ObjectMapper().readTree(response).get("id").asLong();

        // test@test.com should not be able to delete other@test.com's book
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isForbidden());
    }
}