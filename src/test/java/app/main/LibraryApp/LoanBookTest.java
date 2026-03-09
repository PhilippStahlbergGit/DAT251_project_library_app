package app.main.LibraryApp;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.repository.BookCopyRepository;
import app.main.LibraryApp.repository.BookRepository;
import app.main.LibraryApp.repository.UserRepository;
import app.main.LibraryApp.service.LibraryService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class LoanBookTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private BookRepository bookRepository;

    private Long availableBookCopyId;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (userRepository.findByEmail("lender@test.com").isEmpty()) {
            User lender = new User();
            lender.setName("Lender");
            lender.setEmail("lender@test.com");
            lender.setPassword("password");
            userRepository.save(lender);
            libraryService.createLibrary(lender);
        }

        if (userRepository.findByEmail("borrower@test.com").isEmpty()) {
            User borrower = new User();
            borrower.setName("Borrower");
            borrower.setEmail("borrower@test.com");
            borrower.setPassword("password");
            userRepository.save(borrower);
            libraryService.createLibrary(borrower);
        }

        // Create an available BookCopy in the lender's library for each test
        Book book = new Book();
        book.setTitle("Test Book");
        bookRepository.save(book);

        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        copy.setLibrary(libraryService.getLibraryByEmail("lender@test.com"));
        availableBookCopyId = bookCopyRepository.save(copy).getId();
    }

    @Test
    @WithMockUser(username = "borrower@test.com")
    void shouldCreateLoan() throws Exception {
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.borrower.email").value("borrower@test.com"));
    }

    @Test
    @WithMockUser(username = "borrower@test.com")
    void shouldReturnLoan() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isCreated())
                .andReturn();

        Long loanId = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/loans/" + loanId + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanStatus").value("RETURNED"));
    }

    @Test
    @WithMockUser(username = "borrower@test.com")
    void shouldNotBorrowUnavailableBookCopy() throws Exception {
        // First loan — makes it LOANED
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isCreated());

        // Second loan on same copy — should fail with 409 Conflict
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotReturnSomeoneElsesLoan() throws Exception {
        // borrower@test.com creates the loan
        MvcResult result = mockMvc.perform(post("/api/loans")
                .with(user("borrower@test.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isCreated())
                .andReturn();

        Long loanId = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // lender@test.com tries to return it — should be forbidden
        mockMvc.perform(patch("/api/loans/" + loanId + "/return")
                .with(user("lender@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "borrower@test.com")
    void shouldGetAllLoans() throws Exception {
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookCopyId\":" + availableBookCopyId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
