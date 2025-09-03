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
        String text = "Please note, this is an unofficial Tricount bot. It is an open-source project created to help people manage group expenses. You can find its source code and contribute on GitHub.\n\n"
                + "### What is Tricount?\n\n"
                + "Tricount is a Telegram bot designed to help groups of people manage and track shared expenses. Whether you're traveling with friends, sharing rent with roommates, or organizing a group dinner, this bot simplifies the process of knowing who paid for what and who owes whom. No more messy spreadsheets or forgotten IOUs!\n\n"
                + "### How It Works\n\n"
                + "The bot operates through a simple, button-based interface within Telegram. Here's a quick summary of the main features and how to use them:\n\n"
                + "* **Create a Group:** Start a new expense group for any event. You'll get a unique code to invite friends and family.\n\n"
                + "* **Join a Group:** Use a code provided by a group member to join their expense tracking.\n\n"
                + "* **Add an Expense:** This is the core function. You'll enter details like the amount, what the expense was for, who paid for it, and who shared in the cost. The bot automatically handles the math, splitting the cost evenly among the chosen participants.\n\n"
                + "* **Check Balances:** At any time, you can get a clear overview of who owes money and to whom.\n\n"
                + "* **View History:** See a full list of all expenses added to the group, which helps with transparency and makes it easy to find specific transactions.\n\n"
                + "### Why You Need This Bot\n\n"
                + "Managing group finances can be complicated and awkward. Tricount takes the stress out of shared expenses by:\n\n"
                + "* **Eliminating Confusion:** Every transaction is recorded, so there's no more guessing or forgotten debts.\n\n"
                + "* **Saving Time:** The bot handles all the calculations instantly, so you don't have to manually figure out complex splits.\n\n"
                + "* **Improving Transparency:** Everyone in the group can see the full history and balances, building trust and ensuring everyone is on the same page.\n\n"
                + "It's the perfect tool to make sure group activities stay fun and friendly, without the hassle of money disputes.";

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
