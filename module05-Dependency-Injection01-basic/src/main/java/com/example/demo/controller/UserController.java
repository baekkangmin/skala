package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 예: /users/1, /users/2
    @GetMapping("/users/{id}")
    public String user(@PathVariable long id) {
        User u = userService.getUser(id);
        return "사용자: " + u.getName() + " (id=" + u.getId() + ")";
    }
}
