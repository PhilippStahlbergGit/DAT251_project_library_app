package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.domain.dto.BookRequest;

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
    }

    public Collection<Book> getAllBooks() {
        return this.books;
    }

    public boolean deleteBook(Long id) {
        return this.books.removeIf(book -> book.getId().equals(id));
    }

}
