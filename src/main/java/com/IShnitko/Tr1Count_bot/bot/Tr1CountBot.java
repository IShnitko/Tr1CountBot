package com.IShnitko.Tr1Count_bot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Tr1CountBot extends TelegramLongPollingBot {

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String JOIN = "/join";
    private static final String CREATE = "/create";
    private static final String ROOMS = "/rooms";
    private static final String BALANCE = "/balance";
    private static final String ADD_EXPENSE = "/add_expense";
    private static final String MEMBERS = "/members";



    @Autowired
    public Tr1CountBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return "TriCount";
    }
}
