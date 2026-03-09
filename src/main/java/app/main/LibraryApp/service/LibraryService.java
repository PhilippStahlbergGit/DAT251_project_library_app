package app.main.LibraryApp.service;

import org.springframework.stereotype.Service;

import app.main.LibraryApp.domain.Library;
import app.main.LibraryApp.domain.User;
import app.main.LibraryApp.repository.LibraryRepository;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;

    public LibraryService(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    public Library getLibraryByEmail(String email) {
        return libraryRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Library not found"));
    }

    public Library createLibrary(User user) {
        Library library = new Library();
        library.setUser(user);
        return libraryRepository.save(library);
    }
}