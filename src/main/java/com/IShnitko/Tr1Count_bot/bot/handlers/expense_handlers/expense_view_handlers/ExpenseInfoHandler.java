package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.exception.ExpenseNotFoundException;
import com.IShnitko.Tr1Count_bot.service.BalanceService;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StateHandlerFor(UserState.EXPENSE_INFO)
@RequiredArgsConstructor
public class ExpenseInfoHandler implements StateHandler {
    private final UserStateManager userStateManager;
    private final GroupService groupService;
    private final UserInteractionService userInteractionService;
    private final MessageService messageService;
    private final ExpenseManagementService expenseManagementService;
    private final BalanceService balanceService;

    @Override
    public void handle(ChatContext context) throws Exception {
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
            case EDIT_TITLE -> sendEditTitle(context);
            case EDIT_AMOUNT -> sendEditAmount(context);
            case EDIT_DATE -> sendEditDate(context);
            case EDIT_PAID_BY -> sendEditPaidBy(context);
            case EDIT_SHARED_WITH -> sendEditSharedWith(context);
            case CONFIRM -> saveEUDAndExit(context);
            case CANCEL -> returnToExpenseHistoryView(context);
        }
    }

    private void returnToExpenseHistoryView(ChatContext context) {
        userStateManager.clearExpenseUpdateDto(context.getChatId());
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_HISTORY);
        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), 0);
    }

    private void saveEUDAndExit(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_HISTORY);
        Long savedExpenseId = balanceService.saveExpenseUpdateDto(context.getChatId());
        userStateManager.clearExpenseUpdateDto(context.getChatId());
        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), 0,
                "Successfully updated expense " + balanceService.getExpenseById(savedExpenseId)
                        .orElseThrow(
                                () -> new ExpenseNotFoundException("Can't send message with saved expense title, because it doesn't exist")
                        )
                        .getTitle());
    }

    private void sendEditSharedWith(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EDITING_SHARED_USERS);
        expenseManagementService.sendSharedUsers(context.getChatId());
    }

    private void sendEditPaidBy(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EDITING_PAID_BY);
        expenseManagementService.sendPaidBy(context.getChatId(), null);
    }

    private void sendEditDate(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EDITING_DATE);
        expenseManagementService.sendDateInput(context.getChatId(), null);
    }

    private void sendEditAmount(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EDITING_AMOUNT);
        expenseManagementService.sendAmountInput(context.getChatId(), context.getMessage().getMessageId());
    }

    private void sendEditTitle(ChatContext context) {
        userStateManager.setState(context.getChatId(), UserState.EDITING_TITLE);
        expenseManagementService.sendTitleInput(context.getChatId(), context.getMessage().getMessageId());
    }
}
