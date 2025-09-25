package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers.editing_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.impl.MessageServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.service.impl.BalanceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StateHandlerFor(UserState.EDITING_PAID_BY)
@RequiredArgsConstructor
public class EditPaidByHandler implements StateHandler {
    private final MessageServiceImpl messageService;
    private final UserStateManager userStateManager;
    private final BalanceServiceImpl balanceService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public void handle(ChatContext context) throws Exception {
        String input = context.getCallbackData();
        Long chatId = context.getChatId();

        if (context.getText() != null) {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
            return;
        }

        if (input.equals(Command.BACK_COMMAND.getCommand())) {
            userStateManager.setState(chatId, UserState.EXPENSE_INFO);

            messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(),
                    balanceService.getExpenseTextFromExpenseDTO(
                            userStateManager.getOrCreateExpenseUpdateDto(chatId)
                    ),
                    keyboardFactory.expenseDetailsKeyboard());
            return;
        }

        userStateManager.setState(chatId, UserState.EXPENSE_INFO);
        ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);
        expenseUpdateDto.setPaidByUserId(Long.valueOf(input.split("_")[1]));

        messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(),
                balanceService.getExpenseTextFromExpenseDTO(
                        userStateManager.getOrCreateExpenseUpdateDto(chatId)
                ),
                keyboardFactory.expenseDetailsKeyboard());
    }
}
