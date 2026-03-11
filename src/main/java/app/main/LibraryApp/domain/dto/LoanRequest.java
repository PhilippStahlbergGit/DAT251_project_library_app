package app.main.LibraryApp.domain.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LoanRequest {
    private Long bookCopyId;
    private LocalDate dueDate;
    /** Set when the authenticated user is the owner lending to a registered user. */
    private String borrowerEmail;
    /** Set when the authenticated user is the owner lending to a guest (no account required). */
    private String guestBorrowerName;
}
