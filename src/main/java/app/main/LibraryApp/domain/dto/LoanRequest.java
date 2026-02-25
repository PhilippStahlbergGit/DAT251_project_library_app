package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class LoanRequest {
    
    private String loanComment;
    private String strBorrower;
}
