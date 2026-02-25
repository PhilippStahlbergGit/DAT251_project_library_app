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

    public Loan createLoan(Long bookCopyId, String borrowerName) {
        
        BookCopy bookCopy = findBookCopyById(bookCopyId);


       if(bookCopy.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            bookCopy.setAvailabilityStatus(AvailabilityStatus.LOANED);
        } else {
            throw new IllegalStateException("Book copy is not available for loan.");
        }

        Loan loan = new Loan();
        loan.setBookCopyId(bookCopyId);
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setBorrowerName(borrowerName);
        loan.setLoanComment("The book " + bookCopy.getBook().getTitle() + " was loaned to " + borrowerName);
        
        this.loans.add(loan);
        return loan;
    }

    private BookCopy findBookCopyById(Long bookCopyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findBookCopyById'");
    }

    public void deleteLoan(Long loanID) {
        Loan loanToRemove = this.loans.stream()
            .filter(loan -> loan.getId().equals(loanID))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Loan with id " + loanID + " not found."));

        BookCopy bookCopy = findBookCopyById(loanToRemove.getBookCopyId());
        
        bookCopy.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        loanToRemove.setLoanStatus(LoanStatus.RETURNED);

    }

    public List<Loan> getAllLoans() {
        return this.loans;
    }









}
