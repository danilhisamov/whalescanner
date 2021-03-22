package com.danilkhisamov.whalescanner.model.bsc;

import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class BscTransaction {
    private String blockNumber;
    private String timeStamp;
    private String from;
    private String to;
    private String value;

    public String toMessageString() {
        return Instant.ofEpochSecond(Long.parseLong(timeStamp))
                .atZone(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "\n" +
                "from: " + from +
                "\n" +
                "to: " + to +
                "\n" +
                "value: " + value;
    }
}
