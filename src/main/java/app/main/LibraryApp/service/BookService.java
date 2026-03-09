package app.main.LibraryApp.service;

import java.util.List;

import app.main.LibraryApp.api.BookSearch;
import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.Library;
import app.main.LibraryApp.domain.dto.BookRequest;
import app.main.LibraryApp.repository.BookRepository;
import app.main.LibraryApp.repository.LibraryRepository;

import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookSearch bookSearch;
    private final LibraryService libraryService;

    public BookService(BookSearch bookSearch, BookRepository bookRepository, LibraryService libraryService) {
        this.bookRepository = bookRepository;
        this.bookSearch = bookSearch;
        this.libraryService = libraryService;
    }

    public Book addBook(BookRequest bookRequest, String email) {
        Book newBook = new Book();
        newBook.setTitle(bookRequest.getTitle());
        newBook.setAuthors(List.of(bookRequest.getAuthor()));
        newBook.setPublicationYear(bookRequest.getYear());
        newBook = bookSearch.completeBookInfo(newBook);
        Library library = libraryService.getLibraryByEmail(email);
        newBook.setLibrary(library);
        return bookRepository.save(newBook);
    }

    public Book addBook(String email, Book book) {
        Library library = libraryService.getLibraryByEmail(email);
        book.setLibrary(library);
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks(String email) {
        return libraryService.getLibraryByEmail(email).getBooks();
    }

    public void deleteBook(String email, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (!book.getLibrary().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized"); // can't delete someone else's book
        }
        bookRepository.delete(book);
    }

}
