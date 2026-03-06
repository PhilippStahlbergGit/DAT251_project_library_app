package app.main.LibraryApp.domain.dto;

import app.main.LibraryApp.domain.BookCopy;
import lombok.Data;

@Data
public class LoanRequest {
    
    private String bookTitle;
    private String borrowerName;
}
