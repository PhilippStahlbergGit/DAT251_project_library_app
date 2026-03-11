import { useEffect, useState } from "react";
import { useBooks } from "../context/BookContext.jsx";
import { useLoans } from "../context/LoanContext.jsx";
import "./BooksPage.css";

const STATUS_LABEL = {
  AVAILABLE: "Available",
  LOANED: "On Loan",
  RESERVED: "Reserved",
};

const STATUS_CLASS = {
  AVAILABLE: "status--available",
  LOANED: "status--loaned",
  RESERVED: "status--reserved",
};

export default function BooksPage() {
  const { books, fetchBooks, deleteBook } = useBooks();
  const { createLoan } = useLoans();

  // Track which book copy is showing the lend form (by id)
  const [lendingId, setLendingId] = useState(null);
  const [lendData, setLendData] = useState({ borrowerEmail: "", dueDate: "" });
  const [lendError, setLendError] = useState("");
  const [lendLoading, setLendLoading] = useState(false);

  useEffect(() => {
    fetchBooks();
  }, []);

  const openLend = (id) => {
    setLendingId(id);
    setLendData({ borrowerEmail: "", dueDate: "" });
    setLendError("");
  };

  const cancelLend = () => {
    setLendingId(null);
    setLendError("");
  };

  const submitLend = async (e, bookCopyId) => {
    e.preventDefault();
    setLendError("");
    setLendLoading(true);
    try {
      await createLoan({ bookCopyId, ...lendData });
      setLendingId(null);
      fetchBooks(); // refresh availability status
    } catch (err) {
      setLendError(err.message);
    } finally {
      setLendLoading(false);
    }
  };

  return (
    <section>
      <h1>My Library</h1>

      {books.length === 0 ? (
        <p className="books-empty">No books yet. Add some from the home page!</p>
      ) : (
        <div className="books-grid">
          {books.map((copy) => (
            <div key={copy.id} className="book-card">
              <div className="book-card-top">
                <h3 className="book-title">{copy.book?.title}</h3>
                {copy.availabilityStatus && (
                  <span className={`book-status ${STATUS_CLASS[copy.availabilityStatus] ?? ""}`}>
                    {STATUS_LABEL[copy.availabilityStatus] ?? copy.availabilityStatus}
                  </span>
                )}
              </div>
              {copy.book?.authors?.[0] && <p className="book-author">{copy.book.authors[0]}</p>}
              {copy.book?.publicationYear && <p className="book-year">{copy.book.publicationYear}</p>}

              {/* Inline lend form */}
              {lendingId === copy.id ? (
                <form className="lend-form" onSubmit={(e) => submitLend(e, copy.id)}>
                  <input
                    type="email"
                    placeholder="Borrower's email"
                    value={lendData.borrowerEmail}
                    onChange={(e) => setLendData((p) => ({ ...p, borrowerEmail: e.target.value }))}
                    required
                    autoFocus
                  />
                  <input
                    type="date"
                    value={lendData.dueDate}
                    onChange={(e) => setLendData((p) => ({ ...p, dueDate: e.target.value }))}
                    required
                    min={new Date().toISOString().split("T")[0]}
                  />
                  {lendError && <p className="lend-error">{lendError}</p>}
                  <div className="lend-form-actions">
                    <button type="submit" className="lend-confirm-btn" disabled={lendLoading}>
                      {lendLoading ? "Lending…" : "Confirm"}
                    </button>
                    <button type="button" className="lend-cancel-btn" onClick={cancelLend}>
                      Cancel
                    </button>
                  </div>
                </form>
              ) : (
                <div className="book-card-actions">
                  {copy.availabilityStatus === "AVAILABLE" && (
                    <button className="book-lend-btn" onClick={() => openLend(copy.id)}>
                      Lend out
                    </button>
                  )}
                  <button
                    className="book-delete"
                    onClick={() => deleteBook(copy.id)}
                    disabled={copy.availabilityStatus === "LOANED"}
                    title={copy.availabilityStatus === "LOANED" ? "Return the loan before removing" : "Remove from library"}
                  >
                    Remove
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
