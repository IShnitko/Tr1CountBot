package com.IShnitko.Tr1Count_bot.bot.service;

public interface UserInteractionService {
    void startCommand(Long chatId);
    void helpCommand(Long chatId);
    void handleBackCommand(Long chatId);
    void unknownCommand(Long chatId);
}
