package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.User;

@Service
public class UserService {

    List<User> users;
    
    public UserService(List<User> users) {
        this.users = new ArrayList<>(users);
    }

    public User addUser(User user) {
        this.users.add(user);
        return user;
    }

    public List<User> getAllUsers() {
        return this.users;
    }

    public boolean deleteUser(Long id) {
        return this.users.removeIf(user -> user.getId().equals(id));
    }


}
