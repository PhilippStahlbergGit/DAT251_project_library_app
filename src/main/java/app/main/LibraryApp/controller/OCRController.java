package app.main.LibraryApp.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.main.LibraryApp.domain.Book;
import app.main.LibraryApp.service.OCRService;

@RestController
@RequestMapping("/ocr")
public class OCRController {

    private final OCRService ocrService;

    public OCRController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/scan")
    public ResponseEntity<List<Book>> scanBooks(@RequestParam("image") MultipartFile image) {
        try {
            List<Book> books = ocrService.scanAndFindBooks(image.getBytes());
            return ResponseEntity.ok(books);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}