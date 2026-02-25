package app.main.LibraryApp.domain;

import java.time.LocalDate;

import app.main.LibraryApp.domain.enums.LoanStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long bookCopyId;
   
    //private User lender;
    //private User borrower;
    private String loanComment;
    private String borrowerName;
    private String bookTitle;
   
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus loanStatus;

    
        
  
    
}
