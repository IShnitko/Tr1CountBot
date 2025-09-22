package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.impl.ExpenseManagementServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.exception.ExpenseNotFoundException;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StateHandlerFor(UserState.EXPENSE_DELETE_CONFIRMATION)
@RequiredArgsConstructor
public class DeleteConfirmationHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final ExpenseManagementServiceImpl expenseManagementService;
    private final MessageServiceImpl messageService;
    private final BalanceService balanceService;

    @Override
    public void handle(ChatContext context) throws Exception {
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();

        Long expenseId = null;
        Command command = null;
        if (context.getCallbackData() != null) {
            // Split the data and check if it contains at least two parts
            String[] parts = context.getCallbackData().split(":");
            command = Command.fromString(parts[0]);
            if (parts.length > 1) {
                try {
                    // Now we can be more specific with the exception
                    expenseId = Long.valueOf(parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("Invalid expense ID format in callback data: " + parts[1], e);
                }

            } else {
                log.info("Callback data does not contain a valid ID part.");
            }
        } else {
            log.info("Callback data is null.");
        }

        switch (command) {
            case BACK_COMMAND -> returnToExpenseView(context);
            case DELETE -> deleteAndReturnToExpenseView(context, expenseId);
            default -> messageService.deleteMessage(chatId, messageId);
        }
    }

    private void deleteAndReturnToExpenseView(ChatContext context, Long expenseId) {
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_HISTORY);
        String expenseTitle = balanceService.getExpenseById(expenseId)
                        .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"))
                                .getTitle();
        balanceService.deleteExpenseById(expenseId);
        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), 0,
                "Successfully deleted expense " + expenseTitle);

    }

    private void returnToExpenseView(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_HISTORY);
        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), 0);

    }
}
