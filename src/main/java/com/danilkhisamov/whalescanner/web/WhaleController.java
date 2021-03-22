package com.danilkhisamov.whalescanner.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhaleController {
    @GetMapping("/")
    public String home() {
        return "Hello World!";
    }
}
