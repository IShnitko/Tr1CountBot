package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StateHandlerFor(UserState.CONFIRMING_EXPENSE)
@RequiredArgsConstructor
public class ConfirmExpenseHandler implements StateHandler {
    private final MessageService messageService;
    private final UserInteractionService userInteractionService;

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final GroupManagementService groupManagementService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();
        Long chatId = context.getChatId();
        Integer messageId = context.getMessage().getMessageId();

        if (input == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }
        Command command = Command.fromString(context.getCallbackData());

        switch (command) {
            case CONFIRM_SHARED_USERS -> handleConfirm(chatId, messageId);
            case CANCEL_EXPENSE_CREATION -> handleCancel(chatId, messageId);
            case BACK_COMMAND -> handleReturn(chatId, messageId);
        }
        // TODO: after pressing confirm button query is not answered
    }

    private void handleReturn(Long chatId, Integer messageId) {
        userStateManager.setState(chatId, UserState.AWAITING_SHARED_USERS);
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);
        messageService.editMessage(chatId, messageId, "*Who paid?*", keyboardFactory.createSharedUsersKeyboard(members, expenseDto));
    }

    private void handleCancel(Long chatId, Integer messageId) {
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        userStateManager.clearUserData(chatId);
        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId, chosenGroup, messageId);
    }

    private void handleConfirm(Long chatId, Integer messageId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // 2. Pass the DTO to the business service for saving
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        balanceService.createExpense(chosenGroup, expenseDto);

        // 3. Clear the user's session data
        userStateManager.clearUserData(chatId);

        // 4. Send a success message
        messageService.sendMessage(chatId, "✅ Expense successfully added!"); // TODO: get rid of that message and move it somewhere

        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId, chosenGroup, messageId);
    }
}
