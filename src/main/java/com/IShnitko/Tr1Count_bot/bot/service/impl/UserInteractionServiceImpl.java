package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import org.springframework.stereotype.Service;

@Service
public class UserInteractionServiceImpl implements UserInteractionService {
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final UserStateManager userStateManager;

    public UserInteractionServiceImpl(MessageService messageService, KeyboardFactory keyboardFactory, UserStateManager userStateManager) {
        this.messageService = messageService;
        this.keyboardFactory = keyboardFactory;
        this.userStateManager = userStateManager;
    }

    @Override
    public void startCommand(Long chatId) {
        messageService.sendMessage(chatId,
                "Welcome to TriCount bot!\nChoose an option:",
                keyboardFactory.mainMenu());
    }

    @Override
    public void helpCommand(Long chatId) {
        String text = "Bot description:\n\n...";
        messageService.sendMessage(chatId, text);
    }

    @Override
    public void handleBackCommand(Long chatId) {
        userStateManager.setState(chatId, UserState.DEFAULT);
        messageService.sendMessage(chatId, "You came back to main menu");
        startCommand(chatId);
    }

    @Override
    public void unknownCommand(Long chatId) {
        messageService.sendMessage(chatId, "Unknown command.");
    }
}
