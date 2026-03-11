import { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import { useBooks } from "../context/BookContext.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useJoke } from "../context/JokeContext.jsx";
import { useRecommendations } from "../context/RecommendationContext.jsx";
import "./HomePage.css";

export default function HomePage() {
  const { books, addBook, searchBooks } = useBooks();
  const { isAuthenticated } = useAuth();
  const [titleQuery, setTitleQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const debounceRef = useRef(null);
  const dropdownRef = useRef(null);

  const { recommendations, loadingRecommendations, recommendationsError, fetchRecommendations, clearRecommendations } =
    useRecommendations();

  useEffect(() => {
    if (!isAuthenticated) {
      clearRecommendations();
      return;
    }
    const ownedBookIds = books.map((b) => Number(b.id)).filter(Number.isFinite);
    fetchRecommendations(ownedBookIds, 10);
  }, [isAuthenticated, books, fetchRecommendations, clearRecommendations]);

  const { joke, loadingJoke, jokeError, fetchJoke } = useJoke();

  useEffect(() => {
    fetchJoke();
  }, [fetchJoke]);

  // Debounced search as the user types
  useEffect(() => {
    if (selected) return; // already picked, don't re-search
    clearTimeout(debounceRef.current);
    if (titleQuery.trim().length < 2) {
      setSuggestions([]);
      return;
    }
    debounceRef.current = setTimeout(async () => {
      setLoadingSuggestions(true);
      try {
        const results = await searchBooks(titleQuery);
        setSuggestions(results);
      } finally {
        setLoadingSuggestions(false);
      }
    }, 350);
    return () => clearTimeout(debounceRef.current);
  }, [titleQuery]);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setSuggestions([]);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const handleSelect = (suggestion) => {
    setSelected(suggestion);
    setTitleQuery(suggestion.title);
    setSuggestions([]);
    setError("");
  };

  const handleClear = () => {
    setSelected(null);
    setTitleQuery("");
    setSuggestions([]);
    setError("");
    setSuccess("");
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (!selected) {
      setError("Please select a book from the suggestions.");
      return;
    }
    try {
      await addBook({
        title: selected.title,
        author: selected.authors?.[0] ?? "N/A",
        year: selected.year,
        isbn: selected.isbn,
        publisher: selected.publisher,
        genre: selected.genre,
      });
      setSuccess(`"${selected.title}" added to your library.`);
      handleClear();
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
              <label htmlFor="title">Search by title</label>
              <div className="book-search-wrapper" ref={dropdownRef}>
                <input
                  id="title"
                  name="title"
                  type="text"
                  placeholder="Start typing a title…"
                  value={titleQuery}
                  onChange={(e) => {
                    setSelected(null);
                    setTitleQuery(e.target.value);
                  }}
                  autoComplete="off"
                  required
                />
                {loadingSuggestions && <p className="suggestions-loading">Searching…</p>}
                {suggestions.length > 0 && (
                  <ul className="suggestions-dropdown">
                    {suggestions.map((s, i) => (
                      <li key={i} onMouseDown={() => handleSelect(s)}>
                        <span className="suggestion-title">{s.title}</span>
                        {s.authors?.length > 0 && (
                          <span className="suggestion-meta">
                            {s.authors.slice(0, 2).join(", ")}
                            {s.year ? ` · ${s.year}` : ""}
                          </span>
                        )}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              {selected && (
                <div className="selected-book-preview">
                  <div className="selected-book-info">
                    <strong>{selected.title}</strong>
                    <span>{selected.authors?.join(", ")}</span>
                    {selected.year > 0 && <span>{selected.year}</span>}
                    {selected.publisher && selected.publisher !== "N/A" && (
                      <span className="selected-publisher">{selected.publisher}</span>
                    )}
                  </div>
                  <button type="button" className="clear-selection-btn" onClick={handleClear}>
                    ✕ Change
                  </button>
                </div>
              )}

              {error && <p className="add-book-error">{error}</p>}
              {success && <p className="add-book-success">{success}</p>}
              <button type="submit" className="add-book-btn" disabled={!selected}>
                Add to Library
              </button>
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
            {books.map((copy) => (
              <li key={copy.id}>
                <strong>{copy.book?.title}</strong> — {copy.book?.authors?.[0]}
                {copy.book?.publicationYear && ` (${copy.book.publicationYear})`}
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
