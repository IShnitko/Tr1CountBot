package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.exception.UserNotFoundException;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@StateHandlerFor(UserState.AWAITING_GROUP_NAME)
@RequiredArgsConstructor
public class AwaitingGroupNameHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AwaitingGroupNameHandler.class);

    private final UserInteractionService userInteractionService;
    private final MessageService messageService;
    private final GroupService groupService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText();
        if (input == null) {
            userInteractionService.unknownCommand(context.getChatId());

            return;
        }
        if (context.getCallbackQueryId() != null) { // SAFETY CHECK
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }
        handleInputGroupName(context.getChatId(), context.getUser().getId(), input, context.getMessage().getMessageId());
    }

    private void handleInputGroupName(Long chatId, Long userId, String groupName, Integer messageId) {
        messageService.deleteMessage(chatId, messageId);
        try {
            Group group = groupService.createGroup(groupName, userId);
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, group.getId());
            String text = "Group created! Invite link: https://t.me/Tr1Count_bot?start=invite_" + group.getId();
            messageService.sendMessage(chatId, text);
            groupManagementService.displayGroup(chatId, group.getId());
        } catch (UserNotFoundException e) {
            messageService.sendMessage(chatId, "Error creating group");
        }
    }
}
