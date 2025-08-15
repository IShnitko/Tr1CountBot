package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// MessageService.java

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger LOG = LoggerFactory.getLogger(MessageServiceImpl.class);
    // This is the ONE place where the bot instance is directly injected
    private final Tr1CountBot bot;

    // Use a @Lazy annotation to prevent a circular dependency during startup
    // It's a quick fix that allows the architecture to work without major refactoring
    public MessageServiceImpl(@Lazy Tr1CountBot bot) {
        this.bot = bot;
    }

    // The methods now use the bot instance to execute API calls
    public void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            bot.execute(message); // Use the injected bot instance
        } catch (TelegramApiException e) {
            // Log the error
        }
    }

    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(keyboard)
                .build();
        try {
            bot.execute(message); // Use the injected bot instance
        } catch (TelegramApiException e) {
            // Log the error
        }
    }

    public void answerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        try {
            bot.execute(answer); // Use the injected bot instance
        } catch (TelegramApiException e) {
            // Log the error
        }
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {
        try {
            bot.execute(DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .build());
        } catch (TelegramApiException e) {
            LOG.error("Error deleting message", e);
        }
    }
}