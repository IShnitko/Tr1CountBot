package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.Expense;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.util.TelegramApiUtils;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@StateHandlerFor(UserState.IN_THE_GROUP)
@RequiredArgsConstructor
public class InGroupStateHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InGroupStateHandler.class);

    private final MessageService messageService;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final UserService userService;


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

        Command command = Command.fromString(context.getCallbackData());
        switch (command) {
            case BALANCE -> handleBalance(context, groupId);
            case ADD_EXPENSE -> handleAddExpense(context); // TODO: maybe change taking context to just taking params
            case MEMBERS -> handleMembers(context, groupId);
            case HELP -> handleHelp(context);
            case BACK_COMMAND -> handleBackToMain(context);
            case HISTORY -> showHistory(context, groupId);
            case LINK -> sendJoinLink(context, groupId);
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void sendJoinLink(ChatContext context, String groupId) throws TelegramApiException {
        // Construct the deep link URL.
        // The bot's username is retrieved from the bot's configuration,
        // and the start parameter is the groupId.
        String joinLink = String.format("https://t.me/Tr1Count_bot?start=invite_%s", groupId);

        // Build the message to send to the user.
        // Use MarkdownV2 for formatting.
        String message = "🔗 *Here is the link to share with your friends to join the group\\:* \n\n" +
//        message.append(TelegramApiUtils.formatString(joinLink)); // TODO: implement markdownv2
                joinLink;

        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
        messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), message, keyboardFactory.returnButton());
    }

    private void showHistory(ChatContext context, String groupId) {
        List<Expense> expenses = balanceService.getExpensesForGroup(groupId);
        Long chatId = context.getChatId();
        userStateManager.setState(chatId, UserState.ONLY_RETURN_TO_GROUP);
        // Check if there are any expenses to display
        Integer messageId = context.getMessage().getMessageId();

        if (expenses.isEmpty()) {
            messageService.editMessage(chatId, messageId, "There are no expenses recorded in this group yet\\.", keyboardFactory.returnButton());
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
                    """
                            💸 *%s*
                              💵 Amount: `%.2f`
                              👤 Paid by: %s
                              🗓️ Date: %s
                            
                            """,
                    title,
                    expense.getAmount(),
                    paidBy,
                    formattedDate
            ));
        }

        // Send the complete, formatted message
        messageService.editMessage(chatId, messageId, messageBuilder.toString(), keyboardFactory.returnButton());
    }

    private void handleBalance(ChatContext context, String groupId) {
        try {
            String balanceText = balanceService.getBalanceText(groupId);
            userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), balanceText, keyboardFactory.returnButton());
        } catch (Exception e) {
            LOG.error("Error while calculating balance", e);
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), "❌ Error calculating balance", keyboardFactory.returnButton());
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
                               \s
                Please send expense details in format:
                `<description> <amount>`
                               \s
                Example:
                `Dinner 25.50`
               \s""";
            messageService.deleteMessage(chatId, messageId);
            Integer sentMessageId = messageService.sendMessage(chatId, instructions, keyboardFactory.returnButton());
            expenseDto.setMessageId(sentMessageId);
        } catch (Exception e) {
            messageService.deleteMessage(chatId, messageId);
            messageService.sendMessage(chatId, "❌ Error starting expense creation"); // TODO: fix the editing of the message (now it breaks chat)
        }
    }


    private void handleMembers(ChatContext context, String groupId) {
        Long chatId = context.getChatId();
        Long userId = context.getUser().getId();
        Integer messageId = context.getMessage().getMessageId();
        try {
            List<User> members = groupService.getUsersForGroup(groupId);
            if (Objects.equals(userService.getCreatorOfTheGroup(groupId), userId)) {
                messageService.editMessage(chatId, messageId,"👥 *Group Members*\n\n", keyboardFactory.membersMenu(members, true));
            } else {
                messageService.editMessage(chatId, messageId,"👥 *Group Members*\n\n", keyboardFactory.membersMenu(members, false));
            }
            userStateManager.setState(chatId, UserState.MEMBERS_MENU);
        } catch (Exception e) {
            messageService.editMessage(chatId, messageId,"❌ Error retrieving members", keyboardFactory.returnButton());
            userStateManager.setState(chatId, UserState.ONLY_RETURN_TO_GROUP);
        }
    }

    private void handleHelp(ChatContext context) {
        // Используем метод бота для показа справки
        groupManagementService.groupHelpCommand(context.getChatId(), context.getMessage().getMessageId());
        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
    }

    private void handleBackToMain(ChatContext context) {
        // Сбрасываем состояние
        userStateManager.setState(context.getChatId(), UserState.DEFAULT);

        // Сбрасываем выбранную группу
        userStateManager.clearChosenGroup(context.getChatId());
        // Показываем главное меню
        userInteractionService.startCommand(context.getChatId(), context.getMessage().getMessageId());
    }
}