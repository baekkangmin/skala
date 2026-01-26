package com.example.product.controller;

import com.example.product.dto.MemberRequest;
import com.example.product.dto.MemberResponse;
import com.example.product.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService service;

    @GetMapping
    public List<MemberResponse> all() {
        return service.getMembers();
    }

    @GetMapping("/{id}")
    public MemberResponse one(@PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id) {
        return service.getMember(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@RequestBody @Valid MemberRequest r) {
        return service.createMember(r);
    }

    @PutMapping("/{id}")
    public MemberResponse update(
            @PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id,
            @RequestBody @Valid MemberRequest r
    ) {
        return service.updateMember(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id) {
        service.deleteMember(id);
    }
}