package com.danilkhisamov.whalescanner.service;

import com.danilkhisamov.whalescanner.model.WhaleInfo;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import com.danilkhisamov.whalescanner.repository.WhaleRepository;
import com.danilkhisamov.whalescanner.scheduler.WhaleTransactionScanner;
import com.danilkhisamov.whalescanner.webclient.BscScanWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final static Pattern ADDRESS_PATTERN = Pattern.compile("\\dx\\w+");
    private final BscScanWebClient bscScanWebClient;
    private final DBContext dbContext;
    private final WhaleTransactionScanner transactionScanner;
    private final WhaleRepository whaleRepository;

    public SendMessage processRegisterCommand(MessageContext context) {
        String address = context.firstArg();
        Map<String, WhaleInfo> whales = whaleRepository.getWhalesMap();
        if (whales.get(address) == null) {
            List<BscTransaction> list = bscScanWebClient.getLastTransactions(address, 1);
            WhaleInfo whaleInfo = WhaleInfo.builder()
                    .address(address)
                    .name(context.secondArg())
                    .subscribedChats(new HashSet<>(Collections.singletonList(context.chatId())))
                    .lastTransaction(Optional.of(list).map(l -> l.get(0)).map(BscTransaction::getBlockNumber).orElse(null))
                    .build();
            whaleRepository.saveWhale(whaleInfo);
            transactionScanner.addWhaleToTrack(whaleInfo);
            log.info("Whale[{}] is registered to chat[{}]", address, context.chatId());
        } else {
            WhaleInfo existing = whaleRepository.getWhaleByAddress(address);
            existing.getSubscribedChats().add(context.chatId());
            whaleRepository.saveWhale(existing);
            log.info("Whale[{}] already exists. Adding subscription to chat[{}]", address, context.chatId());
        }
        return new SendMessage(context.chatId().toString(), "Registered successfully " + address);
    }

    public SendMessage processDeleteCommand(MessageContext context) {
        String address = context.firstArg();
        WhaleInfo whaleInfo = whaleRepository.getWhaleByAddress(address);
        transactionScanner.removeTrackedWhale(whaleInfo);
        whaleRepository.removeWhaleByAddress(address);
        return new SendMessage(context.chatId().toString(), "Deleted " + address);
    }

    public SendMessage processBalanceCommand(MessageContext  context) {
        String query = context.firstArg();
//        Matcher matcher = ADDRESS_PATTERN.matcher(query);
        String balance = bscScanWebClient.getBalance(query);
        log.info("Balance of " + query + " is:\n" + balance);
        return new SendMessage(context.chatId().toString(), "Balance of " + query + " is:\n" + balance);
    }

    public SendMessage processListCommand(MessageContext context) {
        Map<String, WhaleInfo> whales = whaleRepository.getWhalesMap();
        String message = whales
                .entrySet()
                .stream()
                .map(entry -> String.format("%s: %s",
                        Optional.of(entry).map(Map.Entry::getValue).map(WhaleInfo::getName).orElse("EMPTY_NAME"),
                        entry.getKey()))
                .collect(Collectors.joining("\n"));
        if (StringUtils.isBlank(message))
            message = "EMPTY";
        return new SendMessage(context.chatId().toString(), message);
    }

    public SendMessage processTransactionsCommand(MessageContext context) {
        List<BscTransaction> list = bscScanWebClient.getLastTransactions(context.firstArg(), Long.parseLong(context.secondArg()));
        String message = list.stream().map(BscTransaction::toMessageString).collect(Collectors.joining("\n\n"));
        return new SendMessage(context.chatId().toString(), message);
    }
}
