package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.enums.LoanStatus;

@Service
public class LoanService {

    private List<Loan> loans;

    public LoanService() {
        this.loans = new ArrayList<>();
    }

    public Loan createLoan(String borrowerName, String bookTitle) {

        Loan loan = new Loan();
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setBorrowerName(borrowerName);
        loan.setBookTitle(bookTitle);
        loan.setLoanComment("The book " + bookTitle + " is loaned to " + borrowerName);
        
        this.loans.add(loan);
        return loan;
    }


    public void deleteLoan(Long loanID) {
        // TODO: implement deleteLoan method
        // Need loanId for this method
        // find the loan by ID and set its status to DELETED
    }

    public List<Loan> getAllLoans() {
        return this.loans;
    }









}
