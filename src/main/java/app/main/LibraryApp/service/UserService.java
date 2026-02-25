package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.dto.UserRequest;

@Service
public class UserService {

    List<User> users;

    public UserService(List<User> users) {
        this.users = new ArrayList<>(users);
    }

    public User addUser(UserRequest user) {
        User newUser = new User();
        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        this.users.add(newUser);
        return newUser;
    }

    public List<User> getAllUsers() {
        return this.users;
    }

    public boolean deleteUser(Long id) {
        return this.users.removeIf(user -> user.getId().equals(id));
    }

}
