package com.school.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentResponse {
    private Long id;
    private String rentCode;
    private Long memberId;
    private String memberCode;
    private String memberName;
    private Long bookId;
    private String bookCode;
    private String bookTitle;
    private LocalDate rentDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private BigDecimal fineAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
