package app.main.LibraryApp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.ObjectMapper;

import app.main.LibraryApp.domain.dto.LoginRequest;
import app.main.LibraryApp.domain.dto.RegisterRequest;
import app.main.LibraryApp.domain.dto.UserResponse;

@SpringBootTest
class AuthTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @Test
    void testSuccessfulLoginReturnsToken() throws Exception {
        UserResponse userResponse = registerAndLogin("logintest@example.com", "Login Test User", "password123");

        assertNotNull(userResponse.getToken());
        assertFalse(userResponse.getToken().isEmpty());
    }

    @Test
    void testValidTokenAuthorizesRequest() throws Exception {
        String token = registerAndLogin("authtest@example.com", "Auth Test User", "password123").getToken();

        // Use the token to access a protected endpoint
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testTokenRejectedAfterLogout() throws Exception {
        String token = registerAndLogin("logouttest@example.com", "Logout Test User", "password123").getToken();

        // Logout to blacklist the token
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify the blacklisted token is rejected on a protected endpoint
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private UserResponse registerAndLogin(String email, String name, String password) throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName(name);
        registerReq.setEmail(email);
        registerReq.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(loginResult.getResponse().getContentAsString(), UserResponse.class);
    }
}
