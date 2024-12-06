package com.rgt.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;
	
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
}
