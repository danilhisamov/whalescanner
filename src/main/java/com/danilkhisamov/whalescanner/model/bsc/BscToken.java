package com.danilkhisamov.whalescanner.model.bsc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BscToken {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?<tokenName>.+)\n((?<tokenAddress>0x.*)\n)?(?<tokenSymbol>.+)([ \\s])(?<quantity>[\\d,.]+).{1}(?<tokenPrice>\\$.+\\(.+BNB\\))?(.{1})(?<tokenChange>[\\d.]+%)?(.{1})(?<tokenValueBNB>[\\d,.]+)?(.{1})(?<tokenValueUSD>\\$[\\d,.]+)?");
    private String name;
    private String address;
    private String symbol;
    private String quantity;
    private String price;
    private String change;
    private String valueBNB;
    private String valueUSD;

    public static BscToken fromString(String s) {
        Matcher matcher = TOKEN_PATTERN.matcher(s);
        if (matcher.find()) {
            return BscToken.builder()
                    .name(matcher.group("tokenName"))
                    .address(matcher.group("tokenAddress"))
                    .symbol(matcher.group("tokenSymbol"))
                    .quantity(matcher.group("quantity"))
                    .price(matcher.group("tokenPrice"))
                    .change(matcher.group("tokenChange"))
                    .valueBNB(matcher.group("tokenValueBNB"))
                    .valueUSD(matcher.group("tokenValueUSD"))
                    .build();
        } else {
            throw new IllegalArgumentException(String.format("String \"%s\" doesnt match pattern %s", StringEscapeUtils.escapeJava(s), StringEscapeUtils.escapeJava(TOKEN_PATTERN.toString())));
        }
    }

    public static BscToken fromTDList(List<WebElement> tds) {
        String[] nameAddress = tds.get(1).getText().split("\n");
        String name = nameAddress[0];
        String address = null;
        if (nameAddress.length == 2) {
            address = nameAddress[1];
        }
        return BscToken.builder()
                .name(name)
                .address(address)
                .symbol(tds.get(2).getText())
                .quantity(tds.get(3).getText())
                .price(tds.get(4).getText())
                .change(tds.get(5).getText())
                .valueBNB(tds.get(6).getText())
                .valueUSD(tds.get(7).getText())
                .build();
    }
}
