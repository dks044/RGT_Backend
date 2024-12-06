package com.rgt.book;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rgt.book.dto.CreateBookDTO;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
	private final BookService bookService;
	
    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String bookName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Book> books = bookService.searchBooks(author, bookName, pageable);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            
            return ResponseEntity.internalServerError().body("Server error");
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody CreateBookDTO createBookDTO) {
        try {
            Book createdBook = bookService.createBook(Book.builder()
            														.bookName(createBookDTO.getBookName())
            														.author(createBookDTO.getAuthor())
            														.amount(createBookDTO.getAmount())
            														.description(createBookDTO.getDescription())
            														.price(createBookDTO.getPrice())
            														.publicationDate(createBookDTO.getPublicationDate())
            														.build());
            return ResponseEntity.status(201).body(createdBook);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Book name must be unique.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }
}
