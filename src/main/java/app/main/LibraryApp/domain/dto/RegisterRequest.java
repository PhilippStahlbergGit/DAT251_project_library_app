package app.main.LibraryApp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    String username;

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Size(min = 8, max = 100)
    String password;
}
