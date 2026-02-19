import { useState } from 'react'
import MainLayout from './layouts/Layout'
import HomePage from "./pages/HomePage"
import { Route, Routes, Navigate} from 'react-router-dom'
import LoginPage from './pages/Auth/LoginPage'
import { useAuth } from './context/AuthContext'
import RegisterPage from './pages/Auth/RegisterPage'
import BooksPage from './pages/BooksPage'

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/books" element={<BooksPage />} />
        <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
        <Route path="/register" element={isAuthenticated ? <Navigate to="/" replace /> : <RegisterPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
