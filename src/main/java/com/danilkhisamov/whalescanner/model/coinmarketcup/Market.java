package com.danilkhisamov.whalescanner.model.coinmarketcup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Market {
    private String name;
    private String pair;
    private String price;
    private String volume;
    private String volumePercent;
    private String confidence;
    private String liquidity;
    private String updated;

    public static Market fromTdList(List<WebElement> tds) {
        return Market.builder()
                .name(tds.get(1).getText())
                .pair(tds.get(2).getText())
                .price(tds.get(3).getText())
                .volume(tds.get(6).getText())
                .volumePercent(tds.get(7).getText())
                .confidence(tds.get(8).getText())
                .liquidity(tds.get(9).getText())
                .updated(tds.get(10).getText())
                .build();
    }
}