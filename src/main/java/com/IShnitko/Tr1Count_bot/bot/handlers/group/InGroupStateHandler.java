package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.AddingExpenseService;
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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@StateHandlerFor(UserState.IN_THE_GROUP)
@RequiredArgsConstructor
public class InGroupStateHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InGroupStateHandler.class);

    private final MessageService messageService;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;
    private final UserService userService;
    private final AddingExpenseService addingExpenseService;


    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String groupId = userStateManager.getChosenGroup(context.getChatId());

        if (!groupService.doesGroupExist(groupId)) {
            userStateManager.setState(context.getChatId(), UserState.DEFAULT);
            userInteractionService.startCommand(context.getChatId(), context.getMessage().getMessageId(), "Oops, it seems this group has been deleted!");
            return;
        }

        if (context.getCallbackQueryId() != null) { // SAFETY CHECK
            messageService.answerCallbackQuery(context.getCallbackQueryId());
        }
        if (context.getUpdateType() != ChatContext.UpdateType.CALLBACK) {
            messageService.deleteMessage(context.getChatId(), context.getMessage().getMessageId());
            return;
        }

        Command command = Command.fromString(context.getCallbackData());
        switch (command) {
            case BALANCE -> handleBalance(context, groupId);
            case ADD_EXPENSE -> handleAddExpense(context);
            case MEMBERS -> handleMembers(context);
            case HELP -> handleHelp(context);
            case BACK_COMMAND -> handleBackToMain(context);
            case HISTORY -> showHistory(context, groupId);
            case LINK -> sendJoinLink(context);
            case DELETE -> handleDelete(context); // TODO: add conformation page before deleting
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void handleDelete(ChatContext context) {
        Long chatId = context.getChatId();
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        String groupName = groupService.getGroupName(chosenGroup);
        if (context.getUser().getId().equals(userService.getCreatorOfTheGroup(chosenGroup))){
            groupService.deleteGroup(chosenGroup);
            userStateManager.setState(chatId, UserState.DEFAULT);
            userInteractionService.startCommand(chatId, context.getMessage().getMessageId(), "Deleted group " + groupName);
            userStateManager.clearChosenGroup(chatId); // so basically i want to store states, because if someone is in group state and group is deleted, then nothing happens
        } else {
            groupManagementService.displayGroup(chatId,
                    chosenGroup,
                    null,
                    context.getMessage().getMessageId(),
                    "You can't delete this group, because you are not the creator");
        }
    }

    private void sendJoinLink(ChatContext context) {
        groupManagementService.sendJoinLink(context.getChatId(), context.getMessage().getMessageId());
        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
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
        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
        try {
            String balanceText = balanceService.getBalanceText(groupId);
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
            userStateManager.setState(chatId, UserState.ADDING_EXPENSE_START);
            addingExpenseService.startAddingExpense(chatId, messageId);
        } catch (Exception e) {
            log.error("Error while starting creating expense", e);
            groupManagementService.displayGroup(chatId,
                    userStateManager.getChosenGroup(chatId),
                    messageId,
                    null,
                    "❌ Error starting expense creation");
        }
    }

    private void handleMembers(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.MEMBERS_MENU);
        groupManagementService.viewMembersMenu(context.getChatId(), context.getMessage().getMessageId(), context.getUser().getId());
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