package com.danilkhisamov.whalescanner.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromeDriverConfig {
    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    public ChromeDriver chromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        if ("prod".equals(profile)) {
            options.setBinary(System.getenv("GOOGLE_CHROME_SHIM"));
        } else if ("dev".equals(profile)) {
            WebDriverManager.chromedriver().setup();
        }
        return new ChromeDriver(options);
    }
}
