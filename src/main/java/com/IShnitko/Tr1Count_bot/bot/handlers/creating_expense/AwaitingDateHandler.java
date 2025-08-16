package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.BACK_COMMAND;

@Component
@StateHandlerFor(UserState.AWAITING_DATE)
@RequiredArgsConstructor
public class AwaitingDateHandler implements StateHandler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    public static final String DEFAULT_DATE_COMMAND = "today";

    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupManagementService groupManagementService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getText(); // TODO: this handler can also get query not text input (like button today doesn;t work)
        Long chatId = context.getChatId();

        messageService.deleteMessage(chatId, context.getMessage().getMessageId());

        if (input == null) {
            messageService.sendMessage(chatId, "❌ Invalid input. Please send the date in format `dd.mm.yy` or type `today`.");
            return;
        }

        if (input.equalsIgnoreCase(BACK_COMMAND)) {
            handleReturn(chatId);
            return;
        }

        // Handle "today" command
        if (input.equalsIgnoreCase(DEFAULT_DATE_COMMAND)) {
            handleDateSelection(chatId, LocalDate.now());
            return;
        }

        // Try to parse the date from user input
        try {
            LocalDate expenseDate = LocalDate.parse(input, DATE_FORMATTER);
            handleDateSelection(chatId, expenseDate);
        } catch (DateTimeParseException e) {
            // If parsing fails, inform the user and keep the current state
            // TODO: add return button
            messageService.sendMessage(chatId, "❌ Invalid date format. Please use `dd.mm.yy`, for example: `25.10.24` or type `today`.");
        }
    }

    /**
     * Handles the date selection, updates the DTO, and moves to the next state.
     * @param chatId The chat ID of the user.
     * @param expenseDate The parsed LocalDate object.
     */
    private void handleDateSelection(Long chatId, LocalDate expenseDate) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        expenseDto.setDate(expenseDate.atStartOfDay());

        userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);

        messageService.sendMessage(chatId, "Choose who paid for this purchase:", keyboardFactory.membersMenu(
                groupService.getUsersForGroup(
                        userStateManager.getChosenGroup(chatId)
                ), false));
    }

    /**
     * Clears the user's session and cancels the expense creation process.
     * @param chatId The chat ID of the user.
     */
    private void handleReturn(Long chatId) {
        userStateManager.clearExpenseDto(chatId);
        messageService.sendMessage(chatId, "❌ Expense creation canceled.");
        userStateManager.setState(chatId, UserState.IN_THE_GROUP);
        groupManagementService.displayGroup(chatId, userStateManager.getChosenGroup(chatId));
    }
}
