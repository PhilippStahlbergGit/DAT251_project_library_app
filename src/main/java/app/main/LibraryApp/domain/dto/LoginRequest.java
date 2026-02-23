package app.main.LibraryApp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;
}
