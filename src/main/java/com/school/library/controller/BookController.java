package com.school.library.controller;

import com.school.library.dto.BookResponse;
import com.school.library.dto.WebResponse;
import com.school.library.dto.request.BookRequest;
import com.school.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

        private final BookService bookService;

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<BookResponse> upsert(@RequestBody BookRequest request) {
                BookResponse response = bookService.upsert(request);
                HttpStatus status = request.getId() == null ? HttpStatus.CREATED : HttpStatus.OK;
                return WebResponse.<BookResponse>builder()
                                .code(status.value())
                                .status(status.name())
                                .data(response)
                                .build();
        }

        @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<BookResponse> get(@PathVariable Long id) {
                BookResponse response = bookService.get(id);
                return WebResponse.<BookResponse>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<BookResponse>> getAll() {
                List<BookResponse> response = bookService.getAll();
                return WebResponse.<List<BookResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> delete(@PathVariable Long id) {
                bookService.delete(id);
                return WebResponse.<String>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data("Book with ID " + id + " has been successfully deleted")
                                .build();
        }
}
