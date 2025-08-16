package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@StateHandlerFor(UserState.AWAITING_GROUP_ID)
@RequiredArgsConstructor
public class AwaitingGroupIdHandler implements StateHandler {
    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        // Answer callback to remove loading indicator
        if (context.getCallbackQueryId() != null) {
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }
        // Get selected group ID from callback data
        String groupId = context.getCallbackData();
        Long userId = context.getUser().getId();

        // Validate selection
        if (groupId == null || groupId.isEmpty()) {
            messageService.sendMessage(context.getChatId(), "Error while getting group");
            return;
        }

        // Update state and show group menu
        handleValidSelection(context, groupId);
    }


    private void handleValidSelection(ChatContext context, String groupId) throws TelegramApiException {
        userStateManager.setStateWithChosenGroup(context.getChatId(), UserState.IN_THE_GROUP, groupId);
        messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
        groupManagementService.displayGroup(context.getChatId(), groupId); // TODO: delete message after exec
    }
}