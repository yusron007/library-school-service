package com.school.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private Integer stock;
}
