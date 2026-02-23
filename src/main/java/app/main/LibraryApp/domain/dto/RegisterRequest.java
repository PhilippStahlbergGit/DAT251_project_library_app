package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    String username;
    String email;
    String password;
}
