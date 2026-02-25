import { useState } from "react";
import { Link } from "react-router-dom";
import { useBooks } from "../context/BookContext.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useJoke } from "../context/JokeContext.jsx";
import { useEffect } from "react";
import { useRecommendations } from "../context/RecommendationContext.jsx";
import "./HomePage.css";

export default function HomePage() {
  const { books, addBook } = useBooks();
  const { isAuthenticated } = useAuth();
  const [form, setForm] = useState({ title: "", author: "", year: "" });
  const [error, setError] = useState("");
  const { recommendations, loadingRecommendations, recommendationsError, fetchRecommendations, clearRecommendations } =
  useRecommendations();

  //Recommendations
  useEffect(() => {
  if (!isAuthenticated) {
    clearRecommendations();
    return;
  }
  const ownedBookIds = books.map((b) => Number(b.id)).filter(Number.isFinite);
  fetchRecommendations(ownedBookIds, 10);
}, [isAuthenticated, books, fetchRecommendations, clearRecommendations]);

  //Joke API
  const { joke, loadingJoke, jokeError, fetchJoke } = useJoke();

    useEffect(() => {
    fetchJoke();
  }, [fetchJoke]);

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
        <div>
          <div className="recommendations-section">
            <h2>Recommended for you</h2>

            {loadingRecommendations ? (
              <p>Loading recommendations...</p>
            ) : recommendationsError ? (
              <p>{recommendationsError}</p>
            ) : recommendations.length ? (
              <div className="reco-marquee" aria-label="Recommended books">
                <div className="reco-track">
                  {[...recommendations, ...recommendations].map((rec, index) => (
                    <article className="reco-card" key={`${rec.book_name}-${index}`}>
                      <h3>{rec.book_name}</h3>
                      <small>Score: {Number(rec.score).toFixed(2)}</small>
                    </article>
                  ))}
                </div>
              </div>
            ) : (
              <p>No recommendations yet.</p>
            )}
          </div>
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
      <section>
        <h2>Random Chuck Norris Joke</h2>
        {loadingJoke && <p>Loading joke...</p>}
        {jokeError && <p>{jokeError}</p>}
        {!loadingJoke && !jokeError && <p>{joke}</p>}
        <button onClick={fetchJoke}>Get another joke</button>
      </section>
    </section>
    
  );
}