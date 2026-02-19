import { createContext, useContext, useMemo, useState } from "react";

const BookContext = createContext(null);

export function BookProvider({ children }) {
  const [books, setBooks] = useState([]);

  const addBook = async ({ title, author, year }) => {
    // TODO: replace mock with API call
    // const res = await fetch("/api/books", {
    //   method: "POST",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ title, author, year }),
    // });
    // if (!res.ok) throw new Error(await res.text());
    // const newBook = await res.json();

    // --- mock for now ---
    const newBook = { id: Date.now(), title, author, year };
    console.log("MOCK addBook:", newBook);

    setBooks((prev) => [...prev, newBook]);
  };

  const fetchBooks = async () => {
    // TODO: replace mock with API call
    // const res = await fetch("/api/books");
    // if (!res.ok) throw new Error(await res.text());
    // const data = await res.json();
    // setBooks(data);

    // --- mock for now ---
    console.log("MOCK fetchBooks");
  };

  const deleteBook = async (id) => {
    // TODO: replace mock with API call
    // const res = await fetch(`/api/books/${id}`, { method: "DELETE" });
    // if (!res.ok) throw new Error(await res.text());

    // --- mock for now ---
    console.log("MOCK deleteBook:", id);

    setBooks((prev) => prev.filter((b) => b.id !== id));
  };

  const value = useMemo(
    () => ({ books, addBook, fetchBooks, deleteBook }),
    [books]
  );

  return <BookContext.Provider value={value}>{children}</BookContext.Provider>;
}

export function useBooks() {
  const ctx = useContext(BookContext);
  if (!ctx) throw new Error("useBooks must be used inside BookProvider");
  return ctx;
}