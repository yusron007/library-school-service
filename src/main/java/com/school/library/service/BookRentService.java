package com.school.library.service;

import com.school.library.dto.RentResponse;
import com.school.library.dto.request.RentRequest;
import com.school.library.dto.request.ReturnRequest;

import java.util.List;

public interface BookRentService {
    RentResponse rentBook(RentRequest request);

    RentResponse returnBook(Long id, ReturnRequest request);

    RentResponse get(Long id);

    List<RentResponse> getAll();

    List<RentResponse> getByMember(Long memberId);
}
