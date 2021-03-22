package com.danilkhisamov.whalescanner.scheduler;

import com.danilkhisamov.whalescanner.Bot;
import com.danilkhisamov.whalescanner.model.WhaleInfo;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import com.danilkhisamov.whalescanner.repository.WhaleRepository;
import com.danilkhisamov.whalescanner.webclient.BscScanWebClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhaleTransactionScanner {
    private final DBContext dbContext;
    private final BscScanWebClient bscScanWebClient;
    private final WhaleRepository whaleRepository;
    private final ConcurrentLinkedQueue<WhaleInfo> queue = new ConcurrentLinkedQueue<>();
    private Bot bot;

    @PostConstruct
    private void postConstruct() {
        log.info("Recovering the queue from db...");
        Map<String, WhaleInfo> whales = whaleRepository.getWhalesMap();
        whales.forEach((key, value) -> addWhaleToTrack(value));
    }

    public void addWhaleToTrack(WhaleInfo whaleInfo) {
        queue.add(whaleInfo);
        log.info("Whale added to tracked queue[{}]", whaleInfo);
    }

    public void removeTrackedWhale(WhaleInfo whaleInfo) {
        queue.remove(whaleInfo);
        log.info("Whale removed from tracked queue[{}]", whaleInfo);
    }

    @Scheduled(fixedDelayString = "${whalescanner.scheduler.scan.interval}")
    public void scanTransactions() {
        WhaleInfo whaleInfo = queue.poll();
        if (whaleInfo != null) {
            log.info("Processing {}", whaleInfo.getAddress());
            List<BscTransaction> newTransactions = getNewTransactions(whaleInfo);
            if (!CollectionUtils.isEmpty(newTransactions)) {
                log.info("{} new transactions found for {}", newTransactions.size(), whaleInfo.getAddress());
                String lastTransactionBlock = newTransactions.get(0).getBlockNumber();
                WhaleInfo dbInfo = whaleRepository.getWhaleByAddress(whaleInfo.getAddress());
                dbInfo.setLastTransaction(lastTransactionBlock);
                whaleInfo = whaleRepository.saveWhale(dbInfo);
                String message = newTransactions.stream().map(BscTransaction::toMessageString).collect(Collectors.joining("\n\n"));
                for (Long chat : whaleInfo.getSubscribedChats()) {
                    SendMessage sendMessage = new SendMessage(chat.toString(), message);
                    bot.sendMessage(sendMessage);
                }
            } else {
                log.info("No new transactions for whale[{}]", whaleInfo.getAddress());
            }
            queue.add(whaleInfo);
        }
    }

    @SneakyThrows
    private List<BscTransaction> getNewTransactions(WhaleInfo whaleInfo) {
        final List<BscTransaction> newTransactions = new LinkedList<>();
        while (true) {
            try {
                long page = 1;
                long count = 100;
                List<BscTransaction> list = bscScanWebClient.getLastTransactions(whaleInfo.getAddress(), count, page);
                if (!CollectionUtils.isEmpty(list)) {
                    for (BscTransaction transaction : list) {
                        if (transaction.getBlockNumber().equals(whaleInfo.getLastTransaction())) {
                            return newTransactions;
                        } else {
                            newTransactions.add(transaction);
                        }
                    }
                    page += 1;
                } else {
                    log.info("Transactions of {} is empty on page={}, count={}", whaleInfo.getAddress(), page, count);
                    return newTransactions;
                }
            } catch (Exception e) {
                log.error("Error during scan procedure");
            }
        }
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
