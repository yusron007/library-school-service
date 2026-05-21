package com.school.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String bookCode;
    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private Integer stock;
    private Integer rentedQty;
    private Integer availableQty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
