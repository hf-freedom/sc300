package com.cleaning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CleaningSchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CleaningSchedulingApplication.class, args);
    }
}