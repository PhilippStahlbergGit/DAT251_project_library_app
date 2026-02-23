package app.main.LibraryApp.service;

import java.util.ArrayList;
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

    public List<Book> getAllBooks() {
        return this.books;
    }

    public boolean deleteBook(Book book) {
        return this.books.remove(book);
    }

}
