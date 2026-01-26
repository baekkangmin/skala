package com.example.product.mapper;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.product.domain.Member;
@Mapper
public interface MemberMapper{
 List<Member> findAll();
 Optional<Member> findById(Long id);
 void insert(Member p);
 void update(Member p);
 void deleteById(Long id);
}