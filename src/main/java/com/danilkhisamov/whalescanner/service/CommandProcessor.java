package com.danilkhisamov.whalescanner.service;

import com.danilkhisamov.whalescanner.model.bsc.BscToken;
import com.danilkhisamov.whalescanner.model.bsc.BscTokenTransaction;
import com.danilkhisamov.whalescanner.model.bsc.BscTransaction;
import com.danilkhisamov.whalescanner.model.coinmarketcup.Market;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebClient;
import com.danilkhisamov.whalescanner.service.bscscan.BscScanWebParser;
import com.danilkhisamov.whalescanner.service.coinmarketcup.CoinMarketCupWebParser;
import com.danilkhisamov.whalescanner.util.Constants;
import com.danilkhisamov.whalescanner.util.MessageSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final static Pattern ADDRESS_PATTERN = Pattern.compile("\\dx\\w+");
    private final BscScanWebClient bscScanWebClient;
    private final BscScanWebParser bscScanWebParser;
    private final CoinMarketCupWebParser coinMarketCupWebParser;
    private final WhaleService whaleService;

    public SendMessage processRegisterCommand(MessageContext context) {
        return whaleService.save(context);
    }

    public SendMessage processDeleteCommand(MessageContext context) {
        return whaleService.delete(context);
    }

    public SendMessage processBalanceCommand(MessageContext context) {
        String query = context.firstArg();
//        Matcher matcher = ADDRESS_PATTERN.matcher(query);
        String balance = bscScanWebClient.getBalance(query);
        log.info("Balance of " + query + " is:\n" + balance);
        return new SendMessage(context.chatId().toString(), "Balance of " + query + " is:\n" + balance);
    }

    public SendMessage processListCommand(MessageContext context) {
        return whaleService.list(context);
    }

    public SendMessage processTransactionsCommand(MessageContext context) {
        List<BscTransaction> list = bscScanWebClient.getLastTransactions(context.firstArg(), Long.parseLong(context.secondArg()));
        String message = list.stream().map(BscTransaction::toMessageString).collect(Collectors.joining("\n\n"));
        return new SendMessage(context.chatId().toString(), message);
    }

    public List<SendMessage> processTokensCommand(MessageContext context) {
        List<BscToken> tokens = bscScanWebParser.getWhaleTokens(context.firstArg());
        List<String> messages = MessageSeparator.separateMessages(tokens);
        return messages.stream().map(message -> new SendMessage(context.chatId().toString(), message)).collect(Collectors.toList());
    }

    public List<SendMessage> processTokenTransactionsCommand(MessageContext context) {
        List<BscTokenTransaction> transactions = bscScanWebParser.getWhaleTokenTransactions(context.secondArg(), context.firstArg(), Integer.parseInt(context.thirdArg()));
        List<String> messages = MessageSeparator.separateMessages(transactions);
        return messages.stream().map(message -> new SendMessage(context.chatId().toString(), message)).collect(Collectors.toList());
    }

    public List<SendMessage> processMarketsCommand(MessageContext context) {
        List<Market> markets = coinMarketCupWebParser.getTokenMarkets(context.firstArg());
        List<String> messages = MessageSeparator.separateMessages(markets);
        return messages.stream().map(message -> new SendMessage(context.chatId().toString(), message)).collect(Collectors.toList());
    }
}
