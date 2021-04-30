package com.danilkhisamov.whalescanner.service.bscscan;

import com.danilkhisamov.whalescanner.model.bsc.BscToken;
import com.danilkhisamov.whalescanner.model.bsc.BscTokenTransaction;
import com.danilkhisamov.whalescanner.model.bsc.TransactionDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Slf4j
@Component
@RequiredArgsConstructor
public class BscScanWebParser {
    private static final String WHALE_TOKENS_TEMPLATE = "https://bscscan.com/tokenholdings?a=%s&ps=100&sort=total_price_usd&order=desc&p=1";
    private static final String WHALE_TOKEN_TRANSACTIONS_TEMPLATE = "https://bscscan.com/token/%s?a=%s";
    private final WebDriver driver;
    private WebDriverWait wait;

    @PostConstruct
    public void setUp() {
        wait = new WebDriverWait(driver, 10);
    }

    @PreDestroy
    public void destroy() {
        driver.quit();
    }

    public List<BscToken> getWhaleTokens(String address) {
        try {
            String url = String.format(WHALE_TOKENS_TEMPLATE, address);
            driver.get(url);
            wait.until(presenceOfElementLocated(By.cssSelector("#tb1>tr")));
            WebElement table = driver.findElement(By.id("tb1"));
            return table.findElements(By.tagName("tr")).stream()
                    .map(tr -> tr.findElements(By.tagName("td")))
                    .map(BscToken::fromTDList)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception during getWhaleTokens[{}] operation", address);
            throw e;
        }
    }

    public List<BscTokenTransaction> getWhaleTokenTransactions(String tokenAddress, String address, Integer count) {
        try {
            return getWhaleTokenTransactions(tokenAddress, address, count, null);
        } catch (Exception e) {
            log.error("Exception during getWhaleTokenTransactions[{}, {}, {}] operation", tokenAddress, address, count);
            throw e;
        }
    }

    public List<BscTokenTransaction> getWhaleTokenTransactions(String tokenAddress, String address, Integer count, TransactionDirection direction) {
        try {
            String url = String.format(WHALE_TOKEN_TRANSACTIONS_TEMPLATE, tokenAddress, address);
            driver.get(url);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("tokentxnsiframe"));
            WebElement tbody = wait.until(presenceOfElementLocated(By.tagName("tbody")));
            List<WebElement> trs = tbody.findElements(By.tagName("tr"));
            if (direction != null) {
                trs = trs.stream().filter(webElement -> webElement.getText().contains(direction.name())).collect(Collectors.toList());
            }
            if (count != null && count > 0 && trs.size() > count) {
                trs = trs.subList(0, count);
            }
            return trs.stream().map(tr -> BscTokenTransaction.fromTDList(tr.findElements(By.tagName("td")))).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception during getWhaleTokenTransactions[{}, {}, {}, {}] operation", tokenAddress, address, count, direction);
            throw e;
        }
    }

    public Map<TransactionDirection, List<BscTokenTransaction>> getWhaleTokenTransactionsDividedByDirection(String tokenAddress, String address) {
        try {
            List<BscTokenTransaction> transactions = getWhaleTokenTransactions(tokenAddress, address, null, null);
            Map<TransactionDirection, List<BscTokenTransaction>> map = new HashMap<>();
            map.put(TransactionDirection.IN, new ArrayList<>());
            map.put(TransactionDirection.OUT, new ArrayList<>());
            transactions.forEach(tr -> map.get(TransactionDirection.valueOf(tr.getDirection())).add(tr));
            return map;
        } catch (Exception e) {
            log.error("Exception during getWhaleTokenTransactionsDividedByDirection[{}, {}] operation", tokenAddress, address);
            throw e;
        }
    }
}
