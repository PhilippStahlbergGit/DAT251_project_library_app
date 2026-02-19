package app.main.LibraryApp.domain;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String profileInfo;
    private LocalDate registrationDate;
    private Library library;

}
