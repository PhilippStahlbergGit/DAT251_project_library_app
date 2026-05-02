import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useLoans } from "../context/LoanContext";
import "./LoansPage.css";

export default function LoansPage() {
  const { loans, lentLoans, loading, error, fetchLoans, fetchLentLoans, returnLoan, deleteLoan } = useLoans();
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    fetchLoans();
    fetchLentLoans();
  }, [fetchLoans, fetchLentLoans]);

  const handleReturn = async (id) => {
    setActionError("");
    try {
      await returnLoan(id);
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleDelete = async (id) => {
    setActionError("");
    if (!confirm("Remove this loan record?")) return;
    try {
      await deleteLoan(id);
    } catch (err) {
      setActionError(err.message);
    }
  };

  const activeLoans = loans.filter((l) => l.loanStatus === "ACTIVE");
  const pastLoans = loans.filter((l) => l.loanStatus !== "ACTIVE");
  const activeLentLoans = lentLoans.filter((l) => l.loanStatus === "ACTIVE" && l.guestBorrowerName);

  return (
    <section className="loans-page">
      <h1>Loans</h1>
      <p className="loans-lead">
        Track books you have borrowed and books you have lent out. To lend a book,
        go to <Link to="/books">My Library</Link>.
      </p>

      {actionError && <p className="loan-error loan-action-error">{actionError}</p>}

      {/* Lent out by me */}
      {activeLentLoans.length > 0 && (
        <div className="loan-section">
          <h2>Lent Out</h2>
          <ul className="loans-list">
            {activeLentLoans.map((loan) => (
              <li key={loan.id} className="loan-item loan-active">
                <div className="loan-info">
                  <strong className="loan-title">{loan.bookCopy?.book?.title ?? "Unknown book"}</strong>
                  <span className="loan-meta">
                    Lent to {loan.guestBorrowerName ?? loan.borrower?.name ?? "Unknown"} · Due {loan.dueDate}
                  </span>
                </div>
                <div className="loan-actions">
                  <span className="loan-badge loan-badge--active">Active</span>
                  <button className="loan-btn loan-btn--return" onClick={() => handleReturn(loan.id)}>
                    Return
                  </button>
                  <button className="loan-btn loan-btn--delete" onClick={() => handleDelete(loan.id)}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Active loans */}
      <div className="loan-section">
        <h2>Borrowed</h2>
        {loading && <p>Loading…</p>}
        {error && <p className="loan-error">{error}</p>}
        {!loading && !error && activeLoans.length === 0 && (
          <p className="loans-empty">You have no active borrowed books.</p>
        )}
        {activeLoans.length > 0 && (
          <ul className="loans-list">
            {activeLoans.map((loan) => (
              <li key={loan.id} className="loan-item loan-active">
                <div className="loan-info">
                  <strong className="loan-title">{loan.bookCopy?.book?.title ?? "Unknown book"}</strong>
                  <span className="loan-meta">
                    Borrowed {loan.loanDate} · Due {loan.dueDate}
                  </span>
                </div>
                <div className="loan-actions">
                  <span className="loan-badge loan-badge--active">Active</span>
                  <button className="loan-btn loan-btn--return" onClick={() => handleReturn(loan.id)}>
                    Return
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Loan history */}
      {pastLoans.length > 0 && (
        <div className="loan-section">
          <h2>History</h2>
          <ul className="loans-list">
            {pastLoans.map((loan) => (
              <li key={loan.id} className="loan-item loan-returned">
                <div className="loan-info">
                  <strong className="loan-title">{loan.bookCopy?.book?.title ?? "Unknown book"}</strong>
                  <span className="loan-meta">
                    Borrowed {loan.loanDate}
                    {loan.returnDate && ` · Returned ${loan.returnDate}`}
                  </span>
                </div>
                <div className="loan-actions">
                  <span className="loan-badge loan-badge--returned">Returned</span>
                  <button
                    className="loan-btn loan-btn--delete"
                    onClick={() => handleDelete(loan.id)}
                    aria-label="Delete loan record"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
