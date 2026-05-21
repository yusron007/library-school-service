package com.school.library.controller;

import com.school.library.dto.RentResponse;
import com.school.library.dto.WebResponse;
import com.school.library.dto.request.RentRequest;
import com.school.library.dto.request.ReturnRequest;
import com.school.library.service.BookRentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rents")
@RequiredArgsConstructor
public class BookRentController {

        private final BookRentService bookRentService;

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.CREATED)
        public WebResponse<RentResponse> rentBook(@RequestBody RentRequest request) {
                RentResponse response = bookRentService.rentBook(request);
                return WebResponse.<RentResponse>builder()
                                .code(HttpStatus.CREATED.value())
                                .status("CREATED")
                                .data(response)
                                .build();
        }

        @PostMapping(value = "/{id}/return", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<RentResponse> returnBook(
                        @PathVariable Long id,
                        @RequestBody(required = false) ReturnRequest request) {
                RentResponse response = bookRentService.returnBook(id, request);
                return WebResponse.<RentResponse>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<RentResponse> get(@PathVariable Long id) {
                RentResponse response = bookRentService.get(id);
                return WebResponse.<RentResponse>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<RentResponse>> getAll() {
                List<RentResponse> response = bookRentService.getAll();
                return WebResponse.<List<RentResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @GetMapping(value = "/member/{memberId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<RentResponse>> getByMember(@PathVariable Long memberId) {
                List<RentResponse> response = bookRentService.getByMember(memberId);
                return WebResponse.<List<RentResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }
}
