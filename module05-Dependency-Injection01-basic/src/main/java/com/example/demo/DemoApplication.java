// DemoApplication.java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import com.example.demo.service.UserService;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
       ApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
        UserService service = ctx.getBean("userService", UserService.class);
        service.printUser();
    }
}
