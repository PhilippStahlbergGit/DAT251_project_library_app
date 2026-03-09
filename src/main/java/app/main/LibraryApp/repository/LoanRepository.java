package app.main.LibraryApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.main.LibraryApp.domain.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerId(Long borrowerId);
}
