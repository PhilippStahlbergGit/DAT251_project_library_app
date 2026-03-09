package app.main.LibraryApp;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.repository.UserRepository;
import app.main.LibraryApp.service.LibraryService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BookTest {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (userRepository.findByEmail("test@test.com").isEmpty()) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("test@test.com");
            user.setPassword("password");
            userRepository.save(user);
            libraryService.createLibrary(user);
        }

        if (userRepository.findByEmail("other@test.com").isEmpty()) {
            User otherUser = new User();
            otherUser.setName("Other User");
            otherUser.setEmail("other@test.com");
            otherUser.setPassword("password");
            userRepository.save(otherUser);
            libraryService.createLibrary(otherUser);
        }
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldAddBook() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                // fixed: author is a string, year matches entity field name
                .content("{\"title\":\"Harry Potter\",\"author\":\"J. K. Rowling\",\"year\":1997}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Harry Potter and the Philosopher's Stone"));

    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldDeleteBook() throws Exception {
        String response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Harry Potter\",\"author\":\"J. K. Rowling\",\"year\":1997}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldNotDeleteBookBelongingToAnotherUser() throws Exception {
        // add a book as other@test.com using user() post processor
        MvcResult result = mockMvc.perform(post("/api/books")
                .with(user("other@test.com")) // fixed: override mock user for this request only
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Other Book\",\"author\":\"Other Author\",\"year\":2000}"))
                .andExpect(status().isCreated())
                .andReturn();

        Long bookId = new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // test@test.com should not be able to delete other@test.com's book
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isForbidden());
    }
}