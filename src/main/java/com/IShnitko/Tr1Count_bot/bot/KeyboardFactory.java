package com.IShnitko.Tr1Count_bot.bot;

import com.IShnitko.Tr1Count_bot.dto.CreateExpenseDto;
import com.IShnitko.Tr1Count_bot.model.Group;
import com.IShnitko.Tr1Count_bot.model.User;
import com.IShnitko.Tr1Count_bot.service.GroupService;
import com.IShnitko.Tr1Count_bot.util.user_state.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.IShnitko.Tr1Count_bot.bot.Tr1CountBot.*;
import static com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense.AwaitingDateHandler.DEFAULT_DATE_COMMAND;
import static com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense.ConfirmExpenseHandler.CANCEL_EXPENSE_CREATION;
import static com.IShnitko.Tr1Count_bot.bot.handlers.creating_expense.ConfirmExpenseHandler.CONFIRM_SHARED_USERS;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Join group by code")
                .callbackData(JOIN)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Create code")
                .callbackData(CREATE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("List groups")
                .callbackData(GROUPS)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(HELP)
                .build());
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup groupMenu() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Balance of the group")
                .callbackData(BALANCE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Add expense")
                .callbackData(ADD_EXPENSE)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Group members")
                .callbackData(MEMBERS)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Help")
                .callbackData(HELP)
                .build());
        rows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return to main menu")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup groupsListMenu(List<Group> groups) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        for (var group : groups) {
            row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(group.getName())
                    .callbackData(group.getId())
                    .build());
            rows.add(row);
        }
        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }

    public InlineKeyboardMarkup returnButton() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup membersMenu(List<User> members, Boolean withDeleteButton) {
        if (members == null || members.isEmpty()) {
            // Если нет участников, вернуть пустую или другую клавиатуру
            return returnButton();
        }

        // Шаг 1: Найти длину самого длинного имени
        int maxLength = members.stream()
                .map(User::getName)
                .mapToInt(String::length)
                .max()
                .orElse(0); // В случае пустого списка, вернет 0

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (User member : members) {
            String name = member.getName();

            int paddingLength = maxLength - name.length();
            String paddedName = name + " ".repeat(paddingLength);

            InlineKeyboardButton nameButton = new InlineKeyboardButton(paddedName);
            nameButton.setCallbackData(INFO + "_" + member.getTelegramId());

            InlineKeyboardButton deleteButton = new InlineKeyboardButton("❌");
            deleteButton.setCallbackData(DELETE + "_" + member.getTelegramId());


            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(nameButton);
            if (withDeleteButton) row.add(deleteButton);

            keyboard.add(row);
        }

        InlineKeyboardButton backButton = new InlineKeyboardButton("↩️ Back to Group Menu");
        backButton.setCallbackData(BACK_COMMAND);
        keyboard.add(Collections.singletonList(backButton));

        return new InlineKeyboardMarkup(keyboard);
    }
    public InlineKeyboardMarkup createSharedUsersKeyboard(List<User> members, CreateExpenseDto expenseDto) { // TODO: if no button is pressed then no cross or checkmark is shown
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (expenseDto.getSharedUsers().isEmpty()) {
            expenseDto.initializeSharedUsers(members);
        }

        for (User user : members) {
            // Determine the current checkmark status from the DTO
            boolean isShared = expenseDto.getSharedUsers().getOrDefault(user.getTelegramId(), true);
            String checkmark = isShared ? " ✔️" : " ❌";
            String buttonText = user.getName() + checkmark;

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            // The callback data must contain the user's ID for later processing
            button.setCallbackData("select_shared_user:" + user.getTelegramId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        // Add a final row with "Confirm" and "Cancel" buttons
        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("✅ Confirm");
        confirmButton.setCallbackData("confirm_shared_users");

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Return");
        cancelButton.setCallbackData(BACK_COMMAND);

        List<InlineKeyboardButton> finalRow = new ArrayList<>();
        finalRow.add(confirmButton);
        finalRow.add(cancelButton);
        keyboard.add(finalRow);

        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardMarkup dateButton() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Today")
                .callbackData(DEFAULT_DATE_COMMAND)
                .build());
        rows.add(row);
        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);
        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup finalConfirmationKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("✅ Confirm")
                .callbackData(CONFIRM_SHARED_USERS)
                .build());
        rows.add(row);
        row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("❌ Cancel")
                .callbackData(CANCEL_EXPENSE_CREATION)
                .build());
        rows.add(row);
        row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Return")
                .callbackData(BACK_COMMAND)
                .build());
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }
}
