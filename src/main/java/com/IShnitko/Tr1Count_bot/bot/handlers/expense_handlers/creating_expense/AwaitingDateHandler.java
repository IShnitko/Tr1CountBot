package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.AddingExpenseService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@StateHandlerFor(UserState.AWAITING_DATE)
@RequiredArgsConstructor
public class AwaitingDateHandler implements StateHandler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    private final UserStateManager userStateManager;
    private final AddingExpenseService addingExpenseService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();

        Integer messageId = context.getMessage().getMessageId();

        if (input == null) {
            addingExpenseService.sendInvalidDateInput(chatId, messageId);
            return;
        }

        if (input.equalsIgnoreCase(Command.BACK_COMMAND.getCommand())) {
            userStateManager.clearExpenseDto(chatId);
            userStateManager.setState(chatId, UserState.ADDING_EXPENSE_START);
            addingExpenseService.startAddingExpense(chatId, messageId);
            return;
        }

        // Handle "today" command
        if (input.equalsIgnoreCase(Command.DEFAULT_DATE_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
            expenseDto.setDate(LocalDate.now().atStartOfDay());
            addingExpenseService.sendPaidBy(chatId, null);
            return;
        }

        // Try to parse the date from user input
        try {
            LocalDate expenseDate = LocalDate.parse(input, DATE_FORMATTER);
            userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
            expenseDto.setDate(expenseDate.atStartOfDay());
            addingExpenseService.sendPaidBy(chatId, messageId);
        } catch (DateTimeParseException e) {
           addingExpenseService.sendInvalidDateInput(chatId, messageId);
        }
    }
}
