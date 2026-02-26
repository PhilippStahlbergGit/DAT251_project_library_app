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
}
