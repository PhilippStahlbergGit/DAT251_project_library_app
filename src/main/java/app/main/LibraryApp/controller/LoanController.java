package app.main.LibraryApp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.main.LibraryApp.domain.Loan;
import app.main.LibraryApp.domain.dto.LoanRequest;
import app.main.LibraryApp.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequest request) {
        try {
            Loan loan = loanService.createLoan(getCurrentUserEmail(), request);
            return ResponseEntity.status(201).body(loan);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Book copy not found")) {
                return ResponseEntity.status(404).build();
            } else if (e.getMessage().equals("Book copy is not available")) {
                return ResponseEntity.status(409).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans(getCurrentUserEmail()));
    }

    @GetMapping("/lent")
    public ResponseEntity<List<Loan>> getLentLoans() {
        return ResponseEntity.ok(loanService.getLentLoans(getCurrentUserEmail()));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<Loan> returnLoan(@PathVariable Long id) {
        try {
            Loan loan = loanService.returnLoan(getCurrentUserEmail(), id);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Loan not found")) {
                return ResponseEntity.status(404).build();
            } else if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLoan(@PathVariable Long id) {
        try {
            loanService.deleteLoan(getCurrentUserEmail(), id);
            return ResponseEntity.ok("Loan deleted");
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Loan not found")) {
                return ResponseEntity.status(404).body("Loan not found");
            } else if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body("Unauthorized");
            }
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
