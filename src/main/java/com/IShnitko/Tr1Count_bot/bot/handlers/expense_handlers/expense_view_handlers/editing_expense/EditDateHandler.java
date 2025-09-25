package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers.editing_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.impl.ExpenseManagementServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.service.impl.BalanceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@StateHandlerFor(UserState.EDITING_DATE)
@RequiredArgsConstructor
public class EditDateHandler implements StateHandler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    private final ExpenseManagementServiceImpl expenseManagementService;
    private final UserStateManager userStateManager;
    private final MessageServiceImpl messageService;
    private final BalanceServiceImpl balanceService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();

        if (input == null) {
            expenseManagementService.sendInvalidDateInput(chatId, messageId);
            return;
        }

        if (input.equalsIgnoreCase(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(),
                    balanceService.getExpenseTextFromExpenseDTO(
                            userStateManager.getOrCreateExpenseUpdateDto(chatId)
                    ),
                    keyboardFactory.expenseDetailsKeyboard());
            return;
        }

        if (input.equalsIgnoreCase(Command.DEFAULT_DATE_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);
            ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);
            expenseUpdateDto.setDate(LocalDate.now().atStartOfDay());
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(),
                    balanceService.getExpenseTextFromExpenseDTO(
                            expenseUpdateDto
                    ),
                    keyboardFactory.expenseDetailsKeyboard());
            return;
        }

        try {
            LocalDate expenseDate = LocalDate.parse(input, DATE_FORMATTER);
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);
            ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);
            expenseUpdateDto.setDate(expenseDate.atStartOfDay());

            messageService.deleteMessage(chatId, context.getMessage().getMessageId()); // TODO: extract to service
            messageService.editMessage(context.getChatId(), userStateManager.getBotMessageId(chatId),
                    balanceService.getExpenseTextFromExpenseDTO(
                            expenseUpdateDto
                    ),
                    keyboardFactory.expenseDetailsKeyboard());

        } catch (DateTimeParseException e) {
            expenseManagementService.sendInvalidDateInput(chatId, messageId);
        }
    }
}
