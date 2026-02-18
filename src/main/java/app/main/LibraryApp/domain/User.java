package app.main.LibraryApp.domain;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    UUID id;
    String name;
    String profileInfo;
    LocalDate registrationDate;
    Library library;
}
