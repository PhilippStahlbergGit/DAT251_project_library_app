import { createContext, useCallback, useContext, useMemo, useState } from "react";

const JokeContext = createContext(null);

export function JokeProvider({ children }) {
  const [joke, setJoke] = useState("");
  const [loadingJoke, setLoadingJoke] = useState(false);
  const [jokeError, setJokeError] = useState("");

  const fetchJoke = useCallback(async () => {
    try {
      setLoadingJoke(true);
      setJokeError("");

      const res = await fetch("https://api.chucknorris.io/jokes/random");
      if (!res.ok) throw new Error("Failed to fetch joke");

      const data = await res.json();
      setJoke(data.value ?? "");
    } catch {
      setJokeError("Could not load joke right now.");
    } finally {
      setLoadingJoke(false);
    }
  }, []);

  const value = useMemo(
    () => ({ joke, loadingJoke, jokeError, fetchJoke }),
    [joke, loadingJoke, jokeError, fetchJoke]
  );

  return <JokeContext.Provider value={value}>{children}</JokeContext.Provider>;
}

export function useJoke() {
  const ctx = useContext(JokeContext);
  if (!ctx) throw new Error("useJoke must be used inside JokeProvider");
  return ctx;
}