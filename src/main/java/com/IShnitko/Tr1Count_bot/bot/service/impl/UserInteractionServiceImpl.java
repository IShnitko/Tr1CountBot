package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
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
    public void startCommand(Long chatId, Integer messageId) {
        if (messageId != null) {
            messageService.editMessage(chatId, messageId, "Welcome to TriCount bot!\nChoose an option:",
                    keyboardFactory.mainMenu());
        } else {
            messageService.sendMessage(chatId,
                    "Welcome to TriCount bot!\nChoose an option:",
                    keyboardFactory.mainMenu());
        }
    }

    @Override
    public void startCommand(Long chatId, Integer messageId, String additionalText) {
        messageService.editMessage(chatId,
                messageId,
                additionalText + "\n\nThis is TriCount bot!\nChoose an option:",
                keyboardFactory.mainMenu());
    }

    @Override
    public void helpCommand(Long chatId, Integer messageId) {
        String text = "Bot description:\n\n...";
        messageService.editMessage(chatId,
                messageId,
                text,
                keyboardFactory.returnButton());
    }

    @Override
    public void unknownCommand(Long chatId) {
        messageService.sendMessage(chatId, "Unknown command.");
    }

}
