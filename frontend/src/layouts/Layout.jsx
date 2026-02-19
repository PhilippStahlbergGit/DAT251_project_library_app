import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Layout.css";

export default function MainLayout() {

  const { isAuthenticated, user, logout } = useAuth();
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="page-container header-inner">
          <div className="brand">Library App</div>
          <nav className="nav">
            <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
              Home
            </NavLink>
            <NavLink to="/books" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
              Books
            </NavLink>
            <NavLink to="/about" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
              About
            </NavLink>
            {!isAuthenticated ? (
             <>
                <NavLink to="/login" className={({ isActive}) => `nav-link ${isActive ? "active" : ""}`}>
                Login
                </NavLink>
                <NavLink to="register" className={({ isActive}) => `nav-link ${isActive ? "active" : ""}`}>
                Register
                </NavLink>
            </>
            ):(
                <>
                    <span className="nav-user"> Hi, {user?.name}</span>
                    <button className="nav-logout" onClick={logout}>Logout</button>
                </>
            )}
            
          </nav>
        </div>
      </header>

      <main className="app-main">
        <div className="page-container">
          <Outlet />
        </div>
      </main>

      <footer className="app-footer">
        <div className="page-container">© 2026 Library App</div>
      </footer>
    </div>
  );
}