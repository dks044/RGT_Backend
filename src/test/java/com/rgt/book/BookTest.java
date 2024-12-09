package com.rgt.book;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;

@SpringBootTest
public class BookTest {
	@Autowired
	BookService bookService;
	
	@Test
	@Transactional
	@Description("Book 데이터를 50개 생성하는 과정에서 생성 관련 메소드들을 테스트한다.")
    void Book데이터_50개생성_테스트() {
        for (int i = 1; i <= 50; i++) {
            Book book = Book.builder()
                    .bookName("Book " + i)
                    .author("Author " + i)
                    .amount(10)
                    .description("Description for Book " + i)
                    .price(1000 + (i * 10))
                    .publicationDate(new Date())
                    .build();

            Book createdBook = bookService.createBook(book);
            assertNotNull(createdBook.getId(), "책이 생성되지 않았습니다.");
        }
    }
}
