package com.danilkhisamov.whalescanner.config;

import com.danilkhisamov.whalescanner.Bot;
import com.danilkhisamov.whalescanner.scheduler.WhaleTransactionScanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class CycleDependencyResolver {
    private final Bot bot;
    private final WhaleTransactionScanner whaleTransactionScanner;

    @PostConstruct
    private void postConstruct() {
        whaleTransactionScanner.setBot(bot);
    }
}
