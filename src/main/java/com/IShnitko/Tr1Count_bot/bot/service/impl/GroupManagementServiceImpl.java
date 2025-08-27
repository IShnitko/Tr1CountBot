package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GroupManagementServiceImpl implements GroupManagementService {
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final MessageService messageService;

    public GroupManagementServiceImpl(GroupService groupService, KeyboardFactory keyboardFactory, MessageService messageService) {

        this.groupService = groupService;
        this.keyboardFactory = keyboardFactory;
        this.messageService = messageService;
    }

    @Override
    public void groupHelpCommand(Long chatId, Integer messageId) {
        String text = """
                💡 *Group Help*
                
                Available commands:
                - /balance: Show current balances
                - /add_expense: Record new expense
                - /members: List group members
                - /help: Show this message
                - /back: Return to main menu
                """;
        messageService.editMessage(chatId, messageId, text, keyboardFactory.returnButton());
    }

    @Override
    public void displayGroup(Long chatId, String groupCode, Integer botMessageId, Integer inputMessageId) {
        if (inputMessageId != null) messageService.deleteMessage(chatId, inputMessageId);
//        messageService.deleteMessage(chatId, inputMessageId);
        if (botMessageId == null)
            messageService.sendMessage(chatId, "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        else {
            messageService.editMessage(chatId, botMessageId,
                    "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        }

    }

    @Override
    public void displayGroup(Long chatId, String groupCode, Integer botMessageId) {
        if (botMessageId == null)
            messageService.sendMessage(chatId, "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        else {
            messageService.editMessage(chatId, botMessageId,
                    "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                    keyboardFactory.groupMenu());
        }
    }

    @Override
    public void sendIncorrectGroupId(Long chatId, Integer messageId) { // TODO: this throws TelegramApiRequestException, idk how to fix it
        messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "Incorrect group Id. Try again:",
                keyboardFactory.returnButton());
    }

}
