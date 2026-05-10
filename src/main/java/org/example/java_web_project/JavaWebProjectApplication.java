package org.example.java_web_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaWebProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaWebProjectApplication.class, args);
    }
}