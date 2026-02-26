package app.main.LibraryApp.repository;

import app.main.LibraryApp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
    
}
