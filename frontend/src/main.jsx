import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext.jsx'
import { BookProvider } from './context/BookContext.jsx'
import { JokeProvider } from './context/JokeContext.jsx'
import { RecommendationsProvider } from './context/RecommendationContext.jsx'
import { LoanProvider } from './context/LoanContext.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <BookProvider>
        <LoanProvider>
          <JokeProvider>
            <RecommendationsProvider>
              <BrowserRouter>
                <App />
              </BrowserRouter>
            </RecommendationsProvider>
          </JokeProvider>
        </LoanProvider>
      </BookProvider>
    </AuthProvider>
  </StrictMode>,
)
