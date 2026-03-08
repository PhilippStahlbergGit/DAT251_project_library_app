package app.main.LibraryApp.service;

import java.util.List;

import app.main.LibraryApp.api.BookSearch;
import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.dto.BookRequest;
import app.main.LibraryApp.repository.BookRepository;

import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookSearch bookSearch;

    public BookService(BookSearch bookSearch, BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.bookSearch = bookSearch;
    }

    public Book addBook(BookRequest bookRequest) {
        Book newBook = new Book();
        newBook.setTitle(bookRequest.getTitle());
        newBook.setAuthors(List.of(bookRequest.getAuthor()));
        newBook.setPublicationYear(bookRequest.getYear());
        newBook = bookSearch.completeBookInfo(newBook);
        return bookRepository.save(newBook);
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
