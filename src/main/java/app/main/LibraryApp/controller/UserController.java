package app.main.LibraryApp.controller;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.dto.UserRequest;
import app.main.LibraryApp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody UserRequest user) {

        User addedUser = userService.addUser(user);
        return ResponseEntity.ok(addedUser);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAllUsers() {
        Collection<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User deleted");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
