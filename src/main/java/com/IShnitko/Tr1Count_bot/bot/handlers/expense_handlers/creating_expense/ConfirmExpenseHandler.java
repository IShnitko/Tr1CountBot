 package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@StateHandlerFor(UserState.CONFIRMING_EXPENSE)
@RequiredArgsConstructor
public class ConfirmExpenseHandler implements StateHandler {
    private final MessageService messageService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final GroupManagementService groupManagementService;
    private final ExpenseManagementService expenseManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();

        if (context.getText() != null) {
            messageService.deleteMessage(chatId, messageId);
            return;
        }
        Command command = Command.fromString(context.getCallbackData());

        switch (command) {
            case CONFIRM -> handleConfirm(chatId, messageId);
            case CANCEL -> handleCancel(chatId, messageId);
            case BACK_COMMAND -> handleReturn(chatId);
        }
    }

    private void handleReturn(Long chatId) {
        userStateManager.setState(chatId, UserState.AWAITING_SHARED_USERS);
        expenseManagementService.sendSharedUsers(chatId);
    }

    private void handleCancel(Long chatId, Integer messageId) {
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        userStateManager.clearUserData(chatId);
        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId,
                userStateManager.getChosenGroup(chatId),
                messageId,
                null,
                "Canceled expense creation");
    }

    private void handleConfirm(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Pass the DTO to the business service for saving
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        balanceService.createExpense(chosenGroup, expenseDto);

        userStateManager.clearUserData(chatId);

        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId,
                chosenGroup,
                messageId,
                null,
                "✅ Expense successfully added!");
    }
}
