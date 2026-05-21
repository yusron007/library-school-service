package com.school.library.service;

import com.school.library.dto.BookResponse;
import com.school.library.dto.request.BookRequest;

import java.util.List;

public interface BookService {
    BookResponse create(BookRequest request);

    BookResponse update(Long id, BookRequest request);

    BookResponse upsert(BookRequest request);

    BookResponse get(Long id);

    List<BookResponse> getAll();

    void delete(Long id);
}
