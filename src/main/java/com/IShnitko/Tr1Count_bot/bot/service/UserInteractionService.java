package com.IShnitko.Tr1Count_bot.bot.service;

public interface UserInteractionService {
    void startCommand(Long chatId, Integer messageId);
    void helpCommand(Long chatId);
    void unknownCommand(Long chatId);
}
