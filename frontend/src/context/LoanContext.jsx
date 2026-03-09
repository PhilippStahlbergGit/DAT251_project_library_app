import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { useAuth } from "./AuthContext";

const LoanContext = createContext(null);

export function LoanProvider({ children }) {
  const { authFetch, isAuthenticated } = useAuth();
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchLoans = useCallback(async () => {
    if (!isAuthenticated) return;
    setLoading(true);
    setError("");
    try {
      const res = await authFetch("/api/loans");
      if (!res.ok) throw new Error("Failed to load loans");
      setLoans(await res.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [authFetch, isAuthenticated]);

  const createLoan = useCallback(async ({ bookCopyId, dueDate, borrowerEmail }) => {
    const res = await authFetch("/api/loans", {
      method: "POST",
      body: JSON.stringify({ bookCopyId, dueDate, borrowerEmail }),
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || "Failed to create loan");
    }
    const loan = await res.json();
    setLoans((prev) => [...prev, loan]);
    return loan;
  }, [authFetch]);

  const returnLoan = useCallback(async (id) => {
    const res = await authFetch(`/api/loans/${id}/return`, { method: "PATCH" });
    if (!res.ok) throw new Error("Failed to return loan");
    const updated = await res.json();
    setLoans((prev) => prev.map((l) => (l.id === id ? updated : l)));
  }, [authFetch]);

  const deleteLoan = useCallback(async (id) => {
    const res = await authFetch(`/api/loans/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error("Failed to delete loan");
    setLoans((prev) => prev.filter((l) => l.id !== id));
  }, [authFetch]);

  const value = useMemo(
    () => ({ loans, loading, error, fetchLoans, createLoan, returnLoan, deleteLoan }),
    [loans, loading, error, fetchLoans, createLoan, returnLoan, deleteLoan]
  );

  return <LoanContext.Provider value={value}>{children}</LoanContext.Provider>;
}

export function useLoans() {
  const ctx = useContext(LoanContext);
  if (!ctx) throw new Error("useLoans must be used inside LoanProvider");
  return ctx;
}
