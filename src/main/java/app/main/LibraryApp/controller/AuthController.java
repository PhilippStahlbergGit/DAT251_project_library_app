package app.main.LibraryApp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.dto.LoginRequest;
import app.main.LibraryApp.domain.dto.RegisterRequest;
import app.main.LibraryApp.domain.dto.UserResponse;
import app.main.LibraryApp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authservice) {
        this.authService = authservice;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        // handle registration logic
        authService.register(request);
        return ResponseEntity.ok("User registered");
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // handle login, return token or session
        UserResponse user = authService.login(request);
        return ResponseEntity.ok(Map.of("user", user));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // handle logout, invalidate token or session
        authService.logout();
        return ResponseEntity.ok("User logged out");
    }
}