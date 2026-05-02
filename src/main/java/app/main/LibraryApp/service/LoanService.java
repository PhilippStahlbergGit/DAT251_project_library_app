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

        Loan loan = new Loan();
        loan.setBookCopy(bookCopy);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(request.getDueDate());
        loan.setLoanStatus(LoanStatus.ACTIVE);

        if (request.getGuestBorrowerName() != null && !request.getGuestBorrowerName().isBlank()) {
            lendToGuest(loan, bookCopy, request, requesterEmail);
        } else if (request.getBorrowerEmail() != null && !request.getBorrowerEmail().isBlank()) {
            lendToRegisteredUser(loan, bookCopy, request, requesterEmail);
        } else {
            borrowForSelf(loan, requesterEmail);
        }

        bookCopy.setAvailabilityStatus(AvailabilityStatus.LOANED);
        bookCopyRepository.save(bookCopy);

        return loanRepository.save(loan);
    }

    private void lendToGuest(Loan loan, BookCopy bookCopy, LoanRequest request, String requesterEmail) {
        if (!bookCopy.getLibrary().getUser().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("Unauthorized: book copy does not belong to your library");
        }
        loan.setGuestBorrowerName(request.getGuestBorrowerName());
        loan.setLoanComment("'" + bookCopy.getBook().getTitle() + "' loaned to " + request.getGuestBorrowerName());
    }

    private void lendToRegisteredUser(Loan loan, BookCopy bookCopy, LoanRequest request, String requesterEmail) {
        if (!bookCopy.getLibrary().getUser().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("Unauthorized: book copy does not belong to your library");
        }
        User borrower = userService.getUserByEmail(request.getBorrowerEmail());
        loan.setBorrower(borrower);
        loan.setLoanComment("'" + bookCopy.getBook().getTitle() + "' loaned to " + borrower.getName());
    }

    private void borrowForSelf(Loan loan, String requesterEmail) {
        User borrower = userService.getUserByEmail(requesterEmail);
        loan.setBorrower(borrower);
        loan.setLoanComment("'" + loan.getBookCopy().getBook().getTitle() + "' loaned to " + borrower.getName());
    }

    public List<Loan> getAllLoans(String borrowerEmail) {
        User borrower = userService.getUserByEmail(borrowerEmail);
        return loanRepository.findByBorrowerId(borrower.getId());
    }

    public List<Loan> getLentLoans(String ownerEmail) {
        User owner = userService.getUserByEmail(ownerEmail);
        return loanRepository.findByBookCopyLibraryUserId(owner.getId());
    }

    @Transactional
    public Loan returnLoan(String requesterEmail, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getBorrower() != null) {
            if (!loan.getBorrower().getEmail().equals(requesterEmail)) {
                throw new RuntimeException("Unauthorized");
            }
        } else {
            // Guest loan — only the book copy owner can mark it returned
            if (!loan.getBookCopy().getLibrary().getUser().getEmail().equals(requesterEmail)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        loan.setReturnDate(LocalDate.now());
        loan.setLoanStatus(LoanStatus.RETURNED);

        loan.getBookCopy().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        bookCopyRepository.save(loan.getBookCopy());

        return loanRepository.save(loan);
    }

    @Transactional
    public void deleteLoan(String requesterEmail, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getBorrower() != null) {
            if (!loan.getBorrower().getEmail().equals(requesterEmail)) {
                throw new RuntimeException("Unauthorized");
            }
        } else {
            // Guest loan — only the book copy owner can delete it
            if (!loan.getBookCopy().getLibrary().getUser().getEmail().equals(requesterEmail)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        if (loan.getLoanStatus() == LoanStatus.ACTIVE) {
            loan.getBookCopy().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            bookCopyRepository.save(loan.getBookCopy());
        }

        loanRepository.delete(loan);
    }
}
