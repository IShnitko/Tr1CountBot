package com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.service.AddingExpenseService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@StateHandlerFor(UserState.AWAITING_PAID_BY)
@RequiredArgsConstructor
public class AwaitingPaidByHandler implements StateHandler {
    private final MessageService messageService;
    private final AddingExpenseService addingExpenseService;
    private final UserStateManager userStateManager;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();
        Long chatId = context.getChatId();

        if (context.getText() != null) {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
            return;
        }
        if (input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.AWAITING_DATE);
            addingExpenseService.sendDateInput(chatId, null);
            return;
        }

        userStateManager.setState(chatId, UserState.AWAITING_SHARED_USERS);
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);
        expenseDto.setPaidByUserId(Long.valueOf(input.split("_")[1]));
        addingExpenseService.sendSharedUsers(chatId);
    }
}
