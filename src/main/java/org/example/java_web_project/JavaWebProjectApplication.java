package org.example.java_web_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class JavaWebProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaWebProjectApplication.class, args);

//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//        System.out.println(encoder.encode("admin123"));
//        System.out.println(encoder.encode("staff123"));
//        System.out.println(encoder.encode("123456"));
    }
}