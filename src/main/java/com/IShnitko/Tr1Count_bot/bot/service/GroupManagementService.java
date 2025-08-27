package com.IShnitko.Tr1Count_bot.bot.service;

public interface GroupManagementService {
    void groupHelpCommand(Long chatId, Integer messageId);
    void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId);
    void displayGroup(Long chatId, String groupCode, Integer botMessageId);

    void sendIncorrectGroupId(Long chatId, Integer messageId) throws TelegramApiRequestException;
}
