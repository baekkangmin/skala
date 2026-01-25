// UserRepository.java
package com.example.demo.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.demo.model.User;

@Repository
public class UserRepository {

    // 메모리 DB 역할
    private final Map<Long, User> users = new ConcurrentHashMap<>();

    // 샘플 데이터 초기화
    public UserRepository() {
        users.put(1L, new User(1L, "홍길동"));
        users.put(2L, new User(2L, "백강민"));
        users.put(3L, new User(3L, "이명박"));
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    // 새 유저 추가 API를 위해 준비
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }
}

