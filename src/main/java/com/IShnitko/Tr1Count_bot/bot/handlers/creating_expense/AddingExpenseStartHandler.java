package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.service.impl.UserInteractionServiceImpl;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Integer messageId = context.getMessage().getMessageId();

        if (input == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }

        if(input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.clearExpenseDto(chatId);
            userStateManager.setState(chatId, UserState.IN_THE_GROUP);
            messageService.deleteMessage(chatId, messageId);
            groupManagementService.displayGroup(chatId, userStateManager.getChosenGroup(chatId), context.getMessage().getMessageId());
            return;
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
            if (expenseDto.getMessageId() != null) {
                messageService.deleteMessage(chatId, expenseDto.getMessageId());
            }
            messageService.sendMessage(chatId, "Input date", keyboardFactory.dateButton());
        } else {
            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

            // Here we edit the original message instead of deleting and sending a new one
            // This prevents a "flash" and keeps the flow smoother.
            if (expenseDto.getMessageId() != null) {
                messageService.editMessage(
                        chatId,
                        expenseDto.getMessageId(),
                        "❌ Invalid format. Please send in format: `<description> <amount>`.",
                        keyboardFactory.returnButton()
                );
            } else {
                // As a fallback, send a new message
                messageService.sendMessage(chatId, "❌ Invalid format. Please send in format: `<description> <amount>`.", keyboardFactory.returnButton());
            }
        }
    }
}
