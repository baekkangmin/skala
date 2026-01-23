// UserRepository.java
package com.example.demo.repository;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    public String findUser() {
        return "홍길동";
    }
}

