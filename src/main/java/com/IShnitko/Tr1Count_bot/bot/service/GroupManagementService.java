package com.IShnitko.Tr1Count_bot.bot.service;

public interface GroupManagementService {
    void groupHelpCommand(Long chatId);
    void displayGroup(Long chatId, String groupCode);
}
