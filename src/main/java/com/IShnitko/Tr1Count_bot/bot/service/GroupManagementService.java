package com.IShnitko.Tr1Count_bot.bot.service;

import lombok.NonNull;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public interface GroupManagementService {
    void groupHelpCommand(Long chatId, Integer messageId);

    void displayGroup(Long chatId, String groupCode, Integer botMessageId);

    void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId);

    void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId, String additionalText);

    void sendIncorrectGroupId(Long chatId, Integer messageId, @NonNull Long id) throws TelegramApiRequestException;

    void sendJoinLink(Long chatId, Integer messageId);

    void viewUserInfo(Long chatId, Integer messageId, Long userId);

    void viewMembersMenu(Long chatId, Integer messageId, Long userId);
}
