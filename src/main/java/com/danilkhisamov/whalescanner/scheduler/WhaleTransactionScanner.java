package com.danilkhisamov.whalescanner.scheduler;

import com.danilkhisamov.whalescanner.Bot;
import com.danilkhisamov.whalescanner.model.Whale;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import com.danilkhisamov.whalescanner.repository.WhaleRepository;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhaleTransactionScanner {
    private final BscScanWebClient bscScanWebClient;
    private final WhaleRepository whaleRepository;
    private final ConcurrentLinkedQueue<Whale> queue = new ConcurrentLinkedQueue<>();
    private Bot bot;

    @PostConstruct
    private void postConstruct() {
        log.info("Recovering the queue from db...");
        queue.addAll(whaleRepository.findAll());
    }

    public void removeTrackedWhale(Whale whale) {
        queue.remove(whale);
        log.info("Whale removed from tracked queue[{}]", whale.toSmallString());
    }

    public void addWhaleToTrack(Whale whale) {
        queue.add(whale);
        log.info("Whale added to tracked queue[{}]", whale.toSmallString());
    }

    @Scheduled(fixedDelayString = "${whalescanner.scheduler.scan.interval}")
    public void scanTransactions() {
        Whale whale = queue.poll();
        if (whale != null) {
            log.info("Processing {}", whale.toSmallString());
            List<BscTransaction> newTransactions = getNewTransactions(whale);
            if (!CollectionUtils.isEmpty(newTransactions)) {
                log.info("{} new transactions found for {}", newTransactions.size(), whale.toSmallString());
                String lastTransactionBlock = newTransactions.get(0).getHash();
                whale.setLastTransaction(lastTransactionBlock);
                whale = whaleRepository.save(whale);
                String message = newTransactions.stream().map(BscTransaction::toMessageString).collect(Collectors.joining("\n\n"));
                for (Long chat : whale.getSubscribedChats()) {
                    SendMessage sendMessage = new SendMessage(chat.toString(), message);
                    bot.sendMessage(sendMessage);
                }
            } else {
                log.info("No new transactions for whale[{}]", whale.toSmallString());
            }
            queue.add(whale);
        }
    }

    @SneakyThrows
    private List<BscTransaction> getNewTransactions(Whale whale) {
        final List<BscTransaction> newTransactions = new LinkedList<>();
        long page = 1;
        long count = 100;
        while (true) {
            List<BscTransaction> list = bscScanWebClient.getLastTransactions(whale.getAddress(), count, page);
            if (!CollectionUtils.isEmpty(list)) {
                for (BscTransaction transaction : list) {
                    if (transaction.getHash().equals(whale.getLastTransaction())) {
                        return newTransactions;
                    } else {
                        newTransactions.add(transaction);
                    }
                }
                page += 1;
            } else {
                log.info("Transactions of {} is empty on page={}, count={}", whale.getAddress(), page, count);
                return newTransactions;
            }
        }
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
