package com.rgt.book;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.rgt.book.dto.UpdateBookDTO;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;
	
	@Transactional
	public Page<Book> searchBooks(String searchTerm, Pageable pageable) {
	    return bookRepository.findAll((Specification<Book>) (root, query, criteriaBuilder) -> {
	        var predicates = criteriaBuilder.conjunction();
	        
	        if (searchTerm != null && !searchTerm.isEmpty()) {
	            // author와 bookName에 대해 OR 조건으로 검색
	            predicates = criteriaBuilder.or(
	                criteriaBuilder.like(root.get("author"), "%" + searchTerm + "%"),
	                criteriaBuilder.like(root.get("bookName"), "%" + searchTerm + "%")
	            );
	        }
	        
	        return predicates;
	    }, pageable);
	}
    
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
    
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public Book setBook(Long id,UpdateBookDTO updateBookDTO) {
    	Optional<Book> book = bookRepository.findById(id);
    	if(book.isPresent()) {
    		bookRepository.save(
    				Book.builder()
    					.id(book.get().getId())
    				    .bookName(updateBookDTO.getBookName())
    				    .author(updateBookDTO.getAuthor())
    				    .amount(updateBookDTO.getAmount())
    				    .description(updateBookDTO.getDescription())
    				    .price(updateBookDTO.getPrice())
    				    .publicationDate(updateBookDTO.getPublicationDate())
    				    .build()
    				);
    		return book.get();
    	}else {
    		log.error("Book not found with Id => "+id);
    		throw new RuntimeException("Book not found with Id => "+id);
    	}
    }
    
    @Transactional
    public Book deleteBook(Long id) {
        Optional<Book> deleteBook = bookRepository.findById(id);
        if(deleteBook.isPresent()) {
        	bookRepository.delete(deleteBook.get());
        	return deleteBook.get();
        }else {
        	throw new EntityNotFoundException("Book not found with id "+id);
        }
 
    }
}
