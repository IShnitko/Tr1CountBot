package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@StateHandlerFor(UserState.ONLY_RETURN_TO_GROUP)
@RequiredArgsConstructor
public class ReturnToGroupHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final MessageService messageService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String command = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();
        if (command.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(chatId, userStateManager.getChosenGroup(chatId), context.getMessage().getMessageId());
        } else {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
        }
    }
}
