package com.IShnitko.Tr1Count_bot.bot.handlers.expense_handlers.expense_view_handlers.editing_expense;

import com.IShnitko.Tr1Count_bot.bot.KeyboardFactory;
import com.IShnitko.Tr1Count_bot.bot.context.ChatContext;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.StateHandler;
import com.IShnitko.Tr1Count_bot.bot.handlers.state_handler.annotation.StateHandlerFor;
import com.IShnitko.Tr1Count_bot.bot.model.Command;
import com.IShnitko.Tr1Count_bot.bot.model.UserState;
import com.IShnitko.Tr1Count_bot.bot.service.MessageService;
import com.IShnitko.Tr1Count_bot.bot.service.impl.ExpenseManagementServiceImpl;
import com.IShnitko.Tr1Count_bot.bot.user_state.UserStateManager;
import com.IShnitko.Tr1Count_bot.dto.ExpenseUpdateDto;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@StateHandlerFor(UserState.EDITING_SHARED_USERS)
@RequiredArgsConstructor
public class EditSharedWithHandler implements StateHandler {
    private final MessageService messageService;
    private final UserStateManager userStateManager;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final ExpenseManagementServiceImpl expenseManagementService;
    private final UserService userService;

    @Override
    public void handle(ChatContext context) throws Exception {
        String callbackData = context.getCallbackData();
        Long chatId = context.getChatId();

        if (callbackData == null) {
            messageService.deleteMessage(chatId, context.getMessage().getMessageId());
            return;
        }

        messageService.answerCallbackQuery(context.getCallbackQueryId());
        ExpenseUpdateDto expenseDto = userStateManager.getOrCreateExpenseUpdateDto(chatId);

        if (callbackData.startsWith("select_shared_user:")) {
            handleUserSelection(context, callbackData, expenseDto);
        } else if (callbackData.equals("confirm_shared_users")) {
            handleConfirm(chatId, expenseDto);
        } else if (callbackData.equals(Command.BACK_COMMAND.getCommand())) {
            handleReturn(chatId);
        }
    }

    private void handleUserSelection(ChatContext context, String callbackData, ExpenseUpdateDto expenseDto) {
        Long chatId = context.getChatId();
        Long userId = Long.parseLong(callbackData.split(":")[1]);

        // переключаем: если есть — убираем, если нет — добавляем с 0
        Map<Long, BigDecimal> shared = expenseDto.getSharedUsers();
        if (shared.containsKey(userId)) {
            shared.remove(userId);
        } else {
            shared.put(userId, BigDecimal.ZERO);
        }

        // получаем участников группы
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);

        // обновляем клавиатуру
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(context.getMessage().getMessageId());
        editMessage.setReplyMarkup(keyboardFactory.createSharedUsersKeyboard(members, expenseDto, userService));

        messageService.editMessage(editMessage);
    }

    private void handleConfirm(Long chatId, ExpenseUpdateDto expenseDto) {
        // проверка: хотя бы один выбран
        boolean hasSelectedUsers = !expenseDto.getSharedUsers().isEmpty();

        if (!hasSelectedUsers) {
            expenseManagementService.sendIncorrectSharedUsers(chatId);
            return;
        }

        userStateManager.setState(chatId, UserState.EXPENSE_INFO);
        expenseManagementService.sendExpenseInfo(chatId, null);
    }

    private void handleReturn(Long chatId) {
        userStateManager.setState(chatId, UserState.EXPENSE_INFO);
        expenseManagementService.sendExpenseInfo(chatId, null);
    }
}
