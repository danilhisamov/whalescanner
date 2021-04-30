package com.danilkhisamov.whalescanner.config;

import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.Optional;

@Configuration
public class ChromeDriverConfig {
//    @PostConstruct
//    public void setUp() {
//        WebDriverManager.chromedriver().setup();
//    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ChromeDriver chromeDriver() {
        System.setProperty("webdriver.chrome.driver",
                System.getProperty("GOOGLE_CHROME_SHIM",
                        Optional.ofNullable(BscScanWebParser.class.getClassLoader().getResource("chromedriver_win32/chromedriver.exe"))
                                .map(URL::getFile)
                                .orElseThrow()
                )
        );
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        return new ChromeDriver(options);
    }
}
