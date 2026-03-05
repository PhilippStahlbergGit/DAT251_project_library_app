package app.main.LibraryApp;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.dto.UserRequest;
import app.main.LibraryApp.repository.UserRepository;
import app.main.LibraryApp.service.UserService;

class UserTest {
    @Test
    void testAddUser() {
        // test for adding a user
        
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        UserRequest req = new UserRequest();
        req.setName("Alice Smith");
        req.setEmail("alice.smith@example.com");
        req.setPassword("secret");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.addUser(req);

        assertEquals("Alice Smith", saved.getName());
        assertEquals("alice.smith@example.com", saved.getEmail());
        assertEquals("secret", saved.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        // test for retrieving all users from the library
        
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User user1 = new User();
        user1.setName("Alice Smith");
        user1.setEmail("alice.smith@example.com");

        User user2 = new User();
        user2.setName("Bob Johnson");
        user2.setEmail("bob.johnson@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("Alice Smith", users.get(0).getName());
        assertEquals("Bob Johnson", users.get(1).getName());
        verify(userRepository).findAll();
    }
    @Test
    void testDeleteUser() {
        // test for deleting a user from the library
        
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        
        boolean result = userService.deleteUser(userId);
        
        assertTrue(result);
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteUserNotFound() {
        // test for deleting a user that does not exist in the library
        
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Long userId = 2L;
        when(userRepository.existsById(userId)).thenReturn(false);
        
        boolean result = userService.deleteUser(userId);
        
        assertFalse(result);
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}