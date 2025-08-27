package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.BACK_COMMAND;

@Component
@StateHandlerFor(UserState.AWAITING_GROUP_ID)
@RequiredArgsConstructor
public class AwaitingGroupIdHandler implements StateHandler {
    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        // Validate selection
        if (input.equals(BACK_COMMAND)) {
            userStateManager.setState(context.getChatId(), UserState.DEFAULT);
            userInteractionService.startCommand(context.getChatId(), context.getMessage().getMessageId());
            return;
        }
        handleValidSelection(context, input);
    }

    private void handleValidSelection(ChatContext context, String groupId) {
        Long chatId = context.getChatId();
        if (groupService.doesGroupExist(groupId)) {
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, groupId);
            if (!groupService.doesUserExistInGroup(context.getUser().getId(), groupId)) groupService.joinGroupById(groupId, context.getUser().getId());
            groupManagementService.displayGroup(chatId,
                    groupId,
                    userStateManager.getBotMessageId(chatId),
                    context.getUpdateType() == ChatContext.UpdateType.MESSAGE ? context.getMessage().getMessageId() : null); // if user inputted groupId manually, then we delete his message
        } else {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
            messageService.editMessage(chatId, userStateManager.getBotMessageId(chatId), "Incorrect group Id. Try again:", keyboardFactory.returnButton());
        }
    }
}