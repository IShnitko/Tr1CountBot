package com.IShnitko.Tr1Count_bot.bot.handlers.return_handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@StateHandlerFor(UserState.ONLY_RETURN_TO_MEMBERS_MENU)
@RequiredArgsConstructor
public class ReturnToMembersMenuHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final MessageService messageService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String command = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();
        if (command.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.MEMBERS_MENU);
            groupManagementService.viewMembersMenu(chatId, messageId, context.getUser().getId());
        } else {
            messageService.deleteMessage(chatId, messageId);
        }
    }
}
