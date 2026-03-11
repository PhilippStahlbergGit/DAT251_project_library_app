import { createContext, useCallback, useContext, useMemo, useState } from "react";
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("library_user");
    return saved ? JSON.parse(saved) : null;
  });
  const authFetch = useCallback(
    (url, options = {}) => {
      const token = user?.token;
      return fetch(url, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...(token && { Authorization: `Bearer ${token}` }),
          ...options.headers,
        },
      });
    },
    [user]
  );
  const register = async ({ name, email, password }) => {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email, password }),
    });
    if (!res.ok) throw new Error(await res.text());
  };
  const login = async ({ email, password }) => {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) throw new Error(await res.text());
    const data = await res.json();
    setUser(data);
    localStorage.setItem("library_user", JSON.stringify(data));
  };
  const logout = async () => {
    await authFetch("/api/auth/logout", { method: "POST" });
    setUser(null);
    localStorage.removeItem("library_user");
    window.location.href = "/";
  };
  const value = useMemo(
    () => ({ user, isAuthenticated: !!user, register, login, logout, authFetch }),
    [user, authFetch]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
