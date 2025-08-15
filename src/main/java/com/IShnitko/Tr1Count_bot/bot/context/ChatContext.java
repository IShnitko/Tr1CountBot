package com.IShnitko.Tr1Count_bot.bot.context;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class ChatContext {
    public enum UpdateType { MESSAGE, CALLBACK }

    @Getter
    private final Long chatId;
    @Getter
    private final User user;
    @Nullable
    private final String text;
    @Nullable
    private final String callbackData;
    @Getter
    private final UpdateType updateType;
    @Getter
    private final Message message;
    @Getter
    private final AbsSender bot;
    @Nullable
    @Getter
    private final String callbackQueryId; // NEW FIELD

    public ChatContext(Update update, AbsSender bot) {
        this.bot = bot;

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            this.chatId = msg.getChatId();
            this.user = msg.getFrom();
            this.text = msg.getText();
            this.callbackData = null;
            this.updateType = UpdateType.MESSAGE;
            this.message = msg;
            this.callbackQueryId = null; // Initialize as null
        } else if (update.hasCallbackQuery()) {
            CallbackQuery cb = update.getCallbackQuery();
            this.chatId = cb.getMessage().getChatId();
            this.user = cb.getFrom();
            this.text = null;
            this.callbackData = cb.getData();
            this.updateType = UpdateType.CALLBACK;
            this.message = (Message) cb.getMessage();
            this.callbackQueryId = cb.getId(); // STORE CALLBACK QUERY ID
        } else {
            throw new IllegalArgumentException("Unsupported update type");
        }
    }

    @Nullable
    public String getText() { return text; }

    @Nullable
    public String getCallbackData() { return callbackData; }
}