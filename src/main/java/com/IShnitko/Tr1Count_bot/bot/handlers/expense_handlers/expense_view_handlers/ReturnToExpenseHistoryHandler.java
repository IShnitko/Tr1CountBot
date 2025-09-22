package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StateHandlerFor(UserState.ONLY_RETURN_TO_EXPENSE_HISTORY)
@RequiredArgsConstructor
public class ReturnToExpenseHistoryHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final MessageServiceImpl messageService;
    private final ExpenseManagementService expenseManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String command = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();
        if (command.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.EXPENSE_HISTORY);
            expenseManagementService.sendExpenseHistoryView(chatId, messageId, 0);
        } else {
            messageService.deleteMessage(chatId, messageId);
        }
    }
}
