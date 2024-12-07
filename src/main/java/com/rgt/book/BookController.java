package com.rgt.book;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rgt.book.dto.CreateBookDTO;
import com.rgt.book.dto.UpdateBookDTO;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
	private final BookService bookService;
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/test")
	public String test() {
		return "hi";
	}
	
	
	@GetMapping
	public ResponseEntity<?> getBooks(
	        @RequestParam(name = "searchTerm", required = false) String searchTerm, 
	        @RequestParam(name = "page", defaultValue = "0") int page,
	        @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
	    try {
	        Pageable pageable = PageRequest.of(page, size);
	        Page<Book> books = bookService.searchBooks(searchTerm, pageable); 
	        return ResponseEntity.ok(books);
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body("Server error");
	    }
	}
    
	@GetMapping("/{id}")
	public ResponseEntity<?> getBook(@PathVariable("id") Long id) {
	    try {
	        Book book = bookService.getBookById(id);
	        if (book != null) {
	            return ResponseEntity.ok(book);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body("Server error");
	    }
	}
	
	@PreAuthorize("hasRole('ADMIN')")
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
    
	@PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable("id") Long id, @RequestBody UpdateBookDTO updateBookDTO){
    	try {
    		Book updateBook = bookService.setBook(id, updateBookDTO);
    		return ResponseEntity.ok().body(updateBook);
		} catch (Exception e) {
			log.error("Book not found with Id => "+id);
			return ResponseEntity.badRequest().body("Book not found with Id => "+id);
		}
    }
	
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteBook(@PathVariable("id") Long id) {
	    try {
	        Book deletedBook = bookService.deleteBook(id);
	        return ResponseEntity.ok().body(deletedBook);
	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body("Server error");
	    }
	}
}
