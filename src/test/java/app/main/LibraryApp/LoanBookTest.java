package app.main.LibraryApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.enums.LoanStatus;
import app.main.LibraryApp.service.LoanService;

class LoanBookTest {
    @Test
    void testCreateLoan() {
        // test for creating a loan
        
        LoanService loanService = new LoanService();
        Loan loan = loanService.createLoan("John Doe", "The Great Gatsby");

        assertEquals("John Doe", loan.getBorrowerName());
        assertEquals("The Great Gatsby", loan.getBookTitle());
        assertEquals(LoanStatus.ACTIVE, loan.getLoanStatus());
    }

    // @Test
    // void testReturnLoan() {
    //     // test for returning a loan
        
    //     LoanService loanService = new LoanService();
    //     Loan loan = loanService.createLoan("John Doe", "The Great Gatsby");
    //     loanService.setStatus(loan, LoanStatus.RETURNED);

    //     assertEquals(LoanStatus.RETURNED, loan.getLoanStatus());
    // }

    // @Test
    // void testOverdueLoan() {
    //     // test for marking a loan as overdue
        
    //     LoanService loanService = new LoanService();
    //     Loan loan = loanService.createLoan("John Doe", "The Great Gatsby");
    //     loanService.setStatus(loan, LoanStatus.OVERDUE);

    //     assertEquals(LoanStatus.OVERDUE, loan.getLoanStatus());
    // }

    // @Test
    // void testCancelLoan() {
    //     // test for canceling a loan
        
    //     LoanService loanService = new LoanService();
    //     Loan loan = loanService.createLoan("John Doe", "The Great Gatsby");
    //     loanService.setStatus(loan, LoanStatus.CANCELED);

    //     assertEquals(LoanStatus.CANCELED, loan.getLoanStatus());
    // }

    // @Test
    // void testDeleteLoan() {
    //     // test for deleting a loan
        
    //     LoanService loanService = new LoanService();
    //     Loan loan = loanService.createLoan("John Doe", "The Great Gatsby");
    //     loanService.deleteLoan(loan.getId());

    //     assertEquals(LoanStatus.DELETED, loan.getLoanStatus());
    // }

    @Test
    void getAllLoans() {
        // test for retrieving all loans from the library
        
        LoanService loanService = new LoanService();
        loanService.createLoan("John Doe", "The Great Gatsby");
        loanService.createLoan("Jane Smith", "To Kill a Mockingbird");

        assertEquals(2, loanService.getAllLoans().size());
    }
}
