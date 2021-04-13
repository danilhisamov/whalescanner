package com.danilkhisamov.whalescanner.service.impl;

import com.danilkhisamov.whalescanner.model.Whale;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import com.danilkhisamov.whalescanner.repository.WhaleRepository;
import com.danilkhisamov.whalescanner.scheduler.WhaleTransactionScanner;
import com.danilkhisamov.whalescanner.service.WhaleService;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebClient;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhaleServiceImpl implements WhaleService {
    private final WhaleRepository whaleRepository;
    private final BscScanWebClient bscScanWebClient;
    private final WhaleTransactionScanner transactionScanner;

    @Override
    public Whale save(Whale whale) {
        return null;
    }

    @Override
    public SendMessage save(MessageContext messageContext) {
        String address = messageContext.firstArg();
        String name = messageContext.secondArg();
        Long chatId = messageContext.chatId();
        Whale whale = whaleRepository.findByAddress(address);
        if (whale == null) {
            List<BscTransaction> list = bscScanWebClient.getLastTransactions(address, 1);
            whale = Whale.builder()
                    .address(address)
                    .name(name)
                    .subscribedChats(new HashSet<>(Collections.singletonList(chatId)))
                    .lastTransaction(Optional.of(list).map(l -> l.get(0)).map(BscTransaction::getHash).orElse(null))
                    .build();
            transactionScanner.addWhaleToTrack(whale);
            log.info("Whale[{}] is registered to chat[{}]", address, chatId);
        } else {
            whale.getSubscribedChats().add(chatId);
            log.info("Whale[{}] already exists. Adding subscription to chat[{}]", address, chatId);
        }
        whaleRepository.save(whale);

        return new SendMessage(chatId.toString(), "Registered successfully " + address);
    }

    @Override
    public SendMessage delete(MessageContext messageContext) {
        String address = messageContext.firstArg();
        Whale whale = whaleRepository.findByAddress(address);
        transactionScanner.removeTrackedWhale(whale);
        whaleRepository.delete(whale);
        return new SendMessage(messageContext.chatId().toString(), "Deleted " + address);
    }

    @Override
    public SendMessage list(MessageContext messageContext) {
        String message = whaleRepository.findAll().stream()
                .map(whale -> String.format("%s: %s",
                        whale.getName(),
                        whale.getAddress()))
                .collect(Collectors.joining("\n"));
        if (StringUtils.isBlank(message))
            message = "EMPTY";
        return new SendMessage(messageContext.chatId().toString(), message);
    }
}
