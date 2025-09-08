package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.exception.UserNotFoundException;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@StateHandlerFor(UserState.AWAITING_GROUP_NAME)
@RequiredArgsConstructor
public class AwaitingGroupNameHandler implements StateHandler {
    private final UserInteractionService userInteractionService;
    private final GroupService groupService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();

        if (Objects.equals(input, Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(context.getChatId(), UserState.DEFAULT);
            userInteractionService.startCommand(context.getChatId(), context.getMessage().getMessageId());
        } else if (context.getText() != null) {
            handleInputGroupName(context, input);
        }
    }

    private void handleInputGroupName(ChatContext context, String groupName) {
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();
        try {
            Group group = groupService.createGroup(groupName, context.getUser().getId());
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, group.getId());
            groupManagementService.displayGroup(chatId,
                    group.getId(),
                    userStateManager.getBotMessageId(chatId),
                    context.getMessage().getMessageId(),
                    "Group created! Invite link: https://t.me/Tr1Count_bot?start=invite_" + group.getId());
        } catch (UserNotFoundException e) {
            userStateManager.setState(chatId, UserState.DEFAULT);
            userInteractionService.startCommand(chatId, messageId, "Error while creating group.");
        }
    }
}
