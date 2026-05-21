package com.school.library.service.impl;

import com.school.library.dto.MemberResponse;
import com.school.library.dto.request.MemberRequest;
import com.school.library.entity.Member;
import com.school.library.entity.RentStatus;
import com.school.library.exception.BadRequestException;
import com.school.library.exception.ResourceNotFoundException;
import com.school.library.repository.BookRentRepository;
import com.school.library.repository.MemberRepository;
import com.school.library.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BookRentRepository bookRentRepository;

    @Override
    @Transactional
    public MemberResponse create(MemberRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }
        if (memberRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        String memberCode = generateMemberCode();
        Member member = Member.builder()
                .memberCode(memberCode)
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .isDeleted(false)
                .build();

        Member savedMember = memberRepository.save(member);
        return mapToResponse(savedMember);
    }

    @Override
    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String trimmedEmail = request.getEmail().trim();
            if (!trimmedEmail.equalsIgnoreCase(member.getEmail())
                    && memberRepository.existsByEmailAndIsDeletedFalse(trimmedEmail)) {
                throw new BadRequestException("Email already registered by another member");
            }
            member.setEmail(trimmedEmail);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            member.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            member.setPhone(request.getPhone().trim());
        }

        if (request.getAddress() != null) {
            member.setAddress(request.getAddress().trim());
        }

        Member updatedMember = memberRepository.save(member);
        return mapToResponse(updatedMember);
    }

    @Override
    @Transactional
    public MemberResponse upsert(MemberRequest request) {
        if (request.getId() != null) {
            return update(request.getId(), request);
        } else {
            return create(request);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse get(Long id) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found"));
        return mapToResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getAll() {
        return memberRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found"));

        boolean hasActiveRentals = bookRentRepository.countByMemberIdAndStatus(id, RentStatus.RENTED) > 0;
        if (hasActiveRentals) {
            throw new BadRequestException("Cannot delete member. There are active rentals for this member.");
        }
        member.setIsDeleted(true);
        memberRepository.save(member);
    }

    private String generateMemberCode() {
        Optional<Member> lastMember = memberRepository.findFirstByOrderByIdDesc();
        long nextNum = 1;
        if (lastMember.isPresent()) {
            String code = lastMember.get().getMemberCode();
            try {
                String[] parts = code.split("-");
                nextNum = Long.parseLong(parts[1]) + 1;
            } catch (Exception e) {
                nextNum = lastMember.get().getId() + 1;
            }
        }
        return String.format("MEM-%03d", nextNum);
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
