package com.school.library.service;

import com.school.library.dto.MemberResponse;
import com.school.library.dto.request.MemberRequest;

import java.util.List;

public interface MemberService {
    MemberResponse create(MemberRequest request);

    MemberResponse update(Long id, MemberRequest request);

    MemberResponse upsert(MemberRequest request);

    MemberResponse get(Long id);

    List<MemberResponse> getAll();

    void delete(Long id);
}
