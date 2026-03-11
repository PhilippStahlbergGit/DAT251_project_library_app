package app.main.LibraryApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.main.LibraryApp.domain.BookCopy;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
}
