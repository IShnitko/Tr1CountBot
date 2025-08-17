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
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.service.UserService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import com.IShnitko.Tr1Count_bot.util.user_state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.BACK_COMMAND;

@Component
@StateHandlerFor(UserState.AWAITING_SHARED_USERS)
@RequiredArgsConstructor
public class AwaitingSharedUsersHandler implements StateHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AwaitingSharedUsersHandler.class);
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory;
    private final UserInteractionService userInteractionService;
    private final UserService userService;
    private final GroupManagementService groupManagementService;

    @Override
    public void handle(ChatContext context) throws TelegramApiException {
        String callbackData = context.getCallbackData();
        Long chatId = context.getChatId();

        if (callbackData == null) {
            userInteractionService.unknownCommand(chatId);
            return;
        }

        // Answer the callback query to hide the loading icon on the button
        messageService.answerCallbackQuery(context.getCallbackQueryId());

        if (callbackData.startsWith("select_shared_user:")) {
            handleUserSelection(context, callbackData);
        } else if (callbackData.equals("confirm_shared_users")) {
            handleConfirm(context);
        } else if (callbackData.equals(BACK_COMMAND)) { // TODO: after pressing button it still shows as chosen
            handleReturn(context);
        }
    }

    private void handleUserSelection(ChatContext context, String callbackData) {
        Long chatId = context.getChatId();
        Long userId = Long.parseLong(callbackData.split(":")[1]);

        // Get the DTO from the user session
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Find the user and toggle the shared status
        expenseDto.getSharedUsers().computeIfPresent(userId, (k, v) -> !v);

        // Get the list of all members to regenerate the keyboard
        String groupId = userStateManager.getChosenGroup(chatId);
        List<User> members = groupService.getUsersForGroup(groupId);

        // Create the updated keyboard
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(context.getMessage().getMessageId());
        editMessage.setReplyMarkup(keyboardFactory.createSharedUsersKeyboard(members, expenseDto));

        // Update the message with the new keyboard
        messageService.editMessage(editMessage);
    }

    private void handleConfirm(ChatContext context) {
        Long chatId = context.getChatId();

        // Retrieve the DTO from the user's session
        CreateExpenseDto expenseDto = userStateManager.getOrCreateExpenseDto(chatId);

        // Build the final summary message
        String summary = buildSummaryMessage(expenseDto);
        LOG.info("Summary string is empty: " + summary.isEmpty());
        // Set the state to CONFIRMING_EXPENSE and send the final message with buttons
        userStateManager.setState(chatId, UserState.CONFIRMING_EXPENSE);
        messageService.deleteMessage(chatId, context.getMessage().getMessageId());
        messageService.sendMessage(chatId,
                summary,
                keyboardFactory.finalConfirmationKeyboard());
    }


    private void handleReturn(ChatContext context) {
        Long chatId = context.getChatId();
        userStateManager.clearUserData(chatId);
        userStateManager.setState(chatId, UserState.AWAITING_PAID_BY);
        messageService.sendMessage(chatId, "Choose who paid for this purchase:", keyboardFactory.membersMenu( // TODO: add deleting or editing message
                groupService.getUsersForGroup(
                        userStateManager.getChosenGroup(chatId)
                ), false)); // TODO: i think i should extract every method that is duplicated to service
    }

    private String buildSummaryMessage(CreateExpenseDto expenseDto) {
        StringBuilder builder = new StringBuilder();
        builder.append("💸 *New Expense Summary*:\n\n");
        builder.append("💵 *Title*: ").append(expenseDto.getTitle()).append("\n");
        builder.append("💰 *Amount*: ").append(expenseDto.getAmount()).append("\n");

        String paidByUserName = userService.getUserNameById(expenseDto.getPaidByUserId());
        builder.append("👤 *Paid by*: ").append(paidByUserName).append("\n\n");

        builder.append("👥 *Shared with*:\n");
        String sharedUsersString = expenseDto.getSharedUsers().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> userService.getUserNameById(entry.getKey()))
                .collect(Collectors.joining(", "));

        if (sharedUsersString.isEmpty()) {
            builder.append("- No one\n");
        } else {
            builder.append(sharedUsersString).append("\n");
        }

        builder.append("\n🗓️ *Date*: ");
        if (expenseDto.getDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            builder.append(expenseDto.getDate().format(formatter));
        } else {
            builder.append("Not specified");
        }

        return builder.toString();
    }
}