package app.main.LibraryApp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.LoanStatus;
import app.main.LibraryApp.service.LoanService;

class LoanBookTest {

    @Test
    public void testCreateLoan() {
        // test for creating a loan
        BookCopy bookCopy = new BookCopy();
        bookCopy.setId(1L);
        bookCopy.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);   
        
        LoanService loanService = new LoanService();
        Loan loan = loanService.createLoan(1L, "John Doe");


        assertEquals("John Doe", loan.getBorrowerName());
        
        
        assertEquals(LoanStatus.ACTIVE, loan.getLoanStatus());
        assertEquals(AvailabilityStatus.LOANED, bookCopy.getAvailabilityStatus());
    }
}
