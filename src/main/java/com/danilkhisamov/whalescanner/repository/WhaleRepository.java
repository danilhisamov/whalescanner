package com.danilkhisamov.whalescanner.repository;

import com.danilkhisamov.whalescanner.model.WhaleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WhaleRepository {
    private static final String WHALES_DB_KEY = "whales";
    private final DBContext dbContext;

    public Map<String, WhaleInfo> getWhalesMap() {
        return dbContext.getMap(WHALES_DB_KEY);
    }

    public WhaleInfo getWhaleByAddress(String address) {
        return (WhaleInfo) dbContext.getMap(WHALES_DB_KEY).get(address);
    }

    public WhaleInfo saveWhale(WhaleInfo whaleInfo) {
        getWhalesMap().put(whaleInfo.getAddress(), whaleInfo);
        dbContext.commit();
        return getWhaleByAddress(whaleInfo.getAddress());
    }

    public void removeWhaleByAddress(String address) {
        dbContext.getMap(WHALES_DB_KEY).remove(address);
        dbContext.commit();
    }
}
