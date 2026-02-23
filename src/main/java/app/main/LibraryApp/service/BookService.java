package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.main.LibraryApp.domain.Book;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    List<Book> books;

    public BookService() {
        this.books = new ArrayList<>();
    }

    public Book addBook(Book book) {
        this.books.add(book);
        return book;
    }

    public Collection<Book> getAllBooks() {
        return this.books;
    }

    public boolean deleteBook(Long id) {
        return this.books.removeIf(book -> book.getId().equals(id));
    }

}
