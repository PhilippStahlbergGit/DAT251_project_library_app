package app.main.LibraryApp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.main.LibraryApp.domain.BookCopy;
import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.domain.enums.AvailabilityStatus;
import app.main.LibraryApp.domain.enums.LoanStatus;
import app.main.LibraryApp.domain.dto.LoanRequest;
import app.main.LibraryApp.repository.BookCopyRepository;
import app.main.LibraryApp.repository.LoanRepository;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserService userService;

    public LoanService(LoanRepository loanRepository, BookCopyRepository bookCopyRepository, UserService userService) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.userService = userService;
    }

    @Transactional
    public Loan createLoan(String requesterEmail, LoanRequest request) {
        BookCopy bookCopy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new RuntimeException("Book copy not found"));

        if (bookCopy.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new RuntimeException("Book copy is not available");
        }

        User borrower;
        if (request.getBorrowerEmail() != null && !request.getBorrowerEmail().isBlank()) {
            // Owner is lending to someone else — verify the book belongs to the requester
            if (!bookCopy.getLibrary().getUser().getEmail().equals(requesterEmail)) {
                throw new RuntimeException("Unauthorized: book copy does not belong to your library");
            }
            borrower = userService.getUserByEmail(request.getBorrowerEmail());
        } else {
            // Requester is borrowing someone else's book
            borrower = userService.getUserByEmail(requesterEmail);
        }

        Loan loan = new Loan();
        loan.setBookCopy(bookCopy);
        loan.setBorrower(borrower);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(request.getDueDate());
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setLoanComment("'" + bookCopy.getBook().getTitle() + "' loaned to " + borrower.getName());

        bookCopy.setAvailabilityStatus(AvailabilityStatus.LOANED);
        bookCopyRepository.save(bookCopy);

        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans(String borrowerEmail) {
        User borrower = userService.getUserByEmail(borrowerEmail);
        return loanRepository.findByBorrowerId(borrower.getId());
    }

    @Transactional
    public Loan returnLoan(String borrowerEmail, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(borrowerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setLoanStatus(LoanStatus.RETURNED);

        loan.getBookCopy().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        bookCopyRepository.save(loan.getBookCopy());

        return loanRepository.save(loan);
    }

    @Transactional
    public void deleteLoan(String borrowerEmail, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(borrowerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (loan.getLoanStatus() == LoanStatus.ACTIVE) {
            loan.getBookCopy().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            bookCopyRepository.save(loan.getBookCopy());
        }

        loanRepository.delete(loan);
    }
}
