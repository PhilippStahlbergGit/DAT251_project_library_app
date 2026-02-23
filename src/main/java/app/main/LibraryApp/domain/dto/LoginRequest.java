package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class LoginRequest {

    String email;
    String password;
}
