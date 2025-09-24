package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@StateHandlerFor(UserState.ADDING_EXPENSE_START)
@RequiredArgsConstructor
public class AddingExpenseStartHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AddingExpenseStartHandler.class);

    private static final Pattern EXPENSE_PATTERN = Pattern.compile("^(.+)\\s+([0-9]+\\.?[0-9]*)$");

    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final ExpenseManagementService expenseManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();
        log.info("Bot message id (Expense DTO) was set to {}", messageId);

        assert input != null;
        if (input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.clearExpenseDto(chatId);
            userStateManager.setState(chatId, UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(chatId, userStateManager.getChosenGroup(chatId), context.getMessage().getMessageId());
            return;
        }

        Matcher matcher = EXPENSE_PATTERN.matcher(input);
        LOG.info("Matcher: " + matcher.matches());

        if (matcher.matches()) {
            String title = matcher.group(1).trim();
            BigDecimal amount = new BigDecimal(matcher.group(2));
            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

            expenseDto.setTitle(title);
            expenseDto.setAmount(amount);
            expenseManagementService.sendDateInput(chatId, messageId);
            userStateManager.setState(chatId, UserState.AWAITING_DATE);
        } else {
            expenseManagementService.sendInvalidStartAddingExpense(chatId, messageId);
        }
    }
}
