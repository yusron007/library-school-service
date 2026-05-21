package com.school.library.service;

import com.school.library.dto.MemberResponse;
import com.school.library.dto.request.MemberRequest;
import com.school.library.entity.Member;
import com.school.library.entity.RentStatus;
import com.school.library.exception.BadRequestException;
import com.school.library.exception.ResourceNotFoundException;
import com.school.library.repository.BookRentRepository;
import com.school.library.repository.MemberRepository;
import com.school.library.service.impl.MemberServiceImpl;

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
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRentRepository bookRentRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member existingMember;
    private MemberRequest validRequest;

    @BeforeEach
    void setUp() {
        existingMember = Member.builder()
                .id(1L)
                .memberCode("MEM-001")
                .name("Yusron")
                .email("yusron@example.com")
                .phone("08123456789")
                .address("Bandung, Indonesia")
                .isDeleted(false)
                .build();

        validRequest = MemberRequest.builder()
                .name("Yusron")
                .email("yusron@example.com")
                .phone("08123456789")
                .address("Bandung, Indonesia")
                .build();
    }

    @Test
    void testCreate_Success() {
        when(memberRepository.existsByEmailAndIsDeletedFalse(validRequest.getEmail())).thenReturn(false);
        when(memberRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        MemberResponse result = memberService.create(validRequest);

        assertNotNull(result);
        assertEquals("MEM-001", result.getMemberCode());
        assertEquals("Yusron", result.getName());
        assertEquals("yusron@example.com", result.getEmail());

        verify(memberRepository, times(1)).existsByEmailAndIsDeletedFalse(validRequest.getEmail());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testCreate_EmailAlreadyExists_ThrowsBadRequest() {
        when(memberRepository.existsByEmailAndIsDeletedFalse(validRequest.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> memberService.create(validRequest));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testCreate_MissingEmail_ThrowsBadRequest() {
        MemberRequest request = MemberRequest.builder()
                .name("ucon")
                .email("")
                .build();

        assertThrows(BadRequestException.class, () -> memberService.create(request));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testCreate_MissingName_ThrowsBadRequest() {
        MemberRequest request = MemberRequest.builder()
                .name(null)
                .email("ucon@gmail.com")
                .build();

        assertThrows(BadRequestException.class, () -> memberService.create(request));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testGet_Success() {
        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));

        MemberResponse response = memberService.get(1L);

        assertNotNull(response);
        assertEquals("MEM-001", response.getMemberCode());
        assertEquals("Yusron", response.getName());
        assertEquals("yusron@example.com", response.getEmail());
    }

    @Test
    void testGet_NotFound_ThrowsResourceNotFound() {
        when(memberRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.get(99L));
    }

    @Test
    void testGetAll_ReturnsList() {
        when(memberRepository.findAllByIsDeletedFalse()).thenReturn(List.of(existingMember));

        List<MemberResponse> responses = memberService.getAll();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Yusron", responses.get(0).getName());
    }

    @Test
    void testUpdate_Success() {
        MemberRequest request = MemberRequest.builder()
                .name("Yusron Edited")
                .email("yusron2000@gmail.com")
                .phone("08999999999")
                .build();

        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        MemberResponse response = memberService.update(1L, request);

        assertNotNull(response);
        assertEquals("Yusron Edited", existingMember.getName());
        assertEquals("08999999999", existingMember.getPhone());
        verify(memberRepository, times(1)).save(existingMember);
    }

    @Test
    void testUpdate_DuplicateEmail_ThrowsBadRequest() {
        MemberRequest request = MemberRequest.builder()
                .email("asep2000@gmail.com")
                .name("Yusron")
                .build();

        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));
        when(memberRepository.existsByEmailAndIsDeletedFalse("asep2000@gmail.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> memberService.update(1L, request));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testUpdate_MemberNotFound_ThrowsResourceNotFound() {
        MemberRequest request = MemberRequest.builder()
                .name("Ucon2000Edited")
                .build();

        when(memberRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.update(99L, request));
    }

    @Test
    void testDelete_Success() {
        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));
        when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(0L);
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        memberService.delete(1L);

        assertTrue(existingMember.getIsDeleted());
        verify(memberRepository, times(1)).save(existingMember);
    }

    @Test
    void testDelete_WithActiveRentals_ThrowsBadRequest() {
        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));
        when(bookRentRepository.countByMemberIdAndStatus(1L, RentStatus.RENTED)).thenReturn(2L);

        assertThrows(BadRequestException.class, () -> memberService.delete(1L));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testDelete_NotFound_ThrowsResourceNotFound() {
        when(memberRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.delete(99L));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testUpsert_WithoutId_CallsCreate() {
        when(memberRepository.existsByEmailAndIsDeletedFalse(validRequest.getEmail())).thenReturn(false);
        when(memberRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        MemberResponse response = memberService.upsert(validRequest);

        assertNotNull(response);
        assertEquals("Yusron", response.getName());
    }

    @Test
    void testUpsert_WithId_CallsUpdate() {
        MemberRequest request = MemberRequest.builder()
                .id(1L)
                .name("Yusron Updated")
                .email("yusron2000@gmail.com")
                .build();

        when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        MemberResponse response = memberService.upsert(request);

        assertNotNull(response);
        assertEquals("Yusron Updated", existingMember.getName());
    }
}
