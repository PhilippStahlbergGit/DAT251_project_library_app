import { createContext, useCallback, useContext, useMemo, useState } from "react";

const RecommendationsContext = createContext(null);

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function readError(res) {
  try {
    const data = await res.json();
    return data?.detail || data?.message || `Request failed (${res.status})`;
  } catch {
    return `Request failed (${res.status})`;
  }
}

function normalizeOwnedBooks(ownedBooks = []) {
  return ownedBooks
    .map((item) => item?.book ?? item)
    .map((book) => {
      const publicationYearRaw = book?.publicationYear ?? book?.year;
      const publicationYear = Number.isFinite(Number(publicationYearRaw))
        ? Number(publicationYearRaw)
        : null;

      const authors = Array.isArray(book?.authors)
        ? book.authors
        : book?.author
          ? [book.author]
          : [];

      return {
        id: Number.isFinite(Number(book?.id)) ? Number(book.id) : null,
        isbn: book?.isbn ?? null,
        title: book?.title ?? null,
        authors,
        publisher: book?.publisher ?? null,
        publicationYear,
        genre: book?.genre ?? null,
      };
    })
    .filter((book) => typeof book.title === "string" && book.title.trim().length > 0);
}

async function waitUntilRecommendationDataReady({ maxAttempts = 20, intervalMs = 1000 } = {}) {
  for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
    const res = await fetch("/data/status");
    if (!res.ok) {
      throw new Error(await readError(res));
    }

    const status = await res.json();
    if (status?.ready) {
      return;
    }

    if (status?.error) {
      throw new Error(`Recommendation data load failed: ${status.error}`);
    }

    if (attempt < maxAttempts) {
      await sleep(intervalMs);
    }
  }

  throw new Error("Recommendation data is still loading. Please try again shortly.");
}

export function RecommendationsProvider({ children }) {
  const [recommendations, setRecommendations] = useState([]);
  const [loadingRecommendations, setLoadingRecommendations] = useState(false);
  const [recommendationsError, setRecommendationsError] = useState("");

  const fetchRecommendations = useCallback(async (ownedBooks = [], k = 10) => {
    setLoadingRecommendations(true);
    setRecommendationsError("");

    try {
      const normalizedOwnedBooks = normalizeOwnedBooks(ownedBooks);
      if (!normalizedOwnedBooks.length) {
        setRecommendations([]);
        return;
      }

      await waitUntilRecommendationDataReady();

      const res = await fetch("/recommend", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ owned_books: normalizedOwnedBooks, k }),
      });

      if (!res.ok) {
        throw new Error(await readError(res));
      }

      const data = await res.json();
      const parsedRecommendations = (data?.recommendations ?? []).map((rec) => ({
        book_name: rec?.book?.title ?? rec?.book_name ?? "Unknown title",
        score: Number(rec?.score ?? 0),
        book: rec?.book ?? null,
      }));

      setRecommendations(parsedRecommendations.slice(0, k));
    } catch (err) {
      setRecommendations([]);
      setRecommendationsError(err.message || "Could not load recommendations");
    } finally {
      setLoadingRecommendations(false);
    }
  }, []);

  const clearRecommendations = useCallback(() => {
    setRecommendations([]);
    setRecommendationsError("");
  }, []);

  const value = useMemo(
    () => ({
      recommendations,
      loadingRecommendations,
      recommendationsError,
      fetchRecommendations,
      clearRecommendations,
    }),
    [recommendations, loadingRecommendations, recommendationsError, fetchRecommendations, clearRecommendations]
  );

  return (
    <RecommendationsContext.Provider value={value}>
      {children}
    </RecommendationsContext.Provider>
  );
}

export function useRecommendations() {
  const ctx = useContext(RecommendationsContext);
  if (!ctx) throw new Error("useRecommendations must be used within RecommendationsProvider");
  return ctx;
}