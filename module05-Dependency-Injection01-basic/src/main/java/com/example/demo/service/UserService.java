// UserService.java
package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repository.UserRepository;

@Service("userService")
public class UserService {
  
    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void printUser() {
        System.out.println("사용자: " + userRepository.findUser());
    }
}
