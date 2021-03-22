package com.danilkhisamov.whalescanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WhalescannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhalescannerApplication.class, args);
    }

}
