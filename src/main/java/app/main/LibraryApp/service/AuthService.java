package app.main.LibraryApp.service;

import app.main.LibraryApp.domain.dto.LoginRequest;
import app.main.LibraryApp.domain.dto.RegisterRequest;
import app.main.LibraryApp.domain.dto.UserResponse;
import app.main.LibraryApp.repository.UserRepository;
import org.springframework.stereotype.Service;
import app.main.LibraryApp.domain.User;
@Service
public class AuthService {
    private final UserRepository userRepository;
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }  
    public void register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getName() + ", " + request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        //Må hashes!!!!!!!!!!!
        user.setPassword(request.getPassword());
        userRepository.save(user);
    }
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        //Må sjekkes med hash
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return mapToUserResponse(user);
    }

    public void logout() {

        System.out.println("Logging out user");
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }

    

}
