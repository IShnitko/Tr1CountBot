package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.service.impl.UserInteractionServiceImpl;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.impl.GroupServiceImpl;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.BACK_COMMAND;

@Component
@StateHandlerFor(UserState.ADDING_EXPENSE_START)
@RequiredArgsConstructor
public class AddingExpenseStartHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AddingExpenseStartHandler.class);

    private static final Pattern EXPENSE_PATTERN = Pattern.compile("^(.+)\\s+([0-9]+\\.?[0-9]*)$");
    private final UserInteractionServiceImpl userInteractionService;
    private final UserStateManager userStateManager;
    private final KeyboardFactory keyboardFactory;
    private final MessageServiceImpl messageService;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String input = context.getText() != null ? context.getText() : context.getCallbackData();
        Long chatId = context.getChatId();

        if (input == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }

        if(input.equals(BACK_COMMAND)) {
            userStateManager.clearExpenseDto(chatId);
            userStateManager.setState(chatId, UserState.IN_THE_GROUP);
            groupManagementService.displayGroup(chatId, userStateManager.getChosenGroup(chatId));
        }

        Matcher matcher = EXPENSE_PATTERN.matcher(input);

        messageService.deleteMessage(chatId, messageId);
        LOG.info("Matcher: " +matcher.matches());
        if (matcher.matches()) {
            String title = matcher.group(1).trim();
            BigDecimal amount = new BigDecimal(matcher.group(2));

            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

            expenseDto.setTitle(title);
            expenseDto.setAmount(amount);

            userStateManager.setState(chatId, UserState.AWAITING_DATE);

            messageService.sendMessage(chatId, "Input date", keyboardFactory.dateButton());
        } else {
            // TODO: message is not deleted after incorrect input
            messageService.sendMessage(chatId, "❌ Invalid format. Please send in format: `<description> <amount>`.", keyboardFactory.returnButton());
        }
    }
}
