package com.danilkhisamov.whalescanner.service.bscscan;

import com.danilkhisamov.whalescanner.model.bsc.BscScanResponse;
import com.danilkhisamov.whalescanner.model.bsc.BscScanResponseString;
import com.danilkhisamov.whalescanner.model.bsc.BscScanResponseTransactions;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BscScanWebClient {
    private static final String BASE_URL = "https://api.bscscan.com/api";
    private final WebClient webClient;
    @Value("${whalescanner.bsc.api.key}")
    private String apiKey;

    public BscScanWebClient() {
        this.webClient = WebClient.create(BASE_URL);
    }

    public String getBalance(String address) {
        BscScanResponse<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("module", "account")
                        .queryParam("action", "balance")
                        .queryParam("address", address)
                        .queryParam("tag", "latest")
                        .build())
                .retrieve()
                .bodyToMono(BscScanResponseString.class)
                .block();

        return Optional.ofNullable(response).map(BscScanResponse::getResult).orElse(null);
    }

    public List<BscTransaction> getLastTransactions(String address) {
        return getLastTransactions(address, 10);
    }

    public List<BscTransaction> getLastTransactions(String address, long count) {
        return getLastTransactions(address, count, 1);
    }

    public List<BscTransaction> getLastTransactions(String address, long count, long page) {
        BscScanResponseTransactions response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("module", "account")
                        .queryParam("action", "txlist")
                        .queryParam("address", address)
                        .queryParam("page", page)
                        .queryParam("offset", count)
                        .queryParam("sort", "desc")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(BscScanResponseTransactions.class)
                .block();
        if (response == null)
            return new ArrayList<>();
        return response.getResult();
    }
}
