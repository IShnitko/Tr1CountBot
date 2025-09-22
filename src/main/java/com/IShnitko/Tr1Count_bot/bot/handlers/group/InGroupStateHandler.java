package com.IShnitko.Tr1Count_bot.bot.handlers.group;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@StateHandlerFor(UserState.IN_THE_GROUP)
@RequiredArgsConstructor
public class InGroupStateHandler implements StateHandler {
    private final MessageService messageService;
    private final GroupManagementService groupManagementService;
    private final UserInteractionService userInteractionService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;
    private final UserService userService;
    private final ExpenseManagementService expenseManagementService;


    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String groupId = userStateManager.getChosenGroup(context.getChatId());

        if (!groupService.doesGroupExist(groupId)) {
            userStateManager.setState(context.getChatId(), UserState.DEFAULT);
            userInteractionService.startCommand(context.getChatId(), context.getMessage().getMessageId(), "Oops, it seems this group has been deleted!");
            return;
        }

        if (context.getCallbackQueryId() != null) {
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
            case MEMBERS -> handleMembers(context); // TODO: add update group name
            case HELP -> handleHelp(context);
            case BACK_COMMAND -> handleBackToMain(context);
            case HISTORY -> showHistory(context, groupId);
            case LINK -> sendJoinLink(context);
            case DELETE -> handleDelete(context); // TODO: add conformation page before deleting
        }
    }

    private void handleDelete(ChatContext context) {
        Long chatId = context.getChatId();
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        String groupName = groupService.getGroupName(chosenGroup);
        if (context.getUser().getId().equals(userService.getCreatorOfTheGroup(chosenGroup))) {
            groupService.deleteGroup(chosenGroup);
            userStateManager.setState(chatId, UserState.DEFAULT);
            userInteractionService.startCommand(chatId, context.getMessage().getMessageId(), "Deleted group " + groupName);
            userStateManager.clearChosenGroup(chatId);
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
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_HISTORY);

        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), 0);
    }

    private void handleBalance(ChatContext context, String groupId) {
        userStateManager.setState(context.getChatId(), UserState.ONLY_RETURN_TO_GROUP);
        try {
            String balanceText = balanceService.getBalanceText(groupId);
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), balanceText, keyboardFactory.returnButton());
        } catch (Exception e) {
            log.error("Error while calculating balance", e);
            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(), "❌ Error calculating balance", keyboardFactory.returnButton());
        }
    }

    private void handleAddExpense(ChatContext context) {
        Integer messageId = context.getMessage().getMessageId();
        Long chatId = context.getChatId();
        try {
            userStateManager.setState(chatId, UserState.ADDING_EXPENSE_START);
            expenseManagementService.startAddingExpense(chatId, messageId);
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