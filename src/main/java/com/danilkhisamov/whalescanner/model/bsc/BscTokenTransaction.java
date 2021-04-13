package com.danilkhisamov.whalescanner.model.bsc;

import lombok.Builder;
import lombok.Data;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
@Builder
public class BscTokenTransaction {
    private String hash;
    private String age;
    private String from;
    private String to;
    private String direction;
    private String quantity;

    public static BscTokenTransaction fromTDList(List<WebElement> tds) {
        return BscTokenTransaction.builder()
                .hash(tds.get(0).getText())
                .age(tds.get(2).getText())
                .from(tds.get(3).getText())
                .to(tds.get(5).getText())
                .direction(tds.get(4).getText().trim())
                .quantity(tds.get(6).getText())
                .build();
    }
}
