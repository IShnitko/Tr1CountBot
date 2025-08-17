package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.TelegramApiUtils;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.*;

@Component
@StateHandlerFor(UserState.IN_THE_GROUP)
@RequiredArgsConstructor
public class InGroupStateHandler implements StateHandler {
    private final MessageService messageService;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;


    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String groupId = userStateManager.getChosenGroup(context.getChatId());

        String command = context.getText() != null ? context.getText() : context.getCallbackData();
        if (command == null) {
            userInteractionService.unknownCommand(context.getChatId());
            return;
        }
        if (context.getCallbackQueryId() != null) { // SAFETY CHECK
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }

        switch (command) { // TODO: add Invitation link and expense history
            case BALANCE -> handleBalance(context, groupId);
            case ADD_EXPENSE -> handleAddExpense(context); // TODO: maybe change taking context to just taking params
            case MEMBERS -> handleMembers(context, groupId);
            case HELP -> handleHelp(context);
            case BACK_COMMAND -> handleBackToMain(context);
            case HISTORY -> showHistory(context, groupId);
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void showHistory(ChatContext context, String groupId) {
        List<Expense> expenses = balanceService.getExpensesForGroup(groupId);

        // Check if there are any expenses to display
        if (expenses.isEmpty()) {
            messageService.sendMessage(context.getChatId(), "There are no expenses recorded in this group yet\\.");
            return;
        }

        // Build the message using a StringBuilder for efficiency
        StringBuilder messageBuilder = new StringBuilder();

        // Add a nice header with a list count
        messageBuilder.append("📝 *Group Expense History* 📝\n\n");
        messageBuilder.append(String.format("You have %d expenses\\.\n\n", expenses.size()));

        // Use a date formatter for consistent output
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd\\.MM\\.yyyy");

        // Iterate through each expense and format it
        for (Expense expense : expenses) {
            // Escape any special characters in the title and user name
            String title = TelegramApiUtils.formatString(expense.getTitle());
            String paidBy = TelegramApiUtils.formatString(expense.getPaidBy().getName());
            String formattedDate = TelegramApiUtils.formatString(expense.getDate().format(dateFormatter));

            // Append formatted expense details
            messageBuilder.append(String.format(
                    "💸 *%s*\n" +
                            "  💵 Amount: `%.2f`\n" +
                            "  👤 Paid by: %s\n" +
                            "  🗓️ Date: %s\n\n",
                    title,
                    expense.getAmount(),
                    paidBy,
                    formattedDate
            ));
        }

        // Send the complete, formatted message
        messageService.sendMessage(context.getChatId(), messageBuilder.toString());
    }

    private void handleBalance(ChatContext context, String groupId) {
        try {
            String balanceText = balanceService.getBalanceText(groupId);

            messageService.sendMessage(context.getChatId(), balanceText);
        } catch (Exception e) {
            messageService.sendMessage(context.getChatId(), "❌ Error calculating balance");
        }
    }

    private void handleAddExpense(ChatContext context) {
        Integer messageId = context.getMessage().getMessageId();
        Long chatId = context.getChatId();

        try {
            // Переводим в состояние добавления расхода
            userStateManager.setState(chatId, UserState.ADDING_EXPENSE_START);
            CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
            // Отправляем инструкцию
            String instructions = """
                💸 *Add New Expense*
                                
                Please send expense details in format:
                `<description> <amount>`
                                
                Example:
                `Dinner 25.50`
                """;
            messageService.deleteMessage(chatId, messageId);
            Integer sentMessageId = messageService.sendMessage(chatId, instructions, keyboardFactory.returnButton());
            expenseDto.setMessageId(sentMessageId);
        } catch (Exception e) {
            messageService.deleteMessage(chatId, messageId);
            messageService.sendMessage(chatId, "❌ Error starting expense creation");
        }
    }

    private void handleMembers(ChatContext context, String groupId) {
        Long chatId = context.getChatId();
        try {
            List<User> members = groupService.getUsersForGroup(groupId);
            Integer messageId = context.getMessage().getMessageId();
            messageService.deleteMessage(chatId, messageId);
            messageService.sendMessage(chatId, "👥 *Group Members*\n\n", keyboardFactory.membersMenu(members, true));
            userStateManager.setState(chatId, UserState.MEMBERS_MENU);
        } catch (Exception e) {
            messageService.sendMessage(chatId, "❌ Error retrieving members");
        }
    }

    private void handleHelp(ChatContext context) {
        // Используем метод бота для показа справки
        groupManagementService.groupHelpCommand(context.getChatId());
    }

    private void handleBackToMain(ChatContext context) {
        // Сбрасываем состояние
        userStateManager.setState(context.getChatId(), UserState.DEFAULT);

        // Сбрасываем выбранную группу
        userStateManager.clearChosenGroup(context.getChatId());
        messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
        // Показываем главное меню
        userInteractionService.startCommand(context.getChatId());
    }
}