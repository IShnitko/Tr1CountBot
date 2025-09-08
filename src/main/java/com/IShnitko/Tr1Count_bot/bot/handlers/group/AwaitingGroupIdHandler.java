package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
@StateHandlerFor(UserState.AWAITING_GROUP_ID)
@RequiredArgsConstructor
@Slf4j
public class AwaitingGroupIdHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;
    private final GroupService groupService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();

        if (input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.DEFAULT);
            userInteractionService.startCommand(chatId, context.getMessage().getMessageId());
            return;
        }

        if (groupService.doesGroupExist(input)) {
            log.info("Group was found with ID: {}", input);
            userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, input);

            if (!groupService.doesUserExistInGroup(context.getUser().getId(), input)) {
                groupService.joinGroupById(input, context.getUser().getId());
            }

            Integer botMessageId = userStateManager.getBotMessageId(chatId);
            Integer inputMessageId = context.getUpdateType() == ChatContext.UpdateType.MESSAGE ? context.getMessage().getMessageId() : null;

            groupManagementService.displayGroup(chatId, input, botMessageId, inputMessageId);
        } else {
            groupManagementService.sendIncorrectGroupId(chatId, context.getMessage().getMessageId(), context.getUser().getId()); // throws error message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message
        }
    }
}
