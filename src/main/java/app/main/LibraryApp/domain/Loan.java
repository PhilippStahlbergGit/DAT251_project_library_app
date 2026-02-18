package app.main.LibraryApp.domain;

import java.time.LocalDate;

import app.main.LibraryApp.domain.enums.LoanStatus;

public class Loan {
    BookCopy bookCopy;
    User lender;
    User borrower;
    LocalDate loanDate;
    LocalDate dueDate;
    LocalDate returnDate;
    LoanStatus loanStatus;
}
