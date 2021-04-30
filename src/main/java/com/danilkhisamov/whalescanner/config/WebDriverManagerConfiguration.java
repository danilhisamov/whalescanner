package com.danilkhisamov.whalescanner.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Profile("dev")
public class WebDriverManagerConfiguration {
    @PostConstruct
    public void setUp() {
        WebDriverManager.chromedriver().setup();
    }
}
