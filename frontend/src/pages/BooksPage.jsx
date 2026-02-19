import { useEffect } from "react";
import { useBooks } from "../context/BookContext.jsx";
import "./BooksPage.css";

export default function BooksPage() {
  const { books, fetchBooks, deleteBook } = useBooks();

  useEffect(() => {
    fetchBooks();
  }, []);

  return (
    <section>
      <h1>All Books</h1>

      {books.length === 0 ? (
        <p className="books-empty">No books yet. Add some from the home page!</p>
      ) : (
        <div className="books-grid">
          {books.map((book) => (
            <div key={book.id} className="book-card">
              <h3 className="book-title">{book.title}</h3>
              <p className="book-author">{book.author}</p>
              {book.year && <p className="book-year">{book.year}</p>}
              <button className="book-delete" onClick={() => deleteBook(book.id)}>
                Remove
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}