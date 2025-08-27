package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@StateHandlerFor(UserState.AWAITING_SHARED_USERS)
@RequiredArgsConstructor
public class AwaitingSharedUsersHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AwaitingSharedUsersHandler.class);
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final UserInteractionService userInteractionService;
    private final UserService userService;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String callbackData = context.getCallbackData();
        Long chatId = context.getChatId();

        if (callbackData == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }

        // Answer the callback query to hide the loading icon on the button
        messageService.answerCallbackQuery(context.getCallbackQueryId());

        if (callbackData.startsWith("select_shared_user:")) {
            handleUserSelection(context, callbackData);
        } else if (callbackData.equals("confirm_shared_users")) {
            handleConfirm(context);
        } else if (callbackData.equals(Command.BACK_COMMAND.getCommand())) { // TODO: after pressing button it still shows as chosen
            handleReturn(context);
        }
    }

    private void handleUserSelection(ChatContext context, String callbackData) {
        Long chatId = context.getChatId();
        Long userId = Long.parseLong(callbackData.split(":")[1]);

        // Get the DTO from the user session
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Find the user and toggle the shared status
        expenseDto.getSharedUsers().computeIfPresent(userId, (k, v) -> !v);

        // Get the list of all members to regenerate the keyboard
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);

        // Create the updated keyboard
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(context.getMessage().getMessageId());
        editMessage.setReplyMarkup(keyboardFactory.createSharedUsersKeyboard(members, expenseDto));

        // Update the message with the new keyboard
        messageService.editMessage(editMessage);
    }

    private void handleConfirm(ChatContext context) {
        Long chatId = context.getChatId();

        // Retrieve the DTO from the user's session
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Build the final summary message
        String summary = expenseDto.toString(userService);
        LOG.info("Summary string is empty: " + summary.isEmpty());
        // Set the state to CONFIRMING_EXPENSE and send the final message with buttons
        userStateManager.setState(chatId, UserState.CONFIRMING_EXPENSE);
        messageService.deleteMessage(chatId, context.getMessage().getMessageId());
        messageService.sendMessage(chatId,
                summary,
                keyboardFactory.finalConfirmationKeyboard());
    }


    private void handleReturn(ChatContext context) {
        Long chatId = context.getChatId();
        userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
        messageService.sendMessage(chatId, "Choose who paid for this purchase:", keyboardFactory.membersMenu( // TODO: add deleting or editing message
                groupService.getUsersForGroup(
                        userStateManager.getChosenGroup(chatId)
                ), false)); // TODO: i think i should extract every method that is duplicated to service
    }
}