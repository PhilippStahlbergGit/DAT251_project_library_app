import { useState } from "react";
import { useBooks } from "../context/BookContext.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import "./QuizPage.css";

export default function QuizPage() {
  const { books } = useBooks();
  const { authFetch } = useAuth();

  const [selectedCopyId, setSelectedCopyId] = useState(null);
  const [numQuestions, setNumQuestions] = useState(5);
  const [quiz, setQuiz] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [submitted, setSubmitted] = useState(false);

  const startQuiz = async () => {
    if (!selectedCopyId) return;
    setLoading(true);
    setError("");
    setQuiz(null);
    setSelectedAnswers({});
    setSubmitted(false);
    setCurrentIndex(0);
    try {
      const res = await authFetch("/api/quiz/generate", {
        method: "POST",
        body: JSON.stringify({ bookCopyId: selectedCopyId, numQuestions }),
      });
      if (!res.ok) {
        const msg = res.status === 503
          ? "Quiz service is not configured. Ask your admin to set ANTHROPIC_API_KEY."
          : `Failed to generate quiz (${res.status})`;
        throw new Error(msg);
      }
      const data = await res.json();
      setQuiz(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const selectOption = (questionIndex, optionIndex) => {
    if (submitted) return;
    setSelectedAnswers((prev) => ({ ...prev, [questionIndex]: optionIndex }));
  };

  const submitQuiz = () => {
    setSubmitted(true);
    setCurrentIndex(0);
  };

  const resetQuiz = () => {
    setQuiz(null);
    setSelectedAnswers({});
    setSubmitted(false);
    setCurrentIndex(0);
    setError("");
  };

  const score = submitted
    ? quiz.questions.filter((q, i) => selectedAnswers[i] === q.correctIndex).length
    : 0;

  if (books.length === 0) {
    return (
      <section>
        <h1>Book Quiz</h1>
        <p className="quiz-empty">No books in your library yet. Add some books first!</p>
      </section>
    );
  }

  if (quiz && !submitted) {
    const question = quiz.questions[currentIndex];
    const total = quiz.questions.length;
    const allAnswered = Object.keys(selectedAnswers).length === total;

    return (
      <section className="quiz-section">
        <div className="quiz-header">
          <h1>Quiz: {quiz.bookTitle}</h1>
          <span className="quiz-progress">{currentIndex + 1} / {total}</span>
        </div>

        <div className="quiz-card">
          <p className="quiz-question">{question.question}</p>
          <ul className="quiz-options">
            {question.options.map((opt, i) => (
              <li key={i}>
                <button
                  className={`quiz-option-btn${selectedAnswers[currentIndex] === i ? " selected" : ""}`}
                  onClick={() => selectOption(currentIndex, i)}
                >
                  <span className="quiz-option-letter">{String.fromCharCode(65 + i)}</span>
                  {opt}
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div className="quiz-nav">
          <button
            className="quiz-nav-btn"
            disabled={currentIndex === 0}
            onClick={() => setCurrentIndex((i) => i - 1)}
          >
            Previous
          </button>
          {currentIndex < total - 1 ? (
            <button
              className="quiz-nav-btn primary"
              onClick={() => setCurrentIndex((i) => i + 1)}
            >
              Next
            </button>
          ) : (
            <button
              className="quiz-nav-btn primary"
              disabled={!allAnswered}
              title={!allAnswered ? "Answer all questions first" : ""}
              onClick={submitQuiz}
            >
              Submit Quiz
            </button>
          )}
        </div>
      </section>
    );
  }

  if (quiz && submitted) {
    const total = quiz.questions.length;
    const pct = Math.round((score / total) * 100);

    return (
      <section className="quiz-section">
        <div className="quiz-results-header">
          <h1>Results: {quiz.bookTitle}</h1>
          <div className={`quiz-score ${pct >= 70 ? "score-good" : pct >= 40 ? "score-ok" : "score-bad"}`}>
            {score} / {total} &mdash; {pct}%
          </div>
        </div>

        {quiz.questions.map((q, i) => {
          const chosen = selectedAnswers[i];
          const correct = q.correctIndex;
          const isRight = chosen === correct;
          return (
            <div key={i} className={`quiz-result-card ${isRight ? "correct" : "incorrect"}`}>
              <p className="quiz-result-question">
                <span className="quiz-result-num">{i + 1}.</span> {q.question}
              </p>
              <ul className="quiz-result-options">
                {q.options.map((opt, j) => (
                  <li
                    key={j}
                    className={
                      j === correct ? "option-correct" :
                      j === chosen && !isRight ? "option-wrong" : ""
                    }
                  >
                    <span className="quiz-option-letter">{String.fromCharCode(65 + j)}</span>
                    {opt}
                    {j === correct && <span className="option-tick"> ✓</span>}
                    {j === chosen && !isRight && <span className="option-cross"> ✗</span>}
                  </li>
                ))}
              </ul>
              {q.explanation && (
                <p className="quiz-explanation">{q.explanation}</p>
              )}
            </div>
          );
        })}

        <div className="quiz-results-actions">
          <button className="quiz-retry-btn" onClick={() => { setSubmitted(false); setSelectedAnswers({}); setCurrentIndex(0); }}>
            Retry
          </button>
          <button className="quiz-new-btn" onClick={resetQuiz}>
            New Quiz
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="quiz-section">
      <h1>Book Quiz</h1>
      <p className="quiz-intro">Select a book from your library and test your knowledge with an AI-generated quiz.</p>

      <div className="quiz-setup">
        <label className="quiz-label">Choose a book</label>
        <div className="quiz-book-list">
          {books.map((copy) => (
            <button
              key={copy.id}
              className={`quiz-book-btn${selectedCopyId === copy.id ? " selected" : ""}`}
              onClick={() => setSelectedCopyId(copy.id)}
            >
              <span className="quiz-book-title">{copy.book?.title}</span>
              {copy.book?.authors?.[0] && (
                <span className="quiz-book-author">{copy.book.authors[0]}</span>
              )}
            </button>
          ))}
        </div>

        <label className="quiz-label">Number of questions</label>
        <div className="quiz-num-options">
          {[3, 5, 10].map((n) => (
            <button
              key={n}
              className={`quiz-num-btn${numQuestions === n ? " selected" : ""}`}
              onClick={() => setNumQuestions(n)}
            >
              {n}
            </button>
          ))}
        </div>

        {error && <p className="quiz-error">{error}</p>}

        <button
          className="quiz-start-btn"
          disabled={!selectedCopyId || loading}
          onClick={startQuiz}
        >
          {loading ? "Generating quiz…" : "Start Quiz"}
        </button>
      </div>
    </section>
  );
}
