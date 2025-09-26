package com.IShnitko.Tr1Count_bot.bot.service.impl;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.exception.ExpenseNotFoundException;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.service.impl.BalanceServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ExpenseManagementServiceImpl implements ExpenseManagementService {
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;
    private final UserService userService;
    private final BalanceServiceImpl balanceService;

    @Override
    public void startAddingExpense(Long chatId, Integer messageId) {
        String instructions = """
                 💸 *Add New Expense*
                                \s
                 Please send expense details in format:
                 `description amount`
                                \s
                 Example:
                 `Dinner 25.50`
                \s""";
        messageService.editMessage(chatId,
                messageId,
                instructions,
                keyboardFactory.returnButton());
    }

    @Override
    public void sendInvalidStartAddingExpense(Long chatId, Integer messageId) {
        messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(
                chatId,
                userStateManager.getBotMessageId(chatId),
                "❌ Invalid format. Please send in format: `<description> <amount>`.",
                keyboardFactory.returnButton()
        );

    }

    @Override
    public void sendDateInput(Long chatId, Integer inputMessageId) {
        if (inputMessageId != null) messageService.deleteMessage(chatId, inputMessageId); // TODO: probably move to handler
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "Input date in format dd.mm.yy or use options below",
                keyboardFactory.dateButton());
    }

    @Override
    public void sendInvalidDateInput(Long chatId, Integer messageId) {
        if (messageId != null) messageService.deleteMessage(chatId, messageId);
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "❌ Invalid input. Please send the date in format `dd.mm.yy` or type `today`.",
                keyboardFactory.dateButton());
    }

    @Override
    public void sendPaidBy(Long chatId, Integer messageId) {
        if (messageId != null) messageService.deleteMessage(chatId, messageId); // TODO: probably move to handler
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "Choose who paid for this purchase:", keyboardFactory.membersMenu(
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
                userStateManager.getBotMessageId(chatId),
                "Select who shared the expense:",
                keyboardFactory.createSharedUsersKeyboard(members, expenseDto, userService));
    }

    @Override
    public void sendIncorrectSharedUsers(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "Please select at least one user to share the expense:",
                keyboardFactory.createSharedUsersKeyboard(
                        groupService.getUsersForGroup(
                                userStateManager.getChosenGroup(chatId)
                        ),
                        expenseDto,
                        userService));
    }

    @Override
    public void sendSummary(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Build the final summary message
        String summary = expenseDto.toString(userService);

        // Set the state to CONFIRMING_EXPENSE and send the final message with buttons
        userStateManager.setState(chatId, UserState.CONFIRMING_EXPENSE);
        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                summary,
                keyboardFactory.finalConfirmationKeyboard());
    }

    @Override
    public void sendExpenseHistoryView(Long chatId, Integer messageId, int page) {
        List<Expense> expenses = balanceService.getExpensesForGroup(
                userStateManager.getChosenGroup(chatId)
        );
        messageService.editMessage(chatId,
                messageId,
                expenses.size() > 5 ? "Expense history of your group. Page " + (page + 1) : "Expense history of your group.",
                keyboardFactory.expenseList(expenses, page));
    }

    @Override
    public void sendExpenseHistoryView(Long chatId, Integer messageId, int page, String additionalText) {
        List<Expense> expenses = balanceService.getExpensesForGroup(
                userStateManager.getChosenGroup(chatId)
        );
        messageService.editMessage(chatId,
                messageId,
                additionalText + "\n" +
                (expenses.size() > 5 ? "Expense history of your group. Page " + (page + 1) : "Expense history of your group."),
                keyboardFactory.expenseList(expenses, page));
    }

    @Override
    public void sendDeleteConfirmation(Long chatId, Integer messageId, Long expenseIdToDelete) {
        messageService.editMessage(chatId,
                messageId,
                String.format("Are you sure you want to delete expense %s?", balanceService.getExpenseById(expenseIdToDelete)
                        .orElseThrow(
                                () -> new ExpenseNotFoundException("Expense to delete was not found")
                        ).getTitle()),
                keyboardFactory.confirmationKeyboard(expenseIdToDelete.toString()));
    }

    @Override
    public void sendAmountInput(Long chatId, Integer messageId) {
        messageService.editMessage(chatId,
                messageId,
                "Input new price for your expense: ",
                keyboardFactory.returnButton());
    }

    @Override
    public void sendTitleInput(Long chatId, Integer messageId) {
        messageService.editMessage(chatId,
                messageId,
                "Input new title for your expense: ",
                keyboardFactory.returnButton());
    }

    @Override
    public void sendExpenseInfo(Long chatId, Integer inputMessageId) {
        ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);

        if(inputMessageId != null)
            messageService.deleteMessage(chatId, inputMessageId);

        messageService.editMessage(chatId, userStateManager.getBotMessageId(chatId),
                balanceService.getExpenseTextFromExpenseDTO(
                        expenseUpdateDto
                ),
                keyboardFactory.expenseDetailsKeyboard());
    }

    @Override
    public void sendIncorrectAmount(Long chatId, Integer messageId) {
        if(messageId != null)
            messageService.deleteMessage(chatId, messageId);

        messageService.editMessage(chatId,
                userStateManager.getBotMessageId(chatId),
                "Incorrect amount, input a number like 123.45",
                keyboardFactory.returnButton());
    }

}
