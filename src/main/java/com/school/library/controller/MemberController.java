package com.school.library.controller;

import com.school.library.dto.MemberResponse;
import com.school.library.dto.WebResponse;
import com.school.library.dto.request.MemberRequest;
import com.school.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

        private final MemberService memberService;

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<MemberResponse> upsert(@RequestBody MemberRequest request) {
                MemberResponse response = memberService.upsert(request);
                HttpStatus status = request.getId() == null ? HttpStatus.CREATED : HttpStatus.OK;
                return WebResponse.<MemberResponse>builder()
                                .code(status.value())
                                .status(status.name())
                                .data(response)
                                .build();
        }

        @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<MemberResponse> get(@PathVariable Long id) {
                MemberResponse response = memberService.get(id);
                return WebResponse.<MemberResponse>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<MemberResponse>> getAll() {
                List<MemberResponse> response = memberService.getAll();
                return WebResponse.<List<MemberResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data(response)
                                .build();
        }

        @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> delete(@PathVariable Long id) {
                memberService.delete(id);
                return WebResponse.<String>builder()
                                .code(HttpStatus.OK.value())
                                .status("OK")
                                .data("Member with ID " + id + " has been successfully deleted")
                                .build();
        }
}
