import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { useAuth } from "./AuthContext";
const BookContext = createContext(null);
export function BookProvider({ children }) {
  const [books, setBooks] = useState([]);
  const { authFetch, isAuthenticated } = useAuth();
  // automatically fetch books when user logs in
  useEffect(() => {
    if (isAuthenticated) {
      fetchBooks();
    } else {
      setBooks([]); // clear books on logout
    }
  }, [isAuthenticated]);
  const addBook = async ({ title, author, year, isbn, publisher, genre }) => {
    const res = await authFetch("/api/books", {
      method: "POST",
      body: JSON.stringify({ title, author, year, isbn, publisher, genre }),
    });
    if (!res.ok) throw new Error(await res.text());
    const newBook = await res.json();
    setBooks((prev) => [...prev, newBook]);
  };

  const searchBooks = async (query) => {
    if (!query || query.trim().length < 2) return [];
    const res = await authFetch(`/api/books/search?q=${encodeURIComponent(query.trim())}`);
    if (!res.ok) return [];
    return res.json();
  };
  const fetchBooks = async () => {
    const res = await authFetch("/api/books");
    if (!res.ok) throw new Error(await res.text());
    const data = await res.json();
    setBooks(data);
  };
  const deleteBook = async (id) => {
    const res = await authFetch(`/api/books/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error(await res.text());
    setBooks((prev) => prev.filter((book) => book.id !== id));
  };
  const value = useMemo(
    () => ({ books, addBook, fetchBooks, deleteBook, searchBooks }),
    [books]
  );
  return <BookContext.Provider value={value}>{children}</BookContext.Provider>;
}
export function useBooks() {
  const ctx = useContext(BookContext);
  if (!ctx) throw new Error("useBooks must be used inside BookProvider");
  return ctx;
}
