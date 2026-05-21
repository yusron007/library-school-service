package com.school.library.service;

import com.school.library.dto.BookResponse;
import com.school.library.dto.request.BookRequest;
import com.school.library.entity.Book;
import com.school.library.entity.RentStatus;
import com.school.library.exception.BadRequestException;
import com.school.library.exception.ResourceNotFoundException;
import com.school.library.repository.BookRentRepository;
import com.school.library.repository.BookRepository;
import com.school.library.service.impl.BookServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

        @Mock
        private BookRepository bookRepository;

        @Mock
        private BookRentRepository bookRentRepository;

        @InjectMocks
        private BookServiceImpl bookService;

        private Book existingBook;

        @BeforeEach
        void setUp() {
                existingBook = Book.builder()
                                .id(1L)
                                .bookCode("BOK-001")
                                .title("Atomic Habits")
                                .author("James Clear")
                                .publisher("Avery")
                                .publishYear(2018)
                                .stock(5)
                                .rentedQty(2)
                                .isDeleted(false)
                                .build();
        }

        @Test
        void testCreate_Success() {
                BookRequest request = BookRequest.builder()
                                .title("The Psychology Of Money")
                                .author("Morgan Housel")
                                .publisher("Hapercollins")
                                .publishYear(2020)
                                .stock(3)
                                .build();

                when(bookRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
                when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
                        Book b = inv.getArgument(0);
                        b = Book.builder()
                                        .id(1L)
                                        .bookCode(b.getBookCode())
                                        .title(b.getTitle())
                                        .author(b.getAuthor())
                                        .publisher(b.getPublisher())
                                        .publishYear(b.getPublishYear())
                                        .stock(b.getStock())
                                        .rentedQty(0)
                                        .isDeleted(false)
                                        .build();
                        return b;
                });

                BookResponse response = bookService.create(request);

                assertNotNull(response);
                assertEquals("The Psychology Of Money", response.getTitle());
                assertEquals("Morgan Housel", response.getAuthor());
                assertEquals(3, response.getStock());
                assertEquals(0, response.getRentedQty());
                assertEquals(3, response.getAvailableQty());
                verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        void testCreate_MissingTitle_ThrowsBadRequest() {
                BookRequest request = BookRequest.builder()
                                .title("")
                                .author("Morgan Housel")
                                .stock(2)
                                .build();

                assertThrows(BadRequestException.class, () -> bookService.create(request));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testCreate_MissingAuthor_ThrowsBadRequest() {
                BookRequest request = BookRequest.builder()
                                .title("The Psychology Of Money")
                                .author(null)
                                .stock(2)
                                .build();

                assertThrows(BadRequestException.class, () -> bookService.create(request));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testCreate_NegativeStock_ThrowsBadRequest() {
                BookRequest request = BookRequest.builder()
                                .title("The Psychology Of Money")
                                .author("Morgan Housel")
                                .stock(-1)
                                .build();

                assertThrows(BadRequestException.class, () -> bookService.create(request));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testGet_Success() {
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingBook));

                BookResponse response = bookService.get(1L);

                assertNotNull(response);
                assertEquals("BOK-001", response.getBookCode());
                assertEquals("Atomic Habits", response.getTitle());
                assertEquals(5, response.getStock());
                assertEquals(2, response.getRentedQty());
                assertEquals(3, response.getAvailableQty());
        }

        @Test
        void testGet_NotFound_ThrowsResourceNotFound() {
                when(bookRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookService.get(99L));
        }

        @Test
        void testGetAll_ReturnsList() {
                when(bookRepository.findAllByIsDeletedFalse()).thenReturn(List.of(existingBook));

                List<BookResponse> responses = bookService.getAll();

                assertNotNull(responses);
                assertEquals(1, responses.size());
                assertEquals("Atomic Habits", responses.get(0).getTitle());
        }

        @Test
        void testUpdate_Success() {
                BookRequest request = BookRequest.builder()
                                .title("Atomic Habits (Updated)")
                                .author("James Clear")
                                .stock(10)
                                .build();

                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingBook));
                when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

                BookResponse response = bookService.update(1L, request);

                assertNotNull(response);
                assertEquals("Atomic Habits (Updated)", existingBook.getTitle());
                assertEquals(10, existingBook.getStock());
                verify(bookRepository, times(1)).save(existingBook);
        }

        @Test
        void testUpdate_StockBelowRentedQty_ThrowsBadRequest() {
                BookRequest request = BookRequest.builder()
                                .stock(1)
                                .build();

                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingBook));

                assertThrows(BadRequestException.class, () -> bookService.update(1L, request));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testUpdate_BookNotFound_ThrowsResourceNotFound() {
                BookRequest request = BookRequest.builder()
                                .title("The Psychology Of Money")
                                .build();

                when(bookRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookService.update(99L, request));
        }

        @Test
        void testDelete_Success() {
                Book bookToDelete = Book.builder()
                                .id(2L)
                                .bookCode("BOK-002")
                                .title("The Psychology Of Money")
                                .author("Morgan Housel")
                                .stock(3)
                                .rentedQty(0)
                                .isDeleted(false)
                                .build();

                when(bookRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(bookToDelete));
                when(bookRentRepository.existsByBookIdAndStatus(2L, RentStatus.RENTED)).thenReturn(false);
                when(bookRepository.save(any(Book.class))).thenReturn(bookToDelete);

                bookService.delete(2L);

                assertTrue(bookToDelete.getIsDeleted());
                verify(bookRepository, times(1)).save(bookToDelete);
        }

        @Test
        void testDelete_WithActiveRents_ThrowsBadRequest() {
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingBook));
                when(bookRentRepository.existsByBookIdAndStatus(1L, RentStatus.RENTED)).thenReturn(true);

                assertThrows(BadRequestException.class, () -> bookService.delete(1L));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testDelete_NotFound_ThrowsResourceNotFound() {
                when(bookRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookService.delete(99L));
                verify(bookRepository, never()).save(any());
        }

        @Test
        void testUpsert_WithoutId_CallsCreate() {
                BookRequest request = BookRequest.builder()
                                .title("The Psychology Of Money")
                                .author("Morgan Housel")
                                .stock(3)
                                .build();

                when(bookRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
                when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
                        Book b = inv.getArgument(0);
                        return Book.builder()
                                        .id(10L)
                                        .bookCode(b.getBookCode())
                                        .title(b.getTitle())
                                        .author(b.getAuthor())
                                        .stock(b.getStock())
                                        .rentedQty(0)
                                        .isDeleted(false)
                                        .build();
                });

                BookResponse response = bookService.upsert(request);

                assertNotNull(response);
                assertEquals("The Psychology Of Money", response.getTitle());
        }

        @Test
        void testUpsert_WithId_CallsUpdate() {
                BookRequest request = BookRequest.builder()
                                .id(1L)
                                .title("Atomic Habits (Updated)")
                                .author("James Clear")
                                .stock(10)
                                .build();

                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingBook));
                when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

                BookResponse response = bookService.upsert(request);

                assertNotNull(response);
                assertEquals("Atomic Habits (Updated)", existingBook.getTitle());
        }
}
