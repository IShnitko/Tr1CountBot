package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.service.GroupService;
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

    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText() != null ? context.getText() : context.getCallbackData(); // TODO: this handler can also get query not text input (like button today doesn;t work)
        Long chatId = context.getChatId();

        Integer messageId = context.getMessage().getMessageId();
        messageService.deleteMessage(chatId, messageId);

        if (input == null) {
            messageService.sendMessage(chatId, "❌ Invalid input. Please send the date in format `dd.mm.yy` or type `today`.", keyboardFactory.returnButton());
            return;
        }

        if (input.equalsIgnoreCase(Command.BACK_COMMAND.getCommand())) {
            handleReturn(chatId);
            return;
        }

        // Handle "today" command
        if (input.equalsIgnoreCase(Command.DEFAULT_DATE_COMMAND.getCommand())) {
            handleDateSelection(chatId, messageId, LocalDate.now());
            return;
        }

        // Try to parse the date from user input
        try {
            LocalDate expenseDate = LocalDate.parse(input, DATE_FORMATTER);
            handleDateSelection(chatId, messageId, expenseDate);
        } catch (DateTimeParseException e) {
            // If parsing fails, inform the user and keep the current state
            // TODO: add return button
            messageService.deleteMessage(chatId, messageId);
            messageService.sendMessage(chatId, "❌ Invalid date format. Please use `dd.mm.yy`, for example: `25.10.24` or type `today`.");
        }
    }

    /**
     * Handles the date selection, updates the DTO, and moves to the next state.
     *
     * @param chatId      The chat ID of the user.
     * @param messageId
     * @param expenseDate The parsed LocalDate object.
     */
    private void handleDateSelection(Long chatId, Integer messageId, LocalDate expenseDate) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        expenseDto.setDate(expenseDate.atStartOfDay());

        userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
        messageService.deleteMessage(chatId, messageId);
        messageService.sendMessage(chatId, "Choose who paid for this purchase:", keyboardFactory.membersMenu(
                groupService.getUsersForGroup(
                        userStateManager.getChosenGroup(chatId)
                ), false));
    }

    /**
     * Clears the user's session and cancels the expense creation process.
     *
     * @param chatId    The chat ID of the user.
     */
    private void handleReturn(Long chatId) {
        userStateManager.clearExpenseDto(chatId);
        String instructions = """
                💸 *Add New Expense* [ADDING_EXPENSE_START]
                                
                Please send expense details in format:
                `<description> <amount>`
                                
                Example:
                `Dinner 25.50`
                """;
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        expenseDto.setMessageId(
                messageService.sendMessage(chatId, instructions, keyboardFactory.returnButton())
        );
        userStateManager.setState(chatId, UserState.ADDING_EXPENSE_START);
    }
}
