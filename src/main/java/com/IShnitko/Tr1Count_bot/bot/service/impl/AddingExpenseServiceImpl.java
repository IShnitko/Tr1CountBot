package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.AddingExpenseService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AddingExpenseServiceImpl implements AddingExpenseService {
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void startAddingExpense(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        String instructions = """
                 💸 *Add New Expense*
                                \s
                 Please send expense details in format:
                 `description amount`
                                \s
                 Example:
                 `Dinner 25.50`
                \s""";
        Integer sentMessageId = messageService.editMessage(chatId,
                messageId,
                instructions,
                keyboardFactory.returnButton());
        expenseDto.setMessageId(sentMessageId);
    }

    @Override
    public void sendInvalidStartAddingExpense(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(
                chatId,
                expenseDto.getMessageId(),
                "❌ Invalid format. Please send in format: `<description> <amount>`.",
                keyboardFactory.returnButton()
        );

    }

    @Override
    public void sendDateInput(Long chatId, Integer inputMessageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        if (inputMessageId != null) messageService.deleteMessage(chatId, inputMessageId);
        messageService.editMessage(chatId,
                expenseDto.getMessageId(),
                "Input date in format dd.mm.yy or use options below",
                keyboardFactory.dateButton());
    }

    @Override
    public void sendInvalidDateInput(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(chatId,
                expenseDto.getMessageId(),
                "❌ Invalid input. Please send the date in format `dd.mm.yy` or type `today`.",
                keyboardFactory.dateButton());
    }

    @Override
    public void sendPaidBy(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        if (messageId != null) messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(chatId,
                expenseDto.getMessageId(), "Choose who paid for this purchase:", keyboardFactory.membersMenu(
                        groupService.getUsersForGroup(
                                userStateManager.getChosenGroup(chatId)
                        ), false)
        );
    }

    @Override
    public void sendSharedUsers(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Get the members of the chosen group to display in the next menu.
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);

        // Send a new message with a keyboard for selecting shared users.
        messageService.editMessage(chatId,
                expenseDto.getMessageId(),
                "Now, select who shared the expense:",
                keyboardFactory.createSharedUsersKeyboard(members, expenseDto));
    }

    @Override
    public void sendIncorrectSharedUsers(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        messageService.editMessage(chatId,
                expenseDto.getMessageId(),
                "Please select at least one user to share the expense:",
                keyboardFactory.createSharedUsersKeyboard(
                        groupService.getUsersForGroup(
                                userStateManager.getChosenGroup(chatId)
                        ),
                        expenseDto));
    }

    @Override
    public void sendSummary(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Build the final summary message
        String summary = expenseDto.toString(userService);

        // Set the state to CONFIRMING_EXPENSE and send the final message with buttons
        userStateManager.setState(chatId, UserState.CONFIRMING_EXPENSE);
        messageService.editMessage(chatId,
                expenseDto.getMessageId(),
                summary,
                keyboardFactory.finalConfirmationKeyboard());
    }

}
