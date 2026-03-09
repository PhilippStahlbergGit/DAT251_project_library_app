package app.main.LibraryApp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.dto.BookRequest;
import app.main.LibraryApp.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookCopy> addBook(@RequestBody BookRequest book) {
        BookCopy addedCopy = bookService.addBook(book, getCurrentUserEmail());
        return ResponseEntity.status(201).body(addedCopy);
    }

    @GetMapping
    public ResponseEntity<List<BookCopy>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks(getCurrentUserEmail()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(getCurrentUserEmail(), id);
            return ResponseEntity.ok("Book deleted");
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Book not found")) {
                return ResponseEntity.status(404).body("Book not found");
            } else if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body("Unauthorized");
            } else {
                return ResponseEntity.status(500).body("Internal server error");
            }
        }
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
