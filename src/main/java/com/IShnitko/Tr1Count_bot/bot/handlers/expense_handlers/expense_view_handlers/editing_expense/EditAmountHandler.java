package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers.editing_expense;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.ExpenseManagementServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@StateHandlerFor(UserState.EDITING_AMOUNT)
@RequiredArgsConstructor
public class EditAmountHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final ExpenseManagementService expenseManagementService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();
        Long chatId = context.getChatId();

        if (input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);

            expenseManagementService.sendExpenseInfo(chatId, null);
            return;
        }

        try {
            BigDecimal newAmount = BigDecimal.valueOf(Long.parseLong(input));
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);
            ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);
            expenseUpdateDto.setAmount(newAmount);
            expenseManagementService.sendExpenseInfo(chatId, context.getMessage().getMessageId());
        } catch (NumberFormatException e) {
            expenseManagementService.sendIncorrectAmount(chatId, context.getMessage().getMessageId());
            return;
        }
    }
}
