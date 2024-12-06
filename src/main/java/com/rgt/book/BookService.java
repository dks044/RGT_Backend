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
	
    public Page<Book> searchBooks(String author, String bookName, Pageable pageable) {
        return bookRepository.findAll((Specification<Book>) (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            if (author != null && !author.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("author"), "%" + author + "%"));
            }
            if (bookName != null && !bookName.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("bookName"), "%" + bookName + "%"));
            }
            return predicates;
        }, pageable);
    }
    
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
}
