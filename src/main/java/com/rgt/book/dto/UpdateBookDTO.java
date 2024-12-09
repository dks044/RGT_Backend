package com.rgt.book.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookDTO {

    private String bookName;
    
    private String author;
    
    private int amount;
    
    private String description;
    
    private int price;
    
    private Date publicationDate;
}
