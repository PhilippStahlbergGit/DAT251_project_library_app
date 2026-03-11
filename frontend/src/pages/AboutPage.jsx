import "./AboutPage.css";

export default function AboutPage() {
  return (
    <section className="about-page">
      <h1>About Library App</h1>
      <p className="about-lead">
        A peer-to-peer library system where registered users can manage their
        personal book collections and lend books to each other.
      </p>

      <div className="about-cards">
        <div className="about-card">
          <h2>Manage Your Collection</h2>
          <p>
            Add books to your personal library. Metadata like publisher and
            publication year is fetched automatically from an external book API
            so you only need a title and author.
          </p>
        </div>

        <div className="about-card">
          <h2>Peer Loans</h2>
          <p>
            Lend books from your library to other registered users or to guests
            without an account. Each loan tracks the due date and return date,
            and the book's availability status is updated automatically.
          </p>
        </div>

        <div className="about-card">
          <h2>Recommendations</h2>
          <p>
            Get personalised book recommendations based on the titles already
            in your library. Powered by a collaborative-filtering model.
          </p>
        </div>

        <div className="about-card">
          <h2>Secure by Default</h2>
          <p>
            All endpoints require authentication via JWT. Tokens are
            invalidated on logout using a server-side blacklist, so signing
            out is always safe.
          </p>
        </div>
      </div>

      <div className="about-tech">
        <h2>Tech Stack</h2>
        <ul>
          <li><strong>Backend:</strong> Spring Boot 3, Spring Security, JPA / Hibernate, H2 (dev) · PostgreSQL (prod)</li>
          <li><strong>Auth:</strong> JWT with token blacklisting</li>
          <li><strong>Frontend:</strong> React 19, React Router v7, Context API</li>
          <li><strong>Build:</strong> Gradle · Vite</li>
        </ul>
      </div>
    </section>
  );
}
