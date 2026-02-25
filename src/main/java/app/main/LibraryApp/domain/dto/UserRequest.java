package app.main.LibraryApp.domain.dto;

import lombok.Data;

@Data
public class UserRequest {

    private String name;
    private String email;
    private String password;
}
