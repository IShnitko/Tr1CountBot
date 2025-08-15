package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import org.springframework.stereotype.Service;

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
    public void groupHelpCommand(Long chatId) {
        String text = """
        💡 *Group Help*
        
        Available commands:
        - /balance: Show current balances
        - /add_expense: Record new expense
        - /members: List group members
        - /help: Show this message
        - /back: Return to main menu
        """;
        messageService.sendMessage(chatId, text);
    }

    @Override
    public void displayGroup(Long chatId, String groupCode) {
        messageService.sendMessage(chatId,
                "Welcome to your group " + groupService.getGroupName(groupCode) + "!\nChoose an option:",
                keyboardFactory.groupMenu());
    }
}
