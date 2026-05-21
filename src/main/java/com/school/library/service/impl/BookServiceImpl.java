package com.school.library.service.impl;

import com.school.library.dto.BookResponse;
import com.school.library.dto.request.BookRequest;
import com.school.library.entity.Book;
import com.school.library.entity.RentStatus;
import com.school.library.exception.BadRequestException;
import com.school.library.exception.ResourceNotFoundException;
import com.school.library.repository.BookRentRepository;
import com.school.library.repository.BookRepository;
import com.school.library.service.BookService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookRentRepository bookRentRepository;

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Book title is required");
        }
        if (request.getAuthor() == null || request.getAuthor().isBlank()) {
            throw new BadRequestException("Book author is required");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new BadRequestException("Book stock must be a non-negative number");
        }

        String bookCode = generateBookCode();
        Book book = Book.builder()
                .bookCode(bookCode)
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .publisher(request.getPublisher() != null ? request.getPublisher().trim() : null)
                .publishYear(request.getPublishYear())
                .stock(request.getStock())
                .rentedQty(0)
                .isDeleted(false)
                .build();

        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            book.setTitle(request.getTitle().trim());
        }

        if (request.getAuthor() != null && !request.getAuthor().isBlank()) {
            book.setAuthor(request.getAuthor().trim());
        }

        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher().trim());
        }

        if (request.getPublishYear() != null) {
            book.setPublishYear(request.getPublishYear());
        }

        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new BadRequestException("Book stock must be a non-negative number");
            }
            if (request.getStock() < book.getRentedQty()) {
                throw new BadRequestException(
                        "Cannot decrease stock below current rented quantity (" + book.getRentedQty() + ")");
            }
            book.setStock(request.getStock());
        }

        Book updatedBook = bookRepository.save(book);
        return mapToResponse(updatedBook);
    }

    @Override
    @Transactional
    public BookResponse upsert(BookRequest request) {
        if (request.getId() != null) {
            return update(request.getId(), request);
        } else {
            return create(request);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse get(Long id) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " not found"));
        return mapToResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAll() {
        return bookRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " not found"));

        boolean activeRentsExist = bookRentRepository.existsByBookIdAndStatus(id, RentStatus.RENTED);
        if (activeRentsExist || book.getRentedQty() > 0) {
            throw new BadRequestException(
                    "Cannot delete book. There are active loan for this book.");
        }
        book.setIsDeleted(true);
        bookRepository.save(book);
    }

    private String generateBookCode() {
        Optional<Book> lastBook = bookRepository.findFirstByOrderByIdDesc();
        long nextNum = 1;
        if (lastBook.isPresent()) {
            String code = lastBook.get().getBookCode();
            try {
                String[] parts = code.split("-");
                nextNum = Long.parseLong(parts[1]) + 1;
            } catch (Exception e) {
                nextNum = lastBook.get().getId() + 1;
            }
        }
        return String.format("BOK-%03d", nextNum);
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .bookCode(book.getBookCode())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publishYear(book.getPublishYear())
                .stock(book.getStock())
                .rentedQty(book.getRentedQty())
                .availableQty(book.getStock() - book.getRentedQty())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
