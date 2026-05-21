package com.school.library.service.impl;

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
import com.school.library.service.BookRentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookRentServiceImpl implements BookRentService {

    private final BookRentRepository bookRentRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public RentResponse rentBook(RentRequest request) {
        if (request.getMemberId() == null) {
            throw new BadRequestException("Member ID is required");
        }
        if (request.getBookId() == null) {
            throw new BadRequestException("Book ID is required");
        }

        Member member = memberRepository.findByIdAndIsDeletedFalse(request.getMemberId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Member with ID " + request.getMemberId() + " not found"));

        Book book = bookRepository.findByIdAndIsDeletedFalse(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + request.getBookId() + " not found"));

        if (book.getRentedQty() >= book.getStock()) {
            throw new BadRequestException("Book '" + book.getTitle() + "' is currently out of stock");
        }
        long activeRentsCount = bookRentRepository.countByMemberIdAndStatus(member.getId(), RentStatus.RENTED);
        if (activeRentsCount >= 3) {
            throw new BadRequestException(
                    "Member '" + member.getName() + "' has reached the maximum rent limit of 3 active books");
        }
        boolean alreadyRented = bookRentRepository.existsByMemberIdAndBookIdAndStatus(member.getId(), book.getId(),
                RentStatus.RENTED);
        if (alreadyRented) {
            throw new BadRequestException(
                    "Member has already rented '" + book.getTitle() + "' and has not returned it yet");
        }

        LocalDate rentDate = request.getRentDate() != null ? request.getRentDate() : LocalDate.now();
        LocalDate dueDate = rentDate.plusDays(7);

        String rentCode = generateRentCode();
        BookRent bookRent = BookRent.builder()
                .rentCode(rentCode)
                .member(member)
                .book(book)
                .rentDate(rentDate)
                .dueDate(dueDate)
                .status(RentStatus.RENTED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        book.setRentedQty(book.getRentedQty() + 1);
        bookRepository.save(book);

        BookRent savedRent = bookRentRepository.save(bookRent);
        return mapToResponse(savedRent);
    }

    @Override
    @Transactional
    public RentResponse returnBook(Long id, ReturnRequest request) {
        BookRent bookRent = bookRentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rent transaction with ID " + id + " not found"));

        if (bookRent.getStatus() == RentStatus.RETURNED) {
            throw new BadRequestException("This rent transaction has already been completed and returned");
        }

        LocalDate returnDate = (request != null && request.getReturnDate() != null) ? request.getReturnDate()
                : LocalDate.now();

        if (returnDate.isBefore(bookRent.getRentDate())) {
            throw new BadRequestException(
                    "Return date cannot be earlier than rent date (" + bookRent.getRentDate() + ")");
        }

        BigDecimal fine = BigDecimal.ZERO;
        if (returnDate.isAfter(bookRent.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(bookRent.getDueDate(), returnDate);
            fine = BigDecimal.valueOf(daysLate * 5000);
        }

        bookRent.setReturnDate(returnDate);
        bookRent.setStatus(RentStatus.RETURNED);
        bookRent.setFineAmount(fine);

        Book book = bookRent.getBook();
        book.setRentedQty(Math.max(0, book.getRentedQty() - 1));
        bookRepository.save(book);

        BookRent updatedRent = bookRentRepository.save(bookRent);
        return mapToResponse(updatedRent);
    }

    @Override
    @Transactional(readOnly = true)
    public RentResponse get(Long id) {
        BookRent bookRent = bookRentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rent transaction with ID " + id + " not found"));
        return mapToResponse(bookRent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentResponse> getAll() {
        return bookRentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentResponse> getByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member with ID " + memberId + " not found");
        }
        return bookRentRepository.findByMemberId(memberId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateRentCode() {
        Optional<BookRent> lastRent = bookRentRepository.findFirstByOrderByIdDesc();
        long nextNum = 1;
        if (lastRent.isPresent()) {
            String code = lastRent.get().getRentCode();
            try {
                String[] parts = code.split("-");
                nextNum = Long.parseLong(parts[1]) + 1;
            } catch (Exception e) {
                nextNum = lastRent.get().getId() + 1;
            }
        }
        return String.format("TRA-%03d", nextNum);
    }

    private RentResponse mapToResponse(BookRent rent) {
        return RentResponse.builder()
                .id(rent.getId())
                .rentCode(rent.getRentCode())
                .memberId(rent.getMember().getId())
                .memberCode(rent.getMember().getMemberCode())
                .memberName(rent.getMember().getName())
                .bookId(rent.getBook().getId())
                .bookCode(rent.getBook().getBookCode())
                .bookTitle(rent.getBook().getTitle())
                .rentDate(rent.getRentDate())
                .dueDate(rent.getDueDate())
                .returnDate(rent.getReturnDate())
                .status(rent.getStatus().name())
                .fineAmount(rent.getFineAmount())
                .createdAt(rent.getCreatedAt())
                .updatedAt(rent.getUpdatedAt())
                .build();
    }
}
