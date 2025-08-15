package com.IShnitko.Tr1Count_bot.bot.handlers;

import com.IShnitko.Tr1Count_bot.bot.Tr1CountBot;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        switch (command) {
            case BALANCE -> handleBalance(context, groupId);
            case ADD_EXPENSE -> handleAddExpense(context);
            case MEMBERS -> handleMembers(context, groupId);
            case HELP -> handleHelp(context);
            case BACK_COMMAND -> handleBackToMain(context);
            default -> userInteractionService.unknownCommand(context.getChatId());
        }
    }

    private void handleBalance(ChatContext context, String groupId) {
        try {
            // Получаем форматированный баланс
            String balanceText = balanceService.getBalanceText(groupId);

            // Отправляем сообщение с балансом
            messageService.sendMessage(context.getChatId(), balanceText);
        } catch (Exception e) {
            messageService.sendMessage(context.getChatId(), "❌ Error calculating balance");
        }
    }

    private void handleAddExpense(ChatContext context) {
        try {
            // Переводим в состояние добавления расхода
            userStateManager.setState(context.getChatId(), UserState.ADDING_EXPENSE);

            // Отправляем инструкцию
            String instructions = """
                💸 *Add New Expense*
                                
                Please send expense details in format:
                `<description> <amount>`
                                
                Example:
                `Dinner 25.50`
                """;
            messageService.sendMessage(context.getChatId(), instructions);
        } catch (Exception e) {
            messageService.sendMessage(context.getChatId(), "❌ Error starting expense creation");
        }
    }

    private void handleMembers(ChatContext context, String groupId) {
        try {
            // Получаем участников группы
            List<User> members = groupService.getUsersForGroup(groupId);

            // Форматируем список участников
            StringBuilder sb = new StringBuilder("👥 *Group Members*\n\n");
            for (int i = 0; i < members.size(); i++) {
                sb.append(i + 1).append(". ").append(members.get(i).getName()).append("\n");
            }

            // Отправляем сообщение
            messageService.sendMessage(context.getChatId(), sb.toString());
        } catch (Exception e) {
            messageService.sendMessage(context.getChatId(), "❌ Error retrieving members");
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

        // Показываем главное меню
        userInteractionService.startCommand(context.getChatId());
    }
}