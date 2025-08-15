package com.IShnitko.Tr1Count_bot.bot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface MessageService {
    void sendMessage(Long chatId, String text);
    void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard);
    void answerCallbackQuery(String callbackQueryId);
    void deleteMessage(Long chatId, Integer messageId);
}
