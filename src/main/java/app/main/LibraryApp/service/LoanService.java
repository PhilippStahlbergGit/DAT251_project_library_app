package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.LoanStatus;

@Service
public class LoanService {

    private List<Loan> loans;


    public LoanService() {
        this.loans = new ArrayList<>();
    }

    public Loan createLoan(BookCopy bookCopy, String strBorrower) {
        
       if(bookCopy.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            bookCopy.setAvailabilityStatus(AvailabilityStatus.LOANED);
        } else {
            throw new IllegalStateException("Book copy is not available for loan.");
        }

        Loan loan = new Loan();
        
        loan.setBookCopy(bookCopy);
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setStrBorrower(strBorrower);
        loan.setLoanComment("The book " + bookCopy.getBook().getTitle() + " was loaned to " + strBorrower);
        
        this.loans.add(loan);
        return loan;
    }

    public void deleteLoan(Long id) {
        Loan loanToRemove = this.loans.stream()
            .filter(loan -> loan.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Loan with id " + id + " not found."));

        loanToRemove.getBookCopy().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        loanToRemove.setLoanStatus(LoanStatus.RETURNED);

    }

    public List<Loan> getAllLoans() {
        return this.loans;
    }









}
