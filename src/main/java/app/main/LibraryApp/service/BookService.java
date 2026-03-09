package app.main.LibraryApp.service;

import java.util.List;

import app.main.LibraryApp.api.BookSearch;
import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.Library;
import app.main.LibraryApp.domain.dto.BookRequest;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.repository.BookCopyRepository;
import app.main.LibraryApp.repository.BookRepository;

import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookSearch bookSearch;
    private final LibraryService libraryService;

    public BookService(BookSearch bookSearch, BookRepository bookRepository,
            BookCopyRepository bookCopyRepository, LibraryService libraryService) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.bookSearch = bookSearch;
        this.libraryService = libraryService;
    }

    public BookCopy addBook(BookRequest bookRequest, String email) {
        Book newBook = new Book();
        newBook.setTitle(bookRequest.getTitle());
        newBook.setAuthors(List.of(bookRequest.getAuthor()));
        newBook.setPublicationYear(bookRequest.getYear());
        newBook = bookSearch.completeBookInfo(newBook);
        bookRepository.save(newBook);

        Library library = libraryService.getLibraryByEmail(email);
        BookCopy copy = new BookCopy();
        copy.setBook(newBook);
        copy.setLibrary(library);
        copy.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        return bookCopyRepository.save(copy);
    }

    public List<BookCopy> getAllBooks(String email) {
        return libraryService.getLibraryByEmail(email).getBookCopies();
    }

    public void deleteBook(String email, Long bookCopyId) {
        BookCopy copy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (!copy.getLibrary().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        bookCopyRepository.delete(copy);
    }
}
