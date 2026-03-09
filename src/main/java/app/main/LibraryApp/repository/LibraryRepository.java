package app.main.LibraryApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.main.LibraryApp.domain.Library;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> findByUserEmail(String email);
}
