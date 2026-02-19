import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("library_user");
    return saved ? JSON.parse(saved) : null;
  });

  const register = async ({ name, email, password }) => {
    // TODO: replace mock with API call
    // const res = await fetch("/api/auth/register", {
    //   method: "POST",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ name, email, password }),
    // });
    // if (!res.ok) throw new Error(await res.text());

    console.log("MOCK register:", { name, email, password });
  };

  const login = async ({ email, password }) => {
    // TODO: replace mock with API call
    // const res = await fetch("/api/auth/login", {
    //   method: "POST",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ email, password }),
    // });
    // if (!res.ok) throw new Error(await res.text());
    // const data = await res.json();
    // const loggedIn = data.user;

    const loggedIn = { name: "Test User", email };
    console.log("MOCK login:", loggedIn);

    setUser(loggedIn);
    localStorage.setItem("library_user", JSON.stringify(loggedIn));
  };

  const logout = async () => {
    // TODO: replace mock with API call
    // await fetch("/api/auth/logout", { method: "POST" });

    setUser(null);
    localStorage.removeItem("library_user");
    window.location.href = "/";
  };

  const value = useMemo(
    () => ({ user, isAuthenticated: !!user, register, login, logout }),
    [user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}