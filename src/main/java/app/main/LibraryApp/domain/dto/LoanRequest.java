package app.main.LibraryApp.domain.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LoanRequest {
    private Long bookCopyId;
    private LocalDate dueDate;
}
