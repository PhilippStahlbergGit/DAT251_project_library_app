import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext.jsx'
import { BookProvider } from './context/BookContext.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <BookProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </BookProvider>
    </AuthProvider>
  </StrictMode>,
)
