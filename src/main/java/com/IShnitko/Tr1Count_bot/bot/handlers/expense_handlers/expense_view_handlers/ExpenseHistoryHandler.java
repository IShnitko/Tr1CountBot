package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.ExpenseManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.GroupManagementService;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.UserInteractionService;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.impl.BalanceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StateHandlerFor(UserState.EXPENSE_HISTORY)
@RequiredArgsConstructor
public class ExpenseHistoryHandler implements StateHandler {

    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupService groupService;
    private final UserInteractionService userInteractionService;
    private final KeyboardFactory keyboardFactory;
    private final BalanceServiceImpl balanceService;
    private final GroupManagementService groupManagementService;
    private final ExpenseManagementService expenseManagementService;

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

        Long expenseId = null;
        Command command = null;
        if (context.getCallbackData() != null) {
            // Split the data and check if it contains at least two parts
            String[] parts = context.getCallbackData().split(":");
            command = Command.fromString(parts[0]);
            if (parts.length > 1) {
                try {
                    // Now we can be more specific with the exception
                    expenseId = Long.valueOf(parts[1]);

                } catch (NumberFormatException e) {
                    log.warn("Invalid expense ID format in callback data: " + parts[1], e);
                }
            } else {
                log.info("Callback data does not contain a valid ID part.");
            }
        } else {
            log.info("Callback data is null.");
        }

        switch (command) {
            case NEXT_PAGE -> showNextPage(context);
            case PREV_PAGE -> showPreviousPage(context);
            case BACK_COMMAND -> returnToGroupMenu(context, groupId);
            case INFO -> showInfoForExpense(context, expenseId);
            case DELETE -> deleteExpense(context, expenseId);
        }
    }

    private void deleteExpense(ChatContext context, Long expenseId) {
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_DELETE_CONFIRMATION);
        expenseManagementService.sendDeleteConfirmation(context.getChatId(), context.getMessage().getMessageId(), expenseId);
    }

    private void showInfoForExpense(ChatContext context, Long expenseId) {
        userStateManager.setState(context.getChatId(), UserState.EXPENSE_INFO);
        ExpenseUpdateDto expenseUpdateDto = userStateManager.getOrCreateExpenseUpdateDto(context.getChatId());
        expenseUpdateDto.setId(expenseId);
        messageService.editMessage(context.getChatId(), context.getMessage().getMessageId(),
                balanceService.getExpenseTextById(expenseId), keyboardFactory.expenseDetailsKeyboard());
    }

    private void returnToGroupMenu(ChatContext context, String groupId) {
        userStateManager.setState(context.getChatId(), UserState.IN_THE_GROUP);
        groupManagementService.displayGroup(context.getChatId(), groupId, context.getMessage().getMessageId());
    }

    private void showPreviousPage(ChatContext context) {
        int page = userStateManager.getAndDecPage(context.getChatId());

        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), page);
    }

    private void showNextPage(ChatContext context) {
        int page = userStateManager.getAndIncPage(context.getChatId());
        log.info("Showing next page {}", page);

        expenseManagementService.sendExpenseHistoryView(context.getChatId(), context.getMessage().getMessageId(), page);
    }
}
