package com.danilkhisamov.whalescanner.service.coinmarketcup;

import com.danilkhisamov.whalescanner.model.coinmarketcup.Market;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Component
public class CoinMarketCupWebParser {
    private static final String TOKEN_MARKETS_TEMPLATE = "https://coinmarketcap.com/currencies/%s/markets/";
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

    public List<Market> getTokenMarkets(String token) {
        String url = String.format(TOKEN_MARKETS_TEMPLATE, token);
        driver.get(url);
        wait.until(presenceOfElementLocated(By.cssSelector(".cmc-table>tbody>tr")));
        WebElement table = driver.findElement(By.cssSelector(".cmc-table>tbody"));
        Predicate<String> usdPredicate = Pattern.compile(".*(USDT)|(BUSD).*").asPredicate();
        return table.findElements(By.tagName("tr")).stream()
                .filter(tr -> usdPredicate.test(tr.getText()))
                .map(tr -> Market.fromTdList(tr.findElements(By.tagName("td"))))
                .collect(Collectors.toList());
    }
}