package com.example.product.service;

import com.example.product.domain.Member;
import com.example.product.dto.MemberRequest;
import com.example.product.dto.MemberResponse;
import com.example.product.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberMapper mapper;

    public List<MemberResponse> getMembers() {
        return mapper.findAll().stream().map(this::toRes).toList();
    }

    public MemberResponse getMember(Long id) {
        Member m = mapper.findById(id).orElseThrow();
        return toRes(m);
    }

    @Transactional
    public MemberResponse createMember(MemberRequest r) {
        Member m = new Member();
        m.setName(r.getName());
        m.setEmail(r.getEmail());
        m.setPassword(r.getPassword()); // 과제용(실무면 해시 처리)
        mapper.insert(m);
        return getMember(m.getId());
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest r) {
        Member m = mapper.findById(id).orElseThrow();
        m.setName(r.getName());
        m.setEmail(r.getEmail());
        m.setPassword(r.getPassword());
        mapper.update(m);
        return getMember(id);
    }

    @Transactional
    public void deleteMember(Long id) {
        mapper.deleteById(id);
    }

    private MemberResponse toRes(Member m) {
        return MemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .email(m.getEmail())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
