package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@StateHandlerFor(UserState.AWAITING_SHARED_USERS)
@RequiredArgsConstructor
public class AwaitingSharedUsersHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final ExpenseManagementService expenseManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String callbackData = context.getCallbackData();
        Long chatId = context.getChatId();

        if (callbackData == null) {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
            return;
        }

        messageService.answerCallbackQuery(context.getCallbackQueryId());
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        if (callbackData.startsWith("select_shared_user:")) {
            handleUserSelection(context, callbackData, expenseDto);
        } else if (callbackData.equals("confirm_shared_users")) {
            handleConfirm(context, expenseDto);
        } else if (callbackData.equals(Command.BACK_COMMAND.getCommand())) {
            handleReturn(chatId);
        }
    }


    private void handleUserSelection(ChatContext context, String callbackData, CreateExpenseDto expenseDto) {
        Long chatId = context.getChatId();
        Long userId = Long.parseLong(callbackData.split(":")[1]);

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

    private void handleConfirm(ChatContext context, CreateExpenseDto expenseDto) {
        Long chatId = context.getChatId();

        // Check if at least one user has been selected to share the expense
        boolean hasSelectedUsers = expenseDto.getSharedUsers().containsValue(true);

        if (!hasSelectedUsers) {
            expenseManagementService.sendIncorrectSharedUsers(chatId);
            return;
        }

        userStateManager.setState(chatId, UserState.CONFIRMING_EXPENSE);
        expenseManagementService.sendSummary(chatId);
    }

    private void handleReturn(Long chatId) {
        userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
        expenseManagementService.sendPaidBy(chatId, null);
    }

}