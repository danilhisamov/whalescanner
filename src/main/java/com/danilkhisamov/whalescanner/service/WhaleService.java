package com.danilkhisamov.whalescanner.service;

import com.danilkhisamov.whalescanner.model.Whale;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface WhaleService {
    Whale save(Whale whale);
    SendMessage save(MessageContext messageContext);
    SendMessage delete(MessageContext messageContext);
    SendMessage list(MessageContext messageContext);
}
