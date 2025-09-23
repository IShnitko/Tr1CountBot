package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers;

import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
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

    }

    private void saveEUDAndExit(ChatContext context) {

    }

    private void sendEditSharedWith(ChatContext context) {

    }

    private void sendEditPaidBy(ChatContext context) {

    }

    private void sendEditDate(ChatContext context) {

    }

    private void sendEditAmount(ChatContext context) {

    }

    private void sendEditTitle(ChatContext context) {
    }
}
