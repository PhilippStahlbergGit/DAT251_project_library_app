package app.main.LibraryApp.service;

import app.main.LibraryApp.domain.dto.LoginRequest;
import app.main.LibraryApp.domain.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public void register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getUsername() + ", " + request.getEmail());
    }

    public void logout() {
        System.out.println("Logging out user");
    }

    public void login(LoginRequest request) {
        System.out.println("Logging in user: " + request.getEmail());
    }

}
