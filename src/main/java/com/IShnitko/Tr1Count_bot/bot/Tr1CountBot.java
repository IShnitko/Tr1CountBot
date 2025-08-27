package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandlerFactory;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Tr1CountBot extends TelegramLongPollingBot {

    private final UserService userService;
    private final UserStateManager userStateManager;
    private final StateHandlerFactory stateHandlerFactory;


    @Autowired
    public Tr1CountBot(@Value("${bot.token}") String botToken,
                       UserStateManager userStateManager,
                       UserService userService,
                       StateHandlerFactory stateHandlerFactory) {
        super(botToken);
        this.userStateManager = userStateManager;
        this.userService = userService;
        this.stateHandlerFactory = stateHandlerFactory;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            ChatContext context = new ChatContext(update, this);
            userService.findOrCreateUser(context.getUser());
            UserState state = userStateManager.getState(context.getChatId());

            String text = context.getText() != null ? context.getText() : context.getCallbackData();

            LOG.info("Processing {} ({}) from {} in state: {}",
                    context.getUpdateType(), text, context.getChatId(), state);

            stateHandlerFactory.getHandler(state).handle(context);
        } catch (Exception e) {
            LOG.error("Error processing update", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "TriCount";
    }
}
