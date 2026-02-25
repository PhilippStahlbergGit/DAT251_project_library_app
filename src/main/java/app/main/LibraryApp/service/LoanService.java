package app.main.LibraryApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.Loan;

@Service
public class LoanService {

    private List<Loan> loans;


    public LoanService() {
        this.loans = new ArrayList<>();
    }

    public Loan createLoan(BookCopy bookCopy, String strBorrower) {
        
       
        Loan loan = new Loan();
        loan.setStrBorrower(strBorrower);
        loan.setLoanComment("The book " + bookCopy + " was loaned to " + strBorrower);
        
        this.loans.add(loan);
        return loan;
    }

    public void deleteLoan(Long id) {
        this.loans.removeIf(loan -> loan.getId().equals(id));
    }

    public List<Loan> getAllLoans() {
        return this.loans;
    }









}
