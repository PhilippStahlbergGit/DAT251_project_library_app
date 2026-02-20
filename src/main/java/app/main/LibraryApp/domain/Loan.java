package app.main.LibraryApp.domain;

import java.time.LocalDate;

import app.main.LibraryApp.domain.enums.LoanStatus;
import lombok.Data;

@Data
public class Loan {
    private BookCopy bookCopy;
    private User lender;
    private User borrower;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus loanStatus;

    public Loan(BookCopy bookCopy, User lender, User borrower, LocalDate loanDate, LocalDate dueDate) {
        this.bookCopy = bookCopy;
        this.lender = lender;
        this.borrower = borrower;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.loanStatus = LoanStatus.ACTIVE;
    }
}
