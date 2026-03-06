package app.main.LibraryApp.service;

import java.util.List;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.dto.BookRequest;
import app.main.LibraryApp.repository.BookRepository;

import org.springframework.stereotype.Service;
import app.main.LibraryApp.API.BookSearch;

@Service
public class BookService {

    List<Book> books;
    private final BookSearch bookSearch;


    public BookService(BookSearch bookSearch) {
        this.books = new ArrayList<>();
        this.bookSearch = bookSearch;
    }

    public Book addBook(BookRequest book) {
        Book newBook = new Book();
        newBook.setTitle(book.getTitle());
        newBook.setAuthors(List.of(book.getAuthor()));
        newBook.setPublicationYear((book.getYear()));
        newBook = bookSearch.completeBookInfo(newBook);
        this.books.add(newBook);
        return newBook;
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
