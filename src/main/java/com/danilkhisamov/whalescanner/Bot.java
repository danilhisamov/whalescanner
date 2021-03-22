package com.danilkhisamov.whalescanner;

import com.danilkhisamov.whalescanner.service.CommandProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

import static org.telegram.abilitybots.api.objects.Locality.*;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;

@Slf4j
@Component
public class Bot extends AbilityBot {
    private static final int CREATOR_ID = 123361450;
    private final CommandProcessor commandProcessor;

    public Bot(@Value("${whalescanner.telegram.bot.name}") String username,
               @Value("${whalescanner.telegram.bot.token}") String token,
               DBContext dbContext,
               CommandProcessor commandProcessor) {
        super(token, username, dbContext);
        this.commandProcessor = commandProcessor;
    }

    @Override
    public long creatorId() {
        return CREATOR_ID;
    }

    @PostConstruct
    public void postConstruct() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(this);
            log.info("Bot[{}] successfully registered", this.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Error during registering the bot", e);
        }
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Начать")
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> silent.send("Hello", cnt.chatId()))
                .build();
    }

    public Ability ping() {
        return Ability.builder()
                .name("ping")
                .info("Ping")
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> silent.send("pong", cnt.chatId()))
                .build();
    }

    public Ability register() {
        return Ability.builder()
                .name("register")
                .info("Register whale. Parameters[address, name]")
                .input(2)
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> sendMessage(commandProcessor.processRegisterCommand(cnt)))
                .build();
    }

    public Ability delete() {
        return Ability.builder()
                .name("delete")
                .info("Delete whale. Parameters[address]")
                .input(1)
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> sendMessage(commandProcessor.processDeleteCommand(cnt)))
                .build();
    }

    public Ability balance() {
        return Ability.builder()
                .name("balance")
                .info("Get whale balance. Parameters[address]")
                .input(1)
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> sendMessage(commandProcessor.processBalanceCommand(cnt)))
                .build();
    }

    public Ability transactions() {
        return Ability.builder()
                .name("transactions")
                .info("Get whale's last n transactions. Parameters[address, n]")
                .input(2)
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> sendMessage(commandProcessor.processTransactionsCommand(cnt)))
                .build();
    }

    public Ability list() {
        return Ability.builder()
                .name("list")
                .info("Get list of registered whales")
                .privacy(ADMIN)
                .locality(ALL)
                .action(cnt -> sendMessage(commandProcessor.processListCommand(cnt)))
                .build();
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("Error during send message %s", sendMessage), e);
        }
    }
}
