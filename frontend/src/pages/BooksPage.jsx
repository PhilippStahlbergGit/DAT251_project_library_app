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
          {books.map((copy) => (
            <div key={copy.id} className="book-card">
              <h3 className="book-title">{copy.book?.title}</h3>
              <p className="book-author">{copy.book?.authors?.[0]}</p>
              {copy.book?.publicationYear && <p className="book-year">{copy.book.publicationYear}</p>}
              <button className="book-delete" onClick={() => deleteBook(copy.id)}>
                Remove
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}