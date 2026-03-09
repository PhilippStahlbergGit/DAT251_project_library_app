package app.main.LibraryApp.service;

import app.main.LibraryApp.domain.dto.LoginRequest;
import app.main.LibraryApp.domain.dto.RegisterRequest;
import app.main.LibraryApp.domain.dto.UserResponse;
import app.main.LibraryApp.repository.LibraryRepository;
import app.main.LibraryApp.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.Library;
import app.main.LibraryApp.domain.User;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LibraryService libraryService;
    private final UserService userService;

    public AuthService(PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService,
            TokenBlacklistService tokenBlacklistService, LibraryService libraryService, UserService userService) {

        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.libraryService = libraryService;
        this.userService = userService;
    }

    public void register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getName() + ", " + request.getEmail());
        try {
            System.out.println("Checking if user exists...");
            User existingUser = userService.getUserByEmail(request.getEmail());
            if (existingUser != null) {
                System.out.println("User with email " + request.getEmail() + " already exists");
                throw new RuntimeException("User with this email exists");
            }
        } catch (RuntimeException e) {
            // If the exception is "User not found", we can ignore it
            if (!e.getMessage().equals("User not found")) {
                throw e;
            }
        }
        User user = userService.addUser(request);
        libraryService.createLibrary(user);
    }

    public UserResponse login(LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userService.getUserByEmail(request.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        UserResponse response = mapToUserResponse(user, token);

        return response;
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            tokenBlacklistService.blacklistToken(jwt);
        }
    }

    private UserResponse mapToUserResponse(User user, String token) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setToken(token);
        return response;
    }

}
