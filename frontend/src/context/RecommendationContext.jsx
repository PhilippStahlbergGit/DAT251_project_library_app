import { createContext, useCallback, useContext, useMemo, useState } from "react";

const RecommendationsContext = createContext(null);

const MOCK_RECOMMENDATIONS = [
  { book_name: "Clean Code", score: 0.98 },
  { book_name: "The Pragmatic Programmer", score: 0.95 },
  { book_name: "Refactoring", score: 0.93 },
  { book_name: "Design Patterns", score: 0.91 },
  { book_name: "Domain-Driven Design", score: 0.89 },
  { book_name: "Effective Java", score: 0.87 },
  { book_name: "You Don’t Know JS", score: 0.85 },
  { book_name: "Code Complete", score: 0.84 },
  { book_name: "Head First Design Patterns", score: 0.82 },
  { book_name: "Working Effectively with Legacy Code", score: 0.80 },
];

export function RecommendationsProvider({ children }) {
  const [recommendations, setRecommendations] = useState([]);
  const [loadingRecommendations, setLoadingRecommendations] = useState(false);
  const [recommendationsError, setRecommendationsError] = useState("");

  const fetchRecommendations = useCallback(async (ownedBookIds = [], k = 10) => {
    setLoadingRecommendations(true);
    setRecommendationsError("");

    try {
      // MOCK for now:
      await new Promise((r) => setTimeout(r, 200));
      setRecommendations(MOCK_RECOMMENDATIONS.slice(0, k));

      // TODO: enable later
      // const res = await fetch("http://127.0.0.1:8000/recommend", {
      //   method: "POST",
      //   headers: { "Content-Type": "application/json" },
      //   body: JSON.stringify({ owned_book_ids: ownedBookIds, k }),
      // });
      // if (!res.ok) throw new Error("Failed to fetch recommendations");
      // const data = await res.json();
      // setRecommendations((data.recommendations ?? []).slice(0, k));
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