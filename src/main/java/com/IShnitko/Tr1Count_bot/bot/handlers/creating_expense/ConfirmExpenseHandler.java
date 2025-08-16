package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.BACK_COMMAND;

@Component
@StateHandlerFor(UserState.CONFIRMING_EXPENSE)
@RequiredArgsConstructor
public class ConfirmExpenseHandler implements StateHandler {
    private final MessageService messageService;
    private final UserInteractionService userInteractionService;

    public static final String CONFIRM_SHARED_USERS = "confirm_shared_users";
    public static final String CANCEL_EXPENSE_CREATION = "cancel_expense_creation";

    private final UserStateManager userStateManager;
    private final BalanceService balanceService;
    private final GroupManagementService groupManagementService;
    private final KeyboardFactory keyboardFactory;
    private final GroupService groupService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();
        Long chatId = context.getChatId();

        if (input == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }
        switch (input) {
            case CONFIRM_SHARED_USERS -> handleConfirm(chatId);
            case CANCEL_EXPENSE_CREATION -> handleCancel(chatId); // TODO: have to double click to trigger
            case BACK_COMMAND -> handleReturn(chatId);
        }
        // TODO: after pressing confirm button query is not answered
    }

    private void handleReturn(Long chatId) {
        userStateManager.setState(chatId, UserState.AWAITING_SHARED_USERS);
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);
        messageService.sendMessage(chatId, "*Who paid?*", keyboardFactory.createSharedUsersKeyboard(members, expenseDto));
    }

    private void handleCancel(Long chatId) {
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        userStateManager.clearUserData(chatId);
        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId, chosenGroup);
    }

    private void handleConfirm(Long chatId) {
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // 2. Pass the DTO to the business service for saving
        String chosenGroup = userStateManager.getChosenGroup(chatId);
        balanceService.createExpense(chosenGroup, expenseDto);

        // 3. Clear the user's session data
        userStateManager.clearUserData(chatId);

        // 4. Send a success message
        messageService.sendMessage(chatId, "✅ Expense successfully added!");

        userStateManager.setStateWithChosenGroup(chatId, UserState.IN_THE_GROUP, chosenGroup);
        groupManagementService.displayGroup(chatId, chosenGroup);
    }
}
