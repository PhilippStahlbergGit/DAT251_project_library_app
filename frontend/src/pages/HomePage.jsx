import { useState } from "react";
import { Link } from "react-router-dom";
import { useBooks } from "../context/BookContext.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import "./HomePage.css";

export default function HomePage() {
  const { books, addBook } = useBooks();
  const { isAuthenticated } = useAuth();
  const [form, setForm] = useState({ title: "", author: "", year: "" });
  const [error, setError] = useState("");

  const onChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await addBook(form);
      setForm({ title: "", author: "", year: "" });
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>Library App</h1>
      <p>Browse books, view details, and manage your reading list.</p>

      {isAuthenticated ? (
        <div className="add-book-card">
          <h2>Add a Book</h2>
          <form onSubmit={onSubmit} className="add-book-form">
            <label htmlFor="title">Title</label>
            <input id="title" name="title" type="text" placeholder="Book title" value={form.title} onChange={onChange} required />

            <label htmlFor="author">Author</label>
            <input id="author" name="author" type="text" placeholder="Author name" value={form.author} onChange={onChange} required />

            <label htmlFor="year">Year</label>
            <input id="year" name="year" type="number" placeholder="Publication year" value={form.year} onChange={onChange} />

            {error && <p className="add-book-error">{error}</p>}
            <button type="submit" className="add-book-btn">Add Book</button>
          </form>
        </div>
      ) : (
        <p className="login-prompt">
          <Link to="/login">Login</Link> to add books.
        </p>
      )}

      {books.length > 0 && (
        <div className="added-books">
          <h2>Recently Added</h2>
          <ul>
            {books.map((book) => (
              <li key={book.id}>
                <strong>{book.title}</strong> — {book.author}
                {book.year && ` (${book.year})`}
              </li>
            ))}
          </ul>
        </div>
      )}

      <p><Link to="/books">View all books →</Link></p>
    </section>
  );
}