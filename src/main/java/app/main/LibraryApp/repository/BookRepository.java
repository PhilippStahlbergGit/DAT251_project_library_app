package app.main.LibraryApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.main.LibraryApp.domain.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    
}
