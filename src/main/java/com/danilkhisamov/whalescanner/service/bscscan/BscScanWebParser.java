package com.danilkhisamov.whalescanner.service.bscscan;

import com.danilkhisamov.whalescanner.model.bsc.BscToken;
import com.danilkhisamov.whalescanner.model.bsc.BscTokenTransaction;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Slf4j
@Component
public class BscScanWebParser {
    private static final String WHALE_TOKENS_TEMPLATE = "https://bscscan.com/tokenholdings?a=%s&ps=100&sort=total_price_usd&order=desc&p=1";
    private static final String WHALE_TOKEN_TRANSACTIONS_TEMPLATE = "https://bscscan.com/token/%s?a=%s";
    private WebDriver driver;
    private WebDriverWait wait;

    @PostConstruct
    public void setUp() {
        String driverFile = Optional.ofNullable(BscScanWebParser.class.getClassLoader().getResource("chromedriver_win32/chromedriver.exe"))
                .map(URL::getFile)
                .orElseThrow();

        System.setProperty("webdriver.chrome.driver", driverFile);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
    }

    @PreDestroy
    public void destroy() {
        driver.quit();
    }

    public List<BscToken> getWhaleTokens(String address) {
        String url = String.format(WHALE_TOKENS_TEMPLATE, address);
        driver.get(url);
        wait.until(presenceOfElementLocated(By.cssSelector("#tb1>tr")));
        WebElement table = driver.findElement(By.id("tb1"));
        return table.findElements(By.tagName("tr")).stream()
                .map(tr -> tr.findElements(By.tagName("td")))
                .map(BscToken::fromTDList)
                .collect(Collectors.toList());
    }

    public List<BscTokenTransaction> getWhaleTokenTransactions(String tokenAddress, String address, int count) {
        String url = String.format(WHALE_TOKEN_TRANSACTIONS_TEMPLATE, tokenAddress, address);
        driver.get(url);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("tokentxnsiframe"));
        WebElement tbody = wait.until(presenceOfElementLocated(By.tagName("tbody")));
        List<WebElement> trs = tbody.findElements(By.tagName("tr"));
        return trs.subList(0, count).stream().map(tr -> BscTokenTransaction.fromTDList(tr.findElements(By.tagName("td")))).collect(Collectors.toList());
    }
}
