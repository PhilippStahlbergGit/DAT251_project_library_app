package app.main.LibraryApp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.manager.LibraryManager;

@RestController
@RequestMapping("/books")
public class BookController {

    private final LibraryManager libraryManager;

    public BookController(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        // TODO Implement logic to add a new book using libraryManager
        Book addedBook = libraryManager.addBook(book);
        return ResponseEntity.ok(addedBook);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = libraryManager.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteBook(@RequestBody Book book) {
        if (libraryManager.deleteBook(book)) {
            return ResponseEntity.ok("Book deleted");
        } else {
            return ResponseEntity.status(404).body("Book not found");
        }
    }

}
