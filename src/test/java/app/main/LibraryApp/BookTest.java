package app.main.LibraryApp;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.repository.BookRepository;
import app.main.LibraryApp.service.BookService;

class BookTest {
    @Test
    void testAddBook() {
        // test for adding a book to the library
        
        BookRepository bookRepository = mock(BookRepository.class);
        BookService bookService = new BookService(bookRepository);

        Book book = new Book();
        book.setTitle("1984");
        book.setAuthors(List.of("George Orwell"));
        book.setIsbn("978-0451524935");
        
        when(bookRepository.save(book)).thenReturn(book);
        Book addedBook = bookService.addBook(book);
        
        assertEquals("1984", addedBook.getTitle());
        assertEquals(List.of("George Orwell"), addedBook.getAuthors());
        assertEquals("978-0451524935", addedBook.getIsbn());
        verify(bookRepository).save(book);
    }

    @Test
    void testGetAllBooks() {
        // test for retrieving all books from the library
        
        BookRepository bookRepository = mock(BookRepository.class);
        BookService bookService = new BookService(bookRepository);

        Book book1 = new Book();
        book1.setTitle("The Catcher in the Rye");
        book1.setAuthors(List.of("J.D. Salinger"));
        book1.setIsbn("978-0316769488");

        Book book2 = new Book();
        book2.setTitle("Pride and Prejudice");
        book2.setAuthors(List.of("Jane Austen"));
        book2.setIsbn("978-1503290563");

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));
        List<Book> books = bookService.getAllBooks();

        assertEquals(2, books.size());
        assertEquals("The Catcher in the Rye", books.get(0).getTitle());
        assertEquals("Pride and Prejudice", books.get(1).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void testDeleteBook() {
        // test for deleting a book from the library
        
        BookRepository bookRepository = mock(BookRepository.class);
        BookService bookService = new BookService(bookRepository);

        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        boolean result = bookService.deleteBook(bookId);
        
        assertTrue(result);
        verify(bookRepository).existsById(bookId);
        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void testDeleteBookNotFound() {
        // test for deleting a book that does not exist in the library
        
        BookRepository bookRepository = mock(BookRepository.class);
        BookService bookService = new BookService(bookRepository);

        Long bookId = 2L;
        when(bookRepository.existsById(bookId)).thenReturn(false);
        boolean result = bookService.deleteBook(bookId);
        
        assertFalse(result);
        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).deleteById(bookId);
    }
}