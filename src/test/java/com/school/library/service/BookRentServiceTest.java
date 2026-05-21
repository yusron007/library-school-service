package com.school.library.service;

import com.school.library.dto.RentResponse;
import com.school.library.dto.request.RentRequest;
import com.school.library.dto.request.ReturnRequest;
import com.school.library.entity.Book;
import com.school.library.entity.BookRent;
import com.school.library.entity.Member;
import com.school.library.entity.RentStatus;
import com.school.library.exception.BadRequestException;
import com.school.library.exception.ResourceNotFoundException;
import com.school.library.repository.BookRentRepository;
import com.school.library.repository.BookRepository;
import com.school.library.repository.MemberRepository;
import com.school.library.service.impl.BookRentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookRentServiceTest {

        @Mock
        private BookRentRepository bookRentRepository;
        @Mock
        private MemberRepository memberRepository;
        @Mock
        private BookRepository bookRepository;

        @InjectMocks
        private BookRentServiceImpl bookRentService;

        private Member activeMember;
        private Book inStockBook;
        private Book outOfStockBook;
        private BookRent existingRent;

        @BeforeEach
        void setUp() {
                activeMember = Member.builder()
                                .id(1L)
                                .memberCode("MEM-001")
                                .name("Yusron")
                                .email("yusron@gmail.com")
                                .build();

                inStockBook = Book.builder()
                                .id(1L)
                                .bookCode("BOK-001")
                                .title("Atomic Habits")
                                .author("James clear")
                                .stock(5)
                                .rentedQty(2)
                                .build();

                outOfStockBook = Book.builder()
                                .id(2L)
                                .bookCode("BOK-002")
                                .title("The Psychology of Money")
                                .author("Morgan Housel")
                                .stock(2)
                                .rentedQty(2)
                                .build();

                existingRent = BookRent.builder()
                                .id(10L)
                                .rentCode("TRA-010")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(LocalDate.now().minusDays(5))
                                .dueDate(LocalDate.now().plusDays(2))
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Test
        void testRentBook_Success_DefaultRentDate() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(1L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(inStockBook));
                when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(1L);
                when(bookRentRepository.existsByMemberIdAndBookIdAndStatus(1L, 1L, RentStatus.RENTED))
                                .thenReturn(false);
                when(bookRentRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());

                BookRent savedRent = BookRent.builder()
                                .id(1L)
                                .rentCode("TRA-001")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(LocalDate.now())
                                .dueDate(LocalDate.now().plusDays(7))
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .build();

                when(bookRentRepository.save(any(BookRent.class))).thenReturn(savedRent);

                RentResponse response = bookRentService.rentBook(request);

                assertNotNull(response);
                assertEquals("TRA-001", response.getRentCode());
                assertEquals("Atomic Habits", response.getBookTitle());
                assertEquals("Yusron", response.getMemberName());
                assertEquals(RentStatus.RENTED.name(), response.getStatus());
                assertEquals(LocalDate.now(), response.getRentDate());
                assertEquals(LocalDate.now().plusDays(7), response.getDueDate());

                assertEquals(3, inStockBook.getRentedQty());
                verify(bookRepository, times(1)).save(inStockBook);
                verify(bookRentRepository, times(1)).save(any(BookRent.class));
        }

        @Test
        void testRentBook_Success_WithCustomRentDate() {
                LocalDate customDate = LocalDate.now().minusDays(2);
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(1L)
                                .rentDate(customDate)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(inStockBook));
                when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(0L);
                when(bookRentRepository.existsByMemberIdAndBookIdAndStatus(1L, 1L, RentStatus.RENTED))
                                .thenReturn(false);
                when(bookRentRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());

                BookRent savedRent = BookRent.builder()
                                .id(1L)
                                .rentCode("TRA-001")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(customDate)
                                .dueDate(customDate.plusDays(7))
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .build();

                when(bookRentRepository.save(any(BookRent.class))).thenReturn(savedRent);

                RentResponse response = bookRentService.rentBook(request);

                assertNotNull(response);
                assertEquals("TRA-001", response.getRentCode());
                assertEquals(customDate, response.getRentDate());
                assertEquals(customDate.plusDays(7), response.getDueDate());
        }

        @Test
        void testRentBook_NullMemberId_ThrowsBadRequest() {
                RentRequest request = RentRequest.builder()
                                .bookId(1L)
                                .build();

                assertThrows(BadRequestException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testRentBook_NullBookId_ThrowsBadRequest() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .build();

                assertThrows(BadRequestException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testRentBook_MemberNotFound_ThrowsResourceNotFound() {
                RentRequest request = RentRequest.builder()
                                .memberId(99L)
                                .bookId(1L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testRentBook_BookNotFound_ThrowsResourceNotFound() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(99L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testRentBook_OutOfStock_ThrowsBadRequest() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(2L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(outOfStockBook));

                assertThrows(BadRequestException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any(BookRent.class));
        }

        @Test
        void testRentBook_MaxLimitReached_ThrowsBadRequest() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(1L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(inStockBook));
                when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(3L);

                assertThrows(BadRequestException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any(BookRent.class));
        }

        @Test
        void testRentBook_AlreadyRented_ThrowsBadRequest() {
                RentRequest request = RentRequest.builder()
                                .memberId(1L)
                                .bookId(1L)
                                .build();

                when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
                when(bookRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(inStockBook));
                when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(1L);
                when(bookRentRepository.existsByMemberIdAndBookIdAndStatus(1L, 1L, RentStatus.RENTED)).thenReturn(true);

                assertThrows(BadRequestException.class, () -> bookRentService.rentBook(request));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testReturnBook_Late_CalculatesFine() {
                LocalDate rentDate = LocalDate.now().minusDays(10);
                LocalDate dueDate = rentDate.plusDays(7);
                LocalDate returnDate = LocalDate.now();

                BookRent activeRent = BookRent.builder()
                                .id(1L)
                                .rentCode("TRA-001")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(rentDate)
                                .dueDate(dueDate)
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .build();

                when(bookRentRepository.findById(1L)).thenReturn(Optional.of(activeRent));
                when(bookRentRepository.save(any(BookRent.class))).thenAnswer(inv -> inv.getArgument(0));

                ReturnRequest returnRequest = ReturnRequest.builder()
                                .returnDate(returnDate)
                                .build();

                RentResponse response = bookRentService.returnBook(1L, returnRequest);

                assertNotNull(response);
                assertEquals(RentStatus.RETURNED.name(), response.getStatus());
                assertEquals(0, BigDecimal.valueOf(15000).compareTo(response.getFineAmount()));
                assertEquals(1, inStockBook.getRentedQty());

                verify(bookRepository, times(1)).save(inStockBook);
                verify(bookRentRepository, times(1)).save(activeRent);
        }

        @Test
        void testReturnBook_OnTime_NoFine() {
                LocalDate rentDate = LocalDate.now().minusDays(3);
                LocalDate dueDate = rentDate.plusDays(7);
                LocalDate returnDate = LocalDate.now();

                BookRent activeRent = BookRent.builder()
                                .id(1L)
                                .rentCode("TRA-001")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(rentDate)
                                .dueDate(dueDate)
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .build();

                when(bookRentRepository.findById(1L)).thenReturn(Optional.of(activeRent));
                when(bookRentRepository.save(any(BookRent.class))).thenAnswer(inv -> inv.getArgument(0));

                ReturnRequest returnRequest = ReturnRequest.builder()
                                .returnDate(returnDate)
                                .build();

                RentResponse response = bookRentService.returnBook(1L, returnRequest);

                assertNotNull(response);
                assertEquals(RentStatus.RETURNED.name(), response.getStatus());
                assertEquals(0, BigDecimal.ZERO.compareTo(response.getFineAmount()));
        }

        @Test
        void testReturnBook_NullRequest_UsesNow() {
                LocalDate rentDate = LocalDate.now().minusDays(3);
                LocalDate dueDate = rentDate.plusDays(7);

                BookRent activeRent = BookRent.builder()
                                .id(1L)
                                .rentCode("TRA-001")
                                .member(activeMember)
                                .book(inStockBook)
                                .rentDate(rentDate)
                                .dueDate(dueDate)
                                .status(RentStatus.RENTED)
                                .fineAmount(BigDecimal.ZERO)
                                .build();

                when(bookRentRepository.findById(1L)).thenReturn(Optional.of(activeRent));
                when(bookRentRepository.save(any(BookRent.class))).thenAnswer(inv -> inv.getArgument(0));

                RentResponse response = bookRentService.returnBook(1L, null);

                assertNotNull(response);
                assertEquals(RentStatus.RETURNED.name(), response.getStatus());
                assertEquals(LocalDate.now(), response.getReturnDate());
        }

        @Test
        void testReturnBook_NotFound_ThrowsResourceNotFound() {
                when(bookRentRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookRentService.returnBook(99L, null));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testReturnBook_AlreadyReturned_ThrowsBadRequest() {
                BookRent returnedRent = BookRent.builder()
                                .id(1L)
                                .status(RentStatus.RETURNED)
                                .build();

                when(bookRentRepository.findById(1L)).thenReturn(Optional.of(returnedRent));

                assertThrows(BadRequestException.class, () -> bookRentService.returnBook(1L, null));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testReturnBook_ReturnDateBeforeRentDate_ThrowsBadRequest() {
                LocalDate rentDate = LocalDate.now();
                BookRent activeRent = BookRent.builder()
                                .id(1L)
                                .rentDate(rentDate)
                                .status(RentStatus.RENTED)
                                .build();

                when(bookRentRepository.findById(1L)).thenReturn(Optional.of(activeRent));

                ReturnRequest returnRequest = ReturnRequest.builder()
                                .returnDate(rentDate.minusDays(1))
                                .build();

                assertThrows(BadRequestException.class, () -> bookRentService.returnBook(1L, returnRequest));
                verify(bookRentRepository, never()).save(any());
        }

        @Test
        void testGet_Success() {
                when(bookRentRepository.findById(10L)).thenReturn(Optional.of(existingRent));

                RentResponse response = bookRentService.get(10L);

                assertNotNull(response);
                assertEquals("TRA-010", response.getRentCode());
                assertEquals("Yusron", response.getMemberName());
        }

        @Test
        void testGet_NotFound_ThrowsResourceNotFound() {
                when(bookRentRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> bookRentService.get(99L));
        }

        @Test
        void testGetAll_ReturnsList() {
                when(bookRentRepository.findAll()).thenReturn(List.of(existingRent));

                List<RentResponse> responses = bookRentService.getAll();

                assertNotNull(responses);
                assertEquals(1, responses.size());
                assertEquals("TRA-010", responses.get(0).getRentCode());
        }

        @Test
        void testGetAll_Empty_ReturnsEmptyList() {
                when(bookRentRepository.findAll()).thenReturn(Collections.emptyList());

                List<RentResponse> responses = bookRentService.getAll();

                assertNotNull(responses);
                assertTrue(responses.isEmpty());
        }

        @Test
        void testGetByMember_Success() {
                when(memberRepository.existsById(1L)).thenReturn(true);
                when(bookRentRepository.findByMemberId(1L)).thenReturn(List.of(existingRent));

                List<RentResponse> responses = bookRentService.getByMember(1L);

                assertNotNull(responses);
                assertEquals(1, responses.size());
                assertEquals("TRA-010", responses.get(0).getRentCode());
        }

        @Test
        void testGetByMember_NotFound_ThrowsResourceNotFound() {
                when(memberRepository.existsById(99L)).thenReturn(false);

                assertThrows(ResourceNotFoundException.class, () -> bookRentService.getByMember(99L));
        }
}
