package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StateHandlerFor(UserState.AWAITING_PAID_BY)
@RequiredArgsConstructor
public class AwaitingPaidByHandler implements StateHandler {
    private final UserInteractionService userInteractionService;
    private final UserStateManager userStateManager;
    private final GroupService groupService;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public void handle(ChatContext context) throws Exception { // TODO: add verification if no users are chosen
        String input = context.getCallbackData();
        Long chatId = context.getChatId();
        messageService.deleteMessage(chatId, context.getMessage().getMessageId());
        if (input == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }

        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        expenseDto.setPaidByUserId(Long.valueOf(input.split("_")[1]));

        userStateManager.setState(chatId, UserState.AWAITING_SHARED_USERS);

        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);
        messageService.sendMessage(chatId, "*Who paid?*", keyboardFactory.createSharedUsersKeyboard(members, expenseDto));
    }
}
